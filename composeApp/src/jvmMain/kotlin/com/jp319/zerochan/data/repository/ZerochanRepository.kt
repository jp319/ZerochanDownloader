package com.jp319.zerochan.data.repository

import com.jp319.zerochan.data.model.*
import com.jp319.zerochan.data.network.DownloadProgressTracker
import com.jp319.zerochan.data.network.RequestTracker
import com.jp319.zerochan.data.profile.ProfileManager
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
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import kotlin.time.Duration.Companion.milliseconds

class NoUsernameException : Exception("Zerochan username is required for API access.")

class ZerochanRepository(
    private val client: HttpClient,
    val profileManager: ProfileManager
) {

    private val baseUrl = "https://www.zerochan.net"

    private suspend fun checkRateLimit() {
        val user = profileManager.username
        if (user.isBlank()) throw NoUsernameException()

        val lastTime = profileManager.getLastRequestTime(user)
        val now = System.currentTimeMillis()
        val elapsed = now - lastTime

        if (elapsed < 1000) {
            delay((1000 - elapsed).milliseconds)
        }
        profileManager.updateLastRequestTime(user, System.currentTimeMillis())
    }

    // Added a hidden 'isRetry' parameter to prevent infinite redirect loops
    suspend fun search(params: ZerochanApiParams, isRetry: Boolean = false): List<ZerochanItem> {
        RequestTracker.recordRequest()
        checkRateLimit()
        println("Searching with params: $params (Retry: $isRetry)")

        return try {
            val response: HttpResponse = client.get(baseUrl) {
                url {
                    appendPathSegments(params.tag ?: "")
                    parameters.apply {
                        append("json", "")
                        params.page?.let       { append("p", it.toString()) }
                        params.limit?.let      { append("l", it.toString()) }
                        params.sort?.let       { append("s", it.value) }
                        params.time?.let       { append("t", it.value.toString()) }
                        params.color?.let      { append("c", it) }
                        params.dimensions?.let { append("d", it.value) }
                        params.strict?.let     { if (it) append("strict", "") }
                    }
                }
            }

            val finalUrl = response.request.url
            println("Actual Request URL: $finalUrl")

            // --- AUTO-CORRECTION DETECTION LOGIC ---
            // If the final URL doesn't have our "json" parameter, Zerochan redirected us.
            if (!finalUrl.parameters.contains("json")) {
                if (isRetry) {
                    println("Bailing out: Prevented an infinite redirect loop.")
                    return emptyList()
                }

                // Extract the corrected tag from the end of the new URL path
                val correctedTag = finalUrl.rawSegments.lastOrNull()

                if (!correctedTag.isNullOrBlank()) {
                    println("Zerochan auto-corrected tag to: $correctedTag. Retrying request...")

                    // Copy our original params but inject the newly discovered tag
                    val correctedParams = params.copy(tag = correctedTag)

                    // Call this exact function again with the new params and the retry flag set
                    return search(correctedParams, isRetry = true)
                }
            }
            // ---------------------------------------

            if (!response.status.isSuccess()) {
                println("Search failed with status: ${response.status}")
                return emptyList()
            }

            // Safely parse the body
            val zerochanResponse: ZerochanResponse = response.body()
            zerochanResponse.items

        } catch (e: NoUsernameException) {
            throw e
        } catch (e: Exception) {
            println("Error fetching or parsing search results: ${e.message}")
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun getItem(id: Int): ZerochanFullItem? { // Note: Changed return type to nullable
        RequestTracker.recordRequest()

        return try {
            checkRateLimit()
            val response: HttpResponse = client.get("$baseUrl/$id") {
                url { parameters.append("json", "") }
            }

            if (!response.status.isSuccess()) {
                println("Failed to fetch item $id. Status: ${response.status}")
                return null
            }

            response.body()
        } catch (e: NoUsernameException) {
            throw e
        } catch (e: Exception) {
            println("Error fetching item $id: ${e.message}")
            e.printStackTrace()
            null
        }
    }

    suspend fun findValidFullResUrl(id: Int, tag: String): String? {
        val formattedTag = tag.replace(" ", ".")
        val baseUrl = "https://static.zerochan.net/$formattedTag.full.$id."
        val extensions = listOf("jpg", "png", "jpeg", "gif")

        println("🔍 Attempting to find full-res image for ID: $id with tag: '$tag' (formatted: '$formattedTag')")

        for (ext in extensions) {
            val urlToTest = "$baseUrl$ext"

            try {
                // 👇 1. Add a slight 300ms delay to pace the requests and bypass Cloudflare's bot-protection
                delay(300.milliseconds)

                val response = client.head(urlToTest) {
                    header("Referer", "https://www.zerochan.net/")
                    header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                }

                if (response.status.isSuccess()) {
                    val bytes = response.headers[HttpHeaders.ContentLength]?.toLongOrNull()
                    val sizeString = if (bytes != null) {
                        val mb = bytes / (1024f * 1024f)
                        "%.2f MB".format(mb)
                    } else {
                        "Unknown Size"
                    }

                    println("✅ Found valid full-res image: $urlToTest ($sizeString)")
                    return urlToTest
                } else {
                    // 👇 2. Log the exact status so we know if it's a 404 (Not Found) or 403 (Cloudflare Blocked)
                    println("⚠️ Tested $ext - Rejected with status: ${response.status}")
                }
            } catch (e: Exception) {
                println("⚠️ Network error testing $ext: ${e.message}")
            }
        }

        println("❌ Could not find a full-res match.")
        return null
    }

    suspend fun downloadImageToDisk(url: String, fileName: String, targetDirectoryPath: String): File? = withContext(Dispatchers.IO) {
        try {
            val zerochanDir = File(targetDirectoryPath)
            if (!zerochanDir.exists()) zerochanDir.mkdirs()

            val targetFile = File(zerochanDir, fileName)
            var lastPrintedPercent = -1 // For clean terminal logging

            client.prepareGet(url) {
                header("Referer", "https://www.zerochan.net/")
                header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")

                // 👇 2. Ktor's native progress tracker!
                onDownload { bytesSentTotal, contentLength ->
                    if (contentLength != null) {
                        if (contentLength > 0L) {
                            val progress = bytesSentTotal.toFloat() / contentLength.toFloat()

                            // Send progress to our Compose UI (The Queue Panel will instantly react!)
                            DownloadProgressTracker.updateProgress(url, progress)

                            // Throttle terminal logs so it only prints whole numbers on one line
                            val currentPercent = (progress * 100).toInt()
                            if (currentPercent != lastPrintedPercent) {
                                lastPrintedPercent = currentPercent
                                print("\r💾 Downloading $fileName: $currentPercent%")
                            }

                            // Print a clean newline when finished
                            if (bytesSentTotal == contentLength) {
                                println()
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

            println("✅ Successfully saved: ${targetFile.absolutePath}")
            return@withContext targetFile

        } catch (e: Exception) {
            println("❌ Failed to download $fileName: ${e.message}")
            return@withContext null
        }
    }

    private val suggestionCache = mutableMapOf<String, List<ZerochanSuggestion>>()

    suspend fun getSuggestions(query: String): List<ZerochanSuggestion> {
        val safeQuery = query.trim().lowercase()
        if (safeQuery.isBlank()) return emptyList()

        // LAYER 1: Memory Cache Hit! No network request needed.
        if (suggestionCache.containsKey(safeQuery)) {
            println("⚡ Cache Hit for suggestions: $safeQuery")
            return suggestionCache[safeQuery]!!
        }

        // LAYER 2: Network Fetch
        return try {
            checkRateLimit() // Respect the global rate limit
            val response = client.get("$baseUrl/suggest") {
                url {
                    parameters.append("q", query)
                    parameters.append("json", "1")
                }
            }

            if (response.status.isSuccess()) {
                val data: ZerochanSuggestionResponse = response.body()

                // Save it to RAM so we never have to ask the server for this exact word again
                suggestionCache[safeQuery] = data.suggestions
                data.suggestions
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            println("⚠️ Failed to fetch suggestions: ${e.message}")
            emptyList()
        }
    }

    suspend fun getRemoteFileBytes(url: String): ByteArray? = withContext(Dispatchers.IO) {
        try {
            client.get(url).readBytes()
        } catch (e: Exception) {
            null
        }
    }

    // 👇 Fetch and save to a temporary file
    suspend fun getRemoteGifFile(url: String): File? = withContext(Dispatchers.IO) {
        try {
            val bytes = client.get(url).readBytes()

            // Create a temp file in the OS's temp directory
            val tempFile = File.createTempFile("zerochan_preview_", ".gif")
            tempFile.deleteOnExit() // Tell the OS to delete it when the app closes

            tempFile.writeBytes(bytes)
            tempFile
        } catch (e: Exception) {
            null
        }
    }
}