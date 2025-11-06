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

import com.isaakhanimann.journal.data.room.experiences.relations.ExperienceWithIngestionsCompanionsAndRatings

/**
 * Helper class for multi-substance search functionality
 */
object MultiSubstanceSearchHelper {
    
    /**
     * Filters experiences that contain all the specified substances
     * @param experiences List of experiences to filter
     * @param substances List of substance names to search for (all must be present)
     * @return Filtered list of experiences containing all specified substances
     */
    fun filterBySubstanceCombination(
        experiences: List<ExperienceWithIngestionsCompanionsAndRatings>,
        substances: List<String>
    ): List<ExperienceWithIngestionsCompanionsAndRatings> {
        if (substances.isEmpty()) {
            return experiences
        }
        
        return experiences.filter { experience ->
            val substancesInExperience = experience.ingestionsWithCompanions
                .mapNotNull { it.substanceCompanion?.substanceName }
                .distinct()
            
            // Check if all selected substances are present in this experience
            substances.all { selectedSubstance ->
                substancesInExperience.any { it.equals(selectedSubstance, ignoreCase = true) }
            }
        }
    }
    
    /**
     * Gets all unique substance names from a list of experiences
     */
    fun getAllUniqueSubstances(
        experiences: List<ExperienceWithIngestionsCompanionsAndRatings>
    ): List<String> {
        return experiences
            .flatMap { it.ingestionsWithCompanions }
            .mapNotNull { it.substanceCompanion?.substanceName }
            .distinct()
            .sorted()
    }
}
