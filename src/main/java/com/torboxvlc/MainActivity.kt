package com.torboxvlc

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.torboxvlc.ui.screens.ApiKeyScreen
import com.torboxvlc.ui.screens.LibraryScreen
import com.torboxvlc.ui.theme.TorBoxVLCTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TorBoxVLCTheme {
                TorBoxApp()
            }
        }
    }
}

@Composable
fun TorBoxApp(vm: MainViewModel = viewModel()) {
    val uiState by vm.uiState.collectAsStateWithLifecycle()
    val fileAction by vm.fileAction.collectAsStateWithLifecycle()
    val searchQuery by vm.searchQuery.collectAsStateWithLifecycle()
    val filterType by vm.filterType.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    // Show errors from file action as snackbar
    LaunchedEffect(fileAction) {
        if (fileAction is FileAction.Error) {
            snackbarHostState.showSnackbar(
                message = (fileAction as FileAction.Error).message,
                duration = SnackbarDuration.Long
            )
            vm.clearFileActionError()
        }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (val state = uiState) {
                is UiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                is UiState.NeedsApiKey -> {
                    ApiKeyScreen(onSave = { vm.saveApiKey(it) })
                }

                is UiState.Error -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            "Errore: ${state.message}",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }

                is UiState.Success -> {
                    val filtered = vm.filteredEntries(state.entries)

                    // Loading overlay when fetching a file link
                    LibraryScreen(
                        entries = filtered,
                        searchQuery = searchQuery,
                        filterType = filterType,
                        onSearch = { vm.setSearchQuery(it) },
                        onFilterType = { vm.setFilterType(it) },
                        onRefresh = { vm.loadDownloads(bypassCache = true) },
                        onFileClick = { entry, file -> vm.openFileInVlc(context, entry, file) },
                        onLogout = { vm.logout() }
                    )

                    if (fileAction is FileAction.Loading) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                }
            }
        }
    }
}
