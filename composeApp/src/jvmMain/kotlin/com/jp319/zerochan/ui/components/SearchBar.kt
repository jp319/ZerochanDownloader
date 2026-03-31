package com.jp319.zerochan.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.*
import androidx.compose.ui.input.pointer.*
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.jp319.zerochan.data.model.ZerochanSuggestion
import compose.icons.TablerIcons
import compose.icons.tablericons.*

/**
 * A highly interactive search bar component with autocomplete suggestions,
 * filter toggle capabilities, and keyboard navigation support.
 *
 * @param query The current text value of the search input.
 * @param onQueryChange Callback for text input changes.
 * @param onSearch Callback triggered when a search is executed (Enter or button click).
 * @param suggestions List of autocomplete or historical suggestions to display.
 * @param onFocusChanged Callback for focus state changes.
 * @param isLoading Whether a search or suggestion fetch is currently in progress.
 * @param isFilterPanelVisible Whether the secondary filter panel is currently expanded.
 * @param onToggleFilters Callback to expand/collapse the filter panel.
 * @param onRefresh Callback to re-trigger the current search.
 * @param filterContent Content to be displayed inside the expandable filter section.
 * @param modifier Divider to be applied to the root layout.
 */
@OptIn(ExperimentalComposeUiApi::class, ExperimentalAnimationApi::class)
@Composable
fun SearchBar(
    query: TextFieldValue,
    onQueryChange: (TextFieldValue) -> Unit,
    onSearch: (String) -> Unit,
    suggestions: List<ZerochanSuggestion>,
    onFocusChanged: (Boolean) -> Unit,
    isLoading: Boolean,
    isFilterPanelVisible: Boolean,
    onToggleFilters: () -> Unit,
    onRefresh: () -> Unit,
    ongoingDownloadCount: Int,
    onToggleDownloadManager: () -> Unit,
    filterContent: @Composable ColumnScope.() -> Unit,
    modifier: Modifier = Modifier,
) {
    var isFocused by remember { mutableStateOf(false) }
    var focusedIndex by remember { mutableIntStateOf(-1) }
    val focusRequester = remember { FocusRequester() }

    // --- ENH 4: Focused Border ---
    val borderColor by animateColorAsState(
        if (isFocused) {
            MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
        } else {
            MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
        },
        animationSpec = tween(200),
    )

    Column(modifier = modifier) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            tonalElevation = 0.dp,
            shadowElevation = 4.dp,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .border(2.dp, borderColor, RoundedCornerShape(24.dp))
                    .onFocusChanged {
                        isFocused = it.isFocused
                        onFocusChanged(it.isFocused)
                        if (!it.isFocused) focusedIndex = -1
                    }
                    .onPointerEvent(PointerEventType.Press) {
                        // Consume to prevent root box from clearing focus when clicking inside search bar
                        it.changes.forEach { change -> change.consume() }
                    }
                    .onPreviewKeyEvent { event ->
                        // --- ENH 1: Escape key support ---
                        if (event.key == Key.Escape && event.type == KeyEventType.KeyDown) {
                            onFocusChanged(false)
                            focusedIndex = -1
                            return@onPreviewKeyEvent true
                        }
                        if (event.type == KeyEventType.KeyDown) {
                            when (event.key) {
                                Key.DirectionDown -> {
                                    if (suggestions.isNotEmpty()) {
                                        focusedIndex = (focusedIndex + 1).coerceAtMost(suggestions.lastIndex)
                                        true
                                    } else {
                                        false
                                    }
                                }
                                Key.DirectionUp -> {
                                    if (suggestions.isNotEmpty()) {
                                        focusedIndex = (focusedIndex - 1).coerceAtLeast(-1)
                                        true
                                    } else {
                                        false
                                    }
                                }
                                Key.Enter -> {
                                    if (focusedIndex != -1 && suggestions.indices.contains(focusedIndex)) {
                                        onQueryChange(TextFieldValue(suggestions[focusedIndex].value, selection = TextRange(suggestions[focusedIndex].value.length)))
                                        onSearch(suggestions[focusedIndex].value)
                                        focusedIndex = -1
                                        true
                                    } else {
                                        onSearch(query.text)
                                        true
                                    }
                                }
                                else -> false
                            }
                        } else {
                            false
                        }
                    },
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                ) {
                    Icon(
                        imageVector = TablerIcons.Search,
                        contentDescription = null,
                        tint = if (isFocused) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp),
                    )

                    Box(
                        modifier =
                            Modifier
                                .weight(1f)
                                .padding(horizontal = 12.dp),
                        contentAlignment = Alignment.CenterStart,
                    ) {
                        if (query.text.isEmpty()) {
                            Text(
                                "Search tags...",
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                style = MaterialTheme.typography.bodyLarge,
                            )
                        }
                        BasicTextField(
                            value = query,
                            onValueChange = onQueryChange,
                            textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
                            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .focusRequester(focusRequester)
                                    .semantics {
                                        contentDescription = "Search input"
                                    },
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            keyboardActions = KeyboardActions(onSearch = { onSearch(query.text) }),
                            singleLine = true,
                        )
                    }

                    if (query.text.isNotEmpty()) {
                        IconButton(onClick = { onQueryChange(TextFieldValue("")) }, modifier = Modifier.size(28.dp)) {
                            Icon(TablerIcons.X, contentDescription = "Clear", modifier = Modifier.size(16.dp))
                        }
                    }

                    AppTooltip(text = "Refresh Results") {
                        IconButton(
                            onClick = onRefresh,
                            modifier = Modifier.size(28.dp),
                        ) {
                            Icon(TablerIcons.Refresh, contentDescription = "Refresh", modifier = Modifier.size(16.dp))
                        }
                    }

                    VerticalDivider(modifier = Modifier.height(24.dp).padding(horizontal = 8.dp))

                    AppTooltip(text = "Download Manager") {
                        BadgedBox(
                            badge = {
                                if (ongoingDownloadCount > 0) {
                                    Badge(
                                        containerColor = MaterialTheme.colorScheme.primary,
                                        contentColor = MaterialTheme.colorScheme.onPrimary,
                                    ) {
                                        Text(ongoingDownloadCount.toString())
                                    }
                                }
                            },
                            modifier = Modifier.padding(end = 8.dp),
                        ) {
                            IconButton(
                                onClick = onToggleDownloadManager,
                                modifier = Modifier.size(28.dp),
                            ) {
                                Icon(
                                    TablerIcons.Download,
                                    contentDescription = "Downloads",
                                    modifier = Modifier.size(20.dp),
                                    tint = if (ongoingDownloadCount > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }

                    AppTooltip(text = if (isFilterPanelVisible) "Hide Filters" else "Show Filters") {
                        IconButton(
                            onClick = onToggleFilters,
                            colors =
                                IconButtonDefaults.iconButtonColors(
                                    contentColor = if (isFilterPanelVisible) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                ),
                            modifier = Modifier.size(32.dp),
                        ) {
                            Icon(TablerIcons.AdjustmentsHorizontal, contentDescription = "Toggle Filters")
                        }
                    }

                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(32.dp).padding(6.dp),
                            strokeWidth = 3.dp,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    } else {
                        AppTooltip(text = "Search") {
                            IconButton(onClick = { onSearch(query.text) }, modifier = Modifier.size(32.dp)) {
                                Icon(TablerIcons.ChevronRight, contentDescription = "Search", tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }

                // --- ENH 7i: Spring animation for accordion ---
                AnimatedVisibility(
                    visible = isFilterPanelVisible,
                    enter = expandVertically(spring(dampingRatio = 0.8f, stiffness = 300f)),
                    exit = shrinkVertically(spring(dampingRatio = 0.8f, stiffness = 300f)),
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                        filterContent()
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                AnimatedVisibility(
                    visible = isFocused && (query.text.isNotEmpty() || suggestions.isNotEmpty()),
                    enter = expandVertically(spring(dampingRatio = 0.8f, stiffness = 300f)),
                    exit = shrinkVertically(spring(dampingRatio = 0.8f, stiffness = 300f)),
                ) {
                    Column {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                        LazyColumn(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 250.dp)
                                    .padding(vertical = 8.dp),
                        ) {
                            if (suggestions.isEmpty()) {
                                item {
                                    SuggestionItem(
                                        value = "No matching tags for \"${query.text}\"",
                                        icon = TablerIcons.InfoCircle,
                                        isHighlighted = false,
                                        index = 0,
                                        onClick = {},
                                    )
                                }
                            } else {
                                itemsIndexed(suggestions) { index, suggestion ->
                                    SuggestionItem(
                                        value = suggestion.value,
                                        type = suggestion.type,
                                        total = suggestion.total,
                                        icon =
                                            when (suggestion.type?.lowercase()) {
                                                "character" -> TablerIcons.User
                                                "series" -> TablerIcons.Book
                                                "recent" -> TablerIcons.History
                                                else -> TablerIcons.Tag
                                            },
                                        isHighlighted = index == focusedIndex,
                                        index = index,
                                        onClick = {
                                            // Trigger search immediately
                                            onQueryChange(TextFieldValue(suggestion.value, selection = TextRange(suggestion.value.length)))
                                            onSearch(suggestion.value)
                                        },
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * A single item within the search suggestion dropdown.
 *
 * @param value The text value of the suggestion.
 * @param icon The icon representing the suggestion type.
 * @param isHighlighted Whether the item is currently selected via keyboard navigation.
 * @param index The position of the item in the list (used for animation staggering).
 * @param type Optional category label (e.g., "Character", "Series").
 * @param total Optional count of images for this tag.
 * @param onClick Callback triggered when the suggestion is selected.
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun SuggestionItem(
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isHighlighted: Boolean,
    index: Int,
    type: String? = null,
    total: Int? = null,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    val backgroundColor by animateColorAsState(
        if (isHighlighted || isHovered) {
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
        } else {
            Color.Transparent
        },
        animationSpec = tween(150),
    )

    // --- ENH 7d: Staggered slide-in ---
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { isVisible = true }

    AnimatedVisibility(
        visible = isVisible,
        enter =
            slideInVertically(
                initialOffsetY = { -20 },
                animationSpec = tween(durationMillis = 200, delayMillis = (index * 30).coerceAtMost(150)),
            ) + fadeIn(),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .background(backgroundColor)
                    .hoverable(interactionSource)
                    .onPointerEvent(PointerEventType.Release) {
                        onClick()
                    }
                    .pointerHoverIcon(PointerIcon.Hand)
                    .padding(horizontal = 16.dp, vertical = 10.dp)
                    .semantics {
                        role = Role.Button
                        contentDescription = "Search suggestion: $value"
                    },
        ) {
            Icon(
                icon,
                null,
                modifier = Modifier.size(16.dp),
                tint = if (isHighlighted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            )
            Spacer(Modifier.width(16.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = if (isHighlighted) FontWeight.Bold else FontWeight.Normal,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (type != null) {
                Text(
                    type,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 8.dp),
                )
            }
            if (total != null && total > 0) {
                Text(
                    total.toString(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                )
            }
        }
    }
}
