package com.torboxvlc.data

import com.torboxvlc.data.api.TorBoxApiService
import com.torboxvlc.data.model.DownloadEntry
import com.torboxvlc.data.model.TorrentFile

class TorBoxRepository(private val api: TorBoxApiService) {

    suspend fun getAllDownloads(bypassCache: Boolean = false): Result<List<DownloadEntry>> {
        return try {
            val entries = mutableListOf<DownloadEntry>()

            // Torrents
            val torrentsResp = api.getTorrentList(bypassCache)
            if (torrentsResp.isSuccessful) {
                torrentsResp.body()?.data?.forEach { entries.add(DownloadEntry.Torrent(it)) }
            }

            // Usenet
            val usenetResp = api.getUsenetList(bypassCache)
            if (usenetResp.isSuccessful) {
                usenetResp.body()?.data?.forEach { entries.add(DownloadEntry.Usenet(it)) }
            }

            // Web downloads
            val webResp = api.getWebDownloadList(bypassCache)
            if (webResp.isSuccessful) {
                webResp.body()?.data?.forEach { entries.add(DownloadEntry.WebDl(it)) }
            }

            if (entries.isEmpty() && !torrentsResp.isSuccessful) {
                Result.failure(Exception("API error ${torrentsResp.code()}: ${torrentsResp.message()}"))
            } else {
                // Sort newest first
                Result.success(entries.sortedByDescending { it.createdAt })
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getDownloadLink(
        entry: DownloadEntry,
        file: TorrentFile
    ): Result<String> {
        return try {
            val response = when (entry) {
                is DownloadEntry.Torrent -> api.requestTorrentDownloadLink(
                    torrentId = entry.id,
                    fileId = file.id
                )
                is DownloadEntry.Usenet -> api.requestUsenetDownloadLink(
                    usenetId = entry.id,
                    fileId = file.id
                )
                is DownloadEntry.WebDl -> api.requestWebDownloadLink(
                    webDownloadId = entry.id,
                    fileId = file.id
                )
            }

            if (response.isSuccessful) {
                val link = response.body()?.data?.link
                if (!link.isNullOrBlank()) {
                    Result.success(link)
                } else {
                    Result.failure(Exception(response.body()?.detail ?: "Link vuoto nella risposta"))
                }
            } else {
                Result.failure(Exception("Errore ${response.code()}: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
