package com.jp319.zerochan.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import compose.icons.TablerIcons
import compose.icons.tablericons.*

@Composable
fun TopBar(
    burstCount: Int,
    selectedCount: Int, // 👈 New parameter
    onDownloadClick: () -> Unit, // 👈 New parameter
    onClearSelection: () -> Unit, // 👈 New parameter
    onProfileClick: () -> Unit,
    onLibraryClick: () -> Unit,
    isSelectionModeActive: Boolean, // 👈 New parameter
    onToggleSelectionMode: () -> Unit, // 👈 New parameter
) {
    val counterColor = when {
        burstCount >= 40 -> MaterialTheme.colorScheme.error
        burstCount >= 20 -> MaterialTheme.colorScheme.secondary
        else -> MaterialTheme.colorScheme.primary
    }

    Surface(
        color = if (selectedCount > 0) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.background,
        modifier = Modifier.fillMaxWidth().height(48.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 16.dp).fillMaxSize(),
        ) {

            // 👇 Contextual Action Bar Logic
            if (selectedCount > 0) {
                // SELECTION MODE ACTIVE
                IconButton(onClick = onClearSelection) {
                    Icon(TablerIcons.X, contentDescription = "Clear Selection")
                }
                Text(
                    text = "$selectedCount selected",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Button(
                    onClick = onDownloadClick,
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Icon(TablerIcons.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Download")
                }
            } else {
                // NORMAL MODE ACTIVE
                AppLogo(size = 24.dp)
                Spacer(Modifier.width(12.dp))
                Text(
                    text = "ZeroChan Explorer",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )

                Surface(
                    color = counterColor.copy(alpha = 0.2f),
                    shape = MaterialTheme.shapes.small
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(TablerIcons.AlertTriangle, contentDescription = "API Usage", modifier = Modifier.size(16.dp), tint = counterColor)
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = "$burstCount / 60",
                            style = MaterialTheme.typography.labelMedium,
                            color = counterColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                IconToggleButton(
                    checked = isSelectionModeActive,
                    onCheckedChange = { onToggleSelectionMode() }
                ) {
                    Icon(
                        imageVector = TablerIcons.CircleCheck,
                        contentDescription = "Selection Mode",
                        tint = if (isSelectionModeActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onLibraryClick) { // 👈 Add this button!
                    Icon(TablerIcons.Download, contentDescription = "Local Library", tint = MaterialTheme.colorScheme.primary)
                }
                IconButton(onClick = onProfileClick) {
                    Icon(TablerIcons.User, contentDescription = "Profile Settings", tint = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}