package com.jp319.zerochan.ui.screens

import androidx.compose.ui.text.input.TextFieldValue
import com.jp319.zerochan.data.model.*
import com.jp319.zerochan.data.repository.NoUsernameException
import com.jp319.zerochan.data.repository.ZerochanRepository
import com.jp319.zerochan.utils.FileUtil
import com.jp319.zerochan.utils.Logger
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import java.io.File
import kotlin.time.Duration.Companion.milliseconds

enum class DownloadState { PREPARING, DOWNLOADING, SUCCESS, ERROR, RETRY_STALLED }

enum class UpdateDownloadState { IDLE, DOWNLOADING, SUCCESS, ERROR }

data class DownloadJob(
    val item: ZerochanItem,
    val resolvedUrl: String? = null,
    val state: DownloadState = DownloadState.PREPARING,
    val retryCount: Int = 0,
    val errorMessage: String? = null,
)

/**
 * ViewModel managing the primary state, logic, and user interactions for the Gallery.
 * Acts as the bridge between the Zerochan UI and the data repository.
 *
 * @property repository The data repository for Zerochan API interactions.
 */
class GalleryViewModel(private val repository: ZerochanRepository) {
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val tag = "GalleryViewModel"

    // --- Update State ---
    private val _updateInfo = MutableStateFlow<UpdateInfo?>(null)
    val updateInfo: StateFlow<UpdateInfo?> = _updateInfo.asStateFlow()

    private val _updateDownloadProgress = MutableStateFlow(0f)
    val updateDownloadProgress = _updateDownloadProgress.asStateFlow()

    private val _updateDownloadState = MutableStateFlow(UpdateDownloadState.IDLE)
    val updateDownloadState = _updateDownloadState.asStateFlow()

    private val _downloadedInstallerPath = MutableStateFlow<File?>(null)
    val downloadedInstallerPath = _downloadedInstallerPath.asStateFlow()

    private var updateDownloadJob: Job? = null

    private val _updateError = MutableStateFlow<String?>(null)
    val updateError = _updateError.asStateFlow()

    val isUpdateAvailable = updateInfo.map { it != null }.stateIn(scope, SharingStarted.Lazily, false)

    // --- Search State ---
    private val _query = MutableStateFlow(TextFieldValue(""))
    val query: StateFlow<TextFieldValue> = _query.asStateFlow()

    private val _images = MutableStateFlow<List<ZerochanItem>>(emptyList())
    val images: StateFlow<List<ZerochanItem>> = _images.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _isUsernameMissing = MutableStateFlow(false)
    val isUsernameMissing: StateFlow<Boolean> = _isUsernameMissing.asStateFlow()

    private val _suggestions = MutableStateFlow<List<ZerochanSuggestion>>(emptyList())
    val suggestions: StateFlow<List<ZerochanSuggestion>> = _suggestions.asStateFlow()

    private val _isEndOfPaginationReached = MutableStateFlow(false)
    val isEndOfPaginationReached: StateFlow<Boolean> = _isEndOfPaginationReached.asStateFlow()

    private var currentPage = 1
    private var searchJob: Job? = null

    // --- Modal / Detail State ---
    private val _selectedItem = MutableStateFlow<ZerochanItem?>(null)
    val selectedItem: StateFlow<ZerochanItem?> = _selectedItem.asStateFlow()

    private val _verifiedFullResUrl = MutableStateFlow<String?>(null)
    val verifiedFullResUrl: StateFlow<String?> = _verifiedFullResUrl.asStateFlow()

    private val _selectedItemDetails = MutableStateFlow<ZerochanFullItem?>(null)
    val selectedItemDetails: StateFlow<ZerochanFullItem?> = _selectedItemDetails.asStateFlow()

    private val _isLoadingDetails = MutableStateFlow(false)
    val isLoadingDetails: StateFlow<Boolean> = _isLoadingDetails.asStateFlow()

    // --- Selection State ---
    private val _isSelectionModeActive = MutableStateFlow(false)
    val isSelectionModeActive: StateFlow<Boolean> = _isSelectionModeActive.asStateFlow()

