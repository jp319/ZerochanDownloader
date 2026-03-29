package com.jp319.zerochan

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import com.jp319.zerochan.ui.theme.AppTheme
import compose.icons.TablerIcons
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

    AppTheme {
        var showSplash by remember { mutableStateOf(true) }

        Box(Modifier.fillMaxSize()) {
            // Main content renders behind — no layout jump when splash leaves
            AnimatedVisibility(
                visible = !showSplash,
                enter = fadeIn(tween(400)),
            ) {
                MainScreen(profileManager)
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
        modifier = Modifier
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
    onDismiss: () -> Unit
) {
    var tempUsername by remember { mutableStateOf(profileManager.username) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Profile Settings") },
        text = {
            Column {
                Text(
                    "Enter your Zerochan username to access the API.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                OutlinedTextField(
                    value = tempUsername,
                    onValueChange = { tempUsername = it },
                    label = { Text("Zerochan Username") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(TablerIcons.User, contentDescription = null) }
                )
                Text(
                    "Required by Zerochan for identification.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    profileManager.username = tempUsername
                    onDismiss()
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun MainScreen(profileManager: ProfileManager) {
    var showProfileDialog by remember { mutableStateOf(false) }
    val burstCount by RequestTracker.burstCount.collectAsState()

    val viewModel = remember {
        GalleryViewModel(
            repository = ZerochanRepository(
                client = createHttpClient(profileManager),
                profileManager = profileManager
            )
        )
    }

    val selectedIds by viewModel.selectedIdsForDownload.collectAsState()
    val isSelectionModeActive by viewModel.isSelectionModeActive.collectAsState()

    if (showProfileDialog) {
        ProfileDialog(
            profileManager = profileManager,
            onDismiss = { showProfileDialog = false }
        )
    }

    Scaffold(
        topBar = {
            TopBar(
                burstCount = burstCount,
                selectedCount = selectedIds.size, // 👈 Pass the size
                onDownloadClick = viewModel::downloadSelectedItems, // 👈 Pass the trigger
                onClearSelection = viewModel::clearSelection, // 👈 Pass the cancel action
                onLibraryClick = { viewModel.toggleDownloadsModal(true) },
                onProfileClick = { showProfileDialog = true },
                isSelectionModeActive = isSelectionModeActive, // 👈 Wire it!
                onToggleSelectionMode = viewModel::toggleSelectionMode, // 👈 Wire it!
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