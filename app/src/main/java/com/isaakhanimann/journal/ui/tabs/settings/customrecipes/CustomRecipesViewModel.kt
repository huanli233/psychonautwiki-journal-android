package com.isaakhanimann.journal.ui.tabs.settings.customrecipes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.isaakhanimann.journal.data.room.experiences.ExperienceRepository
import com.isaakhanimann.journal.data.room.experiences.entities.CustomUnit
import com.isaakhanimann.journal.data.room.experiences.relations.CustomRecipeWithSubcomponents
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class CustomRecipesViewModel @Inject constructor(
    private val experienceRepository: ExperienceRepository
) : ViewModel() {

    private val _searchTextFlow = MutableStateFlow("")
    val searchTextFlow: StateFlow<String> = _searchTextFlow

    private val customRecipesFlow = experienceRepository.getSortedCustomRecipesWithSubcomponentsFlow(isArchived = false)

    val customUnitsFlow = experienceRepository.getCustomUnitsFlow(isArchived = false)

    data class RecipesWithUnits(
        val recipes: List<CustomRecipeWithSubcomponents>,
        val customUnitsMap: Map<Int, CustomUnit>
    )

    val filteredRecipesWithUnitsFlow: StateFlow<RecipesWithUnits> = combine(
        customRecipesFlow,
        customUnitsFlow,
        searchTextFlow
    ) { recipes, customUnits, searchText ->
        val customUnitsMap = customUnits.associateBy { it.id }

        val filtered = if (searchText.isBlank()) {
            recipes
        } else {
            recipes.filter { recipeWithSubcomponents ->
                val matchesRecipeName = recipeWithSubcomponents.recipe.name.contains(searchText, ignoreCase = true)
                val matchesRecipeNote = recipeWithSubcomponents.recipe.note.contains(searchText, ignoreCase = true)

                val matchesSubstance = recipeWithSubcomponents.subcomponents.any { subcomponent ->
                    val substanceName = subcomponent.customUnitId?.let { unitId ->
                        customUnitsMap[unitId]?.substanceName
                    } ?: subcomponent.substanceName

                    val customUnitName = subcomponent.customUnitId?.let { unitId ->
                        customUnitsMap[unitId]?.name
                    }

                    substanceName?.contains(searchText, ignoreCase = true) == true ||
                            customUnitName?.contains(searchText, ignoreCase = true) == true
                }

                matchesRecipeName || matchesRecipeNote || matchesSubstance
            }
        }

        RecipesWithUnits(filtered, customUnitsMap)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = RecipesWithUnits(emptyList(), emptyMap())
    )

    fun onSearch(searchText: String) {
        _searchTextFlow.value = searchText
    }
}