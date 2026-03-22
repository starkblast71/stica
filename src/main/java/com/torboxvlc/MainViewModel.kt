package com.torboxvlc

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.torboxvlc.data.PreferencesRepository
import com.torboxvlc.data.TorBoxRepository
import com.torboxvlc.data.api.TorBoxClient
import com.torboxvlc.data.model.DownloadEntry
import com.torboxvlc.data.model.TorrentFile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

sealed class UiState {
    object Loading : UiState()
    object NeedsApiKey : UiState()
    data class Success(val entries: List<DownloadEntry>) : UiState()
    data class Error(val message: String) : UiState()
}

sealed class FileAction {
    object Idle : FileAction()
    object Loading : FileAction()
    data class Error(val message: String) : FileAction()
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val prefsRepo = PreferencesRepository(application)
    private var torboxRepo: TorBoxRepository? = null

    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _fileAction = MutableStateFlow<FileAction>(FileAction.Idle)
    val fileAction: StateFlow<FileAction> = _fileAction.asStateFlow()

    private val _apiKey = MutableStateFlow<String?>(null)
    val apiKey: StateFlow<String?> = _apiKey.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _filterType = MutableStateFlow<DownloadEntry.EntryType?>(null)
    val filterType: StateFlow<DownloadEntry.EntryType?> = _filterType.asStateFlow()

    init {
        viewModelScope.launch {
            val key = prefsRepo.apiKeyFlow.first()
            _apiKey.value = key
            if (key.isNullOrBlank()) {
                _uiState.value = UiState.NeedsApiKey
            } else {
                initRepo(key)
                loadDownloads()
            }
        }
    }

    private fun initRepo(key: String) {
        torboxRepo = TorBoxRepository(TorBoxClient.create(key))
    }

    fun saveApiKey(key: String) {
        viewModelScope.launch {
            prefsRepo.saveApiKey(key)
            _apiKey.value = key
            initRepo(key)
            loadDownloads()
        }
    }

    fun logout() {
        viewModelScope.launch {
            prefsRepo.clearApiKey()
            _apiKey.value = null
            torboxRepo = null
            _uiState.value = UiState.NeedsApiKey
        }
    }

    fun loadDownloads(bypassCache: Boolean = false) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            val repo = torboxRepo ?: return@launch
            repo.getAllDownloads(bypassCache)
                .onSuccess { _uiState.value = UiState.Success(it) }
                .onFailure { _uiState.value = UiState.Error(it.message ?: "Errore sconosciuto") }
        }
    }

    fun openFileInVlc(context: Context, entry: DownloadEntry, file: TorrentFile) {
        viewModelScope.launch {
            _fileAction.value = FileAction.Loading
            val repo = torboxRepo ?: return@launch
            repo.getDownloadLink(entry, file)
                .onSuccess { url ->
                    _fileAction.value = FileAction.Idle
                    launchVlc(context, url, file.name)
                }
                .onFailure { e ->
                    _fileAction.value = FileAction.Error(e.message ?: "Impossibile ottenere il link")
                }
        }
    }

    private fun launchVlc(context: Context, url: String, fileName: String) {
        // Try VLC intent with explicit stream URI
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(Uri.parse(url), guessMimeType(fileName))
            setPackage("org.videolan.vlc")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            putExtra("title", fileName)
        }
        val fallback = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(Uri.parse(url), guessMimeType(fileName))
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            try {
                context.startActivity(fallback)
            } catch (e2: Exception) {
                _fileAction.value = FileAction.Error("VLC non trovato. Installalo dal Play Store.")
            }
        }
    }

    private fun guessMimeType(name: String): String {
        val ext = name.substringAfterLast('.', "").lowercase()
        return when (ext) {
            "mp4", "m4v" -> "video/mp4"
            "mkv" -> "video/x-matroska"
            "avi" -> "video/x-msvideo"
            "mov" -> "video/quicktime"
            "wmv" -> "video/x-ms-wmv"
            "flv" -> "video/x-flv"
            "webm" -> "video/webm"
            "ts", "m2ts" -> "video/mp2t"
            "mp3" -> "audio/mpeg"
            "flac" -> "audio/flac"
            "aac" -> "audio/aac"
            "ogg" -> "audio/ogg"
            "wav" -> "audio/wav"
            "opus" -> "audio/opus"
            else -> "video/*"
        }
    }

    fun setSearchQuery(q: String) { _searchQuery.value = q }
    fun setFilterType(t: DownloadEntry.EntryType?) { _filterType.value = t }
    fun clearFileActionError() { _fileAction.value = FileAction.Idle }

    fun filteredEntries(entries: List<DownloadEntry>): List<DownloadEntry> {
        val q = _searchQuery.value.lowercase()
        val type = _filterType.value
        return entries.filter { entry ->
            (type == null || entry.entryType == type) &&
            (q.isBlank() || entry.name.lowercase().contains(q))
        }
    }
}