    private val _selectedIdsForDownload = MutableStateFlow<Set<Int>>(emptySet())
    val selectedIdsForDownload: StateFlow<Set<Int>> = _selectedIdsForDownload.asStateFlow()

    // --- Download State ---
    private val _downloadQueue = MutableStateFlow<List<DownloadJob>>(emptyList())
    val downloadQueue: StateFlow<List<DownloadJob>> = _downloadQueue.asStateFlow()

    private val _showDownloadsModal = MutableStateFlow(false)
    val showDownloadsModal = _showDownloadsModal.asStateFlow()

    private val _localFiles = MutableStateFlow<List<File>>(emptyList())
    val localFiles = _localFiles.asStateFlow()

    private val _viewingLocalFile = MutableStateFlow<File?>(null)
    val viewingLocalFile = _viewingLocalFile.asStateFlow()

    private val downloadChannel = Channel<DownloadJob>(Channel.UNLIMITED)

    /** Count of active downloads (Preparing or Downloading). */
    val ongoingDownloadCount: StateFlow<Int> =
        downloadQueue.map { queue ->
            queue.count { it.state == DownloadState.PREPARING || it.state == DownloadState.DOWNLOADING }
        }.stateIn(scope, SharingStarted.Lazily, 0)

    init {
        scope.launch {
            for (job in downloadChannel) {
                runDownloadSequentially(job)
            }
        }
        checkForUpdates()

        // Clean up existing search history (remove empty strings)
        val history = repository.profileManager.searchHistory
        if (history.any { it.isBlank() }) {
            repository.profileManager.searchHistory = history.filter { it.isNotBlank() }
        }

        onHomeSearch() // Load default results on startup
    }

    /**
     * Checks for application updates from GitHub.
     */
    private fun checkForUpdates() {
        scope.launch {
            val release = repository.fetchLatestReleaseInfo()
            if (release != null) {
                val latestTag = release.tagName.removePrefix("v")
                val currentTag = com.jp319.zerochan.BuildConfig.VERSION.removePrefix("v")

                // Simple semver compare approximation
                if (isNewerVersion(currentTag, latestTag)) {
                    val bestAsset = getInstallerAssetForCurrentOS(release.assets)
                    _updateInfo.value =
                        UpdateInfo(
                            latestVersion = release.tagName,
                            releaseNotes = release.body,
                            releaseUrl = release.htmlUrl,
                            installerUrl = bestAsset?.browserDownloadUrl,
                            installerName = bestAsset?.name,
                            installerSize = bestAsset?.size ?: 0,
                        )
                    Logger.info(tag, "New update available: $latestTag")
                }
            }
        }
    }

    /**
     * Downloads the latest installer with 3-attempt retry logic.
     */
    fun downloadUpdateInstaller() {
        val info = updateInfo.value ?: return
        val url = info.installerUrl ?: return

        // --- STRONG CONCURRENCY GUARD (Bug 2 Refined) ---
        // Use both job status and state to prevent double-starts from quick clicks
        if (updateDownloadJob?.isActive == true || _updateDownloadState.value == UpdateDownloadState.DOWNLOADING) {
            _updateDownloadState.value = UpdateDownloadState.DOWNLOADING // "Bring to Foreground"
            Logger.info(tag, "Update download already in progress. Restoring UI visibility.")
            return
        }

        updateDownloadJob =
            scope.launch {
                _updateDownloadState.value = UpdateDownloadState.DOWNLOADING
                _updateDownloadProgress.value = 0f
                _updateError.value = null

                val tempDir = File(System.getProperty("java.io.tmpdir"), "zerochan-updates")
                val targetFile = File(tempDir, info.installerName ?: "ZerochanDownloader-Update.exe")

                // --- EXISTING FILE INTEGRITY CHECK (Bug 1 Refined) ---
                // Only skip if the file exists AND its size exactly matches the expected installer size.
                if (targetFile.exists() && info.installerSize > 0 && targetFile.length() == info.installerSize) {
                    Logger.info(tag, "Full installer for version ${info.latestVersion} already exists locally. Skipping download.")
                    _downloadedInstallerPath.value = targetFile
                    _updateDownloadProgress.value = 1f
                    _updateDownloadState.value = UpdateDownloadState.SUCCESS
                    return@launch
                }

                var success = false
                for (attempt in 1..3) {
                    Logger.info(tag, "Download attempt $attempt for update...")
                    val file =
                        repository.downloadInstaller(url, targetFile) { progress ->
                            _updateDownloadProgress.value = progress
                        }

                    if (file != null && file.exists()) {
                        _downloadedInstallerPath.value = file
                        _updateDownloadState.value = UpdateDownloadState.SUCCESS
                        success = true
                        break
                    }

                    if (attempt < 3) {
                        delay(2000.milliseconds) // Backoff
                    }
                }

                if (!success) {
                    _updateDownloadState.value = UpdateDownloadState.ERROR
                    _updateError.value = "Failed to download update after 3 attempts. Please download manually from GitHub."
                }
            }
    }

