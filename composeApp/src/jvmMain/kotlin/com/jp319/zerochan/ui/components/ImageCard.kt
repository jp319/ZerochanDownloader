package com.jp319.zerochan.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.jp319.zerochan.data.model.ZerochanItem
import compose.icons.TablerIcons
import compose.icons.tablericons.Check

@OptIn(ExperimentalComposeUiApi::class, ExperimentalFoundationApi::class)
@Composable
fun ImageCard(
    item: ZerochanItem,
    isSelected: Boolean = false,              // 👈 New param
    isSelectionModeActive: Boolean = false,   // 👈 New param
    onClick: () -> Unit,
    onLongClick: () -> Unit,                  // 👈 New param for right-click/long-press
    modifier: Modifier = Modifier,
) {
    val safeImageUrl = item.thumbnail.replace(".avif", ".jpg")
    val imageAspectRatio = if (item.height > 0) item.width.toFloat() / item.height.toFloat() else 1f

    var isHovered by remember { mutableStateOf(false) }

    val overlayAlpha by animateFloatAsState(
        targetValue = if (isHovered || isSelected) 1f else 0f,
        animationSpec = tween(durationMillis = 200),
        label = "HoverAlpha"
    )

    val placeholderColor = MaterialTheme.colorScheme.surfaceVariant
    val errorColor = MaterialTheme.colorScheme.errorContainer

    Surface(
        shape = MaterialTheme.shapes.medium,
        tonalElevation = 2.dp,
        // 👇 Add a border if it is selected
        border = if (isSelected) BorderStroke(3.dp, MaterialTheme.colorScheme.primary) else null,
        modifier = modifier
            .fillMaxWidth()
            // 👇 Replaced standard clickable with combinedClickable to catch long-press/right-click
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .onPointerEvent(PointerEventType.Enter) { isHovered = true }
            .onPointerEvent(PointerEventType.Exit) { isHovered = false }
    ) {
        Box(contentAlignment = Alignment.BottomCenter) {
            AsyncImage(
                model = safeImageUrl,
                contentDescription = item.tag,
                contentScale = ContentScale.FillWidth,
                placeholder = remember(placeholderColor) { ColorPainter(placeholderColor) },
                error = remember(errorColor) { ColorPainter(errorColor) },
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(imageAspectRatio)
                    .clip(MaterialTheme.shapes.medium)
            )

            // Hover / Selected Overlay
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .alpha(overlayAlpha)
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                        else Color.Black.copy(alpha = 0.6f)
                    )
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                if (!isSelected) {
                    Text(
                        text = item.tag,
                        color = Color.White,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // 👇 Show a beautiful checkmark indicator in the corner
            if (isSelected || (isHovered && isSelectionModeActive)) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .padding(8.dp),
                    contentAlignment = Alignment.TopStart
                ) {
                    Surface(
                        shape = CircleShape,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.5f),
                        modifier = Modifier.size(24.dp)
                    ) {
                        if (isSelected) {
                            Icon(
                                imageVector = TablerIcons.Check,
                                contentDescription = "Selected",
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.padding(4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}