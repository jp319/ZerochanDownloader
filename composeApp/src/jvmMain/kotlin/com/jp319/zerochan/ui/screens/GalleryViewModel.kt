package com.jp319.zerochan.ui.screens

import com.jp319.zerochan.data.model.*
import com.jp319.zerochan.data.repository.NoUsernameException
import com.jp319.zerochan.data.repository.ZerochanRepository
import com.jp319.zerochan.utils.FileUtil
import com.jp319.zerochan.utils.Logger
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.io.File
import kotlin.time.Duration.Companion.milliseconds

enum class DownloadState { PREPARING, DOWNLOADING, SUCCESS, ERROR }

data class DownloadJob(
    val item: ZerochanItem,
    val resolvedUrl: String? = null,
    val state: DownloadState = DownloadState.PREPARING,
)

/**
 * ViewModel managing the primary state, logic, and user interactions for the Gallery.
 * Acts as the bridge between the Zerochan UI and the data repository.
 */
class GalleryViewModel(private val repository: ZerochanRepository) {
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val logTAG = "GalleryViewModel"

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _images = MutableStateFlow<List<ZerochanItem>>(emptyList())
    val images: StateFlow<List<ZerochanItem>> = _images.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _isUsernameMissing = MutableStateFlow(false)
    val isUsernameMissing: StateFlow<Boolean> = _isUsernameMissing.asStateFlow()

    private val _selectedItem = MutableStateFlow<ZerochanItem?>(null)
    val selectedItem: StateFlow<ZerochanItem?> = _selectedItem.asStateFlow()

    private val _verifiedFullResUrl = MutableStateFlow<String?>(null)
    val verifiedFullResUrl: StateFlow<String?> = _verifiedFullResUrl.asStateFlow()

    private var currentPage = 1

    private val _selectedItemDetails = MutableStateFlow<ZerochanFullItem?>(null)
    val selectedItemDetails: StateFlow<ZerochanFullItem?> = _selectedItemDetails.asStateFlow()

    private val _isLoadingDetails = MutableStateFlow(false)
    val isLoadingDetails: StateFlow<Boolean> = _isLoadingDetails.asStateFlow()

    private val _selectedIdsForDownload = MutableStateFlow<Set<Int>>(emptySet())
    val selectedIdsForDownload: StateFlow<Set<Int>> = _selectedIdsForDownload.asStateFlow()

    private val _downloadQueue = MutableStateFlow<List<DownloadJob>>(emptyList())
    val downloadQueue: StateFlow<List<DownloadJob>> = _downloadQueue.asStateFlow()

    private val _showDownloadsModal = MutableStateFlow(false)
    val showDownloadsModal = _showDownloadsModal.asStateFlow()

    private val _localFiles = MutableStateFlow<List<File>>(emptyList())
    val localFiles = _localFiles.asStateFlow()

    private val _isEndOfPaginationReached = MutableStateFlow(false)
    val isEndOfPaginationReached: StateFlow<Boolean> = _isEndOfPaginationReached.asStateFlow()

    val currentDownloadDirectory: String
        get() = repository.profileManager.downloadDirectory

    private val _isSelectionModeActive = MutableStateFlow(false)
    val isSelectionModeActive: StateFlow<Boolean> = _isSelectionModeActive.asStateFlow()

    private val _isFilterPanelVisible = MutableStateFlow(false)
    val isFilterPanelVisible = _isFilterPanelVisible.asStateFlow()

    private val _sortOrder = MutableStateFlow<SortOrder?>(null)
    val sortOrder = _sortOrder.asStateFlow()

    private val _timeFilter = MutableStateFlow<TimeFilter?>(null)
    val timeFilter = _timeFilter.asStateFlow()

    private val _dimensionFilter = MutableStateFlow<DimensionFilter?>(null)
    val dimensionFilter = _dimensionFilter.asStateFlow()

    private val _colorFilter = MutableStateFlow<String?>(null)
    val colorFilter = _colorFilter.asStateFlow()

