package com.torboxvlc.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.torboxvlc.data.model.DownloadEntry
import com.torboxvlc.data.model.isPlayable
import com.torboxvlc.data.model.toReadableSize

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    entries: List<DownloadEntry>,
    searchQuery: String,
    filterType: DownloadEntry.EntryType?,
    onSearch: (String) -> Unit,
    onFilterType: (DownloadEntry.EntryType?) -> Unit,
    onRefresh: () -> Unit,
    onFileClick: (DownloadEntry, com.torboxvlc.data.model.TorrentFile) -> Unit,
    onLogout: () -> Unit
) {
    var expandedEntry by remember { mutableStateOf<Int?>(null) }
    var showMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("TorBox VLC") },
                actions = {
                    IconButton(onClick = onRefresh) {
                        Icon(Icons.Default.Refresh, "Aggiorna")
                    }
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.Default.MoreVert, "Menu")
                        }
                        DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                            DropdownMenuItem(
                                text = { Text("Logout") },
                                onClick = { showMenu = false; onLogout() },
                                leadingIcon = { Icon(Icons.Default.Logout, null) }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Search bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearch,
                placeholder = { Text("Cerca per nome…") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearch("") }) {
                            Icon(Icons.Default.Clear, "Cancella")
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            )

            // Filter chips
            Row(
                modifier = Modifier
                    .padding(horizontal = 12.dp)
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = filterType == null,
                    onClick = { onFilterType(null) },
                    label = { Text("Tutti") }
                )
                FilterChip(
                    selected = filterType == DownloadEntry.EntryType.TORRENT,
                    onClick = { onFilterType(DownloadEntry.EntryType.TORRENT) },
                    label = { Text("Torrent") }
                )
                FilterChip(
                    selected = filterType == DownloadEntry.EntryType.USENET,
                    onClick = { onFilterType(DownloadEntry.EntryType.USENET) },
                    label = { Text("Usenet") }
                )
                FilterChip(
                    selected = filterType == DownloadEntry.EntryType.WEBDL,
                    onClick = { onFilterType(DownloadEntry.EntryType.WEBDL) },
                    label = { Text("Web") }
                )
            }

            if (entries.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.VideoLibrary,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            "Nessun download trovato",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(entries, key = { "${it.entryType}-${it.id}" }) { entry ->
                        DownloadCard(
                            entry = entry,
                            expanded = expandedEntry == entry.id,
                            onToggle = {
                                expandedEntry = if (expandedEntry == entry.id) null else entry.id
                            },
                            onFileClick = { file -> onFileClick(entry, file) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DownloadCard(
    entry: DownloadEntry,
    expanded: Boolean,
    onToggle: () -> Unit,
    onFileClick: (com.torboxvlc.data.model.TorrentFile) -> Unit
) {
    val stateColor = when {
        entry.downloadState.contains("download", ignoreCase = true) &&
        entry.progress >= 1.0 -> MaterialTheme.colorScheme.tertiary
        entry.downloadState.contains("cached", ignoreCase = true) -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
            .clickable { onToggle() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Type badge
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Text(
                        text = when (entry.entryType) {
                            DownloadEntry.EntryType.TORRENT -> "T"
                            DownloadEntry.EntryType.USENET -> "U"
                            DownloadEntry.EntryType.WEBDL -> "W"
                        },
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Bold
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = entry.name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(2.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            entry.size.toReadableSize(),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "•",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            entry.downloadState.replace("_", " "),
                            style = MaterialTheme.typography.labelSmall,
                            color = stateColor
                        )
                    }
                }

                Icon(
                    if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Progress bar if downloading
            if (entry.progress in 0.01..0.99) {
                Spacer(Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { entry.progress.toFloat() },
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    "${"%.0f".format(entry.progress * 100)}%",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // File list when expanded
            if (expanded) {
                val files = entry.files
                if (files.isNullOrEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Nessun file disponibile",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    HorizontalDivider(Modifier.padding(vertical = 8.dp))
                    files.forEach { file ->
                        val playable = file.isPlayable()
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(enabled = playable) { if (playable) onFileClick(file) }
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                if (playable) Icons.Default.PlayCircleOutline else Icons.Default.InsertDriveFile,
                                contentDescription = null,
                                tint = if (playable) MaterialTheme.colorScheme.primary
                                       else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    file.shortName.ifBlank { file.name },
                                    style = MaterialTheme.typography.bodySmall,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    color = if (playable) MaterialTheme.colorScheme.onSurface
                                            else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    file.size.toReadableSize(),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (playable) {
                                Text(
                                    "▶ VLC",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
