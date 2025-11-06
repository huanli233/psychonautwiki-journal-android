/*
 * Copyright (c) 2022-2023. Isaak Hanimann.
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

package com.isaakhanimann.journal.ui.tabs.settings

import android.app.Application
import android.net.Uri
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.isaakhanimann.journal.R
import com.isaakhanimann.journal.data.room.experiences.ExperienceRepository
import com.isaakhanimann.journal.data.substances.repositories.SubstanceRepository
import com.isaakhanimann.journal.ui.tabs.settings.combinations.UserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val application: Application,
    private val experienceRepository: ExperienceRepository,
    private val fileSystemConnection: FileSystemConnection,
    private val userPreferences: UserPreferences,
    private val imageExportHelper: ImageExportHelper
) : ViewModel() {
    
    // Progress tracking for import/export operations
    var isImporting by mutableStateOf(false)
        private set
    var isExporting by mutableStateOf(false)
        private set
    var importProgress by mutableStateOf(0f)
        private set
    var exportProgress by mutableStateOf(0f)
        private set
    var progressMessage by mutableStateOf("")
        private set
    
    // Error handling state
    var showErrorDialog by mutableStateOf(false)
        private set
    var errorTitle by mutableStateOf("")
        private set
    var errorMessage by mutableStateOf("")
        private set
    var errorDetails by mutableStateOf("")
        private set

    fun saveLanguage(language: String) = viewModelScope.launch {
        userPreferences.setLanguage(language)
    }

    fun saveDosageDotsAreHidden(value: Boolean) = viewModelScope.launch {
        userPreferences.saveDosageDotsAreHidden(value)
    }

    fun saveAreSubstanceHeightsIndependent(value: Boolean) = viewModelScope.launch {
        userPreferences.saveAreSubstanceHeightsIndependent(value)
    }

    fun saveIsTimelineHidden(value: Boolean) = viewModelScope.launch {
        userPreferences.saveIsTimelineHidden(value)
    }

    val languageFlow = userPreferences.languageFlow.stateIn(
        initialValue = "SYSTEM",
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000)
    )

    val isTimelineHiddenFlow = userPreferences.isTimelineHiddenFlow.stateIn(
        initialValue = false,
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000)
    )

    val areSubstanceHeightsIndependentFlow = userPreferences.areSubstanceHeightsIndependentFlow.stateIn(
        initialValue = false,
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000)
    )

    val areDosageDotsHiddenFlow = userPreferences.areDosageDotsHiddenFlow.stateIn(
        initialValue = false,
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000)
    )

    val snackbarHostState = SnackbarHostState()

    @OptIn(ExperimentalSerializationApi::class)
    fun importFile(uri: Uri) {
        viewModelScope.launch {
            isImporting = true
            importProgress = 0f
            progressMessage = "Reading file..."
            
            try {
                val text = fileSystemConnection.getTextFromUri(uri)
                if (text == null) {
                    snackbarHostState.showSnackbar(
                        message = application.getString(R.string.file_not_found),
                        duration = SnackbarDuration.Short
                    )
                    return@launch
                }
                
                importProgress = 0.1f
                progressMessage = "Parsing data..."
                
                val json = Json { ignoreUnknownKeys = true }
                val journalExport = json.decodeFromString<JournalExport>(text)
                
                importProgress = 0.2f
                progressMessage = "Clearing existing data..."
                
                experienceRepository.deleteEverything()
                
                importProgress = 0.3f
                progressMessage = "Importing experiences..."
                
                // Import with progress tracking
                experienceRepository.insertEverythingWithProgress(
                    journalExport = journalExport,
                    imageExportHelper = imageExportHelper,
                    onProgressUpdate = { progress, message ->
                        importProgress = 0.3f + (progress * 0.7f) // Scale to 30-100%
                        progressMessage = message
                    }
                )
                
                importProgress = 1f
                progressMessage = "Import completed!"
                
                snackbarHostState.showSnackbar(
                    message = application.getString(R.string.import_successful),
                    duration = SnackbarDuration.Short
                )
            } catch (e: Throwable) {
                val errorDetails = when (e) {
                    is java.lang.OutOfMemoryError -> {
                        "Out of memory - file too large: ${e.message}"
                    }
                    is kotlinx.serialization.MissingFieldException -> {
                        "Missing required field: ${e.message}"
                    }
                    is kotlinx.serialization.SerializationException -> {
                        "JSON parsing error: ${e.message}"
                    }
                    is java.io.IOException -> {
                        "File reading error: ${e.message}"
                    }
                    is android.database.sqlite.SQLiteConstraintException -> {
                        "Database constraint error: ${e.message}"
                    }
                    else -> {
                        "Unknown error: ${e.javaClass.simpleName} - ${e.message}"
                    }
                }
                
                println("Error when importing: $errorDetails")
                println("Full stack trace: ${e.stackTraceToString()}")
                
                // Show user-friendly error with option to copy details
                showImportError("Import Failed", errorDetails)
            } finally {
                isImporting = false
                importProgress = 0f
                progressMessage = ""
            }
        }
    }

    fun exportFile(uri: Uri) {
        viewModelScope.launch {
            isExporting = true
            exportProgress = 0f
            progressMessage = "Loading data..."
            
            try {
                val experiencesWithIngestionsAndRatings =
                    experienceRepository.getAllExperiencesWithIngestionsTimedNotesAndRatingsSorted()
                
                exportProgress = 0.1f
                progressMessage = "Processing experiences..."
                
                val totalExperiences = experiencesWithIngestionsAndRatings.size
                val experiencesSerializable = experiencesWithIngestionsAndRatings.mapIndexed { index, it ->
                    val location = it.experience.location
                    
                    // Update progress for experiences
                    exportProgress = 0.1f + (index.toFloat() / totalExperiences * 0.3f)
                    progressMessage = "Processing experience ${index + 1}/$totalExperiences..."
                    
                    // Process timed notes with photos
                    val timedNotesWithPhotos = experienceRepository.getTimedNotesWithPhotos(it.experience.id)
                    val totalPhotos = timedNotesWithPhotos.sumOf { timedNote -> timedNote.photos.size }
                    var processedPhotos = 0
                    
                    val timedNotesSerializable = timedNotesWithPhotos.map { timedNote ->
                        val photosSerializable = timedNote.photos.map { photo ->
                            processedPhotos++
                            if (totalPhotos > 0) {
                                val photoProgress = processedPhotos.toFloat() / totalPhotos
                                progressMessage = "Processing images... ($processedPhotos/$totalPhotos)"
                            }
                            
                            val imageBase64 = imageExportHelper.imageToBase64(photo.filePath) ?: ""
                            TimedNotePhotoSerializable(
                                id = photo.id,
                                imageBase64 = imageBase64,
                                creationDate = photo.creationDate,
                                caption = photo.caption,
                                originalFileName = photo.filePath.substringAfterLast('/')
                            )
                        }
                        
                        TimedNoteSerializable(
                            creationDate = timedNote.timedNote.creationDate,
                            time = timedNote.timedNote.time,
                            note = timedNote.timedNote.note,
                            color = timedNote.timedNote.color,
                            customColor = timedNote.timedNote.customColor,
                            isPartOfTimeline = timedNote.timedNote.isPartOfTimeline,
                            photos = photosSerializable
                        )
                    }
                    
                    ExperienceSerializable(
                        title = it.experience.title,
                        text = it.experience.text,
                        creationDate = it.experience.creationDate,
                        sortDate = it.experience.sortDate,
                        isFavorite = it.experience.isFavorite,
                        ingestions = it.ingestions.map { ingestion ->
                            IngestionSerializable(
                                substanceName = ingestion.substanceName,
                                time = ingestion.time,
                                endTime = ingestion.endTime,
                                creationDate = ingestion.creationDate,
                                administrationRoute = ingestion.administrationRoute,
                                dose = ingestion.dose,
                                estimatedDoseStandardDeviation = ingestion.estimatedDoseStandardDeviation,
                                isDoseAnEstimate = ingestion.isDoseAnEstimate,
                                units = ingestion.units,
                                notes = ingestion.notes,
                                stomachFullness = ingestion.stomachFullness,
                                consumerName = ingestion.consumerName,
                                customUnitId = ingestion.customUnitId,
                                customRecipeId = ingestion.customRecipeId,
                                recipeGroupId = ingestion.recipeGroupId
                            )
                        },
                        location = if (location != null) {
                            LocationSerializable(
                                name = location.name,
                                latitude = location.latitude,
                                longitude = location.longitude
                            )
                        } else {
                            null
                        },
                        ratings = it.ratings.map { rating ->
                            RatingSerializable(
                                option = rating.option,
                                time = rating.time,
                                creationDate = rating.creationDate
                            )
                        },
                        timedNotes = timedNotesSerializable
                    )
                }
                
                exportProgress = 0.5f
                progressMessage = "Loading custom units..."
                
                val customUnitsSerializable = experienceRepository.getAllCustomUnitsSorted().map {
                CustomUnitSerializable(
                    id = it.id,
                    substanceName = it.substanceName,
                    name = it.name,
                    creationDate = it.creationDate,
                    administrationRoute = it.administrationRoute,
                    dose = it.dose,
                    estimatedDoseStandardDeviation = it.estimatedDoseStandardDeviation,
                    isEstimate = it.isEstimate,
                    isArchived = it.isArchived,
                    unit = it.unit,
                    unitPlural = it.unitPlural,
                    originalUnit = it.originalUnit,
                    note = it.note
                )
                }
                
                exportProgress = 0.6f
                progressMessage = "Loading custom recipes..."
                
                val customRecipesSerializable = experienceRepository.getAllCustomRecipesWithSubcomponentsSorted().map {
                    CustomRecipeSerializable(
                        id = it.recipe.id,
                        name = it.recipe.name,
                        creationDate = it.recipe.creationDate,
                        administrationRoute = it.recipe.administrationRoute,
                        unit = it.recipe.unit,
                        unitPlural = it.recipe.unitPlural,
                        note = it.recipe.note,
                        isArchived = it.recipe.isArchived,
                        subcomponents = it.subcomponents.map { subcomponent ->
                            RecipeSubcomponentSerializable(
                                id = subcomponent.id,
                                substanceName = subcomponent.substanceName,
                                customUnitId = subcomponent.customUnitId,
                                dose = subcomponent.dose,
                                estimatedDoseStandardDeviation = subcomponent.estimatedDoseStandardDeviation,
                                isEstimate = subcomponent.isEstimate,
                                originalUnit = subcomponent.originalUnit
                            )
                        }
                    )
                }
                
                exportProgress = 0.7f
                progressMessage = "Loading reminders..."
                
                val remindersSerializable = experienceRepository.getAllReminders().map { reminder ->
                    IngestionReminderSerializable(
                        id = reminder.id,
                        substanceName = reminder.substanceName,
                        reminderTime = reminder.reminderTime,
                        repeatMode = reminder.repeatMode,
                        dose = reminder.dose,
                        units = reminder.units,
                        note = reminder.note,
                        isEnabled = reminder.isEnabled,
                        createdAt = reminder.createdAt,
                        customRepeatData = reminder.customRepeatData
                    )
                }
                
                exportProgress = 0.8f
                progressMessage = "Creating export file..."
                
                val journalExport = JournalExport(
                    experiences = experiencesSerializable,
                    substanceCompanions = experienceRepository.getAllSubstanceCompanions(),
                    customSubstances = experienceRepository.getAllCustomSubstances(),
                    customUnits = customUnitsSerializable,
                    customRecipes = customRecipesSerializable,
                    ingestionReminders = remindersSerializable
                )
                
                exportProgress = 0.9f
                progressMessage = "Saving file..."
                
                val jsonList = Json.encodeToString(journalExport)
                fileSystemConnection.saveTextInUri(uri, text = jsonList)
                
                exportProgress = 1f
                progressMessage = "Export completed!"
                
                snackbarHostState.showSnackbar(
                    message = application.getString(R.string.export_successful),
                    duration = SnackbarDuration.Short
                )
            } catch (e: Exception) {
                val errorDetails = when (e) {
                    is kotlinx.serialization.SerializationException -> {
                        "JSON serialization error: ${e.message}"
                    }
                    is java.io.IOException -> {
                        "File writing error: ${e.message}"
                    }
                    is java.lang.OutOfMemoryError -> {
                        "Out of memory - too much data to export: ${e.message}"
                    }
                    is SecurityException -> {
                        "Permission denied - cannot write to selected location: ${e.message}"
                    }
                    else -> {
                        "Unknown error: ${e.javaClass.simpleName} - ${e.message}"
                    }
                }
                
                println("Error when exporting: $errorDetails")
                println("Full stack trace: ${e.stackTraceToString()}")
                
                // Show user-friendly error with option to copy details
                showImportError("Export Failed", errorDetails)
            } finally {
                isExporting = false
                exportProgress = 0f
                progressMessage = ""
            }
        }
    }

    fun deleteEverything() {
        viewModelScope.launch {
            experienceRepository.deleteEverything()
        }
    }
    
    private fun showImportError(title: String, details: String) {
        errorTitle = title
        errorMessage = when {
            details.contains("JSON parsing error") -> "The selected file is not a valid journal export or is corrupted."
            details.contains("Missing required field") -> "The file format is outdated or incomplete. Please export from a newer version."
            details.contains("File reading error") -> "Cannot read the selected file. Please check if the file exists and is accessible."
            details.contains("Database constraint error") -> "Data conflict detected. This usually happens when importing duplicate data."
            details.contains("Out of memory") -> "The file is too large to import. Please try with a smaller export file."
            details.contains("Permission denied") -> "Cannot access the selected location. Please choose a different file or location."
            else -> "An unexpected error occurred during the operation."
        }
        errorDetails = details
        showErrorDialog = true
    }
    
    fun dismissErrorDialog() {
        showErrorDialog = false
        errorTitle = ""
        errorMessage = ""
        errorDetails = ""
    }
    
    fun copyErrorToClipboard() {
        val fullError = """
            Error: $errorTitle
            
            Description: $errorMessage
            
            Technical Details:
            $errorDetails
            
            Please include this information when reporting the issue.
        """.trimIndent()
        
        // This would need to be handled in the UI layer with ClipboardManager
        // For now, we'll just log it for easy copying from logs
        println("=== ERROR REPORT FOR COPY ===")
        println(fullError)
        println("=== END ERROR REPORT ===")
    }
}