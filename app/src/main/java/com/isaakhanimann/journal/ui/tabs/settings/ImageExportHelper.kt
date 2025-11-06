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

package com.isaakhanimann.journal.ui.tabs.settings

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ImageExportHelper @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    
    /**
     * Convert image file to base64 string
     */
    suspend fun imageToBase64(filePath: String): String? = withContext(Dispatchers.IO) {
        try {
            val file = File(filePath)
            if (!file.exists()) return@withContext null
            
            val bitmap = BitmapFactory.decodeFile(filePath)
            val byteArrayOutputStream = ByteArrayOutputStream()
            
            // Compress to JPEG with 85% quality to balance size and quality
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, byteArrayOutputStream)
            val byteArray = byteArrayOutputStream.toByteArray()
            
            Base64.encodeToString(byteArray, Base64.DEFAULT)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    /**
     * Convert base64 string to image file
     */
    suspend fun base64ToImage(
        base64String: String,
        fileName: String,
        directory: File
    ): String? = withContext(Dispatchers.IO) {
        try {
            if (!directory.exists()) {
                directory.mkdirs()
            }
            
            val decodedBytes = Base64.decode(base64String, Base64.DEFAULT)
            val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
            
            val file = File(directory, fileName)
            val fileOutputStream = FileOutputStream(file)
            
            // Save as JPEG
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, fileOutputStream)
            fileOutputStream.close()
            
            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    /**
     * Get the app's internal images directory
     */
    fun getImagesDirectory(): File {
        return File(context.filesDir, "images")
    }
    
    /**
     * Generate a unique filename for imported images
     */
    fun generateUniqueFileName(originalFileName: String?): String {
        val timestamp = System.currentTimeMillis()
        val extension = originalFileName?.substringAfterLast('.', "jpg") ?: "jpg"
        return "imported_${timestamp}.$extension"
    }
    
    /**
     * Calculate progress for image processing
     */
    fun calculateImageProgress(current: Int, total: Int): Float {
        return if (total > 0) current.toFloat() / total.toFloat() else 0f
    }
}