    /** Resets the update download state to IDLE, dismissing the dialog while preserving background job. */
    fun dismissUpdateDialog() {
        _updateDownloadState.value = UpdateDownloadState.IDLE
        // We preserve _updateDownloadProgress and _updateError so they remain available if brought back to foreground
    }

    private fun isNewerVersion(
        current: String,
        latest: String,
    ): Boolean {
        try {
            val c = current.split(".").map { it.toInt() }
            val l = latest.split(".").map { it.toInt() }
            for (i in 0 until minOf(c.size, l.size)) {
                if (l[i] > c[i]) return true
                if (l[i] < c[i]) return false
            }
            return l.size > c.size
        } catch (_: Exception) {
            return current != latest // Fallback to literal compare
        }
    }

    private fun getInstallerAssetForCurrentOS(assets: List<GitHubAsset>): GitHubAsset? {
        val osName = System.getProperty("os.name").lowercase()
        return when {
            osName.contains("win") -> assets.find { it.name.endsWith(".exe") || it.name.endsWith(".msi") }
            osName.contains("mac") -> assets.find { it.name.endsWith(".dmg") }
            osName.contains("nix") || osName.contains("nux") -> assets.find { it.name.endsWith(".deb") || it.name.endsWith(".rpm") }
            else -> assets.firstOrNull()
        }
    }

    // --- Filter State ---
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

    // --- Drag-to-select tracking ---
    private var isDragging = false
    private var dragInitialStateIsSelect = true // true = selecting, false = deselecting
    private var preExistingSelection = emptySet<Int>()

    /** The current local directory where images are saved. */
    val currentDownloadDirectory: String
        get() = repository.profileManager.downloadDirectory

    // -------------------------------------------------------------------------
    // Filter Panel Actions
    // -------------------------------------------------------------------------

    /** Hides the search filter panel. */
    fun hideFilterPanel() {
        _isFilterPanelVisible.value = false
    }

    /** Resets the search results to the front page (Home). */
    fun onHomeSearch() {
        _query.update { TextFieldValue("") }
        onSearch("")
    }

    /** Toggles the visibility of the search filter panel. */
    fun toggleFilterPanel() {
        _isFilterPanelVisible.value = !isFilterPanelVisible.value
    }

    /** Updates the result sorting order and refreshes the search. */
    fun setSortOrder(sort: SortOrder?) {
        _sortOrder.value = sort
        onSearch(_query.value.text)
    }

    /** Updates the time range filter and refreshes the search. */
    fun setTimeFilter(time: TimeFilter?) {
        _timeFilter.value = time
        onSearch(_query.value.text)
    }

    /** Updates the image dimension filter and refreshes the search. */
    fun setDimensionFilter(dim: DimensionFilter?) {
        _dimensionFilter.value = dim
        onSearch(_query.value.text)
    }

    /** Updates the primary color filter and refreshes the search. */
    fun setColorFilter(color: String?) {
        _colorFilter.value = color
        onSearch(_query.value.text)
    }

