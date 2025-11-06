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

package com.isaakhanimann.journal.ui.tabs.journal.experience.timednote.add

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.isaakhanimann.journal.R
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import android.widget.Toast
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import com.isaakhanimann.journal.ui.tabs.journal.experience.timednote.TimedNoteScreenContent
import com.isaakhanimann.journal.ui.utils.PhotoPickerDialog
import com.isaakhanimann.journal.ui.utils.copyPhotoToInternalStorage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTimedNoteScreen(
    viewModel: AddTimedNoteViewModel = hiltViewModel(),
    navigateBack: () -> Unit
) {
    val context = LocalContext.current
    var showPhotoPicker by remember { mutableStateOf(false) }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add timed note") },
                actions = {
                    IconButton(onClick = {
                        viewModel.onDoneTap()
                        navigateBack()
                    }) {
                        Icon(
                            Icons.Filled.Done,
                            contentDescription = "Done icon"
                        )
                    }
                }
            )
        }
    ) { padding ->
        TimedNoteScreenContent(
            selectedTime = viewModel.localDateTimeFlow.collectAsState().value,
            onTimeChange = viewModel::onChangeTime,
            note = viewModel.note,
            onNoteChange = viewModel::onChangeNote,
            color = viewModel.color,
            onColorChange = viewModel::onChangeColor,
            modifier = Modifier.padding(padding),
            alreadyUsedColors = viewModel.alreadyUsedColors,
            otherColors = viewModel.otherColors,
            isPartOfTimeline = viewModel.isPartOfTimeline,
            onChangeOfIsPartOfTimeline = viewModel::onChangeIsPartOfTimeline,
            shouldFocusTextFieldOnAppear = true,
            onAddPhotoClick = { showPhotoPicker = true },
            selectedPhotos = viewModel.selectedPhotoFilePaths,
            onRemovePhoto = viewModel::removePhotoFilePath
        )
    }
    
    if (showPhotoPicker) {
        PhotoPickerDialog(
            onDismiss = { showPhotoPicker = false },
            onPhotoSelected = { uri ->
                val filePath = context.copyPhotoToInternalStorage(uri)
                if (filePath != null) {
                    viewModel.addPhotoFilePath(filePath)
                    Toast.makeText(context, context.getString(R.string.photo_added), Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, context.getString(R.string.error_saving_photo), Toast.LENGTH_SHORT).show()
                }
            }
        )
    }
}