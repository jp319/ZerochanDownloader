package com.jp319.zerochan.data.model

import kotlinx.serialization.Serializable

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

@Serializable
data class ZerochanFullItem(
    val id: Int,
    val small: String? = null,
    val medium: String? = null,
    val large: String? = null,
    val full: String? = null,
    val width: Int? = null,
    val height: Int? = null,
    val size: Long? = null, // File size in bytes
    val hash: String? = null,
    val source: String? = null,
    val primary: String? = null,
    val tags: List<String> = emptyList(),
)

@Serializable
data class ZerochanResponse(
    val items: List<ZerochanItem> = emptyList(),
)

enum class SortOrder(val value: String) {
    Id("id"),
    Favorites("fav"),
}

enum class TimeFilter(val value: Int) {
    AllTime(0),
    ThisMonth(1),
    ThisWeek(2),
}

enum class DimensionFilter(val value: String) {
    Large("large"),
    Huge("huge"),
    Landscape("landscape"),
    Portrait("portrait"),
    Square("square"),
}

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

@Serializable
data class ZerochanSuggestionResponse(
    val query: String,
    val language: String? = null,
    val suggestions: List<ZerochanSuggestion> = emptyList(), // Graceful fallback!
)

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
