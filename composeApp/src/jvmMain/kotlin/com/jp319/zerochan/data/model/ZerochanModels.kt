package com.jp319.zerochan.data.model

import kotlinx.serialization.Serializable

/**
 * Represents a single image item returned from the Zerochan API.
 * Contains basic metadata such as dimensions, MD5 hash, and tags.
 *
 * @property id Unique Zerochan ID for the image.
 * @property width The width of the image in pixels.
 * @property height The height of the image in pixels.
 * @property md5 The MD5 checksum of the image file.
 * @property thumbnail URL to the thumbnail version of the image.
 * @property source The original source URL if available.
 * @property tag The primary descriptive tag for the image.
 * @property tags A list of all tags associated with the image.
 */
@Serializable
data class ZerochanItem(
    val id: Int,
    val width: Int,
    val height: Int,
    val md5: String,
    val thumbnail: String,
    val source: String? = null,
    val tag: String,
    val tags: List<String>,
)

/**
 * Represents the full details of a Zerochan image, including multiple resolution URLs.
 * Usually fetched specifically for a single item ID.
 *
 * @property id Unique Zerochan ID.
 * @property small URL to the small version.
 * @property medium URL to the medium version.
 * @property large URL to the large version.
 * @property full URL to the maximum resolution version.
 * @property width The native width of the full image.
 * @property height The native height of the full image.
 * @property size File size in bytes.
 * @property hash The MD5 hash of the full image file.
 * @property source The original source URL.
 * @property primary The primary character or category tag.
 * @property tags list of all tags associated with the item.
 */
@Serializable
data class ZerochanFullItem(
    val id: Int,
    val small: String? = null,
    val medium: String? = null,
    val large: String? = null,
    val full: String? = null,
    val width: Int? = null,
    val height: Int? = null,
    val size: Long? = null,
    val hash: String? = null,
    val source: String? = null,
    val primary: String? = null,
    val tags: List<String> = emptyList(),
)

/**
 * Wrapper for the primary API search response.
 *
 * @property items The list of image items found in the search results.
 */
@Serializable
data class ZerochanResponse(
    val items: List<ZerochanItem> = emptyList(),
)

/**
 * Supported sorting orders for Zerochan searches.
 */
enum class SortOrder(val value: String) {
    Id("id"),
    Favorites("fav"),
}

/**
 * Time-based filtering options for searches.
 */
enum class TimeFilter(val value: Int) {
    AllTime(0),
    ThisMonth(1),
    ThisWeek(2),
}

/**
 * Filter options based on image dimensions or orientation.
 */
enum class DimensionFilter(val value: String) {
    Large("large"),
    Huge("huge"),
    Landscape("landscape"),
    Portrait("portrait"),
    Square("square"),
}

/**
 * Parameters for building a Zerochan API search request.
 *
 * @property tag The search query/tag.
 * @property page The page number for pagination.
 * @property limit Number of items to return per page.
 * @property sort Desired sort order.
 * @property time Time range filter.
 * @property color Hex color filter (e.g., "ff0000").
 * @property dimensions Orientation/size filter.
 * @property strict Whether to enforce strict tag matching.
 */
data class ZerochanApiParams(
    val tag: String? = null,
    val page: Int? = null,
    val limit: Int? = null,
    val sort: SortOrder? = null,
    val time: TimeFilter? = null,
    val color: String? = null,
    val dimensions: DimensionFilter? = null,
    val strict: Boolean? = null,
)

/**
 * Response object for the tag suggestion API.
 */
@Serializable
data class ZerochanSuggestionResponse(
    val query: String,
    val language: String? = null,
    val suggestions: List<ZerochanSuggestion> = emptyList(),
)

/**
 * A single tag suggestion from the autocomplete API.
 *
 * @property value The tag string.
 * @property type The category of the tag (e.g., Character, Series).
 * @property thumb Number of images associated with this tag.
 */
@Serializable
data class ZerochanSuggestion(
    val value: String,
    val type: String? = null,
    val thumb: Int? = null,
    val total: Int? = 0,
    val icon: String? = null,
    val parent: String? = null,
    val alias: String? = null,
)
