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

package com.isaakhanimann.journal.ui.tabs.journal.search

import com.isaakhanimann.journal.data.room.experiences.entities.TimedNote
import com.isaakhanimann.journal.data.room.experiences.relations.ExperienceWithIngestionsTimedNotesAndRatings

/**
 * Helper class for searching within timed notes
 */
object TimedNoteSearchHelper {
    
    /**
     * Filters experiences that have timed notes containing the search text
     * @param experiences List of experiences with timed notes to filter
     * @param searchText Text to search for in timed notes
     * @return List of experience IDs that have matching timed notes
     */
    fun getExperienceIdsWithMatchingTimedNotes(
        experiences: List<ExperienceWithIngestionsTimedNotesAndRatings>,
        searchText: String
    ): Set<Int> {
        if (searchText.isBlank()) {
            return emptySet()
        }
        
        return experiences
            .filter { experience ->
                experience.timedNotes.any { timedNote ->
                    timedNote.note.contains(searchText, ignoreCase = true)
                }
            }
            .map { it.experience.id }
            .toSet()
    }
    
    /**
     * Gets all timed notes from an experience that match the search text
     */
    fun filterTimedNotesBySearchText(
        timedNotes: List<TimedNote>,
        searchText: String
    ): List<TimedNote> {
        if (searchText.isBlank()) {
            return timedNotes
        }
        
        return timedNotes.filter { note ->
            note.note.contains(searchText, ignoreCase = true)
        }
    }
    
    /**
     * Checks if any timed note in a list contains the search text
     */
    fun hasMatchingTimedNote(
        timedNotes: List<TimedNote>,
        searchText: String
    ): Boolean {
        if (searchText.isBlank()) {
            return false
        }
        
        return timedNotes.any { note ->
            note.note.contains(searchText, ignoreCase = true)
        }
    }
}
