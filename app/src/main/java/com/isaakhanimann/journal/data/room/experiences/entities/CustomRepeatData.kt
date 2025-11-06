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
 * along with PsychonautWiki Journal. If not, see https://www.gnu.org/licenses/gpl-3.0.en.html.
 */

package com.isaakhanimann.journal.data.room.experiences.entities

import kotlinx.serialization.Serializable

@Serializable
data class CustomRepeatData(
    // Days of week (1=Monday, 7=Sunday)
    val daysOfWeek: Set<Int> = emptySet(),
    // Interval in days (e.g., every 2 days, every 3 days)
    val intervalDays: Int? = null,
    // End date for the reminder (optional)
    val endDate: String? = null, // ISO date string
    // Maximum number of occurrences (optional)
    val maxOccurrences: Int? = null
) {
    companion object {
        fun fromJson(json: String?): CustomRepeatData? {
            if (json.isNullOrBlank()) return null
            return try {
                kotlinx.serialization.json.Json.decodeFromString<CustomRepeatData>(json)
            } catch (e: Exception) {
                null
            }
        }
        
        fun toJson(data: CustomRepeatData?): String? {
            if (data == null) return null
            return try {
                kotlinx.serialization.json.Json.encodeToString(serializer(), data)
            } catch (e: Exception) {
                null
            }
        }
    }
    
    fun toDisplayString(): String {
        return when {
            daysOfWeek.isNotEmpty() -> {
                val dayNames = daysOfWeek.sorted().map { dayOfWeek ->
                    when (dayOfWeek) {
                        1 -> "Mon"
                        2 -> "Tue" 
                        3 -> "Wed"
                        4 -> "Thu"
                        5 -> "Fri"
                        6 -> "Sat"
                        7 -> "Sun"
                        else -> "?"
                    }
                }
                "Every ${dayNames.joinToString(", ")}"
            }
            intervalDays != null -> "Every $intervalDays days"
            else -> "Custom"
        }
    }
}
