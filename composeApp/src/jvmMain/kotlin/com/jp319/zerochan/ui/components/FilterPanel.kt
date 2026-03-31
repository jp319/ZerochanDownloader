package com.jp319.zerochan.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.jp319.zerochan.data.model.DimensionFilter
import com.jp319.zerochan.data.model.SortOrder
import com.jp319.zerochan.data.model.TimeFilter
import compose.icons.TablerIcons
import compose.icons.tablericons.ArrowsSort
import compose.icons.tablericons.Calendar
import compose.icons.tablericons.Check
import compose.icons.tablericons.ChevronDown
import compose.icons.tablericons.Lock
import compose.icons.tablericons.Maximize
import compose.icons.tablericons.Palette
import compose.icons.tablericons.X

/**
 * A panel containing various search filters such as sorting, time range,
 * dimensions, and color matching.
 *
 * @param sortOrder The current sorting strategy (Recent vs Popular).
 * @param onSortChange Callback for changing the sort order.
 * @param timeFilter The current time range filter (only applicable for Popular sort).
 * @param onTimeChange Callback for changing the time filter.
 * @param dimensionFilter The current image dimension/orientation filter.
 * @param onDimensionChange Callback for changing the dimension filter.
 * @param colorFilter The current primary color filter.
 * @param onColorChange Callback for changing the color filter.
 * @param strictMode Whether to use strict tag matching.
 * @param onStrictToggle Callback to toggle strict mode.
 * @param onClearFilters Callback to reset all filters to their defaults.
 * @param modifier Modifier to be applied to the root container.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FilterPanel(
    sortOrder: SortOrder?,
    onSortChange: (SortOrder?) -> Unit,
    timeFilter: TimeFilter?,
    onTimeChange: (TimeFilter?) -> Unit,
    dimensionFilter: DimensionFilter?,
    onDimensionChange: (DimensionFilter?) -> Unit,
    colorFilter: String?,
    onColorChange: (String?) -> Unit,
    strictMode: Boolean,
    onStrictToggle: () -> Unit,
    onClearFilters: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
        // 1. Header Row (Re-introducing a clean title + Clear All)
        Row(
            modifier = Modifier.fillMaxWidth().height(30.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                "Filter Results",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            if (sortOrder != null || timeFilter != null || dimensionFilter != null || colorFilter != null || strictMode) {
                TextButton(onClick = onClearFilters, contentPadding = PaddingValues(horizontal = 0.dp, vertical = 0.dp)) {
                    Icon(TablerIcons.X, null, Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Reset All", style = MaterialTheme.typography.labelLarge)
                }
            }
        }

        // 2. Chip Flow (Bigger, icon-based chips)
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            // Strict Mode
            ZerochanChip(
                selected = strictMode,
                onClick = onStrictToggle,
                label = "Strict Mode",
                leadingIcon = {
                    Icon(if (strictMode) TablerIcons.Check else TablerIcons.Lock, null, Modifier.size(18.dp))
                },
            )

            // Sort Dropdown
            FilterDropdown(
                icon = TablerIcons.ArrowsSort,
                label = "Sort",
                currentValue = if (sortOrder == SortOrder.Favorites) "Popular" else "Recent",
                isDefault = sortOrder == null || sortOrder == SortOrder.Id,
                options = listOf("Recent" to SortOrder.Id, "Popular" to SortOrder.Favorites),
                onSelect = onSortChange,
            )

            // Time
            if (sortOrder == SortOrder.Favorites) {
                FilterDropdown(
                    icon = TablerIcons.Calendar,
                    label = "Time",
                    currentValue = timeFilter?.name ?: "All Time",
                    isDefault = timeFilter == null || timeFilter == TimeFilter.AllTime,
                    options = listOf("All Time" to TimeFilter.AllTime, "Month" to TimeFilter.ThisMonth, "Week" to TimeFilter.ThisWeek),
                    onSelect = onTimeChange,
                )
            }

            // Size
            FilterDropdown(
                icon = TablerIcons.Maximize,
                label = "Size",
                currentValue = dimensionFilter?.name ?: "Any",
                isDefault = dimensionFilter == null,
                options =
                    listOf(
                        "Any" to null,
                        "Large" to DimensionFilter.Large,
                        "Huge" to DimensionFilter.Huge,
                        "Landscape" to DimensionFilter.Landscape,
                        "Portrait" to DimensionFilter.Portrait,
                        "Square" to DimensionFilter.Square,
                    ),
                onSelect = onDimensionChange,
            )

            // Color
            FilterDropdown(
                icon = TablerIcons.Palette,
                label = "Color",
                currentValue = colorFilter?.replaceFirstChar { it.uppercase() } ?: "Any",
                isDefault = colorFilter == null,
                colorPreview = colorFilter,
                options = listOf("Any" to null, "Red" to "red", "Blue" to "blue", "Green" to "green", "Black" to "black"),
                onSelect = onColorChange,
            )
        }
    }
}

@Composable
private fun <T> FilterDropdown(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    currentValue: String,
    isDefault: Boolean,
    options: List<Pair<String, T?>>,
    onSelect: (T?) -> Unit,
    colorPreview: String? = null,
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        ZerochanChip(
            selected = !isDefault,
            onClick = { expanded = true },
            label = if (isDefault) label else currentValue,
            leadingIcon = {
                if (colorPreview != null && !isDefault) {
                    Box(Modifier.size(14.dp).background(parseColor(colorPreview), CircleShape))
                } else {
                    Icon(icon, null, Modifier.size(18.dp))
                }
            },
            trailingIcon = { Icon(TablerIcons.ChevronDown, null, Modifier.size(16.dp)) },
        )

        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { (name, value) ->
                DropdownMenuItem(
                    text = { Text(name) },
                    leadingIcon = { if (currentValue == name) Icon(TablerIcons.Check, null, Modifier.size(18.dp)) },
                    onClick = {
                        onSelect(value)
                        expanded = false
                    },
                )
            }
        }
    }
}

// Helper to turn your string colors into actual Compose colors for the dot
private fun parseColor(color: String): Color =
    when (color) {
        "red" -> Color(0xFFEF5350)
        "blue" -> Color(0xFF42A5F5)
        "green" -> Color(0xFF66BB6A)
        "black" -> Color(0xFF212121)
        else -> Color.Gray
    }

/**
 * A custom chip-like button used for filter toggles and dropdown triggers.
 *
 * @param selected Whether the chip is currently in a "selected" or "active" state.
 * @param onClick Callback triggered when the chip is clicked.
 * @param label The text to display on the chip.
 * @param leadingIcon Optional icon to display before the label.
 * @param trailingIcon Optional icon to display after the label.
 * @param modifier Modifier to be applied to the chip surface.
 */
@Composable
fun ZerochanChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: String,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    // Manually calculate colors to ensure perfect coverage
    val backgroundColor by animateColorAsState(
        when {
            selected -> MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
            isHovered -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f)
            else -> Color.Transparent
        },
    )

    val contentColor by animateColorAsState(
        if (selected) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
    )

    val borderColor by animateColorAsState(
        if (selected) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        },
    )

    Surface(
        onClick = onClick,
        interactionSource = interactionSource,
        shape = MaterialTheme.shapes.medium,
        color = backgroundColor,
        contentColor = contentColor,
        border = BorderStroke(1.dp, borderColor),
        modifier = modifier.height(36.dp),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            leadingIcon?.invoke()
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            )
            trailingIcon?.invoke()
        }
    }
}
