package com.jp319.zerochan.data.profile

import java.io.File
import java.util.prefs.Preferences

class ProfileManager {
    private val prefs = Preferences.userNodeForPackage(ProfileManager::class.java)

    var username: String
        get() = prefs.get(KEY_USERNAME, "")
        set(value) {
            prefs.put(KEY_USERNAME, value)
            prefs.flush()
        }

    // 👇 1. Add the persistent download directory property
    var downloadDirectory: String
        get() {
            // Create a safe default path in the user's native Downloads folder
            val defaultPath = File(System.getProperty("user.home"), "Downloads/Zerochan").absolutePath
            return prefs.get(KEY_DOWNLOAD_DIR, defaultPath)
        }
        set(value) {
            prefs.put(KEY_DOWNLOAD_DIR, value)
            prefs.flush()
        }

    var searchHistory: List<String>
        get() {
            val historyString = prefs.get("search_history", "")
            return if (historyString.isBlank()) emptyList() else historyString.split("||")
        }
        set(value) {
            // Keep only the last 10 searches
            val safeList = value.take(10).joinToString("||")
            prefs.put("search_history", safeList)
            prefs.flush()
        }

    fun getLastRequestTime(user: String): Long {
        return prefs.getLong("${KEY_LAST_REQUEST_PREFIX}_$user", 0L)
    }

    fun updateLastRequestTime(user: String, time: Long) {
        prefs.putLong("${KEY_LAST_REQUEST_PREFIX}_$user", time)
        prefs.flush()
    }

    companion object {
        private const val KEY_USERNAME = "username"
        private const val KEY_LAST_REQUEST_PREFIX = "last_request_time"
        // 👇 2. Add the key constant
        private const val KEY_DOWNLOAD_DIR = "download_directory"
    }
}