package com.jp319.zerochan

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.window.WindowDraggableArea
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowScope
import com.jp319.zerochan.data.network.RequestTracker
import com.jp319.zerochan.data.network.createHttpClient
import com.jp319.zerochan.data.profile.ProfileManager
import com.jp319.zerochan.data.repository.ZerochanRepository
import com.jp319.zerochan.ui.components.GuideDialog
import com.jp319.zerochan.ui.components.HslColorPicker
import com.jp319.zerochan.ui.components.TopBar
import com.jp319.zerochan.ui.components.ZerochanChip
import com.jp319.zerochan.ui.screens.GalleryScreen
import com.jp319.zerochan.ui.screens.GalleryViewModel
import com.jp319.zerochan.ui.theme.*
import com.jp319.zerochan.utils.ColorUtils
import com.jp319.zerochan.utils.FileUtil
import compose.icons.TablerIcons
import compose.icons.tablericons.Check
import compose.icons.tablericons.User
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.painterResource
import zerochan.composeapp.generated.resources.Res
import zerochan.composeapp.generated.resources.logo
import kotlin.time.Duration.Companion.milliseconds

@Composable
@Preview
fun App(
    windowScope: WindowScope? = null,
    onMinimize: () -> Unit = {},
    onMaximizeToggle: () -> Unit = {},
    onClose: () -> Unit = {},
) {
    val profileManager = remember { ProfileManager() }
    var currentTheme by remember { mutableStateOf(profileManager.themePreference) }
    var currentThemeMode by remember { mutableStateOf(profileManager.themeMode) }

    AppTheme(themePreference = currentTheme, themeMode = currentThemeMode) {
        var showSplash by remember { mutableStateOf(true) }

        Box(Modifier.fillMaxSize()) {
            // Main content renders behind — no layout jump when splash leaves
            AnimatedVisibility(
                visible = !showSplash,
                enter = fadeIn(tween(400)),
            ) {
                MainScreen(
                    profileManager = profileManager,
                    currentTheme = currentTheme,
                    currentThemeMode = currentThemeMode,
                    onThemeChange = { newTheme ->
                        currentTheme = newTheme
                        profileManager.themePreference = newTheme
                    },
                    onThemeModeChange = { newMode ->
                        currentThemeMode = newMode
                        profileManager.themeMode = newMode
                    },
                    windowScope = windowScope,
                    onMinimize = onMinimize,
                    onMaximizeToggle = onMaximizeToggle,
                    onClose = onClose,
                )
            }

            // Splash sits on top and fades out
            AnimatedVisibility(
                visible = showSplash,
                exit = fadeOut(tween(500)),
            ) {
                SplashScreen(onFinished = { showSplash = false })
            }
        }
    }
}

@Composable
private fun SplashScreen(onFinished: () -> Unit) {
    LaunchedEffect(Unit) {
        delay(1800.milliseconds) // swap for real init work if needed
        onFinished()
    }

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(Res.drawable.logo),
            contentDescription = "ZeroChan logo",
            modifier = Modifier.size(160.dp),
        )
    }
}

@Composable
fun ProfileDialog(
    profileManager: ProfileManager,
    currentTheme: String,
    currentThemeMode: String,
    onThemeChange: (String) -> Unit,
    onThemeModeChange: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var tempUsername by remember { mutableStateOf(profileManager.username) }
    var tempTheme by remember { mutableStateOf(currentTheme) }
    var tempThemeMode by remember { mutableStateOf(currentThemeMode) }

    val themePresets =
        listOf(
            "Orange" to DraculaBurntOrange,
            "Purple" to DraculaPurple,
            "Pink" to DraculaPink,
            "Green" to DraculaGreen,
            "Red" to DraculaRed,
            "Yellow" to DraculaYellow,
            "Cyan" to DraculaCyan,
        )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Profile Settings") },
        text = {
            Column {
                Text(
                    "Enter your Zerochan username to access the API.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 16.dp),
                )
                OutlinedTextField(
                    value = tempUsername,
                    onValueChange = { tempUsername = it },
                    label = { Text("Zerochan Username") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(TablerIcons.User, contentDescription = null) },
                )
                Text(
                    "Required by Zerochan for identification.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp),
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    "Appearance",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 12.dp),
                )

                // Theme Mode Selection
                Box(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        val modes = listOf("Dark", "Light", "AMOLED", "Dracula", "Nord", "Monokai")
                        modes.forEach { mode ->
                            val isSelected = tempThemeMode == mode
                            ZerochanChip(
                                selected = isSelected,
                                onClick = {
                                    tempThemeMode = mode
                                    onThemeModeChange(mode)
                                },
                                label = mode,
                                leadingIcon =
                                    if (isSelected) {
                                        { Icon(TablerIcons.Check, null, Modifier.size(16.dp)) }
                                    } else {
                                        null
                                    },
                            )
                        }
                    }
                }

                Text(
                    "Preset Themes",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(bottom = 12.dp),
                )

                Box(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        themePresets.forEach { (name, color) ->
                            val isSelected = tempTheme == LegacyThemeMap[name] || tempTheme == name
                            Surface(
                                shape = CircleShape,
                                color = color,
                                modifier =
                                    Modifier
                                        .size(36.dp)
                                        .clickable {
                                            tempTheme = LegacyThemeMap[name] ?: name
                                            onThemeChange(tempTheme)
                                        },
                                border =
                                    if (isSelected) {
                                        BorderStroke(
                                            2.dp,
                                            MaterialTheme.colorScheme.onSurface,
                                        )
                                    } else {
                                        BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                                    },
                            ) {
                                if (isSelected) {
                                    Icon(
                                        imageVector = TablerIcons.Check,
                                        contentDescription = null,
                                        tint = if (Color(color.toArgb()).luminance() > 0.5f) Color.Black else Color.White,
                                        modifier = Modifier.padding(6.dp),
                                    )
                                }
                            }
                        }
                    }
                }

                Text(
                    "Custom Color",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(bottom = 8.dp),
                )

                // --- HSL Color Picker ---
                HslColorPicker(
                    initialColor = ColorUtils.parseHexColor(tempTheme, DraculaBurntOrange),
                    onColorChanged = { newColor ->
                        val argb = newColor.toArgb()
                        val hex = String.format("#%06X", (0xFFFFFF and argb))
                        tempTheme = hex
                        onThemeChange(hex)
                    },
                    modifier = Modifier.fillMaxWidth().height(200.dp).padding(horizontal = 16.dp),
                )

                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 8.dp)) {
                    Surface(
                        modifier = Modifier.size(24.dp),
                        shape = CircleShape,
                        color = ColorUtils.parseHexColor(tempTheme, DraculaBurntOrange),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    ) {}
                    Spacer(Modifier.width(8.dp))
                    Text(tempTheme, style = MaterialTheme.typography.labelMedium)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    profileManager.username = tempUsername
                    if (tempTheme != currentTheme) {
                        onThemeChange(tempTheme)
                    }
                    onDismiss()
                },
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
    )
}

