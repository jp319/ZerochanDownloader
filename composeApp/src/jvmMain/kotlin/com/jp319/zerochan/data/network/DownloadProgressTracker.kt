package com.jp319.zerochan.data.network

import com.jp319.zerochan.utils.Logger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import okhttp3.Interceptor
import okhttp3.MediaType
import okhttp3.Response
import okhttp3.ResponseBody
import okio.*

/**
 * Global state holder for monitoring active download progress across the application.
 * The Compose UI observes this to render progress bars dynamically.
 */
object DownloadProgressTracker {
    private val _progress = MutableStateFlow<Map<String, Float>>(emptyMap())
    val progress = _progress.asStateFlow()

    fun updateProgress(
        url: String,
        fraction: Float,
    ) {
        _progress.update { currentMap ->
            currentMap + (url to fraction)
        }
    }
}

/**
 * OkHttp Interceptor that wraps the response body to measure and report download progress.
 */
class ProgressInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val url = request.url.toString()
        val tag = "ProgressInterceptor"

        // Avoid logging thumbnail/avatar spam, track heavy requests
        if (!url.contains("/240/") && !url.contains("/75/") && !url.contains("/avatars")) {
            Logger.debug(tag, "Intercepted download: $url")
        }

        val originalResponse = chain.proceed(request)
        val body = originalResponse.body

        return originalResponse.newBuilder()
            .body(ProgressResponseBody(url, body))
            .build()
    }
}

/**
 * Custom ResponseBody that forwards byte reading events to the [DownloadProgressTracker].
 */
private class ProgressResponseBody(
    private val url: String,
    private val responseBody: ResponseBody,
) : ResponseBody() {
    private var bufferedSource: BufferedSource? = null

    override fun contentType(): MediaType? = responseBody.contentType()

    override fun contentLength(): Long = responseBody.contentLength()

    override fun source(): BufferedSource {
        if (bufferedSource == null) {
            bufferedSource = source(responseBody.source()).buffer()
        }
        return bufferedSource!!
    }

    private fun source(source: Source): Source {
        return object : ForwardingSource(source) {
            var totalBytesRead = 0L
            var lastPrintedPercent = -1

            override fun read(
                sink: Buffer,
                byteCount: Long,
            ): Long {
                val bytesRead = super.read(sink, byteCount)
                totalBytesRead += if (bytesRead != -1L) bytesRead else 0

                val total = contentLength()
                if (total > 0L) {
                    val progress = totalBytesRead.toFloat() / total.toFloat()

                    // Update UI state
                    DownloadProgressTracker.updateProgress(url, progress)

                    val currentPercent = (progress * 100).toInt()

                    // Optional console log filtering for high-value files
                    if (!url.contains("/240/") && !url.contains("/75/") && !url.contains("/avatars")) {
                        if (currentPercent != lastPrintedPercent) {
                            lastPrintedPercent = currentPercent
                            if (currentPercent % 10 == 0 || currentPercent == 100) {
                                Logger.debug("DownloadProgress", "Downloading: $currentPercent%")
                            }
                        }
                    }
                }
                return bytesRead
            }
        }
    }
}
