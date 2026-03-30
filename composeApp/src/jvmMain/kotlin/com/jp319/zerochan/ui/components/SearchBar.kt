package com.jp319.zerochan.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import com.jp319.zerochan.data.model.ZerochanSuggestion
import compose.icons.TablerIcons
import compose.icons.tablericons.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: (String) -> Unit,
    suggestions: List<ZerochanSuggestion>,
    onFocusChanged: (Boolean) -> Unit,
    onToggleFilters: () -> Unit,
    isFilterPanelVisible: Boolean,
    isLoading: Boolean,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(modifier = modifier) {
        val searchBarWidth = maxWidth

        Row(verticalAlignment = Alignment.CenterVertically) {
            // The custom Surface provides the background color and shape
            Surface(
                modifier =
                    Modifier
                        .weight(1f)
                        .height(40.dp),
                // Perfect 40dp height without clipping
                shape = MaterialTheme.shapes.extraLarge,
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), // The background color!
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 16.dp),
                ) {
                    // Leading Icon
                    Icon(
                        TablerIcons.Search,
                        contentDescription = "Search",
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    // BasicTextField gives us raw control with zero default padding
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.CenterStart,
                    ) {
                        // Custom Placeholder
                        if (query.isEmpty()) {
                            Text(
                                "Search tags (e.g., Frieren, One Piece)...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            )
                        }

                        BasicTextField(
                            value = query,
                            onValueChange = onQueryChange,
                            singleLine = true,
                            textStyle = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface),
                            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            keyboardActions =
                                KeyboardActions(
                                    onSearch = {
                                        if (query.isNotBlank() && !isLoading) {
                                            onFocusChanged(false)
                                            onSearch(query)
                                        }
                                    },
                                ),
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .onFocusChanged { focusState ->
                                        onFocusChanged(focusState.isFocused)
                                    },
                        )
                    }

                    // Trailing Clear Button
                    if (query.isNotEmpty()) {
                        AppTooltip(text = "Clear Search") {
                            IconButton(
                                onClick = {
                                    onQueryChange("")
                                    onFocusChanged(true)
                                },
                                modifier = Modifier.size(24.dp),
                            ) {
                                Icon(
                                    TablerIcons.X,
                                    contentDescription = "Clear",
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // The Dedicated Search / Loading Button
            Button(
                onClick = {
                    onFocusChanged(false)
                    onSearch(query)
                },
                enabled = query.isNotBlank() && !isLoading,
                modifier = Modifier.height(40.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                shape = MaterialTheme.shapes.extraLarge,
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp,
                    )
                    Spacer(Modifier.width(6.dp))
                    Text("Searching...", style = MaterialTheme.typography.labelMedium)
                } else {
                    Icon(TablerIcons.Search, contentDescription = "Search", modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Search", style = MaterialTheme.typography.labelMedium)
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // The Filter Toggle
            AppTooltip(text = "Advanced Filters") {
                IconButton(
                    onClick = onToggleFilters,
                    modifier = Modifier.size(40.dp),
                    colors =
                        if (isFilterPanelVisible) {
                            IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                        } else {
                            IconButtonDefaults.filledTonalIconButtonColors()
                        },
                ) {
                    Icon(TablerIcons.AdjustmentsHorizontal, contentDescription = "Filters", modifier = Modifier.size(20.dp))
                }
            }
        }

        // The Dropdown Menu (unchanged)
        DropdownMenu(
            expanded = suggestions.isNotEmpty(),
            onDismissRequest = { onFocusChanged(false) },
            modifier = Modifier.width(searchBarWidth).heightIn(max = 400.dp),
            properties = PopupProperties(focusable = false),
        ) {
            suggestions.forEach { suggestion ->
                DropdownMenuItem(
                    onClick = {
                        onQueryChange(suggestion.value)
                        onFocusChanged(false)
                        onSearch(suggestion.value)
                    },
                    text = {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = suggestion.value,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    if (!suggestion.type.isNullOrBlank()) {
                                        Text(
                                            text = suggestion.type,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.primary,
                                        )
                                    }
                                    if (!suggestion.alias.isNullOrBlank() && suggestion.alias != suggestion.value) {
                                        Text(
                                            text = "alias: ${suggestion.alias}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                        )
                                    }
                                }
                            }

                            if (suggestion.total != null && suggestion.total > 0) {
                                Surface(
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    shape = MaterialTheme.shapes.small,
                                ) {
                                    Text(
                                        text = "${suggestion.total}",
                                        style = MaterialTheme.typography.labelSmall,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    )
                                }
                            }
                        }
                    },
                    leadingIcon = {
                        val icon =
                            when (suggestion.type?.lowercase()) {
                                "recent search" -> TablerIcons.History
                                "character" -> TablerIcons.User
                                "mangaka", "studio" -> TablerIcons.Brush
                                "series", "game" -> TablerIcons.Book
                                else -> TablerIcons.Tag
                            }
                        Icon(
                            icon,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    },
                )
            }
        }
    }
}