    private val _strictMode = MutableStateFlow(false)
    val strictMode = _strictMode.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    private val _viewingLocalFile = MutableStateFlow<File?>(null)
    val viewingLocalFile = _viewingLocalFile.asStateFlow()

    private val _suggestions = MutableStateFlow<List<ZerochanSuggestion>>(emptyList())
    val suggestions: StateFlow<List<ZerochanSuggestion>> = _suggestions.asStateFlow()

    private var searchJob: Job? = null

    // --- Drag-to-select tracking ---
    private var isDragging = false
    private var dragInitialStateIsSelect = true // true = selecting, false = deselecting
    private val draggedIdsInCurrentSession = mutableSetOf<Int>()

    // -------------------------------------------------------------------------
    // Filter Panel
    // -------------------------------------------------------------------------

    fun hideFilterPanel() {
        _isFilterPanelVisible.value = false
    }

    fun toggleFilterPanel() {
        _isFilterPanelVisible.value = !isFilterPanelVisible.value
    }

    fun setSortOrder(sort: SortOrder?) {
        _sortOrder.value = sort
        onSearch(_query.value)
    }

    fun setTimeFilter(time: TimeFilter?) {
        _timeFilter.value = time
        onSearch(_query.value)
    }

    fun setDimensionFilter(dim: DimensionFilter?) {
        _dimensionFilter.value = dim
        onSearch(_query.value)
    }

    fun setColorFilter(color: String?) {
        _colorFilter.value = color
        onSearch(_query.value)
    }

    fun toggleStrictMode() {
        _strictMode.update { !it }
        onSearch(_query.value)
    }

    fun clearFilters() {
        _sortOrder.value = null
        _timeFilter.value = null
        _dimensionFilter.value = null
        _colorFilter.value = null
        _strictMode.value = false
        onSearch(_query.value)
    }

    private fun getCurrentApiParams(page: Int): ZerochanApiParams {
        return ZerochanApiParams(
            tag = _query.value,
            page = page,
            limit = 30,
            sort = _sortOrder.value,
            time = _timeFilter.value,
            dimensions = _dimensionFilter.value,
            color = _colorFilter.value,
            strict = if (_strictMode.value) true else null,
        )
    }

    // -------------------------------------------------------------------------
    // Selection Mode
    // -------------------------------------------------------------------------

    fun toggleSelectionMode() {
        _isSelectionModeActive.update { !it }
        if (!_isSelectionModeActive.value) {
            clearSelection()
        }
    }

    fun toggleSelection(id: Int) {
        _selectedIdsForDownload.update { current ->
            if (current.contains(id)) current - id else current + id
        }
    }

    fun startDragSelection(id: Int) {
        isDragging = true
        draggedIdsInCurrentSession.clear()
        dragInitialStateIsSelect = !_selectedIdsForDownload.value.contains(id)
        updateDragSelection(id)
    }

    fun updateDragSelection(id: Int) {
        if (!isDragging || draggedIdsInCurrentSession.contains(id)) return
        draggedIdsInCurrentSession.add(id)
        _selectedIdsForDownload.update { current ->
            if (dragInitialStateIsSelect) current + id else current - id
        }
    }

    fun endDragSelection() {
        isDragging = false
        draggedIdsInCurrentSession.clear()
    }

    fun clearSelection() {
        _selectedIdsForDownload.update { emptySet() }
    }

    // -------------------------------------------------------------------------
    // Local Files / Library
    // -------------------------------------------------------------------------

    fun openLocalFile(file: File) {
        _viewingLocalFile.value = file
    }

    fun closeLocalFile() {
        _viewingLocalFile.value = null
    }

    fun loadLocalLibrary() {
        _localFiles.value = FileUtil.getImagesFromDirectory(currentDownloadDirectory)
    }

    fun setDownloadDirectory(path: String) {
        repository.profileManager.downloadDirectory = path
        loadLocalLibrary()
    }

