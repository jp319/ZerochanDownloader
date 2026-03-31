package com.jp319.zerochan.data.repository

import com.jp319.zerochan.data.model.*
import com.jp319.zerochan.data.network.DownloadProgressTracker
import com.jp319.zerochan.data.network.RequestTracker
import com.jp319.zerochan.data.profile.ProfileManager
import com.jp319.zerochan.utils.Logger
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.utils.io.*
import io.ktor.utils.io.jvm.javaio.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import kotlin.time.Duration.Companion.milliseconds

/**
 * Exception thrown when a network request is attempted without a valid username configured.
 */
class NoUsernameException : Exception("Zerochan username is required for API access.")

/**
 * The main data repository for interacting with Zerochan's API and scraping required assets.
 * Handles rate-limiting, error recovery, tag autocorrection, and full-resolution image discovery.
 *
 * @property client The Ktor HTTP client to use for requests.
 * @property profileManager Manager for user profiles and application settings.
 */
class ZerochanRepository(
    private val client: HttpClient,
    val profileManager: ProfileManager,
) {
    companion object {
        private const val BASE_URL = "https://www.zerochan.net"
        private const val STATIC_BASE_URL = "https://static.zerochan.net"
        private const val SUGGEST_ENDPOINT = "$BASE_URL/suggest"
        private const val TAG = "ZerochanRepository"
        private const val RATE_LIMIT_MS = 1000L
        private const val FULL_RES_DELAY_MS = 300L
    }

    init {
        cleanupTempGifs()
    }

    /**
     * Cleans up any temporary GIF files stored in the system's temp directory
     * from previous sessions.
     */
    private fun cleanupTempGifs() {
        kotlinx.coroutines.CoroutineScope(Dispatchers.IO).launch {
            try {
                val tempDir = File(System.getProperty("java.io.tmpdir"))
                val files = tempDir.listFiles { _, name -> name.startsWith("zerochan-") && name.endsWith(".gif") }
                files?.forEach { it.delete() }
                if (!files.isNullOrEmpty()) {
                    Logger.info(TAG, "Cleaned up ${files.size} cached zerochan-*.gif files on startup.")
                }
            } catch (e: Exception) {
                Logger.error(TAG, "Failed to clean up temp GIFs", e)
            }
        }
    }

    /**
     * Enforces a global rate limit between API requests for the current user.
     * Prevents IP blacklisting by the Zerochan server.
     *
     * @throws NoUsernameException if the username is not configured.
     */
    private suspend fun checkRateLimit() {
        val user = profileManager.username
        if (user.isBlank()) throw NoUsernameException()

        val lastTime = profileManager.getLastRequestTime(user)
        val now = System.currentTimeMillis()
        val elapsed = now - lastTime

        if (elapsed < RATE_LIMIT_MS) {
            delay((RATE_LIMIT_MS - elapsed).milliseconds)
        }
        profileManager.updateLastRequestTime(user, System.currentTimeMillis())
    }

    /**
     * Searches the Zerochan API for images matching the given parameters.
     * Includes automatic detection and recovery for Zerochan's server-side tag corrections.
     *
     * @param params The search filters and pagination details.
     * @param isRetry Internal flag used to prevent infinite redirect loops during autocorrection.
     * @return A list of items found, or empty list on failure.
     */
    suspend fun search(
        params: ZerochanApiParams,
        isRetry: Boolean = false,
    ): List<ZerochanItem> {
        RequestTracker.recordRequest()
        checkRateLimit()
        Logger.debug(TAG, "Searching for tag: '${params.tag}' (Page: ${params.page}, Retry: $isRetry)")

        return try {
            val response: HttpResponse =
                client.get(BASE_URL) {
                    url {
                        appendPathSegments(params.tag ?: "")
                        parameters.apply {
                            append("json", "")
                            params.page?.let { append("p", it.toString()) }
                            params.limit?.let { append("l", it.toString()) }
                            params.sort?.let { append("s", it.value) }
                            params.time?.let { append("t", it.value.toString()) }
                            params.color?.let { append("c", it) }
                            params.dimensions?.let { append("d", it.value) }
                            params.strict?.let { if (it) append("strict", "") }
                        }
                    }
                }

            val finalUrl = response.request.url
            Logger.debug(TAG, "Request URL: $finalUrl")

            // Autocorrection detection logic.
            // If the final URL doesn't have our "json" parameter, Zerochan redirected us.
            if (!finalUrl.parameters.contains("json")) {
                if (isRetry) {
                    Logger.warn(TAG, "Prevented an infinite redirect loop for tag: ${params.tag}")
                    return emptyList()
                }

                // Extract the corrected tag from the end of the new URL path
                val correctedTag = finalUrl.rawSegments.lastOrNull()

                if (!correctedTag.isNullOrBlank()) {
                    Logger.info(TAG, "Zerochan auto-corrected tag to: $correctedTag. Retrying...")

                    // Copy our original params but inject the newly discovered tag
                    val correctedParams = params.copy(tag = correctedTag)

                    // Recursively call for the corrected tag once
                    return search(correctedParams, isRetry = true)
                }
            }

            if (!response.status.isSuccess()) {
                Logger.error(TAG, "Search failed: ${response.status}")
                return emptyList()
            }

            val zerochanResponse: ZerochanResponse = response.body()
            zerochanResponse.items
        } catch (e: NoUsernameException) {
            throw e
        } catch (e: Exception) {
            Logger.error(TAG, "Search failed for '${params.tag}'", e)
            emptyList()
        }
    }

    /**
     * Retrieves specific details for a single item by its ID.
     *
     * @param id The Zerochan ID of the item.
     * @return The item details, or null if the fetch fails.
     */
    suspend fun getItem(id: Int): ZerochanFullItem? {
        RequestTracker.recordRequest()

        return try {
            checkRateLimit()
            val response: HttpResponse =
                client.get("$BASE_URL/$id") {
                    url { parameters.append("json", "") }
                }

            if (!response.status.isSuccess()) {
                Logger.error(TAG, "Failed to fetch item $id. Status: ${response.status}")
                return null
            }

            response.body()
        } catch (e: NoUsernameException) {
            throw e
        } catch (e: Exception) {
            Logger.error(TAG, "Error fetching item $id", e)
            null
        }
    }

    /**
     * Attempts to find a valid full-resolution image URL dynamically by guessing
     * the file extension on static.zerochan.net.
     *
     * @param id The zerochan item ID.
     * @param tag The primary character tag for the image format.
     * @return The absolute URL if found, or null otherwise.
     */
    suspend fun findValidFullResUrl(
        id: Int,
        tag: String,
    ): String? {
        val formattedTag = tag.replace(" ", ".")
        val staticUrlPrefix = "$STATIC_BASE_URL/$formattedTag.full.$id."
        val extensions = listOf("jpg", "png", "jpeg", "gif")

        Logger.debug(TAG, "Discovering full-res for ID: $id (Tag: $tag)")

        for (ext in extensions) {
            val urlToTest = "$staticUrlPrefix$ext"

            try {
                // Add a slight delay to bypass Cloudflare's bot-protection on static assets
                delay(FULL_RES_DELAY_MS.milliseconds)

                val response =
                    client.head(urlToTest) {
                        header("Referer", "$BASE_URL/")
                        header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    }

                if (response.status.isSuccess()) {
                    val bytes = response.headers[HttpHeaders.ContentLength]?.toLongOrNull()
                    val sizeString =
                        if (bytes != null) {
                            val mb = bytes / (1024f * 1024f)
                            "%.2f MB".format(mb)
                        } else {
                            "Unknown Size"
                        }

                    Logger.info(TAG, "Found full-res: $urlToTest ($sizeString)")
                    return urlToTest
                }
            } catch (e: Exception) {
                Logger.debug(TAG, "Test failed for $ext on ID $id: ${e.message}")
            }
        }

        Logger.warn(TAG, "No full-res match found for ID $id.")
        return null
    }

    /**
     * Downloads an image to the local filesystem while updating a real-time progress tracker.
     *
     * @param url The URL of the image to download.
     * @param fileName The name to save the file as.
     * @param targetDirectoryPath The directory where the file should be saved.
     * @return The downloaded file, or null if the download fails.
     */
    suspend fun downloadImageToDisk(
        url: String,
        fileName: String,
        targetDirectoryPath: String,
    ): File? =
        withContext(Dispatchers.IO) {
            try {
                val zerochanDir = File(targetDirectoryPath)
                if (!zerochanDir.exists()) zerochanDir.mkdirs()

                val targetFile = File(zerochanDir, fileName)
                var lastPrintedPercent = -1

                client.prepareGet(url) {
                    header("Referer", "$BASE_URL/")
                    header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")

                    // Track and report download progress using Ktor's native callback
                    onDownload { bytesSentTotal, contentLength ->
                        if (contentLength != null && contentLength > 0L) {
                            val progress = bytesSentTotal.toFloat() / contentLength.toFloat()
                            DownloadProgressTracker.updateProgress(url, progress)

                            // Clean console progress logging
                            val currentPercent = (progress * 100).toInt()
                            if (currentPercent != lastPrintedPercent) {
                                lastPrintedPercent = currentPercent
                                if (currentPercent % 10 == 0) {
                                    Logger.debug(TAG, "Downloading $fileName: $currentPercent%")
                                }
                            }
                        }
                    }
                }.execute { response ->
                    val inputStream = response.bodyAsChannel().toInputStream()
                    FileOutputStream(targetFile).use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }

                Logger.info(TAG, "Successfully saved: ${targetFile.absolutePath}")
                return@withContext targetFile
            } catch (e: Exception) {
                Logger.error(TAG, "Failed to download $fileName from $url", e)
                return@withContext null
            }
        }

    private val suggestionCache = mutableMapOf<String, List<ZerochanSuggestion>>()

    /**
     * Fetches tag suggestions for the search bar, with a memory cache
     * to prevent redundant network calls.
     *
     * @param query The search query string.
     * @return A list of tag suggestions.
     */
    suspend fun getSuggestions(query: String): List<ZerochanSuggestion> {
        val safeQuery = query.trim().lowercase()
        if (safeQuery.isBlank()) return emptyList()

        if (suggestionCache.containsKey(safeQuery)) {
            Logger.debug(TAG, "Suggestion Cache Hit: $safeQuery")
            return suggestionCache[safeQuery]!!
        }

        return try {
            checkRateLimit()
            val response =
                client.get(SUGGEST_ENDPOINT) {
                    url {
                        parameters.append("q", query)
                        parameters.append("json", "1")
                    }
                }

            if (response.status.isSuccess()) {
                val data: ZerochanSuggestionResponse = response.body()
                suggestionCache[safeQuery] = data.suggestions
                data.suggestions
            } else {
                Logger.error(TAG, "Suggestions failed: ${response.status}")
                emptyList()
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Logger.error(TAG, "Failed to fetch suggestions for '$query'", e)
            emptyList()
        }
    }

    /**
     * Fetches a GIF and saves it to a temporary system file.
     * Caches it using the Zerochan ID to prevent redundant downloads within the session.
     *
     * @param id The Zerochan ID.
     * @param url The image URL.
     * @return The temporary GIF file, or null if the fetch fails.
     */
    suspend fun getRemoteGifFile(
        id: Int,
        url: String,
    ): File? =
        withContext(Dispatchers.IO) {
            try {
                val tempDir = File(System.getProperty("java.io.tmpdir"))
                val tempFile = File(tempDir, "zerochan-$id.gif")

                if (tempFile.exists() && tempFile.length() > 0) {
                    Logger.debug(TAG, "Using cached GIF: ${tempFile.absolutePath}")
                    return@withContext tempFile
                }

                var lastPrintedPercent = -1

                client.prepareGet(url) {
                    header("Referer", "$BASE_URL/")
                    header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")

                    onDownload { bytesSentTotal, contentLength ->
                        if (contentLength != null && contentLength > 0L) {
                            val progress = bytesSentTotal.toFloat() / contentLength.toFloat()
                            DownloadProgressTracker.updateProgress(url, progress)

                            val currentPercent = (progress * 100).toInt()
                            if (currentPercent != lastPrintedPercent) {
                                lastPrintedPercent = currentPercent
                                if (currentPercent % 10 == 0) {
                                    Logger.debug(TAG, "Downloading GIF zerochan-$id.gif: $currentPercent%")
                                }
                            }
                        }
                    }
                }.execute { response ->
                    val inputStream = response.bodyAsChannel().toInputStream()
                    FileOutputStream(tempFile).use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }

                Logger.debug(TAG, "Saved temp GIF to: ${tempFile.absolutePath}")
                tempFile
            } catch (e: Exception) {
                Logger.error(TAG, "Failed to fetch GIF $id", e)
                null
            }
        }
}