@Composable
private fun MainScreen(
    profileManager: ProfileManager,
    currentTheme: String,
    currentThemeMode: String,
    onThemeChange: (String) -> Unit,
    onThemeModeChange: (String) -> Unit,
    windowScope: WindowScope?,
    onMinimize: () -> Unit,
    onMaximizeToggle: () -> Unit,
    onClose: () -> Unit,
) {
    var showProfileDialog by remember { mutableStateOf(false) }
    var showGuideDialog by remember { mutableStateOf(profileManager.isFirstLaunch) }
    val burstCount by RequestTracker.burstCount.collectAsState()

    val (zoomLevel, setZoomLevel) = remember { mutableStateOf(1f) }

    val viewModel =
        remember {
            GalleryViewModel(
                repository =
                    ZerochanRepository(
                        client = createHttpClient(profileManager),
                        profileManager = profileManager,
                    ),
            )
        }

    val updateInfo by viewModel.updateInfo.collectAsState()
    val isUpdateAvailable by viewModel.isUpdateAvailable.collectAsState()
    val selectedIds by viewModel.selectedIdsForDownload.collectAsState()
    val isSelectionModeActive by viewModel.isSelectionModeActive.collectAsState()

    if (showGuideDialog) {
        GuideDialog(
            updateInfo = updateInfo,
            onDownloadUpdate = {
                showGuideDialog = false
                viewModel.downloadUpdateInstaller()
            },
            onOpenGitHub = {
                val url = updateInfo?.releaseUrl ?: "https://github.com/jp319/ZerochanDownloader/releases/latest"
                FileUtil.openWebpage(url)
            },
            onDismiss = {
                showGuideDialog = false
                if (profileManager.isFirstLaunch) {
                    profileManager.isFirstLaunch = false
                }
            },
        )
    }

    if (showProfileDialog) {
        ProfileDialog(
            profileManager = profileManager,
            currentTheme = currentTheme,
            currentThemeMode = currentThemeMode,
            onThemeChange = onThemeChange,
            onThemeModeChange = onThemeModeChange,
            onDismiss = { showProfileDialog = false },
        )
    }

    @Composable
    fun RenderTopBar() {
        val ongoingDownloadCount by viewModel.ongoingDownloadCount.collectAsState()
        TopBar(
            burstCount = burstCount,
            ongoingDownloadCount = ongoingDownloadCount,
            isUpdateAvailable = isUpdateAvailable,
            selectedCount = selectedIds.size,
            onDownloadClick = { viewModel.downloadSelectedItems() },
            onClearSelection = { viewModel.clearSelection() },
            onProfileClick = { showProfileDialog = true },
            onLibraryClick = { viewModel.toggleDownloadsModal(true) },
            onHelpClick = { showGuideDialog = true },
            isSelectionModeActive = isSelectionModeActive,
            onToggleSelectionMode = { viewModel.toggleSelectionMode() },
            zoomLevel = zoomLevel,
            onZoomIn = { setZoomLevel((zoomLevel + 0.1f).coerceAtMost(1.5f)) },
            onZoomOut = { setZoomLevel((zoomLevel - 0.1f).coerceAtLeast(0.5f)) },
            onZoomReset = { setZoomLevel(1f) },
            onMinimize = onMinimize,
            onMaximizeToggle = onMaximizeToggle,
            onClose = onClose,
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Column {
                if (windowScope != null) {
                    windowScope.WindowDraggableArea {
                        RenderTopBar()
                    }
                } else {
                    RenderTopBar()
                }
            }
        },
    ) { innerPadding ->
        GalleryScreen(
            viewModel = viewModel,
            zoomLevel = zoomLevel,
            onZoomChange = setZoomLevel,
            modifier = Modifier.padding(innerPadding).fillMaxSize(),
        )
    }
}
