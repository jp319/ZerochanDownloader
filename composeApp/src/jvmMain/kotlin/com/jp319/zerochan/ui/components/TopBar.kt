package com.jp319.zerochan.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import compose.icons.TablerIcons
import compose.icons.tablericons.*
import kotlin.math.roundToInt

/**
 * The application's top navigation and control bar.
 * Adapts between "Normal" mode (logo, search tools, settings) and "Contextual" mode
 * (selection count, batch actions) based on the application state.
 *
 * @param burstCount Current number of API requests made in the last minute.
 * @param selectedCount The number of images currently selected for batch operations.
 * @param onDownloadClick Callback to download all selected items.
 * @param onClearSelection Callback to deselect all items.
 * @param onProfileClick Callback to open user settings.
 * @param onLibraryClick Callback to open the local downloads library.
 * @param onHelpClick Callback to open the user guide.
 * @param isSelectionModeActive Whether the manual selection mode is currently enabled.
 * @param onToggleSelectionMode Callback to enter/exit selection mode.
 * @param zoomLevel The current display scale for the image grid.
 * @param onZoomIn Callback to increase the grid scale.
 * @param onZoomOut Callback to decrease the grid scale.
 * @param onZoomReset Callback to return the grid scale to 100%.
 * @param onMinimize Window control: Minimize to taskbar.
 * @param onMaximizeToggle Window control: Toggle maximized/windowed state.
 * @param onClose Window control: Exit the application.
 */
