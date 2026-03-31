package com.jp319.zerochan.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.isCtrlPressed
import androidx.compose.ui.input.pointer.isPrimaryPressed
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.jp319.zerochan.ui.components.*
import compose.icons.TablerIcons
import compose.icons.tablericons.AlertTriangle
import compose.icons.tablericons.Flag
import compose.icons.tablericons.Plus
import compose.icons.tablericons.Search

@OptIn(ExperimentalFoundationApi::class, ExperimentalComposeUiApi::class)
@Composable
fun GalleryScreen(
    viewModel: GalleryViewModel,
    zoomLevel: Float,
    onZoomChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    // --- State Management ---
    val query by viewModel.query.collectAsState()
    val images by viewModel.images.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()
    val error by viewModel.error.collectAsState()
    val isUsernameMissing by viewModel.isUsernameMissing.collectAsState()
    val selectedItem by viewModel.selectedItem.collectAsState()
    val selectedIds by viewModel.selectedIdsForDownload.collectAsState()
    val verifiedUrl by viewModel.verifiedFullResUrl.collectAsState()
    val downloadQueue by viewModel.downloadQueue.collectAsState()
    val focusManager = LocalFocusManager.current

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

    // --- Scroll Hide/Show Logic (ENH 5) ---
    var lastScrollOffset by remember { mutableStateOf(0) }
    var lastScrollIndex by remember { mutableStateOf(0) }
    var isSearchBarVisible by remember { mutableStateOf(true) }

    LaunchedEffect(gridState) {
        snapshotFlow { gridState.firstVisibleItemIndex to gridState.firstVisibleItemScrollOffset }
            .collect { (index, offset) ->
                val scrolledDown = index > lastScrollIndex || (index == lastScrollIndex && offset > lastScrollOffset)
                // Always visible at the top, or when scrolling up
                isSearchBarVisible = !scrolledDown || (index == 0 && offset < 10)
                
                if (scrolledDown && isSearchBarVisible) {
                    // Force collapse when bar hides
                    viewModel.hideFilterPanel()
                    focusManager.clearFocus()
                }
                
                lastScrollIndex = index
                lastScrollOffset = offset
            }
    }

    // --- Modals & Overlays ---
    LocalImageModal(
        file = viewingLocalFile,
        onDismiss = viewModel::closeLocalFile,
    )

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
            },
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

    // --- Pagination Trigger (BUG 3 Fix) ---
    var lastTriggeredTotal by remember { mutableStateOf(0) }
    val shouldLoadMore by remember {
        derivedStateOf {
            val lastVisible = gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            val total = gridState.layoutInfo.totalItemsCount
            total > 0 && lastVisible >= total - 12 // Trigger slightly before the end
        }
    }

    LaunchedEffect(shouldLoadMore, images.size) {
        val currentTotal = images.size
        if (shouldLoadMore && !isLoading && currentTotal != lastTriggeredTotal) {
            lastTriggeredTotal = currentTotal
            viewModel.onLoadMore()
        }
    }

    // --- Main Layered UI Root ---
    Box(
        modifier =
            modifier
                .fillMaxSize(),
    ) {
        // LAYER 1: THE GRID (Bottom Layer)
        val gridPaddingTop by animateDpAsState(
            targetValue = if (isSearchBarVisible) 80.dp else 0.dp,
            animationSpec = spring(stiffness = 300f)
        )

        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(top = gridPaddingTop)
                    .onPointerEvent(PointerEventType.Press) {
                        // Global click outside (ENH 5 & 7j)
                        focusManager.clearFocus()
                        viewModel.hideFilterPanel()
                    }
                    .onPointerEvent(PointerEventType.Release) {
                        // Reset dragging state globally if needed
                        viewModel.endDragSelection()
                    }
                    .onPointerEvent(PointerEventType.Scroll) { event ->
                        // Ctrl+Scroll Zoom (ENH 2)
                        // In PointerEvent, keyboardModifiers is of type PointerKeyboardModifiers
                        if (event.keyboardModifiers.isCtrlPressed) {
                            val delta = event.changes.firstOrNull()?.scrollDelta?.y ?: 0f
                            onZoomChange((zoomLevel - delta * 0.1f).coerceIn(0.5f, 10f))
                            event.changes.forEach { it.consume() }
                        }
                    },
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
                        title = "Search Error",
                        description = error ?: "Unknown error occurred",
                        color = MaterialTheme.colorScheme.error,
                    )
                }
                images.isEmpty() && !isLoading -> {
                    StateMessage(
                        icon = TablerIcons.Search,
                        title = if (query.isEmpty()) "Zerochan Downloader" else "No results found",
                        description =
                            if (query.isEmpty()) {
                                "Search for anime tags to begin."
                            } else {
                                "We couldn't find anything for \"$query\"."
                            },
                    )
                }
                else -> {
                    LazyVerticalStaggeredGrid(
                        columns = StaggeredGridCells.Adaptive(minSize = (220 * zoomLevel).dp),
                        state = gridState,
                        contentPadding = PaddingValues(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalItemSpacing = 12.dp,
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .onPointerEvent(PointerEventType.Move, PointerEventPass.Initial) { event ->
                                    // Drag-to-select logic (Enhanced with toggle)
                                    val isModeActive = isSelectionModeActive || selectedIds.isNotEmpty()
                                    if (isModeActive && event.buttons.isPrimaryPressed) {
                                        val position = event.changes.first().position
                                        gridState.layoutInfo.visibleItemsInfo.forEach { item ->
                                            if (position.x in item.offset.x.toFloat()..(item.offset.x + item.size.width).toFloat() &&
                                                position.y in item.offset.y.toFloat()..(item.offset.y + item.size.height).toFloat()
                                            ) {
                                                (item.key as? Int)?.let { id ->
                                                    viewModel.updateDragSelection(id)
                                                }
                                            }
                                        }
                                    }
                                },
                    ) {
                        items(items = images, key = { it.id }) { item ->
                            val isModeActive = isSelectionModeActive || selectedIds.isNotEmpty()
                            ImageCard(
                                item = item,
                                isSelected = selectedIds.contains(item.id),
                                isSelectionModeActive = isModeActive,
                                onClick = {
                                    if (isModeActive) {
                                        viewModel.toggleSelection(item.id)
                                    } else {
                                        viewModel.onImageClick(item)
                                    }
                                },
                                onLongClick = { viewModel.toggleSelection(item.id) },
                                onDragStart = { viewModel.startDragSelection(item.id) },
                            )
                        }

                        // Always Visible Load More Fallback (BUG 3 Fix)
                        if (!isEndOfPaginationReached && images.isNotEmpty()) {
                            item(span = StaggeredGridItemSpan.FullLine) {
                                Box(Modifier.fillMaxWidth().padding(16.dp), Alignment.Center) {
                                    OutlinedButton(
                                        onClick = viewModel::onLoadMore,
                                        enabled = !isLoading,
                                    ) {
                                        if (isLoading && !isSearching) {
                                            CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                                        } else {
                                            Icon(TablerIcons.Plus, null, Modifier.size(18.dp))
                                        }
                                        Spacer(Modifier.width(8.dp))
                                        Text(if (isLoading && !isSearching) "Loading..." else "Load More")
                                    }
                                }
                            }
                        }

                        // Shimmers for new searches
                        if (isLoading && images.isEmpty()) {
                            items(15) { ShimmerItem() }
                        }

                        if (isEndOfPaginationReached && images.isNotEmpty()) {
                            item(span = StaggeredGridItemSpan.FullLine) { EndOfPaginationMessage() }
                        }
                    }
                }
            }
        }

        // LAYER 2: THE FLOATING HEADER (Top Layer) (ENH 5 & 7j)
        AnimatedVisibility(
            visible = isSearchBarVisible,
            enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
            modifier = Modifier.zIndex(10f)
        ) {
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                contentAlignment = Alignment.TopCenter,
            ) {
                SearchBar(
                    query = query,
                    onQueryChange = viewModel::onQueryChange,
                    onSearch = {
                        focusManager.clearFocus()
                        viewModel.onSearch(it)
                    },
                    suggestions = suggestions,
                    onFocusChanged = viewModel::onSearchFocusChanged,
                    isLoading = isSearching,
                    isFilterPanelVisible = isFilterPanelVisible,
                    onToggleFilters = {
                        focusManager.clearFocus()
                        viewModel.toggleFilterPanel()
                    },
                    filterContent = {
                        FilterPanel(
                            sortOrder = sortOrder, onSortChange = viewModel::setSortOrder,
                            timeFilter = timeFilter, onTimeChange = viewModel::setTimeFilter,
                            dimensionFilter = dimensionFilter, onDimensionChange = viewModel::setDimensionFilter,
                            strictMode = strictMode, onStrictToggle = viewModel::toggleStrictMode,
                            onClearFilters = viewModel::clearFilters,
                            colorFilter = colorFilter, onColorChange = viewModel::setColorFilter,
                        )
                    },
                    modifier = Modifier.widthIn(max = 700.dp),
                )
            }
        }

        // LAYER 3: OVERLAYS
        DownloadQueueOverlay(
            downloadQueue = downloadQueue,
            onClearCompleted = viewModel::clearCompletedDownloads,
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
        )
    }
}

// --- Helper UI Components ---

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
        Icon(icon, null, Modifier.size(64.dp), tint = color.copy(alpha = 0.5f))
        Spacer(Modifier.height(16.dp))
        Text(title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text(description, style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun DownloadQueueOverlay(
    downloadQueue: List<DownloadJob>,
    onClearCompleted: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AnimatedVisibility(
        visible = downloadQueue.isNotEmpty(),
        enter = slideInVertically(initialOffsetY = { it }),
        exit = slideOutVertically(targetOffsetY = { it }),
        modifier = modifier,
    ) {
        DownloadQueuePanel(queue = downloadQueue, onClearCompleted = onClearCompleted)
    }
}

@Composable
fun EndOfPaginationMessage() {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(TablerIcons.Flag, null, Modifier.size(24.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
        Spacer(Modifier.height(8.dp))
        Text("You've reached the end of the results!", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
