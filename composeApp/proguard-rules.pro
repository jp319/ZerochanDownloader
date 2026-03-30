# Ignore missing optional OkHttp dependencies
-dontwarn okhttp3.internal.platform.ConscryptPlatform**
-dontwarn okhttp3.internal.platform.BouncyCastlePlatform**
-dontwarn okhttp3.internal.platform.OpenJSSEPlatform**
-dontwarn okhttp3.internal.graal.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**
-dontwarn org.graalvm.nativeimage.**

# Ignore warnings for Ktor, Okio, and SLF4J
-dontwarn io.ktor.**
-dontwarn org.slf4j.**
-dontwarn okio.**

# Keep networking and coroutines fully intact (Prevents VerifyError)
-keep class io.ktor.** { *; }
-keep class okhttp3.** { *; }
-keep class okio.** { *; }
-keep interface okio.** { *; }
-keep class kotlinx.coroutines.** { *; }

# Keep Coil Image Loader intact (Prevents silent image failures)
-keep class coil3.** { *; }
-keep class coil.** { *; }

# Keep SLF4J logging provider intact
-keep class org.slf4j.** { *; }