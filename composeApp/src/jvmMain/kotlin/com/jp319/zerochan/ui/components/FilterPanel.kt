package com.jp319.zerochan.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.jp319.zerochan.data.model.DimensionFilter
import com.jp319.zerochan.data.model.SortOrder
import com.jp319.zerochan.data.model.TimeFilter
import compose.icons.TablerIcons
import compose.icons.tablericons.Check
import compose.icons.tablericons.ChevronDown

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class) // 👈 Add Layout API Opt-In
@Composable
fun FilterPanel(
    sortOrder: SortOrder?, onSortChange: (SortOrder?) -> Unit,
    timeFilter: TimeFilter?, onTimeChange: (TimeFilter?) -> Unit,
    dimensionFilter: DimensionFilter?, onDimensionChange: (DimensionFilter?) -> Unit,
    colorFilter: String?, onColorChange: (String?) -> Unit,
    strictMode: Boolean, onStrictToggle: () -> Unit,
    onClearFilters: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Search Filters", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                if (sortOrder != null || timeFilter != null || dimensionFilter != null || colorFilter != null || strictMode) {
                    TextButton(onClick = onClearFilters, contentPadding = PaddingValues(0.dp)) {
                        Text("Clear All", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 👇 Replace Row + Scroll with FlowRow!
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp) // 👈 Adds spacing between wrapped lines
            ) {
                // Strict Mode
                FilterChip(
                    selected = strictMode,
                    onClick = onStrictToggle,
                    label = { Text("Strict Mode") },
                    leadingIcon = if (strictMode) { { Icon(TablerIcons.Check, null, Modifier.size(16.dp)) } } else null
                )

                // Sort
                FilterDropdown(
                    label = "Sort",
                    currentValue = sortOrder?.name ?: "Recent",
                    options = listOf("Recent" to null, "Popular" to SortOrder.Favorites),
                    onSelect = onSortChange
                )

                // Time (Only visible if Popular is selected)
                if (sortOrder == SortOrder.Favorites) {
                    FilterDropdown(
                        label = "Time",
                        currentValue = timeFilter?.name ?: "All Time",
                        options = listOf("All Time" to TimeFilter.AllTime, "This Month" to TimeFilter.ThisMonth, "This Week" to TimeFilter.ThisWeek),
                        onSelect = onTimeChange
                    )
                }

                // Dimensions
                FilterDropdown(
                    label = "Dimensions",
                    currentValue = dimensionFilter?.name ?: "Any",
                    options = listOf(
                        "Any" to null,
                        "Large" to DimensionFilter.Large,
                        "Huge" to DimensionFilter.Huge,
                        "Landscape" to DimensionFilter.Landscape,
                        "Portrait" to DimensionFilter.Portrait,
                        "Square" to DimensionFilter.Square
                    ),
                    onSelect = onDimensionChange
                )

                // Color
                FilterDropdown(
                    label = "Color",
                    currentValue = colorFilter?.replaceFirstChar { it.uppercase() } ?: "Any",
                    options = listOf(
                        "Any" to null,
                        "Red" to "red",
                        "Orange" to "orange",
                        "Yellow" to "yellow",
                        "Green" to "green",
                        "Blue" to "blue",
                        "Purple" to "purple",
                        "Pink" to "pink",
                        "Black" to "black",
                        "White" to "white",
                        "Gray" to "gray",
                        "Brown" to "brown"
                    ),
                    onSelect = onColorChange
                )
            }
        }
    }
}

// Helper component to make dropdowns clean and reusable
@Composable
private fun <T> FilterDropdown(
    label: String,
    currentValue: String,
    options: List<Pair<String, T?>>,
    onSelect: (T?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        InputChip(
            selected = currentValue != "Any" && currentValue != "Recent" && currentValue != "All Time",
            onClick = { expanded = true },
            label = { Text("$label: $currentValue") },
            trailingIcon = { Icon(TablerIcons.ChevronDown, null, Modifier.size(16.dp)) }
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { (name, value) ->
                DropdownMenuItem(
                    text = { Text(name) },
                    onClick = { onSelect(value); expanded = false }
                )
            }
        }
    }
}