    /** Toggles strict tag matching mode and refreshes the search. */
    fun toggleStrictMode() {
        _strictMode.update { !it }
        onSearch(_query.value.text)
    }

    /** Resets all search filters to their default values. */
    fun clearFilters() {
        _sortOrder.value = null
        _timeFilter.value = null
        _dimensionFilter.value = null
        _colorFilter.value = null
        _strictMode.value = false
        onSearch(_query.value.text)
    }

    private fun getCurrentApiParams(page: Int): ZerochanApiParams {
        return ZerochanApiParams(
            tag = _query.value.text,
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
    // Selection Actions
    // -------------------------------------------------------------------------

    /** Toggles the multi-selection mode for batch operations. */
    fun toggleSelectionMode() {
        _isSelectionModeActive.update { !it }
        if (!_isSelectionModeActive.value) {
            clearSelection()
        }
    }

    /** Toggles the selection state of a specific image ID. */
    fun toggleSelection(id: Int) {
        _selectedIdsForDownload.update { current ->
            if (current.contains(id)) current - id else current + id
        }
    }

    /** Initiates a drag-selection gesture starting from a specific image ID. */
    fun startDragSelection(
        id: Int,
        isSelect: Boolean? = null,
    ) {
        isDragging = true
        preExistingSelection = _selectedIdsForDownload.value
        dragInitialStateIsSelect = isSelect ?: !preExistingSelection.contains(id)
    }

    /** Updates the selection set during a drag gesture based on hovered IDs. */
    fun updateDragSelectionWithSet(idsUnderDrag: Set<Int>) {
        if (!isDragging) return
        _selectedIdsForDownload.update {
            if (dragInitialStateIsSelect) {
                preExistingSelection + idsUnderDrag
            } else {
                preExistingSelection - idsUnderDrag
            }
        }
    }

    /** Ends the current drag-selection gesture. */
    fun endDragSelection() {
        isDragging = false
    }

    /** Clears all selected image IDs. */
    fun clearSelection() {
        _selectedIdsForDownload.update { emptySet() }
    }

    // -------------------------------------------------------------------------
    // Local Library Actions
    // -------------------------------------------------------------------------

    /** Opens a local image file in the previewer. */
    fun openLocalFile(file: File) {
        _viewingLocalFile.value = file
    }

    /** Closes the local image previewer. */
    fun closeLocalFile() {
        _viewingLocalFile.value = null
    }

    /** Synchronizes the local library state with the disk contents. */
    fun loadLocalLibrary() {
        _localFiles.value = FileUtil.getImagesFromDirectory(currentDownloadDirectory)
    }

    /** Updates the target download directory and refreshes the local library. */
    fun setDownloadDirectory(path: String) {
        repository.profileManager.downloadDirectory = path
        loadLocalLibrary()
    }

    /** Toggles the downloads/library management modal. */
    fun toggleDownloadsModal(show: Boolean) {
        if (show) loadLocalLibrary()
        _showDownloadsModal.value = show
    }

    // -------------------------------------------------------------------------
    // Item Detail Actions
    // -------------------------------------------------------------------------

    /** Fetches detailed metadata for a single Zerochan image. */
    fun fetchItemDetails(id: Int) {
        scope.launch {
            _isLoadingDetails.value = true
            val details = repository.getItem(id)
            _selectedItemDetails.value = details
            _isLoadingDetails.value = false
        }
    }

    /** Dismisses the image details dialog. */
    fun onDismissDetails() {
        _selectedItemDetails.value = null
    }

    // -------------------------------------------------------------------------
    // Modal Actions
    // -------------------------------------------------------------------------

    /** Handles clicks on an image card to open the preview modal. */
    fun onImageClick(item: ZerochanItem) {
        _selectedItem.update { item }
        _verifiedFullResUrl.update { null }

        scope.launch {
            val validUrl = repository.findValidFullResUrl(item.id, item.tag)
            _verifiedFullResUrl.update { validUrl ?: item.thumbnail.replace(".avif", ".jpg") }
        }
    }

    /** Dismisses the active preview modal or details dialog. */
    fun onDismissModal() {
        _selectedItem.update { null }
        _verifiedFullResUrl.update { null }
        _selectedItemDetails.value = null
    }

    // -------------------------------------------------------------------------
    // Search Actions
    // -------------------------------------------------------------------------

    /** Loads recent search history as initial suggestions. */
    fun loadSearchHistory() {
        val history =
            repository.profileManager.searchHistory.map {
                ZerochanSuggestion(value = it, type = "Recent Search")
            }
        _suggestions.update { history }
    }

    /** Executes a new search with the given query. */
    fun onSearch(query: String) {
        _suggestions.update { emptyList() }
        // Allow blank searches for "Home" view via the repository
        // (If query is blank, getCurrentApiParams uses empty tag which Zerochan treats as home)

        clearSelection()
        _isSelectionModeActive.value = false

        // Update search history (exclude empty queries/Home search)
        if (query.isNotBlank()) {
            val currentHistory = repository.profileManager.searchHistory.toMutableList()
            currentHistory.remove(query)
            currentHistory.add(0, query)
            repository.profileManager.searchHistory = currentHistory
        }

        Logger.debug(tag, "Searching for: $query")
        currentPage = 1
        _isEndOfPaginationReached.update { false }

        scope.launch {
            _isSearching.update { true }
            _isLoading.update { true }
            _error.update { null }
            _isUsernameMissing.update { false }

            runCatching {
                repository.search(getCurrentApiParams(currentPage))
            }.onSuccess { items ->
                _images.update { items.distinctBy { it.id } }
            }.onFailure { e ->
                handleNetworkError("Search entirely failed", e)
            }
            _isSearching.update { false }
            _isLoading.update { false }
        }
    }

    /** Refreshes the current search results. */
    fun onRefresh() {
        onSearch(_query.value.text)
    }

    /** Loads the next page of results for the current search. */
    fun onLoadMore() {
        if (_isLoading.value || _isEndOfPaginationReached.value) return

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
                handleNetworkError("Pagination failed", e)
            }
            _isLoading.update { false }
        }
    }

