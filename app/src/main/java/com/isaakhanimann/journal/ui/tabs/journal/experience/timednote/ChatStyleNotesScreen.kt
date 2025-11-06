/*
 * Copyright (c) 2024. Isaak Hanimann.
 * This file is part of PsychonautWiki Journal.
 */

package com.isaakhanimann.journal.ui.tabs.journal.experience.timednote

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.isaakhanimann.journal.R
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatStyleNotesScreen(
    experienceId: Int,
    viewModel: ChatStyleNotesViewModel = hiltViewModel(),
    navigateBack: () -> Unit
) {
    val timedNotes = viewModel.timedNotesWithPhotos.collectAsState().value
    var selectedPhotoPath by remember { mutableStateOf<String?>(null) }

    val listState = rememberLazyListState()
    
    // Auto-scroll to bottom when new notes are added
    LaunchedEffect(timedNotes.size) {
        if (timedNotes.isNotEmpty()) {
            listState.animateScrollToItem(timedNotes.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    MaterialTheme.colorScheme.primaryContainer,
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Column {
                            Text(
                                text = stringResource(R.string.chat_style_notes),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            if (timedNotes.isNotEmpty()) {
                                Text(
                                    text = "${timedNotes.size} ${if (timedNotes.size == 1) "note" else "notes"}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = navigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
    ) { paddingValues ->
        if (timedNotes.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        Icons.Default.Schedule,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = stringResource(R.string.no_matching_timed_notes),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Start adding timed notes to see them here",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(timedNotes.size) { index ->
                    val noteWithPhotos = timedNotes[index]
                    val isFirstOfDay = index == 0 || !isSameDay(
                        timedNotes[index - 1].timedNote.time,
                        noteWithPhotos.timedNote.time
                    )
                    
                    Column {
                        // Date separator
                        if (isFirstOfDay) {
                            DateSeparator(noteWithPhotos.timedNote.time)
                        }
                        
                        // Chat message
                        ChatMessage(
                            noteWithPhotos = noteWithPhotos,
                            onPhotoClick = { selectedPhotoPath = it },
                            showTime = shouldShowTime(timedNotes, index)
                        )
                    }
                }
            }
        }
    }
    
    // Full screen photo dialog
    selectedPhotoPath?.let { photoPath ->
        FullScreenPhotoDialog(
            photoPath = photoPath,
            onDismiss = { selectedPhotoPath = null }
        )
    }
}

@Composable
private fun DateSeparator(time: java.time.Instant) {
    val dateFormatter = DateTimeFormatter.ofPattern("EEEE, MMMM d")
    val dateText = time.atZone(java.time.ZoneId.systemDefault()).format(dateFormatter)
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.outlineVariant
        )
        Surface(
            modifier = Modifier.padding(horizontal = 16.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                text = dateText,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
        }
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.outlineVariant
        )
    }
}

@Composable
private fun ChatMessage(
    noteWithPhotos: com.isaakhanimann.journal.data.room.experiences.relations.TimedNoteWithPhotos,
    onPhotoClick: (String) -> Unit,
    showTime: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End
    ) {
        Spacer(modifier = Modifier.width(48.dp))
        
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.End
        ) {
            Surface(
                shape = RoundedCornerShape(
                    topStart = 20.dp,
                    topEnd = 4.dp,
                    bottomStart = 20.dp,
                    bottomEnd = 20.dp
                ),
                color = MaterialTheme.colorScheme.primaryContainer,
                shadowElevation = 1.dp
            ) {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    // Show note text if not empty
                    if (noteWithPhotos.timedNote.note.isNotBlank()) {
                        Text(
                            text = noteWithPhotos.timedNote.note,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        
                        if (noteWithPhotos.photos.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                    
                    // Show photos if any
                    if (noteWithPhotos.photos.isNotEmpty()) {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            items(noteWithPhotos.photos.size) { index ->
                                val photo = noteWithPhotos.photos[index]
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    shadowElevation = 2.dp
                                ) {
                                    AsyncImage(
                                        model = java.io.File(photo.filePath),
                                        contentDescription = "Photo ${index + 1}",
                                        modifier = Modifier
                                            .size(120.dp)
                                            .clickable { onPhotoClick(photo.filePath) },
                                        contentScale = ContentScale.Crop
                                    )
                                }
                            }
                        }
                        
                        // Show placeholder for photo-only notes
                        if (noteWithPhotos.timedNote.note.isBlank()) {
                            Text(
                                text = "📷 Photo",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            }
            
            // Show timestamp
            if (showTime) {
                Text(
                    text = noteWithPhotos.timedNote.time.atZone(java.time.ZoneId.systemDefault())
                        .format(DateTimeFormatter.ofPattern("HH:mm")),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp, end = 4.dp)
                )
            }
        }
    }
}

private fun isSameDay(time1: java.time.Instant, time2: java.time.Instant): Boolean {
    val date1 = time1.atZone(java.time.ZoneId.systemDefault()).toLocalDate()
    val date2 = time2.atZone(java.time.ZoneId.systemDefault()).toLocalDate()
    return date1 == date2
}

private fun shouldShowTime(
    timedNotes: List<com.isaakhanimann.journal.data.room.experiences.relations.TimedNoteWithPhotos>,
    index: Int
): Boolean {
    if (index == timedNotes.size - 1) return true // Always show time for last message
    
    val currentTime = timedNotes[index].timedNote.time
    val nextTime = timedNotes[index + 1].timedNote.time
    
    // Show time if next message is more than 5 minutes later
    return ChronoUnit.MINUTES.between(currentTime, nextTime) > 5
}

@Composable
private fun FullScreenPhotoDialog(
    photoPath: String,
    onDismiss: () -> Unit
) {
    androidx.compose.ui.window.Dialog(
        onDismissRequest = onDismiss
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color.Black.copy(alpha = 0.9f)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable { onDismiss() },
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = java.io.File(photoPath),
                    contentDescription = "Full screen photo",
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentScale = ContentScale.Fit
                )
            }
        }
    }
}