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

package com.isaakhanimann.journal.data.room.experiences.dao

import androidx.room.*
import com.isaakhanimann.journal.data.room.experiences.entities.IngestionReminder
import kotlinx.coroutines.flow.Flow

@Dao
interface IngestionReminderDao {
    
    @Query("SELECT * FROM ingestion_reminders ORDER BY reminderTime ASC")
    fun getAllRemindersFlow(): Flow<List<IngestionReminder>>
    
    @Query("SELECT * FROM ingestion_reminders WHERE id = :id")
    suspend fun getReminderById(id: Int): IngestionReminder?
    
    @Query("SELECT * FROM ingestion_reminders WHERE isEnabled = 1 ORDER BY reminderTime ASC")
    fun getEnabledRemindersFlow(): Flow<List<IngestionReminder>>
    
    @Insert
    suspend fun insert(reminder: IngestionReminder): Long
    
    @Update
    suspend fun update(reminder: IngestionReminder)
    
    @Delete
    suspend fun delete(reminder: IngestionReminder)
    
    @Query("DELETE FROM ingestion_reminders WHERE id = :id")
    suspend fun deleteById(id: Int)
}
