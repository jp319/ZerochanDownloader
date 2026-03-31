package com.jp319.zerochan.utils

import java.awt.Desktop
import java.io.File
import javax.swing.JFileChooser

/**
 * A utility class for file system operations, including image discovery,
 * native OS file selection, and opening files or URLs with default system applications.
 */
object FileUtil {
    /**
     * Scans a directory for supported image formats.
     *
     * @param directoryPath The path to search.
     * @return A list of [File] objects found, sorted by modification date.
     */
    fun getImagesFromDirectory(directoryPath: String): List<File> {
        val dir = File(directoryPath)
        if (!dir.exists() || !dir.isDirectory) return emptyList()

        return dir.listFiles { file ->
            file.isFile && file.extension.lowercase() in listOf("jpg", "jpeg", "png", "gif")
        }?.sortedByDescending { it.lastModified() } ?: emptyList()
    }

    /**
     * Opens a native OS directory picker dialog using JFileChooser.
     *
     * @param currentPath The initial path to open the picker at.
     * @param onPathSelected Callback triggered once a valid directory is chosen.
     */
    fun chooseDirectory(
        currentPath: String,
        onPathSelected: (String) -> Unit,
    ) {
        val chooser =
            JFileChooser(currentPath).apply {
                fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
                dialogTitle = "Select Download Directory"
                isAcceptAllFileFilterUsed = false
            }

        if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            onPathSelected(chooser.selectedFile.absolutePath)
        }
    }

    /**
     * Opens a specific file using the OS's default associated application.
     *
     * @param file The file to be opened.
     */
    fun openFileNatively(file: File) {
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(file)
            } else {
                val osName = System.getProperty("os.name").lowercase()
                val runtime = Runtime.getRuntime()
                when {
                    osName.contains("win") -> runtime.exec(arrayOf("explorer", file.absolutePath))
                    osName.contains("mac") -> runtime.exec(arrayOf("open", file.absolutePath))
                    osName.contains("nix") || osName.contains("nux") -> runtime.exec(arrayOf("xdg-open", file.absolutePath))
                }
            }
        } catch (e: Exception) {
            println("Failed to open file: ${e.message}")
        }
    }

    /**
     * Opens a URL in the system's default web browser.
     * Supports multi-platform commands for Windows, Mac, and Linux.
     *
     * @param url The target website address.
     */
    fun openWebpage(url: String) {
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(java.net.URI(url))
            } else {
                val osName = System.getProperty("os.name").lowercase()
                val runtime = Runtime.getRuntime()
                when {
                    osName.contains("win") -> runtime.exec(arrayOf("rundll32", "url.dll,FileProtocolHandler", url))
                    osName.contains("mac") -> runtime.exec(arrayOf("open", url))
                    osName.contains("nix") || osName.contains("nux") -> runtime.exec(arrayOf("xdg-open", url))
                    else -> println("Cannot open URL on this OS")
                }
            }
        } catch (e: Exception) {
            println("Failed to open URL: ${e.message}")
        }
    }
}
