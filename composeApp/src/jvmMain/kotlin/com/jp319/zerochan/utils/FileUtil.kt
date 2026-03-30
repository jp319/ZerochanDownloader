package com.jp319.zerochan.utils

import java.awt.Desktop
import java.io.File
import javax.swing.JFileChooser

object FileUtil {
    // Reads image files directly from the hard drive
    fun getImagesFromDirectory(directoryPath: String): List<File> {
        val dir = File(directoryPath)
        if (!dir.exists() || !dir.isDirectory) return emptyList()

        return dir.listFiles { file ->
            file.isFile && file.extension.lowercase() in listOf("jpg", "jpeg", "png", "gif")
        }?.sortedByDescending { it.lastModified() } ?: emptyList()
    }

    // Opens the OS native folder picker
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

    // Opens the image in Windows Photo Viewer / Mac Preview natively!
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

    // Opens a webpage reliably across Windows, Mac, and Linux
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
