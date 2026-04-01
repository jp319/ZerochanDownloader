package com.jp319.zerochan.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.jp319.zerochan.BuildConfig
import com.jp319.zerochan.data.model.UpdateInfo
import compose.icons.TablerIcons
import compose.icons.tablericons.BrandGithub
import compose.icons.tablericons.ChevronRight
import compose.icons.tablericons.Download
import compose.icons.tablericons.InfoCircle

/**
 * An informational dialog providing a "Getting Started" guide and Update management.
 *
 * @param updateInfo The latest version info from GitHub, if any.
 * @param onDownloadUpdate Callback to start downloading the binary update.
 * @param onOpenGitHub Callback to open the releases page in browser.
 * @param onDismiss Callback to close the dialog.
 */
@Composable
fun GuideDialog(
    updateInfo: UpdateInfo?,
    onDownloadUpdate: () -> Unit,
    onOpenGitHub: () -> Unit,
    onDismiss: () -> Unit,
) {
    var selectedTabIndex by remember { mutableStateOf(if (updateInfo != null) 1 else 0) }
    val tabs = listOf("Guide", "Updates")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = "App Resources",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
                SecondaryTabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = Color.Transparent,
                    contentColor = MaterialTheme.colorScheme.primary,
                    divider = {},
                    indicator = {
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(selectedTabIndex),
                            color = MaterialTheme.colorScheme.primary,
                        )
                    },
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index },
                            text = {
                                BadgedBox(
                                    badge = {
                                        if (index == 1 && updateInfo != null) {
                                            Badge(containerColor = MaterialTheme.colorScheme.error)
                                        }
                                    },
                                ) {
                                    Text(title)
                                }
                            },
                        )
                    }
                }
            }
        },
        text = {
            Box(modifier = Modifier.fillMaxWidth().heightIn(max = 500.dp)) {
                Column(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                            .padding(end = 8.dp),
                ) {
                    if (selectedTabIndex == 0) {
                        GuideTabContent()
                    } else {
                        UpdateTabContent(
                            updateInfo = updateInfo,
                            onDownloadUpdate = onDownloadUpdate,
                            onOpenGitHub = onOpenGitHub,
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        textContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        shape = MaterialTheme.shapes.large,
    )
}

@Composable
private fun GuideTabContent() {
    GuideSection(
        title = "Getting Started",
        content = "To use the app, simply enter your Zerochan username in Settings. No password or login is required! This authenticates your session for exploring.",
    )

    GuideSection(
        title = "Searching & Filtering",
        content = "Use the top search bar to find images by tags (e.g. 'One Piece'). Click the filter icon to refine results by dimensions, upload time, or primary colors.",
    )

    GuideSection(
        title = "Downloading & Multi-Selection",
        content = "You can download individual images by clicking them and hitting Download. For bulk downloads, long-press any image to enter Selection Mode. You can then quickly click, or even drag your mouse across multiple images to select them all at once!",
    )

    GuideSection(
        title = "Animated Previews",
        content = "When previewing an animated GIF, the app automatically caches it to prevent redownloading the same animation repeatedly.",
    )

    Spacer(modifier = Modifier.height(16.dp))
    HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
    Spacer(modifier = Modifier.height(16.dp))

    Text(
        text = "API Documentation",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.primary,
    )
    Spacer(modifier = Modifier.height(4.dp))
    Text(
        text = "This app is powered by the official Zerochan API. Note that rate limits apply per username, so rapid searching might be momentarily delayed.",
        style = MaterialTheme.typography.bodyMedium,
    )
    Spacer(modifier = Modifier.height(8.dp))
    Button(
        onClick = {
            try {
                com.jp319.zerochan.utils.FileUtil.openWebpage("https://www.zerochan.net/api")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        },
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text("Read Zerochan API Docs")
    }
}

@Composable
private fun UpdateTabContent(
    updateInfo: UpdateInfo?,
    onDownloadUpdate: () -> Unit,
    onOpenGitHub: () -> Unit,
) {
    if (updateInfo != null) {
        // --- UPDATE AVAILABLE ---
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(TablerIcons.InfoCircle, null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "New Version Available!",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    "Version ${updateInfo.latestVersion} is out (Current: ${BuildConfig.VERSION})",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = onDownloadUpdate,
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                ) {
                    Icon(TablerIcons.Download, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Download & Update Now")
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        Text(
            "What's New in ${updateInfo.latestVersion}",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            updateInfo.releaseNotes.take(300) + if (updateInfo.releaseNotes.length > 300) "..." else "",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    } else {
        // --- UP TO DATE ---
        Column(
            modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                TablerIcons.InfoCircle,
                null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
            )
            Spacer(Modifier.height(16.dp))
            Text("Your app is up to date!", fontWeight = FontWeight.Bold)
            Text("Version ${BuildConfig.VERSION}", style = MaterialTheme.typography.labelMedium)
        }
    }

    Spacer(Modifier.height(24.dp))
    HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
    Spacer(Modifier.height(16.dp))

    // --- MANUAL UPDATE SECTION (LINUX/ARCH) ---
    Text(
        "Manual Update (Linux / AUR)",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
    )
    Spacer(Modifier.height(8.dp))
    Text(
        "If you are on Arch Linux or preferred the AUR, use one of these commands:",
        style = MaterialTheme.typography.bodySmall,
    )
    Spacer(Modifier.height(8.dp))

    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        shape = MaterialTheme.shapes.small,
        modifier = Modifier.fillMaxWidth(),
    ) {
        SelectionContainer {
            Column(modifier = Modifier.padding(12.dp)) {
                CodeLine("paru -S zerochan-downloader-bin")
                CodeLine("yay -S zerochan-downloader-bin")
                CodeLine("git pull && ./gradlew build")
            }
        }
    }

    Spacer(Modifier.height(16.dp))
    OutlinedButton(
        onClick = onOpenGitHub,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Icon(TablerIcons.BrandGithub, null, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Text("View on GitHub Releases")
    }
}

@Composable
private fun CodeLine(text: String) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 2.dp)) {
        Icon(TablerIcons.ChevronRight, null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.width(4.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            fontFamily = FontFamily.Monospace,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun GuideSection(
    title: String,
    content: String,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.padding(vertical = 8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = content,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