    fun toggleDownloadsModal(show: Boolean) {
        if (show) loadLocalLibrary()
        _showDownloadsModal.value = show
    }

    // -------------------------------------------------------------------------
    // Item Details
    // -------------------------------------------------------------------------

    fun fetchItemDetails(id: Int) {
        scope.launch {
            _isLoadingDetails.value = true
            val details = repository.getItem(id)
            _selectedItemDetails.value = details
            _isLoadingDetails.value = false
        }
    }

    fun onDismissDetails() {
        _selectedItemDetails.value = null
    }

    // -------------------------------------------------------------------------
    // Image Modal
    // -------------------------------------------------------------------------

    fun onImageClick(item: ZerochanItem) {
        _selectedItem.update { item }
        _verifiedFullResUrl.update { null }

        scope.launch {
            val validUrl = repository.findValidFullResUrl(item.id, item.tag)
            _verifiedFullResUrl.update { validUrl ?: item.thumbnail.replace(".avif", ".jpg") }
        }
    }

    fun onDismissModal() {
        _selectedItem.update { null }
        _verifiedFullResUrl.update { null }
        _selectedItemDetails.value = null
    }

    // -------------------------------------------------------------------------
    // Search
    // -------------------------------------------------------------------------

    fun loadSearchHistory() {
        val history =
            repository.profileManager.searchHistory.map {
                ZerochanSuggestion(value = it, type = "Recent Search")
            }
        _suggestions.update { history }
    }

    fun onSearch(query: String) {
        _suggestions.update { emptyList() }
        if (query.isBlank()) return

        clearSelection()
        _isSelectionModeActive.value = false

        val currentHistory = repository.profileManager.searchHistory.toMutableList()
        currentHistory.remove(query)
        currentHistory.add(0, query)
        repository.profileManager.searchHistory = currentHistory

        Logger.debug(logTAG, "Searching for: $query")
        currentPage = 1

        scope.launch {
            _isSearching.update { true }
            _isLoading.update { true }
            _error.update { null }
            _isUsernameMissing.update { false }

            runCatching {
                repository.search(getCurrentApiParams(currentPage))
            }.onSuccess { items ->
                _images.update { items.distinctBy { it.id } }
                if (items.isEmpty()) {
                    _isEndOfPaginationReached.update { true }
                }
            }.onFailure { e ->
                if (e is NoUsernameException) {
                    _isUsernameMissing.update { true }
                } else {
                    Logger.error(logTAG, "Search entirely failed", e)
                    _error.update { e.message }
                }
            }
            _isSearching.update { false }
            _isLoading.update { false }
        }
    }

    fun onLoadMore() {
        val currentQuery = _query.value
        if (currentQuery.isBlank() || _isLoading.value || _isEndOfPaginationReached.value) return

        currentPage++
        scope.launch {
            _isLoading.update { true }
            _isUsernameMissing.update { false }

            runCatching {
                repository.search(getCurrentApiParams(currentPage))
            }.onSuccess { items ->
                if (items.isEmpty()) {
                    _isEndOfPaginationReached.update { true }
                } else {
                    _images.update { current ->
                        (current + items).distinctBy { it.id }
                    }
                }
            }.onFailure { e ->
                currentPage-- // roll back on failure
                if (e is NoUsernameException) {
                    _isUsernameMissing.update { true }
                } else {
                    Logger.error(logTAG, "Pagination failed", e)
                    _error.update { e.message }
                }
            }
            _isLoading.update { false }
        }
    }

    fun onQueryChange(value: String) {
        _query.update { value }
        searchJob?.cancel()

        if (value.isBlank()) {
            loadSearchHistory()
            return
        }

        searchJob =
            scope.launch {
                delay(300.milliseconds)
                val results = repository.getSuggestions(value)
                _suggestions.update { results }
            }
    }

