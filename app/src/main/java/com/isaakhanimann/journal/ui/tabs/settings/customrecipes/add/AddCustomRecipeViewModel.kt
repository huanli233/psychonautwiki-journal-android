package com.isaakhanimann.journal.ui.tabs.settings.customrecipes.add

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.isaakhanimann.journal.data.room.experiences.ExperienceRepository
import com.isaakhanimann.journal.data.room.experiences.entities.CustomRecipe
import com.isaakhanimann.journal.data.room.experiences.entities.RecipeSubcomponent
import com.isaakhanimann.journal.data.substances.AdministrationRoute
import com.isaakhanimann.journal.data.substances.repositories.SearchRepository
import com.isaakhanimann.journal.data.substances.repositories.SubstanceRepository
import com.isaakhanimann.journal.di.RecipeResultHolder
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@HiltViewModel
class AddCustomRecipeViewModel @Inject constructor(
    private val experienceRepository: ExperienceRepository,
    private val substanceRepository: SubstanceRepository,
    searchRepository: SearchRepository,
    private val savedStateHandle: SavedStateHandle,
    private val resultHolder: RecipeResultHolder,
) : ViewModel() {

    var recipeName by mutableStateOf("")
    var administrationRoute by mutableStateOf(AdministrationRoute.ORAL)
    var unit by mutableStateOf("")
    var unitPlural by mutableStateOf("")
    var note by mutableStateOf("")
    var subcomponents by mutableStateOf(listOf<SubcomponentData>())

    var isLoading by mutableStateOf(false)

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

    init {
        viewModelScope.launch {
            resultHolder.resultFlow
                .collect { selection ->
                    selection?.let {
                        val updatedSubcomponent = subcomponents[it.index].copy(
                            substanceName = it.substanceName,
                            customUnitId = it.customUnitId
                        )
                        updateSubcomponent(it.index, updatedSubcomponent)
                        resultHolder.clearResult()
                    }
                }
        }
    }

    data class SubcomponentData(
        val substanceName: String = "",
        val customUnitId: Int? = null,
        val dose: String = "",
        val estimatedDoseStandardDeviation: String = "",
        val isEstimate: Boolean = false,
        val originalUnit: String = "",
        val autoFilledForSubstance: String? = null,
        val customUnitDose: String = ""
    )

    fun getCustomUnit(customUnitId: Int?): com.isaakhanimann.journal.data.room.experiences.entities.CustomUnit? {
        return customUnitId?.let {
            runBlocking { experienceRepository.getCustomUnit(it) }
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
        val shouldAutoFill = updatedSubcomponent.customUnitId == null &&
                updatedSubcomponent.originalUnit.isBlank() &&
                updatedSubcomponent.substanceName.isNotBlank() &&
                updatedSubcomponent.substanceName != updatedSubcomponent.autoFilledForSubstance

        val subcomponentWithUnit = updatedSubcomponent.copy(
            originalUnit = if (shouldAutoFill) getDefaultUnitForSubstance(updatedSubcomponent.substanceName)
            else updatedSubcomponent.originalUnit,
            autoFilledForSubstance = if (shouldAutoFill) updatedSubcomponent.substanceName
            else updatedSubcomponent.autoFilledForSubstance
        )

        subcomponents = subcomponents.mapIndexed { i, subcomponent ->
            if (i == index) subcomponentWithUnit else subcomponent
        }
    }

    private fun getDefaultUnitForSubstance(substanceName: String): String {
        val substance = substanceRepository.getSubstance(substanceName) ?: return ""

        val defaultRoa = substance.roas.firstOrNull { it.route == administrationRoute }
        return defaultRoa?.roaDose?.units ?: ""
    }

    fun saveRecipe(onSuccess: () -> Unit) {
        if (!isValid()) {
            return
        }

        isLoading = true
        viewModelScope.launch {
            try {
                val recipe = CustomRecipe(
                    name = recipeName,
                    administrationRoute = administrationRoute,
                    isArchived = false,
                    unit = unit,
                    unitPlural = unitPlural.takeIf { it.isNotBlank() },
                    note = note
                )

                val recipeId = experienceRepository.insert(recipe)

                subcomponents.forEach { subcomponentData ->
                    val subcomponent = if (subcomponentData.customUnitId != null) {
                        RecipeSubcomponent(
                            recipeId = recipeId.toInt(),
                            substanceName = subcomponentData.substanceName,
                            customUnitId = subcomponentData.customUnitId,
                            customUnitDose = subcomponentData.customUnitDose.toDoubleOrNull(),
                            dose = null,
                            estimatedDoseStandardDeviation = null,
                            isEstimate = false,
                            unit = "",
                            originalUnit = ""
                        )
                    } else {
                        RecipeSubcomponent(
                            recipeId = recipeId.toInt(),
                            substanceName = subcomponentData.substanceName,
                            customUnitId = null,
                            customUnitDose = null,
                            dose = subcomponentData.dose.toDoubleOrNull(),
                            estimatedDoseStandardDeviation = subcomponentData.estimatedDoseStandardDeviation.toDoubleOrNull(),
                            isEstimate = subcomponentData.isEstimate,
                            unit = subcomponentData.originalUnit,
                            originalUnit = subcomponentData.originalUnit
                        )
                    }
                    experienceRepository.insert(subcomponent)
                }

                onSuccess()
            } catch (e: Exception) {
            } finally {
                isLoading = false
            }
        }
    }

    fun isValid(): Boolean {
        return recipeName.isNotBlank() &&
                unit.isNotBlank() &&
                subcomponents.isNotEmpty() &&
                subcomponents.all {
                    it.substanceName.isNotBlank() &&
                            (it.customUnitId != null || (it.originalUnit.isNotBlank() && it.dose.isNotBlank()))
                }
    }
}