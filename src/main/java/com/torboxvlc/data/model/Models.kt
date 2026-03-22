package com.torboxvlc.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// ── Generic wrapper ──────────────────────────────────────────────────────────

@Serializable
data class TorBoxResponse<T>(
    val success: Boolean,
    val detail: String? = null,
    val data: T? = null
)

// ── Torrent list ─────────────────────────────────────────────────────────────

@Serializable
data class TorrentItem(
    val id: Int,
    val name: String,
    val hash: String? = null,
    val size: Long = 0L,
    @SerialName("download_state") val downloadState: String = "",
    val progress: Double = 0.0,
    @SerialName("download_speed") val downloadSpeed: Long = 0L,
    @SerialName("upload_speed") val uploadSpeed: Long = 0L,
    val eta: Int = 0,
    val seeds: Int = 0,
    val peers: Int = 0,
    @SerialName("created_at") val createdAt: String = "",
    @SerialName("updated_at") val updatedAt: String = "",
    val files: List<TorrentFile>? = null
)

@Serializable
data class TorrentFile(
    val id: Int,
    val name: String,
    @SerialName("short_name") val shortName: String = "",
    val size: Long = 0L,
    @SerialName("mimetype") val mimeType: String = "",
    @SerialName("short_path") val shortPath: String? = null,
    val md5: String? = null,
    val s3Path: String? = null
)

// ── Download link ─────────────────────────────────────────────────────────────

@Serializable
data class DownloadLinkData(
    val link: String
)

// ── Usenet list ───────────────────────────────────────────────────────────────

@Serializable
data class UsenetItem(
    val id: Int,
    val name: String,
    val size: Long = 0L,
    @SerialName("download_state") val downloadState: String = "",
    val progress: Double = 0.0,
    @SerialName("created_at") val createdAt: String = "",
    @SerialName("updated_at") val updatedAt: String = "",
    val files: List<TorrentFile>? = null
)

// ── Web download list ─────────────────────────────────────────────────────────

@Serializable
data class WebDownloadItem(
    val id: Int,
    val name: String,
    val size: Long = 0L,
    @SerialName("download_state") val downloadState: String = "",
    val progress: Double = 0.0,
    @SerialName("original_url") val originalUrl: String = "",
    @SerialName("created_at") val createdAt: String = "",
    @SerialName("updated_at") val updatedAt: String = "",
    val files: List<TorrentFile>? = null
)

// ── Convenience sealed type for unified list display ─────────────────────────

sealed class DownloadEntry {
    abstract val id: Int
    abstract val name: String
    abstract val size: Long
    abstract val downloadState: String
    abstract val progress: Double
    abstract val createdAt: String
    abstract val files: List<TorrentFile>?
    abstract val entryType: EntryType

    enum class EntryType { TORRENT, USENET, WEBDL }

    data class Torrent(val item: TorrentItem) : DownloadEntry() {
        override val id = item.id
        override val name = item.name
        override val size = item.size
        override val downloadState = item.downloadState
        override val progress = item.progress
        override val createdAt = item.createdAt
        override val files = item.files
        override val entryType = EntryType.TORRENT
    }

    data class Usenet(val item: UsenetItem) : DownloadEntry() {
        override val id = item.id
        override val name = item.name
        override val size = item.size
        override val downloadState = item.downloadState
        override val progress = item.progress
        override val createdAt = item.createdAt
        override val files = item.files
        override val entryType = EntryType.USENET
    }

    data class WebDl(val item: WebDownloadItem) : DownloadEntry() {
        override val id = item.id
        override val name = item.name
        override val size = item.size
        override val downloadState = item.downloadState
        override val progress = item.progress
        override val createdAt = item.createdAt
        override val files = item.files
        override val entryType = EntryType.WEBDL
    }
}

// ── Helpers ───────────────────────────────────────────────────────────────────

fun TorrentFile.isPlayable(): Boolean {
    val playableExtensions = setOf(
        "mp4", "mkv", "avi", "mov", "wmv", "flv", "webm", "m4v", "ts", "m2ts",
        "mp3", "flac", "aac", "ogg", "wav", "wma", "opus",
        "srt", "sub", "ass", "ssa"
    )
    val ext = name.substringAfterLast('.', "").lowercase()
    return ext in playableExtensions || mimeType.startsWith("video/") || mimeType.startsWith("audio/")
}

fun Long.toReadableSize(): String {
    return when {
        this < 1024 -> "$this B"
        this < 1024 * 1024 -> "${"%.1f".format(this / 1024.0)} KB"
        this < 1024 * 1024 * 1024 -> "${"%.1f".format(this / (1024.0 * 1024))} MB"
        else -> "${"%.2f".format(this / (1024.0 * 1024 * 1024))} GB"
    }
}
