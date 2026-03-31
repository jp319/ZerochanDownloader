package com.jp319.zerochan.ui.components

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.jp319.zerochan.ui.screens.UpdateDownloadState
import compose.icons.TablerIcons
import compose.icons.tablericons.AlertTriangle
import compose.icons.tablericons.Check
import compose.icons.tablericons.Download
import compose.icons.tablericons.ExternalLink
import java.io.File

/**
 * A modal dialog that manages and displays the progress of an application update download.
 *
 * @param state The current phase of the download (Downloading, Success, or Error).
 * @param progress A float from 0.0 to 1.0 representing download completion.
 * @param error An optional error message if the download fails.
 * @param onRetry Callback to restart the download process.
 * @param onShowInFolder Callback to open the OS file manager highlighting the installer.
 * @param onOpenGitHub Fallback callback to open the releases page if automatic download fails.
 * @param onDismiss Callback to close the dialog.
 */
@Composable
fun UpdateDownloadDialog(
    state: UpdateDownloadState,
    progress: Float,
    error: String?,
    onRetry: () -> Unit,
    onShowInFolder: (File) -> Unit,
    onOpenGitHub: () -> Unit,
    onDismiss: () -> Unit,
    downloadedFile: File? = null,
) {
    AlertDialog(
        onDismissRequest = { if (state != UpdateDownloadState.DOWNLOADING) onDismiss() },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(TablerIcons.Download, null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Downloading Update",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // --- STATUS ICON & MESSAGE ---
                Crossfade(targetState = state) { currentState ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        when (currentState) {
                            UpdateDownloadState.DOWNLOADING, UpdateDownloadState.IDLE -> {
                                Text(
                                    "Please keep the app open while we fetch the latest installer.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    textAlign = TextAlign.Center,
                                )
                                Spacer(Modifier.height(24.dp))
                                LinearProgressIndicator(
                                    progress = { progress },
                                    modifier = Modifier.fillMaxWidth().height(8.dp),
                                    strokeCap = ProgressIndicatorDefaults.LinearStrokeCap,
                                    color = MaterialTheme.colorScheme.primary,
                                    trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                )
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    "${(progress * 100).toInt()}%",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                            }
                            UpdateDownloadState.SUCCESS -> {
                                Icon(
                                    TablerIcons.Check,
                                    null,
                                    modifier = Modifier.size(48.dp),
                                    tint = MaterialTheme.colorScheme.primary,
                                )
                                Spacer(Modifier.height(16.dp))
                                Text(
                                    "Download Complete!",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                )
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    "Please close this application and run the downloaded installer to complete the update.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            UpdateDownloadState.ERROR -> {
                                Icon(
                                    TablerIcons.AlertTriangle,
                                    null,
                                    modifier = Modifier.size(48.dp),
                                    tint = MaterialTheme.colorScheme.error,
                                )
                                Spacer(Modifier.height(16.dp))
                                Text(
                                    "Download Failed",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.error,
                                )
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    error ?: "An unexpected error occurred.",
                                    style = MaterialTheme.typography.bodySmall,
                                    textAlign = TextAlign.Center,
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Column(modifier = Modifier.fillMaxWidth()) {
                when (state) {
                    UpdateDownloadState.SUCCESS -> {
                        Button(
                            onClick = { onDismiss() },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text("Got it")
                        }
                        Spacer(Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = {
                                downloadedFile?.let { onShowInFolder(it) }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = downloadedFile != null,
                        ) {
                            Text("Show Installer Folder")
                        }
                    }
                    UpdateDownloadState.ERROR -> {
                        Button(
                            onClick = onRetry,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text("Retry Download")
                        }
                        Spacer(Modifier.height(8.dp))
                        TextButton(
                            onClick = onOpenGitHub,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Icon(TablerIcons.ExternalLink, null, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Download Manually from GitHub")
                        }
                    }
                    else -> {
                        // Downloading - confirm button stays hidden or as a cancel button
                        TextButton(
                            onClick = onDismiss,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text("Run in Background")
                        }
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = MaterialTheme.shapes.large,
    )
}