    private fun handleNetworkError(
        tag: String,
        e: Throwable,
    ) {
        if (e is NoUsernameException) {
            _isUsernameMissing.update { true }
        } else {
            Logger.error(tag, tag, e)
            _error.update { e.message }
        }
    }

    /** Handles changes to the search query text input. */
    fun onQueryChange(value: TextFieldValue) {
        _query.update { value }
        searchJob?.cancel()

        if (value.text.isBlank()) {
            loadSearchHistory()
            return
        }

        searchJob =
            scope.launch {
                delay(300.milliseconds)
                val results = repository.getSuggestions(value.text)
                _suggestions.update { results }
            }
    }

    /** Manages suggestion visibility based on search bar focus. */
    fun onSearchFocusChanged(isFocused: Boolean) {
        if (isFocused && _query.value.text.isBlank()) {
            loadSearchHistory()
        } else if (!isFocused) {
            // Delay clear to allow click events on suggestions to fire first
            scope.launch {
                delay(200.milliseconds)
                _suggestions.update { emptyList() }
            }
        }
    }

    // -------------------------------------------------------------------------
    // Download Logic
    // -------------------------------------------------------------------------

    /** Initiates a download for a single image item. */
    fun downloadSingleItem(
        item: ZerochanItem,
        knownUrl: String? = null,
    ) {
        val job = DownloadJob(item, knownUrl)
        _downloadQueue.update { current -> current + job }
        processDownload(job)
    }

    /** Initiates downloads for all currently selected images. */
    fun downloadSelectedItems() {
        val selectedIds = _selectedIdsForDownload.value
        if (selectedIds.isEmpty()) return

        val itemsToDownload = _images.value.filter { it.id in selectedIds }
        clearSelection()
        _isSelectionModeActive.value = false

        val newJobs = itemsToDownload.map { DownloadJob(it) }
        _downloadQueue.update { current -> current + newJobs }

        newJobs.forEach { job ->
            processDownload(job)
        }
    }

