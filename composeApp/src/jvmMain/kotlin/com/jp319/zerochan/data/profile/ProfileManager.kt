package com.jp319.zerochan.data.profile

import java.io.File
import java.util.prefs.Preferences

/**
 * Manages user profile settings and state persistence across application sessions.
 * Backed by Java Preferences API for lightweight, simple local storage.
 */
class ProfileManager {
    private val prefs = Preferences.userNodeForPackage(ProfileManager::class.java)

    /**
     * The active Zerochan username. Required for making authenticated/whitelisted API calls.
     */
    var username: String
        get() = prefs.get(KEY_USERNAME, "")
        set(value) {
            prefs.put(KEY_USERNAME, value)
            prefs.flush()
        }

    /**
     * The system path where downloaded full-resolution images are saved.
     * Defaults to the native OS 'Downloads/Zerochan' folder.
     */
    var downloadDirectory: String
        get() {
            val defaultPath = File(System.getProperty("user.home"), "Downloads/Zerochan").absolutePath
            return prefs.get(KEY_DOWNLOAD_DIR, defaultPath)
        }
        set(value) {
            prefs.put(KEY_DOWNLOAD_DIR, value)
            prefs.flush()
        }

    /**
     * Stores up to the last 10 successful search queries for quick autocomplete referencing.
     */
    var searchHistory: List<String>
        get() {
            val historyString = prefs.get(KEY_SEARCH_HISTORY, "")
            return if (historyString.isBlank()) emptyList() else historyString.split("||")
        }
        set(value) {
            val safeList = value.take(10).joinToString("||")
            prefs.put(KEY_SEARCH_HISTORY, safeList)
            prefs.flush()
        }

    /**
     * Retrieves the UNIX timestamp of the last API request made by this user.
     * Used exclusively for rate-limiting calculations.
     */
    fun getLastRequestTime(user: String): Long {
        return prefs.getLong("${KEY_LAST_REQUEST_PREFIX}_$user", 0L)
    }

    /**
     * Records the exact time an API request was dispatched to Zerochan.
     */
    fun updateLastRequestTime(
        user: String,
        time: Long,
    ) {
        prefs.putLong("${KEY_LAST_REQUEST_PREFIX}_$user", time)
        prefs.flush()
    }

    /**
     * Property for observing or updating the user's theme color preference.
     */
    var themePreference: String
        get() = prefs.get(KEY_THEME_PREFERENCE, "Orange")
        set(value) {
            prefs.put(KEY_THEME_PREFERENCE, value)
            prefs.flush()
        }

    /**
     * Property indicating whether the user is opening the app for the very first time.
     */
    var isFirstLaunch: Boolean
        get() = prefs.getBoolean(KEY_IS_FIRST_LAUNCH, true)
        set(value) {
            prefs.putBoolean(KEY_IS_FIRST_LAUNCH, value)
            prefs.flush()
        }

    companion object {
        private const val KEY_USERNAME = "username"
        private const val KEY_LAST_REQUEST_PREFIX = "last_request_time"
        private const val KEY_DOWNLOAD_DIR = "download_directory"
        private const val KEY_SEARCH_HISTORY = "search_history"
        private const val KEY_THEME_PREFERENCE = "theme_preference"
        private const val KEY_IS_FIRST_LAUNCH = "is_first_launch"
    }
}
