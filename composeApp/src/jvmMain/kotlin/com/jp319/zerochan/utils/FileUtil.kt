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
    fun chooseDirectory(currentPath: String, onPathSelected: (String) -> Unit) {
        val chooser = JFileChooser(currentPath).apply {
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
            }
        } catch (e: Exception) {
            println("Failed to open file: ${e.message}")
        }
    }
}