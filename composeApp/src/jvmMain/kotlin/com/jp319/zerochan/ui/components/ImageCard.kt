package com.jp319.zerochan.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.jp319.zerochan.data.model.ZerochanItem
import compose.icons.TablerIcons
import compose.icons.tablericons.Check

@OptIn(ExperimentalFoundationApi::class, ExperimentalComposeUiApi::class)
@Composable
fun ImageCard(
    item: ZerochanItem,
    isSelected: Boolean,
    isSelectionModeActive: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onDragStart: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    val isPressed by interactionSource.collectIsPressedAsState()

    // --- ENH 7a: Spring-based press & hover scaling ---
    val cardScale by animateFloatAsState(
        targetValue = when {
            isPressed -> 0.96f
            isHovered -> 1.04f
            else -> 1f
        },
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 400f)
    )

    // --- ENH 7b: Staggered entrance animation ---
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { isVisible = true }
    
    val entranceAlpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(600, easing = LinearOutSlowInEasing)
    )
    val entranceTranslation by animateDpAsState(
        targetValue = if (isVisible) 0.dp else 40.dp,
        animationSpec = tween(600, easing = LinearOutSlowInEasing)
    )

    val safeImageUrl = item.thumbnail.replace(".avif", ".jpg")
    val imageAspectRatio = if (item.height > 0) item.width.toFloat() / item.height.toFloat() else 1f

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                alpha = entranceAlpha
                translationY = entranceTranslation.toPx()
            }
            .scale(cardScale)
            .onPointerEvent(PointerEventType.Press) {
                if (isSelectionModeActive) onDragStart()
            }
            .combinedClickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
                onLongClick = onLongClick,
            ),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = if (isHovered) 6.dp else 1.dp,
        border = if (isSelected) BorderStroke(3.dp, MaterialTheme.colorScheme.primary) else null,
    ) {
        Box(contentAlignment = Alignment.BottomCenter) {
            AsyncImage(
                model = safeImageUrl,
                contentDescription = item.tag,
                contentScale = ContentScale.FillWidth,
                placeholder = remember { ColorPainter(Color.LightGray.copy(alpha = 0.2f)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(imageAspectRatio)
                    .clip(RoundedCornerShape(12.dp)),
            )

            // Selection Indicator Overlay
            if (isSelectionModeActive || isSelected) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
                            else Color.Transparent,
                        ),
                )

                // --- ENH 7c: Checkmark Pop Animation ---
                AnimatedVisibility(
                    visible = isSelected,
                    enter = scaleIn(spring(dampingRatio = 0.5f, stiffness = 500f)) + fadeIn(),
                    exit = scaleOut() + fadeOut(),
                    modifier = Modifier.align(Alignment.TopEnd).padding(10.dp),
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary,
                        tonalElevation = 4.dp,
                        modifier = Modifier.size(28.dp),
                    ) {
                        Icon(
                            imageVector = TablerIcons.Check,
                            contentDescription = "Selected",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.padding(6.dp),
                        )
                    }
                }
            }

            // Hover Tags Overlay (Animated)
            AnimatedVisibility(
                visible = isHovered && !isSelectionModeActive,
                enter = fadeIn() + slideInVertically { it / 2 },
                exit = fadeOut() + slideOutVertically { it / 2 },
                modifier = Modifier.align(Alignment.BottomStart),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Transparent, Color.Black.copy(alpha = 0.75f)),
                            ),
                        )
                        .padding(12.dp),
                ) {
                    Text(
                        text = item.tag,
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            // GIF Badge
            val hasGifTag = remember(item.tags) { item.tags.any { it.lowercase().contains("animated gif") } }
            if (hasGifTag) {
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.9f),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.align(Alignment.TopStart).padding(8.dp),
                ) {
                    Text(
                        "GIF",
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                    )
                }
            }
        }
    }
}
