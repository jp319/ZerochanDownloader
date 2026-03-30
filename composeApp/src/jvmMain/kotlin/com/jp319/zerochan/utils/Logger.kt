package com.jp319.zerochan.utils

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * A central logging utility for adjusting console outputs with standard colors.
 * This class replaces direct raw print statements to ensure logs are easy to follow,
 * searchable, and maintainable without relying on emojis.
 */
object Logger {
    // ANSI Escape Codes for colored terminal output
    private const val RESET = "\u001B[0m"
    private const val RED = "\u001B[31m"
    private const val GREEN = "\u001B[32m"
    private const val YELLOW = "\u001B[33m"
    private const val BLUE = "\u001B[34m"
    private const val CYAN = "\u001B[36m"

    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss.SSS")

    private fun currentTime(): String = LocalDateTime.now().format(timeFormatter)

    /**
     * Logs informational messages (e.g., standard workflow progress).
     * Output color: Standard/Default JVM with a cyan prefix.
     */
    fun info(
        tag: String,
        message: String,
    ) {
        println("[$CYAN${currentTime()}$RESET] [$GREEN INFO$RESET ] [$tag] $message")
    }

    /**
     * Logs debugging information (e.g., parameter states, payloads).
     * Output color: Blue.
     */
    fun debug(
        tag: String,
        message: String,
    ) {
        println("[$CYAN${currentTime()}$RESET] [$BLUE DEBUG$RESET] [$tag] $message")
    }

    /**
     * Logs warnings (e.g., unexpected states that are handled properly).
     * Output color: Yellow.
     */
    fun warn(
        tag: String,
        message: String,
    ) {
        println("[$CYAN${currentTime()}$RESET] [$YELLOW WARN$RESET ] [$tag] $message")
    }

    /**
     * Logs errors (e.g., exceptions, network failures, missing data).
     * Output color: Red.
     */
    fun error(
        tag: String,
        message: String,
        throwable: Throwable? = null,
    ) {
        println("[$CYAN${currentTime()}$RESET] [$RED ERROR$RESET] [$tag] $message")
        throwable?.printStackTrace()
    }
}
