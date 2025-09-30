package com.isaakhanimann.journal.ui.tabs.settings.customrecipes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.isaakhanimann.journal.data.room.experiences.ExperienceRepository
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

    val filteredCustomRecipesFlow: StateFlow<List<CustomRecipeWithSubcomponents>> = combine(
        customRecipesFlow,
        searchTextFlow
    ) { recipes, searchText ->
        if (searchText.isBlank()) {
            recipes
        } else {
            recipes.filter { recipeWithSubcomponents ->
                recipeWithSubcomponents.recipe.name.contains(searchText, ignoreCase = true) ||
                recipeWithSubcomponents.subcomponents.any { subcomponent ->
                    subcomponent.substanceName.contains(searchText, ignoreCase = true)
                }
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun onSearch(searchText: String) {
        _searchTextFlow.value = searchText
    }
}