@Composable
fun TopBar(
    burstCount: Int,
    ongoingDownloadCount: Int,
    isUpdateAvailable: Boolean,
    selectedCount: Int,
    onDownloadClick: () -> Unit,
    onClearSelection: () -> Unit,
    onProfileClick: () -> Unit,
    onLibraryClick: () -> Unit,
    onHelpClick: () -> Unit,
    isSelectionModeActive: Boolean,
    onToggleSelectionMode: () -> Unit,
    zoomLevel: Float,
    onZoomIn: () -> Unit,
    onZoomOut: () -> Unit,
    onZoomReset: () -> Unit,
    onMinimize: () -> Unit,
    onMaximizeToggle: () -> Unit,
    onClose: () -> Unit,
) {
    val counterColor =
        when {
            burstCount >= 40 -> MaterialTheme.colorScheme.error
            burstCount >= 20 -> MaterialTheme.colorScheme.secondary
            else -> MaterialTheme.colorScheme.primary
        }

    val isContextualMode = isSelectionModeActive || selectedCount > 0

    Surface(
        color = if (isContextualMode) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.background,
        modifier = Modifier.fillMaxWidth().height(48.dp),
    ) {
        AnimatedContent(
            targetState = isContextualMode,
            transitionSpec = {
                (fadeIn() + slideInVertically { it }).togetherWith(fadeOut() + slideOutVertically { -it })
            },
            modifier = Modifier.fillMaxSize(),
        ) { contextual ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 16.dp).fillMaxSize(),
            ) {
                if (contextual) {
                    // SELECTION MODE ACTIVE (FULL BAR)
                    AppTooltip(text = "Clear Selection / Exit Mode") {
                        IconButton(onClick = {
                            if (selectedCount > 0) {
                                onClearSelection()
                            } else {
                                onToggleSelectionMode()
                            }
                        }) {
                            Icon(TablerIcons.X, contentDescription = "Exit Selection Mode")
                        }
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (selectedCount > 0) "$selectedCount selected" else "Select images",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                        if (selectedCount == 0) {
                            Text(
                                "Click or drag to select",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                            )
                        }
                    }

                    if (selectedCount > 0) {
                        AppTooltip(text = "Download Selected Images") {
                            Button(
                                onClick = onDownloadClick,
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                                modifier = Modifier.height(32.dp),
                            ) {
                                Icon(TablerIcons.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Download", fontSize = 12.sp)
                            }
                        }
                    }
                } else {
                    // NORMAL MODE ACTIVE
                    AppLogo(size = 24.dp)
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = "Zerochan Downloader",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f),
                    )

                    AppTooltip(text = "API Usage / Rate Limits") {
                        Surface(
                            color = counterColor.copy(alpha = 0.2f),
                            shape = MaterialTheme.shapes.small,
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            ) {
                                Icon(
                                    TablerIcons.AlertTriangle,
                                    contentDescription = "API Usage",
                                    modifier = Modifier.size(16.dp),
                                    tint = counterColor,
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    text = "$burstCount / 60",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = counterColor,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                        }
                    }

                    Spacer(Modifier.width(16.dp))

                    // ZOOM CONTROLS
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onZoomOut, modifier = Modifier.size(28.dp)) {
                            Icon(TablerIcons.Minus, null, modifier = Modifier.size(16.dp))
                        }

                        val zoomScale by animateFloatAsState(
                            targetValue = 1f,
                            animationSpec = spring(dampingRatio = 0.5f),
                            label = "zoomLabelBounce",
                        )
                        TextButton(
                            onClick = onZoomReset,
                            contentPadding = PaddingValues(0.dp),
                            modifier = Modifier.widthIn(min = 48.dp).scale(zoomScale),
                        ) {
                            Text(
                                "${(zoomLevel * 100).roundToInt()}%",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                            )
                        }

                        IconButton(onClick = onZoomIn, modifier = Modifier.size(28.dp)) {
                            Icon(TablerIcons.Plus, null, modifier = Modifier.size(16.dp))
                        }
                    }

                    Spacer(Modifier.width(8.dp))

                    AppTooltip(text = "Toggle Selection Mode") {
                        IconToggleButton(
                            checked = isSelectionModeActive,
                            onCheckedChange = { onToggleSelectionMode() },
                        ) {
                            Icon(
                                imageVector = TablerIcons.CircleCheck,
                                contentDescription = "Selection Mode",
                                tint = if (isSelectionModeActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                    AppTooltip(text = "Local Library") {
                        IconButton(onClick = onLibraryClick) {
                            BadgedBox(
                                badge = {
                                    if (ongoingDownloadCount > 0) {
                                        Badge(
                                            containerColor = MaterialTheme.colorScheme.primary,
                                            contentColor = MaterialTheme.colorScheme.onPrimary,
                                            modifier = Modifier.offset(x = (-4).dp, y = 4.dp),
                                        ) {
                                            Text(ongoingDownloadCount.toString())
                                        }
                                    }
                                },
                            ) {
                                Icon(TablerIcons.Folder, contentDescription = "Local Library", tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                    AppTooltip(text = "Profile Settings") {
                        IconButton(onClick = onProfileClick) {
                            Icon(TablerIcons.User, contentDescription = "Profile Settings", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                    AppTooltip(text = "Help & Guide") {
                        IconButton(onClick = onHelpClick) {
                            BadgedBox(
                                badge = {
                                    if (isUpdateAvailable) {
                                        Badge(
                                            containerColor = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.offset(x = (-4).dp, y = 4.dp),
                                        )
                                    }
                                },
                            ) {
                                Icon(TablerIcons.InfoCircle, contentDescription = "Help & Guide", tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }

                // WINDOW CONTROLS (Hidden in Contextual Mode)
                if (!isContextualMode) {
                    Spacer(Modifier.width(8.dp))
                    VerticalDivider(
                        modifier = Modifier.height(24.dp),
                        thickness = 1.dp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                    )
                    Spacer(Modifier.width(8.dp))

                    IconButton(onClick = onMinimize, modifier = Modifier.size(32.dp)) {
                        Icon(TablerIcons.Minus, contentDescription = "Minimize", modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = onMaximizeToggle, modifier = Modifier.size(32.dp)) {
                        Icon(TablerIcons.Square, contentDescription = "Maximize", modifier = Modifier.size(14.dp))
                    }
                    IconButton(
                        onClick = onClose,
                        modifier = Modifier.size(32.dp),
                        colors = IconButtonDefaults.iconButtonColors(contentColor = MaterialTheme.colorScheme.onSurfaceVariant),
                    ) {
                        Icon(TablerIcons.X, contentDescription = "Close", modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}
