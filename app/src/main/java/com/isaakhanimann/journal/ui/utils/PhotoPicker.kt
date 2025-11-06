/*
 * Copyright (c) 2024. Isaak Hanimann.
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

package com.isaakhanimann.journal.ui.utils

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.content.FileProvider
import com.isaakhanimann.journal.R
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * Composable for photo selection (camera or gallery)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoPickerDialog(
    onDismiss: () -> Unit,
    onPhotoSelected: (Uri) -> Unit
) {
    val context = LocalContext.current
    var showCameraPermission by remember { mutableStateOf(false) }
    var showStoragePermission by remember { mutableStateOf(false) }
    var photoUri by remember { mutableStateOf<Uri?>(null) }

    // Camera launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && photoUri != null) {
            onPhotoSelected(photoUri!!)
            onDismiss()
        }
    }

    // Gallery launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            onPhotoSelected(it)
            onDismiss()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.select_photo_source)) },
        text = null,
        confirmButton = {
            TextButton(
                onClick = {
                    if (context.hasCameraPermission()) {
                        photoUri = createImageUri(context)
                        photoUri?.let { cameraLauncher.launch(it) }
                    } else {
                        showCameraPermission = true
                    }
                }
            ) {
                Icon(Icons.Default.CameraAlt, contentDescription = null)
                Text(stringResource(R.string.take_photo))
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    if (context.hasReadMediaImagesPermission()) {
                        galleryLauncher.launch("image/*")
                    } else {
                        showStoragePermission = true
                    }
                }
            ) {
                Icon(Icons.Default.Photo, contentDescription = null)
                Text(stringResource(R.string.choose_from_gallery))
            }
        }
    )

    if (showCameraPermission) {
        RequestCameraPermission(
            onPermissionGranted = {
                showCameraPermission = false
                photoUri = createImageUri(context)
                photoUri?.let { cameraLauncher.launch(it) }
            },
            onPermissionDenied = {
                showCameraPermission = false
            }
        )
    }

    if (showStoragePermission) {
        RequestReadMediaImagesPermission(
            onPermissionGranted = {
                showStoragePermission = false
                galleryLauncher.launch("image/*")
            },
            onPermissionDenied = {
                showStoragePermission = false
            }
        )
    }
}

/**
 * Create a temporary URI for camera capture
 */
private fun createImageUri(context: Context): Uri? {
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val imageFileName = "JPEG_${timeStamp}_"
    val storageDir = context.getExternalFilesDir("photos")
    
    storageDir?.mkdirs()
    
    return try {
        val imageFile = File.createTempFile(
            imageFileName,
            ".jpg",
            storageDir
        )
        FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            imageFile
        )
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

/**
 * Copy URI to app's internal storage
 */
fun Context.copyPhotoToInternalStorage(sourceUri: Uri): String? {
    return try {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "photo_${timeStamp}.jpg"
        val destDir = File(filesDir, "photos")
        destDir.mkdirs()
        val destFile = File(destDir, fileName)

        contentResolver.openInputStream(sourceUri)?.use { input ->
            destFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        
        destFile.absolutePath
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
