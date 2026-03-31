package com.jp319.zerochan.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Data class representing a release from the GitHub API.
 */
@Serializable
data class GitHubRelease(
    @SerialName("tag_name") val tagName: String,
    @SerialName("name") val name: String,
    @SerialName("body") val body: String? = null,
    @SerialName("html_url") val htmlUrl: String,
    @SerialName("assets") val assets: List<GitHubAsset>,
)

/**
 * Data class representing an asset (e.g. installer) attached to a GitHub release.
 */
@Serializable
data class GitHubAsset(
    @SerialName("name") val name: String,
    @SerialName("browser_download_url") val browserDownloadUrl: String,
    @SerialName("size") val size: Long,
)

/**
 * Simplified information about an update to be used by the UI.
 */
data class UpdateInfo(
    val latestVersion: String,
    val releaseNotes: String,
    val releaseUrl: String,
    val installerUrl: String?,
    val installerName: String?,
    val installerSize: Long = 0,
)
