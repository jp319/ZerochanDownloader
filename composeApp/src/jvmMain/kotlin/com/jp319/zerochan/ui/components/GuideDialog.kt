package com.jp319.zerochan.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.awt.Desktop
import java.net.URI

@Composable
fun GuideDialog(
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Welcome to Zerochan Explorer",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(end = 8.dp),
            ) {
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
                    text = "This app is powered by the official Zerochan API. Note that rate limits apply per username, so rapid searching might be momentarily delayed. For full API details, visit:",
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
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Read Zerochan API Docs")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Got it!")
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        textContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        shape = MaterialTheme.shapes.large,
    )
}

@Composable
private fun GuideSection(
    title: String,
    content: String,
    modifier: Modifier = Modifier
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
