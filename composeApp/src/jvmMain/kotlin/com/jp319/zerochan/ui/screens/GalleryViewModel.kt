package com.jp319.zerochan.ui.screens

import com.jp319.zerochan.data.model.*
import com.jp319.zerochan.data.repository.NoUsernameException
import com.jp319.zerochan.data.repository.ZerochanRepository
import com.jp319.zerochan.utils.FileUtil
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
    val state: DownloadState = DownloadState.PREPARING
)

class GalleryViewModel(private val repository: ZerochanRepository) {

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

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

    // 👇 1. New State for the verified high-res URL
    private val _verifiedFullResUrl = MutableStateFlow<String?>(null)
    val verifiedFullResUrl: StateFlow<String?> = _verifiedFullResUrl.asStateFlow()

    private var currentPage = 1

    // 👇 1. Add state to hold the fetched details and a loading flag
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
        get() = repository.profileManager.downloadDirectory // Assuming repo holds profileManager

    private val _isSelectionModeActive = MutableStateFlow(false)
    val isSelectionModeActive = _isSelectionModeActive.asStateFlow()

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

    fun toggleFilterPanel() { _isFilterPanelVisible.update { !it } }

    fun setSortOrder(sort: SortOrder?) { _sortOrder.value = sort; onSearch(_query.value) }
    fun setTimeFilter(time: TimeFilter?) { _timeFilter.value = time; onSearch(_query.value) }
    fun setDimensionFilter(dim: DimensionFilter?) { _dimensionFilter.value = dim; onSearch(_query.value) }
    fun setColorFilter(color: String?) { _colorFilter.value = color; onSearch(_query.value) }
    fun toggleStrictMode() { _strictMode.update { !it }; onSearch(_query.value) }

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
            strict = if (_strictMode.value) true else null
        )
    }

    fun toggleSelectionMode() {
        _isSelectionModeActive.update { !it }
        if (!_isSelectionModeActive.value) {
            clearSelection() // Clear selections when turning the mode off
        }
    }

    private val _viewingLocalFile = MutableStateFlow<File?>(null)
    val viewingLocalFile = _viewingLocalFile.asStateFlow()

    private val _suggestions = MutableStateFlow<List<ZerochanSuggestion>>(emptyList())
    val suggestions: StateFlow<List<ZerochanSuggestion>> = _suggestions.asStateFlow()

    private var searchJob: Job? = null

    fun openLocalFile(file: File) { _viewingLocalFile.value = file }
    fun closeLocalFile() { _viewingLocalFile.value = null }

    // 👇 3. Single Image Download Function
    fun downloadSingleItem(item: ZerochanItem, knownUrl: String? = null) {
        val job = DownloadJob(item, knownUrl)
        _downloadQueue.update { current -> current + job }

        scope.launch {
            // 👇 1. Sanitize the knownUrl to prevent Ktor localhost crashes
            val sanitizedUrl = knownUrl?.let { url ->
                when {
                    url.isBlank() -> null
                    url.startsWith("//") -> "https:$url"
                    url.startsWith("/") -> null // If it's a relative path, ignore it and force our engine to find the real link
                    else -> url
                }
            }

            // Use the sanitized URL, or fallback to our finder engine
            val validUrl = sanitizedUrl ?: repository.findValidFullResUrl(item.id, item.tag) ?: item.thumbnail.replace(".avif", ".jpg")

            updateJobState(item.id, validUrl, DownloadState.DOWNLOADING)

            // 👇 2. A safer way to extract the file extension
            val cleanUrl = validUrl.substringBefore("?") // Strip query parameters just in case
            val ext = if (cleanUrl.substringAfterLast("/", "").contains(".")) {
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

    // 👇 3. Load files from disk
    fun loadLocalLibrary() {
        _localFiles.value = FileUtil.getImagesFromDirectory(currentDownloadDirectory)
    }

    // 👇 4. Change directory and reload
    fun setDownloadDirectory(path: String) {
        repository.profileManager.downloadDirectory = path
        loadLocalLibrary()
    }

    fun toggleDownloadsModal(show: Boolean) {
        if (show) loadLocalLibrary() // Refresh whenever opened
        _showDownloadsModal.value = show
    }

    // 👇 2. Add the fetch function
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

    // Toggle an item's selection
    fun toggleSelection(id: Int) {
        _selectedIdsForDownload.update { current ->
            if (current.contains(id)) {
                current - id
            } else {
                current + id
            }
        }
    }

    // Clear all selections
    fun clearSelection() {
        _selectedIdsForDownload.update { emptySet() }
    }

    // 👇 2. Update the click handler to run the verifier
    fun onImageClick(item: ZerochanItem) {
        _selectedItem.update { item }
        _verifiedFullResUrl.update { null } // Reset the URL so the previous image doesn't flash

        scope.launch {
            // Ask the repository to ping the server
            val validUrl = repository.findValidFullResUrl(item.id, item.tag)

            // If it found one, use it. If it returned null, use the safe thumbnail
            _verifiedFullResUrl.update { validUrl ?: item.thumbnail.replace(".avif", ".jpg") }
        }
    }

    // 👇 3. Clear the URL when dismissing the modal
    fun onDismissModal() {
        _selectedItem.update { null }
        _verifiedFullResUrl.update { null }
        _selectedItemDetails.value = null // Clear details too
    }

    fun loadSearchHistory() {
        val history = repository.profileManager.searchHistory.map {
            // We package history items as Dummy Suggestions so the UI can render them identically!
            ZerochanSuggestion(value = it, type = "Recent Search")
        }
        _suggestions.update { history }
    }

    fun onSearch(query: String) {
        _suggestions.update { emptyList() } // Hide the dropdown

        if (query.isBlank()) return

        // --- NEW: Save to History ---
        val currentHistory = repository.profileManager.searchHistory.toMutableList()
        currentHistory.remove(query) // Remove if it already exists to avoid duplicates
        currentHistory.add(0, query) // Add to the very top!
        repository.profileManager.searchHistory = currentHistory
        // -----------------------------

        println("Searching for: $query")
        currentPage = 1

        scope.launch {
            _isLoading.update { true }
            _error.update { null }
            _isUsernameMissing.update { false }
            runCatching {
                repository.search(getCurrentApiParams(currentPage))
            }.onSuccess { items ->
                // 👇 Force the list to only keep items with unique IDs
                _images.update { items.distinctBy { it.id } }

                if (items.isEmpty()) {
                    _isEndOfPaginationReached.update { true }
                }
            }.onFailure { e ->
                if (e is NoUsernameException) {
                    _isUsernameMissing.update { true }
                } else {
                    _error.update { e.message }
                }
            }
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
                // 👇 Force the list to only keep items with unique IDs
                if (items.isEmpty()) {
                    // 👇 Zerochan returned 0 items. We've hit the end!
                    _isEndOfPaginationReached.update { true }
                } else {
                    // Force the list to only keep items with unique IDs
                    _images.update { current ->
                        (current + items).distinctBy { it.id }
                    }
                }
            }.onFailure { e ->
                currentPage--                   // roll back on failure
                if (e is NoUsernameException) {
                    _isUsernameMissing.update { true }
                } else {
                    _error.update { e.message }
                }
            }
            _isLoading.update { false }
        }
    }

    fun downloadSelectedItems() {
        val selectedIds = _selectedIdsForDownload.value
        if (selectedIds.isEmpty()) return

        val itemsToDownload = _images.value.filter { it.id in selectedIds }
        clearSelection()

        // 1. Add new items to the visual queue
        val newJobs = itemsToDownload.map { DownloadJob(it) }
        _downloadQueue.update { current -> current + newJobs }

        // 2. Process the queue sequentially
        scope.launch {
            newJobs.forEach { job ->
                // Ping server to find the real extension
                val validUrl = repository.findValidFullResUrl(job.item.id, job.item.tag)
                val downloadUrl = validUrl ?: job.item.thumbnail.replace(".avif", ".jpg")

                // Update UI: Tell it we found the URL and are starting the download
                updateJobState(job.item.id, downloadUrl, DownloadState.DOWNLOADING)

                val ext = downloadUrl.substringAfterLast(".", "jpg")
                val safeTag = job.item.tag.replace("[^a-zA-Z0-9.-]".toRegex(), "_")
                val fileName = "Zerochan_${safeTag}_${job.item.id}.$ext"

                // Execute the download
                val file = repository.downloadImageToDisk(downloadUrl, fileName, currentDownloadDirectory)

                // If the modal is currently open, refresh the files list so it appears instantly!
                if (file != null && _showDownloadsModal.value) {
                    loadLocalLibrary()
                }

                // Update UI: Tell it if we succeeded or failed
                updateJobState(
                    id = job.item.id,
                    url = downloadUrl,
                    state = if (file != null) DownloadState.SUCCESS else DownloadState.ERROR
                )
            }
        }
    }

    // Helper to safely update a specific job in the state flow list
    private fun updateJobState(id: Int, url: String, state: DownloadState) {
        _downloadQueue.update { queue ->
            queue.map {
                if (it.item.id == id) it.copy(resolvedUrl = url, state = state) else it
            }
        }
    }

    // Give the user a way to dismiss finished downloads from the panel
    fun clearCompletedDownloads() {
        _downloadQueue.update { queue ->
            queue.filter { it.state == DownloadState.PREPARING || it.state == DownloadState.DOWNLOADING }
        }
    }

    // 👇 2. Replace your existing onQueryChange with this upgraded Debouncer!
    fun onQueryChange(value: String) {
        _query.update { value }
        searchJob?.cancel()

        if (value.isBlank()) {
            loadSearchHistory() // 👈 Show history when input is cleared!
            return
        }

        searchJob = scope.launch {
            delay(300.milliseconds)
            val results = repository.getSuggestions(value)
            _suggestions.update { results }
        }
    }

    fun onSearchFocusChanged(isFocused: Boolean) {
        if (isFocused && _query.value.isBlank()) {
            loadSearchHistory()
        } else if (!isFocused) {
            // Clear suggestions when the user clicks away so the menu closes
            _suggestions.update { emptyList() }
        }
    }

    suspend fun fetchRemoteGif(url: String): File? {
        return repository.getRemoteGifFile(url)
    }
}
