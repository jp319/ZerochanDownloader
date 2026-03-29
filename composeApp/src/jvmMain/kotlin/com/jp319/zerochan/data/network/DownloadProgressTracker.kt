package com.jp319.zerochan.data.network

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import okhttp3.Interceptor
import okhttp3.MediaType
import okhttp3.Response
import okhttp3.ResponseBody
import okio.*

// 1. A global state holder that our Compose UI can listen to
object DownloadProgressTracker {
    private val _progress = MutableStateFlow<Map<String, Float>>(emptyMap())
    val progress = _progress.asStateFlow()

    fun updateProgress(url: String, fraction: Float) {
        _progress.update { currentMap ->
            currentMap + (url to fraction)
        }
    }
}

// 2. The Interceptor that plugs into our OkHttpClient
class ProgressInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val url = request.url.toString()

        // 👇 1. Prove that OkHttp actually woke up and caught the network request!
        // 👇 Only print logs if it's NOT a thumbnail or avatar!
        if (!url.contains("/240/") && !url.contains("/75/") && !url.contains("/avatars")) {
            println("📡 OkHttp Intercepted: $url")
        }

        val originalResponse = chain.proceed(request)

        // 👇 2. Warning fixed: Removed the elvis operator (?:) entirely
        val body = originalResponse.body

        return originalResponse.newBuilder()
            .body(ProgressResponseBody(url, body))
            .build()
    }
}

// 3. The engine that counts the bytes as they download
private class ProgressResponseBody(
    private val url: String,
    private val responseBody: ResponseBody
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

            override fun read(sink: Buffer, byteCount: Long): Long {
                val bytesRead = super.read(sink, byteCount)
                totalBytesRead += if (bytesRead != -1L) bytesRead else 0

                val total = contentLength()
                if (total > 0L) {
                    val progress = totalBytesRead.toFloat() / total.toFloat()

                    // Update the UI State (Runs for everything so UI is accurate)
                    DownloadProgressTracker.updateProgress(url, progress)

                    val currentPercent = (progress * 100).toInt()

                    // 👇 Silences terminal logs for grid thumbnails and avatars!
                    if (!url.contains("/240/") && !url.contains("/75/") && !url.contains("/avatars")) {

                        // Only print if the percentage actually went up
                        if (currentPercent != lastPrintedPercent) {
                            lastPrintedPercent = currentPercent
                            print("\r📡 Downloading: $currentPercent%")

                            // Print a clean newline ONLY when it exactly hits 100%
                            if (currentPercent == 100) {
                                println()
                            }
                        }
                    }
                }
                return bytesRead
            }
        }
    }
}