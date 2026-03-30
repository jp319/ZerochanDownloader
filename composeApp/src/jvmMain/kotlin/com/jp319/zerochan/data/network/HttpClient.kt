package com.jp319.zerochan.data.network

import com.jp319.zerochan.data.profile.ProfileManager
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

/**
 * Creates and configures the singleton Ktor HttpClient used throughout the repository.
 * It strictly configures OkHttp as the backing engine and applies critical headers
 * required by Zerochan to prevent API bans.
 *
 * @param profileManager Used to retrieve the current username for dynamic User-Agent injection.
 */
fun createHttpClient(profileManager: ProfileManager): HttpClient =
    HttpClient(OkHttp) {
        install(ContentNegotiation) {
            json(
                Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                },
            )
        }
        install(DefaultRequest) {
            val user = profileManager.username

            // Zerochan enforces strict User-Agent policies; if missing or generic, requests are blocked.
            val userAgent =
                if (user.isNotBlank()) {
                    "$user's ZerochanApp/1.0 - $user"
                } else {
                    "ZerochanApp/1.0"
                }

            headers {
                append("User-Agent", userAgent)
                append("Accept", "application/json")
            }
        }
    }
