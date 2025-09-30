package com.isaakhanimann.journal.ui.tabs.settings.customrecipes.edit

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.isaakhanimann.journal.data.room.experiences.ExperienceRepository
import com.isaakhanimann.journal.data.room.experiences.entities.CustomRecipe
import com.isaakhanimann.journal.data.room.experiences.entities.RecipeSubcomponent
import com.isaakhanimann.journal.data.substances.AdministrationRoute
import com.isaakhanimann.journal.data.substances.repositories.SearchRepository
import com.isaakhanimann.journal.di.RecipeResultHolder
import com.isaakhanimann.journal.ui.main.navigation.graphs.EditCustomRecipeRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditCustomRecipeViewModel @Inject constructor(
    private val experienceRepository: ExperienceRepository,
    savedStateHandle: SavedStateHandle,
    searchRepository: SearchRepository,
    private val resultHolder: RecipeResultHolder,
) : ViewModel() {

    private val recipeId: Int = savedStateHandle.toRoute<EditCustomRecipeRoute>().customRecipeId

    var recipeName by mutableStateOf("")
    var administrationRoute by mutableStateOf(AdministrationRoute.ORAL)
    var unit by mutableStateOf("")
    var unitPlural by mutableStateOf("")
    var note by mutableStateOf("")
    var subcomponents by mutableStateOf(listOf<SubcomponentData>())
    var isArchived by mutableStateOf(false)

    var isLoading by mutableStateOf(false)
    var isDeleting by mutableStateOf(false)

    val allSubstances: StateFlow<List<String>> = combine(
        searchRepository.getMatchingSubstancesFlow(""),
        experienceRepository.getCustomSubstancesFlow()
    ) { predefined, custom ->
        val predefinedNames = predefined.map { it.substance.name }
        val customNames = custom.map { it.name }
        (predefinedNames + customNames).distinct().sorted()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    data class SubcomponentData(
        val id: Int = 0,
        val substanceName: String = "",
        val dose: String = "",
        val estimatedDoseStandardDeviation: String = "",
        val isEstimate: Boolean = false,
        val originalUnit: String = ""
    )

    init {
        loadRecipe()
        viewModelScope.launch {
            resultHolder.resultFlow
                .collect {
                    it?.let { (index, name) ->
                        val updatedSubcomponent = subcomponents[index].copy(substanceName = name)
                        updateSubcomponent(index, updatedSubcomponent)
                        resultHolder.clearResult()
                    }
                }
        }
    }

    private fun loadRecipe() {
        viewModelScope.launch {
            experienceRepository.getCustomRecipeWithSubcomponents(recipeId)?.let { recipeWithSubcomponents ->
                val recipe = recipeWithSubcomponents.recipe
                recipeName = recipe.name
                administrationRoute = recipe.administrationRoute
                unit = recipe.unit
                unitPlural = recipe.unitPlural ?: ""
                note = recipe.note
                isArchived = recipe.isArchived

                subcomponents = recipeWithSubcomponents.subcomponents.map { subcomponent ->
                    SubcomponentData(
                        id = subcomponent.id,
                        substanceName = subcomponent.substanceName,
                        dose = subcomponent.dose?.toString() ?: "",
                        estimatedDoseStandardDeviation = subcomponent.estimatedDoseStandardDeviation?.toString() ?: "",
                        isEstimate = subcomponent.isEstimate,
                        originalUnit = subcomponent.originalUnit
                    )
                }
            }
        }
    }

    fun updateRecipeName(name: String) {
        recipeName = name
    }

    fun updateAdministrationRoute(route: AdministrationRoute) {
        administrationRoute = route
    }

    fun updateUnit(newUnit: String) {
        unit = newUnit
    }

    fun updateUnitPlural(newUnitPlural: String) {
        unitPlural = newUnitPlural
    }

    fun updateNote(newNote: String) {
        note = newNote
    }

    fun addSubcomponent() {
        subcomponents = subcomponents + SubcomponentData()
    }

    fun removeSubcomponent(index: Int) {
        subcomponents = subcomponents.filterIndexed { i, _ -> i != index }
    }

    fun updateSubcomponent(index: Int, updatedSubcomponent: SubcomponentData) {
        subcomponents = subcomponents.mapIndexed { i, subcomponent ->
            if (i == index) updatedSubcomponent else subcomponent
        }
    }

    fun saveRecipe(onSuccess: () -> Unit) {
        if (!isValid()) {
            return
        }

        isLoading = true
        viewModelScope.launch {
            try {
                val recipe = CustomRecipe(
                    id = recipeId,
                    name = recipeName,
                    administrationRoute = administrationRoute,
                    isArchived = isArchived,
                    unit = unit,
                    unitPlural = unitPlural.takeIf { it.isNotBlank() },
                    note = note
                )

                experienceRepository.update(recipe)

                // Delete existing subcomponents and insert new ones
                val existingSubcomponents = experienceRepository.getRecipeSubcomponents(recipeId)
                existingSubcomponents.forEach { experienceRepository.delete(it) }

                subcomponents.forEach { subcomponentData ->
                    val subcomponent = RecipeSubcomponent(
                        recipeId = recipeId,
                        substanceName = subcomponentData.substanceName,
                        dose = subcomponentData.dose.toDoubleOrNull(),
                        estimatedDoseStandardDeviation = subcomponentData.estimatedDoseStandardDeviation.toDoubleOrNull(),
                        isEstimate = subcomponentData.isEstimate,
                        originalUnit = subcomponentData.originalUnit
                    )
                    experienceRepository.insert(subcomponent)
                }

                onSuccess()
            } catch (e: Exception) {
                // Handle error
            } finally {
                isLoading = false
            }
        }
    }

    fun deleteRecipe(onSuccess: () -> Unit) {
        isDeleting = true
        viewModelScope.launch {
            try {
                experienceRepository.getCustomRecipeWithSubcomponents(recipeId)?.let { recipeWithSubcomponents ->
                    experienceRepository.deleteCustomRecipeWithSubcomponents(recipeWithSubcomponents)
                }
                onSuccess()
            } catch (e: Exception) {
                // Handle error
            } finally {
                isDeleting = false
            }
        }
    }

    fun toggleArchive() {
        isArchived = !isArchived
    }

    fun isValid(): Boolean {
        return recipeName.isNotBlank() &&
                unit.isNotBlank() &&
                subcomponents.isNotEmpty() &&
                subcomponents.all { it.substanceName.isNotBlank() && it.originalUnit.isNotBlank() && it.dose.isNotBlank() }
    }
}