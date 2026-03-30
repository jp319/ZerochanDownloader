package com.jp319.zerochan.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.isPrimaryPressed
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.jp319.zerochan.ui.components.*
import compose.icons.TablerIcons
import compose.icons.tablericons.AlertTriangle
import compose.icons.tablericons.Flag
import compose.icons.tablericons.Search

@OptIn(ExperimentalFoundationApi::class, ExperimentalComposeUiApi::class)
@Composable
fun GalleryScreen(
    viewModel: GalleryViewModel,
    modifier: Modifier = Modifier,
) {
    val query by viewModel.query.collectAsState()
    val images by viewModel.images.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val isUsernameMissing by viewModel.isUsernameMissing.collectAsState()
    val selectedItem by viewModel.selectedItem.collectAsState()
    val selectedIds by viewModel.selectedIdsForDownload.collectAsState()
    val verifiedUrl by viewModel.verifiedFullResUrl.collectAsState()
    val downloadQueue by viewModel.downloadQueue.collectAsState()

    val itemDetails by viewModel.selectedItemDetails.collectAsState()
    val isLoadingDetails by viewModel.isLoadingDetails.collectAsState()

    val showDownloadsModal by viewModel.showDownloadsModal.collectAsState()
    val localFiles by viewModel.localFiles.collectAsState()

    val gridState = rememberLazyStaggeredGridState()
    val isSelectionModeActive by viewModel.isSelectionModeActive.collectAsState()
    val viewingLocalFile by viewModel.viewingLocalFile.collectAsState()
    val isEndOfPaginationReached by viewModel.isEndOfPaginationReached.collectAsState()

    val suggestions by viewModel.suggestions.collectAsState()

    val isFilterPanelVisible by viewModel.isFilterPanelVisible.collectAsState()
    val sortOrder by viewModel.sortOrder.collectAsState()
    val timeFilter by viewModel.timeFilter.collectAsState()
    val dimensionFilter by viewModel.dimensionFilter.collectAsState()
    val strictMode by viewModel.strictMode.collectAsState()
    val colorFilter by viewModel.colorFilter.collectAsState()

    LocalImageModal(
        file = viewingLocalFile,
        onDismiss = viewModel::closeLocalFile,
    )

    // Render the Details Dialog when active
    if (itemDetails != null || isLoadingDetails) {
        ItemDetailsDialog(
            details = itemDetails,
            isLoading = isLoadingDetails,
            onDismiss = viewModel::onDismissDetails,
            onDownload = {
                selectedItem?.let { item ->
                    viewModel.downloadSingleItem(item, itemDetails?.full)
                }
            },
            onSearchTag = { tag ->
                viewModel.onDismissDetails()
                viewModel.onDismissModal()
                viewModel.onQueryChange(tag)
                viewModel.onSearch(tag)
            }
        )
    }

    if (showDownloadsModal) {
        DownloadsLibraryDialog(
            currentPath = viewModel.currentDownloadDirectory,
            localFiles = localFiles,
            onChangePath = viewModel::setDownloadDirectory,
            onDismiss = { viewModel.toggleDownloadsModal(false) },
            onImageClick = viewModel::openLocalFile,
        )
    }

    ImageModal(
        item = selectedItem,
        verifiedUrl = verifiedUrl,
        onDismiss = viewModel::onDismissModal,
        onViewDetails = { id -> viewModel.fetchItemDetails(id) },
        onDownload = {
            selectedItem?.let { item -> viewModel.downloadSingleItem(item, verifiedUrl) }
        },
        fetchGifFile = viewModel::fetchRemoteGif,
    )

    val shouldLoadMore by remember {
        derivedStateOf {
            val lastVisible = gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            val total = gridState.layoutInfo.totalItemsCount
            total > 0 && lastVisible >= total - 6
        }
    }
    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) viewModel.onLoadMore()
    }

    Column(modifier = modifier.fillMaxSize()) {
        Box(
            contentAlignment = Alignment.Center,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
        ) {
            Column(modifier = Modifier.widthIn(max = 600.dp)) {
                // 1. The Search Bar
                SearchBar(
                    query = query,
                    onQueryChange = viewModel::onQueryChange,
                    onSearch = viewModel::onSearch,
                    suggestions = suggestions,
                    onFocusChanged = viewModel::onSearchFocusChanged,
                    onToggleFilters = viewModel::toggleFilterPanel,
                    isFilterPanelVisible = isFilterPanelVisible,
                    isLoading = isLoading,
                    modifier = Modifier.fillMaxWidth(),
                )

                // 2. The Animated Filter Panel!
                AnimatedVisibility(
                    visible = isFilterPanelVisible,
                    enter = expandVertically(),
                    exit = shrinkVertically(),
                ) {
                    Column {
                        Spacer(modifier = Modifier.height(8.dp))
                        FilterPanel(
                            sortOrder = sortOrder, onSortChange = viewModel::setSortOrder,
                            timeFilter = timeFilter, onTimeChange = viewModel::setTimeFilter,
                            dimensionFilter = dimensionFilter, onDimensionChange = viewModel::setDimensionFilter,
                            strictMode = strictMode, onStrictToggle = viewModel::toggleStrictMode,
                            onClearFilters = viewModel::clearFilters,
                            colorFilter = colorFilter, onColorChange = viewModel::setColorFilter,
                        )
                    }
                }
            }
        }

        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .weight(1f),
        ) {
            when {
                isUsernameMissing -> {
                    StateMessage(
                        icon = TablerIcons.AlertTriangle,
                        title = "Username Required",
                        description = "Please set your Zerochan username in settings to use the API.",
                        color = MaterialTheme.colorScheme.error,
                    )
                }
                error != null -> {
                    StateMessage(
                        icon = TablerIcons.AlertTriangle,
                        title = "Something went wrong",
                        description = error ?: "Unknown error occurred",
                        color = MaterialTheme.colorScheme.error,
                    )
                }
                images.isEmpty() && !isLoading -> {
                    if (query.isEmpty()) {
                        StateMessage(
                            icon = TablerIcons.Search,
                            title = "Zerochan Explorer",
                            description = "Search for tags like 'One Piece' to begin.",
                        )
                    } else {
                        StateMessage(
                            icon = TablerIcons.Search,
                            title = "No results found",
                            description = "We couldn't find any images for \"$query\".",
                        )
                    }
                }
                else -> {
                    LazyVerticalStaggeredGrid(
                        columns = StaggeredGridCells.Adaptive(minSize = 200.dp),
                        state = gridState,
                        contentPadding = PaddingValues(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalItemSpacing = 12.dp,
                        modifier = Modifier
                            .fillMaxSize()
                            .onPointerEvent(PointerEventType.Move, PointerEventPass.Initial) { event ->
                                val isModeActive = isSelectionModeActive || selectedIds.isNotEmpty()
                                if (isModeActive && event.buttons.isPrimaryPressed) {
                                    val position = event.changes.first().position
                                    val visibleItems = gridState.layoutInfo.visibleItemsInfo
                                    for (itemInfo in visibleItems) {
                                        val offset = itemInfo.offset
                                        val size = itemInfo.size
                                        if (position.x >= offset.x && position.x <= offset.x + size.width &&
                                            position.y >= offset.y && position.y <= offset.y + size.height) {
                                            val id = itemInfo.key as? Int
                                            if (id != null) {
                                                viewModel.selectItem(id)
                                            }
                                        }
                                    }
                                }
                            },
                    ) {
                        items(
                            items = images,
                            key = { item -> item.id },
                        ) { item ->
                            val isSelected = selectedIds.contains(item.id)

                            // Combine selection states
                            val isModeActive = isSelectionModeActive || selectedIds.isNotEmpty()

                            ImageCard(
                                item = item,
                                isSelected = isSelected,
                                isSelectionModeActive = isModeActive,
                                onClick = {
                                    if (isModeActive) {
                                        viewModel.toggleSelection(item.id)
                                    } else {
                                        viewModel.onImageClick(item)
                                    }
                                },
                                onLongClick = { viewModel.toggleSelection(item.id) },
                                onDragSelect = { viewModel.selectItem(item.id) }
                            )
                        }

                        if (isLoading && !isEndOfPaginationReached) {
                            items(10) {
                                ShimmerItem()
                            }
                        }

                        // Append end message spanning entire grid line
                        if (isEndOfPaginationReached && images.isNotEmpty()) {
                            item(span = StaggeredGridItemSpan.FullLine) {
                                EndOfPaginationMessage()
                            }
                        }
                    }
                }
            }

            DownloadQueueOverlay(
                downloadQueue = downloadQueue,
                onClearCompleted = viewModel::clearCompletedDownloads,
                modifier = Modifier.align(Alignment.BottomEnd),
            )
        }
    }
}

@Composable
fun StateMessage(
    icon: ImageVector,
    title: String,
    description: String,
    color: Color = MaterialTheme.colorScheme.primary,
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = color.copy(alpha = 0.6f),
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = description,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun DownloadQueueOverlay(
    downloadQueue: List<DownloadJob>,
    onClearCompleted: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Because this is isolated, the compiler safely uses the standard top-level AnimatedVisibility!
    AnimatedVisibility(
        visible = downloadQueue.isNotEmpty(),
        enter = slideInVertically(initialOffsetY = { it }),
        exit = slideOutVertically(targetOffsetY = { it }),
        modifier = modifier,
    ) {
        DownloadQueuePanel(
            queue = downloadQueue,
            onClearCompleted = onClearCompleted,
        )
    }
}

@Composable
fun EndOfPaginationMessage() {
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(vertical = 32.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = TablerIcons.Flag,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "You've reached the end!",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}
