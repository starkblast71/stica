package com.torboxvlc.data.api

import com.torboxvlc.data.model.DownloadLinkData
import com.torboxvlc.data.model.TorBoxResponse
import com.torboxvlc.data.model.TorrentItem
import com.torboxvlc.data.model.UsenetItem
import com.torboxvlc.data.model.WebDownloadItem
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface TorBoxApiService {

    // ── Torrents ──────────────────────────────────────────────────────────────

    @GET("api/v1/api/torrents/mylist")
    suspend fun getTorrentList(
        @Query("bypass_cache") bypassCache: Boolean = false
    ): Response<TorBoxResponse<List<TorrentItem>>>

    @GET("api/v1/api/torrents/requestdl")
    suspend fun requestTorrentDownloadLink(
        @Query("torrent_id") torrentId: Int,
        @Query("file_id") fileId: Int,
        @Query("zip_link") zipLink: Boolean = false,
        @Query("user_ip") userIp: String? = null,
        @Query("redirect_link") redirectLink: Boolean = false
    ): Response<TorBoxResponse<DownloadLinkData>>

    // ── Usenet ────────────────────────────────────────────────────────────────

    @GET("api/v1/api/usenet/mylist")
    suspend fun getUsenetList(
        @Query("bypass_cache") bypassCache: Boolean = false
    ): Response<TorBoxResponse<List<UsenetItem>>>

    @GET("api/v1/api/usenet/requestdl")
    suspend fun requestUsenetDownloadLink(
        @Query("usenet_id") usenetId: Int,
        @Query("file_id") fileId: Int,
        @Query("zip_link") zipLink: Boolean = false
    ): Response<TorBoxResponse<DownloadLinkData>>

    // ── Web Downloads ─────────────────────────────────────────────────────────

    @GET("api/v1/api/webdl/mylist")
    suspend fun getWebDownloadList(
        @Query("bypass_cache") bypassCache: Boolean = false
    ): Response<TorBoxResponse<List<WebDownloadItem>>>

    @GET("api/v1/api/webdl/requestdl")
    suspend fun requestWebDownloadLink(
        @Query("web_download_id") webDownloadId: Int,
        @Query("file_id") fileId: Int,
        @Query("zip_link") zipLink: Boolean = false
    ): Response<TorBoxResponse<DownloadLinkData>>
}
