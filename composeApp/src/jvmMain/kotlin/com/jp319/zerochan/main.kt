package com.jp319.zerochan

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.memory.MemoryCache
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import coil3.request.crossfade
import com.jp319.zerochan.data.network.ProgressInterceptor
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit
import javax.imageio.ImageIO

fun main() {
    // Initialize Coil with OkHttp network support
    SingletonImageLoader.setSafe { context ->

        // Build the custom OkHttpClient to handle massive files and bypass Cloudflare
        val customOkHttpClient =
            OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS) // 60 seconds to connect
                .readTimeout(60, TimeUnit.SECONDS) // 60 seconds to download
                .addNetworkInterceptor(ProgressInterceptor())
                .addInterceptor(
                    Interceptor { chain ->
                        val originalRequest = chain.request()
                        val disguisedRequest =
                            originalRequest.newBuilder()
                                .header("Referer", "https://www.zerochan.net/")
                                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                                .build()
                        chain.proceed(disguisedRequest)
                    },
                )
                .build()

        ImageLoader.Builder(PlatformContext.INSTANCE)
            .components {
                // Tell the fetcher to use our custom client!
                add(OkHttpNetworkFetcherFactory(callFactory = { customOkHttpClient }))
            }
            .memoryCache {
                MemoryCache.Builder()
                    .maxSizePercent(context, 0.25) // Use 25% of available app memory for images
                    .build()
            }
            .crossfade(false)
            .build()
    }

    // Load via AWT — works across KDE, XFCE, GNOME, and Hyprland/Wayland
    val iconStream =
        Thread.currentThread()
            .contextClassLoader
            .getResourceAsStream("composeResources/zerochan.composeapp.generated.resources/drawable/logo.png")
    val icon = iconStream?.let { ImageIO.read(it) }

    // Set the icon in the AWT Toolkit for Wayland/XWayland compositors (e.g. Hyprland)
    // This is the only reliable way to set the taskbar icon on some compositors
    if (icon != null) {
        try {
            val iconImages = listOf(icon)
            // Reflect into sun.awt to set the default icon images used by all windows
            val awtAppClass = Class.forName("sun.awt.AppContext")
            val getAppContext = awtAppClass.getMethod("getAppContext")
            val appContext = getAppContext.invoke(null)
            val setMethod = awtAppClass.getMethod("put", Any::class.java, Any::class.java)
            setMethod.invoke(appContext, "window.icons", iconImages)
        } catch (_: Exception) {
            // Reflection not available — fall through to per-window icon below
        }
    }

    application {
        val windowState = androidx.compose.ui.window.rememberWindowState()
        Window(
            onCloseRequest = ::exitApplication,
            title = "Zerochan Downloader",
            state = windowState,
            undecorated = true,
            transparent = true,
        ) {
            // Set icon on the concrete AWT window
            if (icon != null) {
                window.iconImage = icon
                window.iconImages = listOf(icon)
            }
            App(
                windowScope = this,
                onMinimize = { windowState.isMinimized = true },
                onMaximizeToggle = {
                    if (windowState.placement == androidx.compose.ui.window.WindowPlacement.Maximized) {
                        windowState.placement = androidx.compose.ui.window.WindowPlacement.Floating
                    } else {
                        windowState.placement = androidx.compose.ui.window.WindowPlacement.Maximized
                    }
                },
                onClose = ::exitApplication,
            )
        }
    }
}