    fun onSearchFocusChanged(isFocused: Boolean) {
        if (isFocused && _query.value.isBlank()) {
            loadSearchHistory()
        } else if (!isFocused) {
            // Delay clear to allow click events on suggestions to fire first
            scope.launch {
                delay(200)
                _suggestions.update { emptyList() }
            }
        }
    }

    // -------------------------------------------------------------------------
    // Downloads
    // -------------------------------------------------------------------------

    fun downloadSingleItem(
        item: ZerochanItem,
        knownUrl: String? = null,
    ) {
        val job = DownloadJob(item, knownUrl)
        _downloadQueue.update { current -> current + job }

        scope.launch {
            val sanitizedUrl =
                knownUrl?.let { url ->
                    when {
                        url.isBlank() -> null
                        url.startsWith("//") -> "https:$url"
                        url.startsWith("/") -> null
                        else -> url
                    }
                }

            val validUrl = sanitizedUrl ?: repository.findValidFullResUrl(item.id, item.tag) ?: item.thumbnail.replace(".avif", ".jpg")
            updateJobState(item.id, validUrl, DownloadState.DOWNLOADING)

            val cleanUrl = validUrl.substringBefore("?")
            val ext =
                if (cleanUrl.substringAfterLast("/", "").contains(".")) {
                    cleanUrl.substringAfterLast(".", "jpg")
                } else {
                    "jpg"
                }

            val safeTag = item.tag.replace("[^a-zA-Z0-9.-]".toRegex(), "_")
            val fileName = "Zerochan_${safeTag}_${item.id}.$ext"

            val file = repository.downloadImageToDisk(validUrl, fileName, currentDownloadDirectory)
            if (file != null && _showDownloadsModal.value) loadLocalLibrary()

            updateJobState(item.id, validUrl, if (file != null) DownloadState.SUCCESS else DownloadState.ERROR)
        }
    }

    fun downloadSelectedItems() {
        val selectedIds = _selectedIdsForDownload.value
        if (selectedIds.isEmpty()) return

        val itemsToDownload = _images.value.filter { it.id in selectedIds }
        clearSelection()

        val newJobs = itemsToDownload.map { DownloadJob(it) }
        _downloadQueue.update { current -> current + newJobs }

        scope.launch {
            newJobs.forEach { downloadJob ->
                val validUrl = repository.findValidFullResUrl(downloadJob.item.id, downloadJob.item.tag)
                val downloadUrl = validUrl ?: downloadJob.item.thumbnail.replace(".avif", ".jpg")

                updateJobState(downloadJob.item.id, downloadUrl, DownloadState.DOWNLOADING)

                val ext = downloadUrl.substringAfterLast(".", "jpg")
                val safeTag = downloadJob.item.tag.replace("[^a-zA-Z0-9.-]".toRegex(), "_")
                val fileName = "Zerochan_${safeTag}_${downloadJob.item.id}.$ext"

                val file = repository.downloadImageToDisk(downloadUrl, fileName, currentDownloadDirectory)

                if (file != null && _showDownloadsModal.value) {
                    loadLocalLibrary()
                }

                updateJobState(
                    id = downloadJob.item.id,
                    url = downloadUrl,
                    newState = if (file != null) DownloadState.SUCCESS else DownloadState.ERROR,
                )
            }
        }
    }

    private fun updateJobState(
        id: Int,
        url: String,
        newState: DownloadState,
    ) {
        _downloadQueue.update { queue ->
            queue.map { entry ->
                if (entry.item.id == id) entry.copy(resolvedUrl = url, state = newState) else entry
            }
        }
    }

    fun clearCompletedDownloads() {
        _downloadQueue.update { queue ->
            queue.filter { entry -> entry.state == DownloadState.PREPARING || entry.state == DownloadState.DOWNLOADING }
        }
    }

    /**
     * Fetches a GIF file directly via the UI thread if requested, bypassing the download queue
     * since GIFs are only temporarily viewed.
     */
    suspend fun fetchRemoteGif(
        id: Int,
        url: String,
    ): File? {
        return repository.getRemoteGifFile(id, url)
    }
}