    /** Re-attempts a stalled or failed download. */
    fun retryDownload(job: DownloadJob) {
        val updatedJob = job.copy(state = DownloadState.PREPARING, errorMessage = null)
        _downloadQueue.update { queue ->
            queue.map { if (it.item.id == job.item.id) updatedJob else it }
        }
        processDownload(updatedJob)
    }

    /** Re-attempts all stalled or failed downloads in the current queue. */
    fun retryAllDownloads() {
        val stalledJobs = _downloadQueue.value.filter { it.state == DownloadState.RETRY_STALLED || it.state == DownloadState.ERROR }
        stalledJobs.forEach { retryDownload(it) }
    }

    private fun processDownload(job: DownloadJob) {
        downloadChannel.trySend(job)
    }

    private suspend fun runDownloadSequentially(job: DownloadJob) {
        try {
            val downloadUrl = resolveDownloadUrl(job.item, job.resolvedUrl)
            if (downloadUrl == null) {
                val newRetryCount = job.retryCount + 1
                if (newRetryCount >= 3) {
                    updateJobState(
                        id = job.item.id,
                        url = "",
                        newState = DownloadState.ERROR,
                        retryCount = newRetryCount,
                        error = "App might be blocked or try again at a later time.",
                    )
                } else {
                    updateJobState(
                        id = job.item.id,
                        url = "",
                        newState = DownloadState.RETRY_STALLED,
                        retryCount = newRetryCount,
                        error = "Full-res not found",
                    )
                }
                return
            }

            updateJobState(job.item.id, downloadUrl, DownloadState.DOWNLOADING, job.retryCount)

            val fileName = generateFileName(job.item, downloadUrl)
            val file = repository.downloadImageToDisk(downloadUrl, fileName, currentDownloadDirectory)

            if (file != null && _showDownloadsModal.value) loadLocalLibrary()

            updateJobState(
                id = job.item.id,
                url = downloadUrl,
                newState = if (file != null) DownloadState.SUCCESS else DownloadState.ERROR,
                retryCount = job.retryCount,
            )
        } catch (e: Exception) {
            Logger.error(tag, "Download failed for item ${job.item.id}", e)
            updateJobState(job.item.id, job.resolvedUrl ?: "", DownloadState.ERROR, job.retryCount, e.message)
        }
    }

    /**
     * Resolves the best available download URL for an item.
     * Prioritizes full-resolution discovery over thumbnails.
     */
    private suspend fun resolveDownloadUrl(
        item: ZerochanItem,
        knownUrl: String?,
    ): String? {
        val sanitizedKnown =
            knownUrl?.let { url ->
                when {
                    url.isBlank() -> null
                    url.startsWith("//") -> "https:$url"
                    url.startsWith("/") -> null
                    else -> url
                }
            }

        return sanitizedKnown
            ?: repository.findValidFullResUrl(item.id, item.tag)
    }

    /** Generates a safe, descriptive filename for a Zerochan image. */
    private fun generateFileName(
        item: ZerochanItem,
        url: String,
    ): String {
        val cleanUrl = url.substringBefore("?")
        val ext =
            if (cleanUrl.substringAfterLast("/", "").contains(".")) {
                cleanUrl.substringAfterLast(".", "jpg")
            } else {
                "jpg"
            }
        val safeTag = item.tag.replace("[^a-zA-Z0-9.-]".toRegex(), "_")
        return "Zerochan_${safeTag}_${item.id}.$ext"
    }

    private fun updateJobState(
        id: Int,
        url: String,
        newState: DownloadState,
        retryCount: Int = 0,
        error: String? = null,
    ) {
        _downloadQueue.update { queue ->
            queue.map { entry ->
                if (entry.item.id == id) {
                    entry.copy(
                        resolvedUrl = url,
                        state = newState,
                        retryCount = retryCount,
                        errorMessage = error,
                    )
                } else {
                    entry
                }
            }
        }
    }

    /** Clears successful and failed downloads from the active queue. */
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
