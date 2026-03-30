package com.jp319.zerochan

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jp319.zerochan.data.network.RequestTracker
import com.jp319.zerochan.data.network.createHttpClient
import com.jp319.zerochan.data.profile.ProfileManager
import com.jp319.zerochan.data.repository.ZerochanRepository
import com.jp319.zerochan.ui.components.TopBar
import com.jp319.zerochan.ui.screens.GalleryScreen
import com.jp319.zerochan.ui.screens.GalleryViewModel
import com.jp319.zerochan.ui.theme.*
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
fun App() {
    val profileManager = remember { ProfileManager() }
    var currentTheme by remember { mutableStateOf(profileManager.themePreference) }

    AppTheme(themePreference = currentTheme) {
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
                    onThemeChange = { newTheme ->
                        currentTheme = newTheme
                        profileManager.themePreference = newTheme
                    },
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
    onThemeChange: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var tempUsername by remember { mutableStateOf(profileManager.username) }
    var tempTheme by remember { mutableStateOf(currentTheme) }

    val themes = listOf("Orange", "Purple", "Pink", "Green", "Red", "Yellow", "Cyan")
    val themeColors =
        listOf(
            DraculaBurntOrange,
            DraculaPurple,
            DraculaPink,
            DraculaGreen,
            DraculaRed,
            DraculaYellow,
            DraculaCyan,
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
                    "Theme Color",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 12.dp),
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    themes.forEachIndexed { index, themeName ->
                        val color = themeColors[index]
                        val isSelected = tempTheme == themeName
                        Surface(
                            shape = CircleShape,
                            color = color,
                            modifier =
                                Modifier
                                    .size(32.dp)
                                    .clickable { tempTheme = themeName },
                            border =
                                if (isSelected) {
                                    BorderStroke(
                                        2.dp,
                                        MaterialTheme.colorScheme.onSurface,
                                    )
                                } else {
                                    null
                                },
                        ) {
                            if (isSelected) {
                                Icon(
                                    imageVector = TablerIcons.Check,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.surface,
                                    modifier = Modifier.padding(4.dp),
                                )
                            }
                        }
                    }
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
    onThemeChange: (String) -> Unit,
) {
    var showProfileDialog by remember { mutableStateOf(false) }
    val burstCount by RequestTracker.burstCount.collectAsState()

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

    val selectedIds by viewModel.selectedIdsForDownload.collectAsState()
    val isSelectionModeActive by viewModel.isSelectionModeActive.collectAsState()

    if (showProfileDialog) {
        ProfileDialog(
            profileManager = profileManager,
            currentTheme = currentTheme,
            onThemeChange = onThemeChange,
            onDismiss = { showProfileDialog = false },
        )
    }

    Scaffold(
        topBar = {
            TopBar(
                burstCount = burstCount,
                selectedCount = selectedIds.size,
                onDownloadClick = viewModel::downloadSelectedItems,
                onClearSelection = viewModel::clearSelection,
                onLibraryClick = { viewModel.toggleDownloadsModal(true) },
                onProfileClick = { showProfileDialog = true },
                isSelectionModeActive = isSelectionModeActive,
                onToggleSelectionMode = viewModel::toggleSelectionMode,
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        GalleryScreen(
            viewModel = viewModel,
            modifier = Modifier.padding(innerPadding),
        )
    }
}
