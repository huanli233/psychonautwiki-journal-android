/*
 * Copyright (c) 2023. Isaak Hanimann.
 * This file is part of PsychonautWiki Journal.
 *
 * PsychonautWiki Journal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version.
 *
 * PsychonautWiki Journal is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with PsychonautWiki Journal.  If not, see https://www.gnu.org/licenses/gpl-3.0.en.html.
 */

package com.isaakhanimann.journal.ui.tabs.journal.experience.timednote.edit

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.isaakhanimann.journal.data.room.experiences.ExperienceRepository
import com.isaakhanimann.journal.data.room.experiences.entities.AdaptiveColor
import com.isaakhanimann.journal.data.room.experiences.entities.SubstanceColor
import com.isaakhanimann.journal.data.room.experiences.entities.TimedNote
import com.isaakhanimann.journal.data.room.experiences.entities.TimedNotePhoto
import com.isaakhanimann.journal.data.room.experiences.entities.getSubstanceColor
import com.isaakhanimann.journal.ui.main.navigation.graphs.EditTimedNoteRoute
import com.isaakhanimann.journal.ui.utils.getInstant
import com.isaakhanimann.journal.ui.utils.getLocalDateTime
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class EditTimedNoteViewModel @Inject constructor(
    private val experienceRepo: ExperienceRepository,
    state: SavedStateHandle
) : ViewModel() {
    var note by mutableStateOf("")
    var color by mutableStateOf<SubstanceColor>(SubstanceColor.Predefined(AdaptiveColor.BLUE))
    var isPartOfTimeline by mutableStateOf(true)
    
    // Photo support
    val selectedPhotoFilePaths = mutableStateListOf<String>()
    val maxPhotos = 10
    var localDateTimeFlow = MutableStateFlow(LocalDateTime.now())
    private var timedNote: TimedNote? = null
    private val editTimedNoteRoute = state.toRoute<EditTimedNoteRoute>()
    val experienceId = editTimedNoteRoute.experienceId

    private val timedNoteId: Int

    init {
        val timedNoteId = editTimedNoteRoute.timedNoteId
        this.timedNoteId = timedNoteId
        viewModelScope.launch {
            val loadedNote = experienceRepo.getTimedNote(id = timedNoteId) ?: return@launch
            timedNote = loadedNote
            loadedNote.time.let { time ->
                localDateTimeFlow.emit(time.getLocalDateTime())
            }
            note = loadedNote.note
            color = loadedNote.customColor?.let { SubstanceColor.Custom(it) }
                ?: loadedNote.color?.let { SubstanceColor.Predefined(it) }
                        ?: SubstanceColor.Predefined(AdaptiveColor.BLUE)
            isPartOfTimeline = loadedNote.isPartOfTimeline

            // Load existing photos
            experienceRepo.getPhotosForTimedNoteFlow(timedNoteId).collect { photos ->
                selectedPhotoFilePaths.clear()
                selectedPhotoFilePaths.addAll(photos.map { it.filePath })
            }
        }
    }


    fun onChangeTime(newLocalDateTime: LocalDateTime) {
        viewModelScope.launch {
            localDateTimeFlow.emit(newLocalDateTime)
        }
    }

    fun onChangeIsPartOfTimeline(newIsPartOfTimeline: Boolean) {
        isPartOfTimeline = newIsPartOfTimeline
    }

    fun onChangeNote(newNote: String) {
        note = newNote
    }

    fun onChangeColor(newColor: SubstanceColor) {
        color = newColor
    }
    
    fun addPhotoFilePath(filePath: String) {
        if (selectedPhotoFilePaths.size < maxPhotos) {
            selectedPhotoFilePaths.add(filePath)
        }
    }
    
    fun removePhotoFilePath(filePath: String) {
        selectedPhotoFilePaths.remove(filePath)
    }

    private val ingestionsFlow = experienceRepo.getIngestionsWithCompanionsFlow(experienceId)
    private val timedNotesFlow = experienceRepo.getTimedNotesFlowSorted(experienceId)

    val alreadyUsedColorsFlow: StateFlow<List<AdaptiveColor>> =
        ingestionsFlow.combine(timedNotesFlow) { ingestions, notes ->
            val companionSubstanceColors = ingestions.mapNotNull { it.substanceCompanion?.getSubstanceColor() }
            val noteSubstanceColors = notes.map { timedNote ->
                timedNote.customColor?.let { SubstanceColor.Custom(it) }
                    ?: timedNote.color?.let { SubstanceColor.Predefined(it) }
                    ?: SubstanceColor.Predefined(AdaptiveColor.BLUE)
            }
            return@combine (companionSubstanceColors + noteSubstanceColors)
                .mapNotNull { (it as? SubstanceColor.Predefined)?.color }
                .distinct()
        }.stateIn(
            initialValue = emptyList(),
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000)
        )

    val otherColorsFlow: StateFlow<List<AdaptiveColor>> =
        alreadyUsedColorsFlow.map { alreadyUsedColors ->
            AdaptiveColor.entries.filter {
                !alreadyUsedColors.contains(it)
            }
        }.stateIn(
            initialValue = emptyList(),
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000)
        )

    fun delete() {
        viewModelScope.launch {
            timedNote?.let {
                experienceRepo.delete(it)
            }
        }
    }

    fun onDoneTap() {
        viewModelScope.launch {
            val selectedInstant = localDateTimeFlow.firstOrNull()?.getInstant() ?: return@launch
            timedNote?.let {
                val (adaptiveColor, customColor) = when (val c = color) {
                    is SubstanceColor.Predefined -> Pair(c.color, null)
                    is SubstanceColor.Custom -> Pair(null, c.value)
                }
                it.time = selectedInstant
                it.note = note
                it.color = adaptiveColor
                it.customColor = customColor
                it.isPartOfTimeline = isPartOfTimeline
                experienceRepo.update(it)

                // Handle photos
                // First, get existing photos
                val existingPhotos = experienceRepo.getPhotosForTimedNoteFlow(timedNoteId).firstOrNull() ?: emptyList()
                val existingPaths = existingPhotos.map { it.filePath }.toSet()
                val currentPaths = selectedPhotoFilePaths.toSet()

                // Delete removed photos
                val photosToDelete = existingPhotos.filter { !currentPaths.contains(it.filePath) }
                photosToDelete.forEach { photo ->
                    experienceRepo.delete(photo)
                }

                // Add new photos
                val newPaths = currentPaths - existingPaths
                newPaths.forEach { filePath ->
                    val photo = TimedNotePhoto(
                        timedNoteId = timedNoteId,
                        filePath = filePath,
                        creationDate = Instant.now()
                    )
                    experienceRepo.insert(photo)
                }
            }
        }
    }
}
