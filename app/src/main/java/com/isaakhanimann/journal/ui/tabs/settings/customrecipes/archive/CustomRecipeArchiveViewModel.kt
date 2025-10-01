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

package com.isaakhanimann.journal.ui.tabs.settings.customrecipes.archive

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.isaakhanimann.journal.data.room.experiences.ExperienceRepository
import com.isaakhanimann.journal.data.room.experiences.entities.CustomUnit
import com.isaakhanimann.journal.data.room.experiences.relations.CustomRecipeWithSubcomponents
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class RecipesWithUnits(
    val recipes: List<CustomRecipeWithSubcomponents>,
    val customUnitsMap: Map<Int, CustomUnit>
)

@HiltViewModel
class CustomRecipeArchiveViewModel @Inject constructor(
    experienceRepository: ExperienceRepository,
) : ViewModel() {

    val recipesWithUnitsFlow: StateFlow<RecipesWithUnits> = combine(
        experienceRepository.getSortedCustomRecipesWithSubcomponentsFlow(isArchived = true),
        experienceRepository.getCustomUnitsFlow(false)
    ) { recipes, customUnits ->
        RecipesWithUnits(
            recipes = recipes,
            customUnitsMap = customUnits.associateBy { it.id }
        )
    }.stateIn(
        initialValue = RecipesWithUnits(emptyList(), emptyMap()),
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000)
    )
}