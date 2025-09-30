package com.isaakhanimann.journal.ui.tabs.journal.addingestion.dose.customrecipe

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.isaakhanimann.journal.data.room.experiences.ExperienceRepository
import com.isaakhanimann.journal.data.room.experiences.entities.CustomUnit
import com.isaakhanimann.journal.data.room.experiences.entities.RecipeSubcomponent
import com.isaakhanimann.journal.data.room.experiences.entities.custom.toRoaDose
import com.isaakhanimann.journal.data.room.experiences.entities.getActualDose
import com.isaakhanimann.journal.data.substances.AdministrationRoute
import com.isaakhanimann.journal.data.substances.classes.roa.DoseClass
import com.isaakhanimann.journal.data.substances.classes.roa.RoaDose
import com.isaakhanimann.journal.data.substances.repositories.SubstanceRepository
import com.isaakhanimann.journal.ui.main.navigation.graphs.ChooseDoseCustomRecipeRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

data class SubcomponentDisplayInfo(
    val subcomponent: RecipeSubcomponent,
    val roaDose: RoaDose?,
    val calculatedDose: Double?,
    val doseClass: DoseClass?
)

@HiltViewModel
class ChooseDoseCustomRecipeViewModel @Inject constructor(
    private val experienceRepo: ExperienceRepository,
    private val substanceRepo: SubstanceRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    var recipeName: String by mutableStateOf("")
        private set

    var customUnits: Map<Int, CustomUnit> by mutableStateOf(emptyMap())
        private set

    var administrationRoute: AdministrationRoute by mutableStateOf(AdministrationRoute.ORAL)

    var recipeUnitPlural: String by mutableStateOf("units")
        private set

    var recipeNote: String by mutableStateOf("")
        private set

    var subcomponentInfos: List<SubcomponentDisplayInfo> by mutableStateOf(emptyList())
        private set

    var isLoading: Boolean by mutableStateOf(true)
        private set

    val recipeId: Int

    var dose: String by mutableStateOf("")
    var isDoseAnEstimate: Boolean by mutableStateOf(false)
    var estimatedDoseStandardDeviation: String by mutableStateOf("")
    var notes: String by mutableStateOf("")

    init {
        val route = savedStateHandle.toRoute<ChooseDoseCustomRecipeRoute>()
        recipeId = route.customRecipeId
        viewModelScope.launch {
            val recipeWithSubcomponents = experienceRepo.getCustomRecipeWithSubcomponents(recipeId)
            if (recipeWithSubcomponents != null) {
                val recipe = recipeWithSubcomponents.recipe
                recipeName = recipe.name
                recipeUnitPlural = recipe.getPluralizableUnit().plural
                recipeNote = recipe.note
                administrationRoute = recipe.administrationRoute

                val infos = recipeWithSubcomponents.subcomponents.map { subcomponent ->
                    val customUnit = subcomponent.customUnitId?.let {
                        experienceRepo.getCustomUnit(it)?.also { customUnit ->
                            customUnits = customUnits + (it to customUnit)
                        }
                    }
                    val substance = (subcomponent.substanceName)?.let { substanceRepo.getSubstance(it) }
                    val customSubstance = customUnit?.substanceName?.let { experienceRepo.getCustomSubstance(it) }
                    val roaDose = substance?.getRoa(recipe.administrationRoute)?.roaDose
                        ?: customSubstance?.roaInfos?.find { it.administrationRoute == recipe.administrationRoute }?.toRoaDose()
                    SubcomponentDisplayInfo(subcomponent, roaDose, null, null)
                }
                subcomponentInfos = infos
                updateCalculatedDoses()
            }
            isLoading = false
        }
    }

    fun updateDose(newDose: String) {
        dose = newDose.replace(',', '.')
        updateCalculatedDoses()
    }

    private fun updateCalculatedDoses() {
        val currentDose = dose.toDoubleOrNull()
        subcomponentInfos = subcomponentInfos.map { info ->
            val calculatedDose = currentDose?.let { runBlocking { info.subcomponent.getActualDose(experienceRepo) }?.let { it1 -> it * it1 } }
            val doseClass = calculatedDose?.let { info.roaDose?.getDoseClass(it) }
            info.copy(calculatedDose = calculatedDose, doseClass = doseClass)
        }
    }

    fun updateIsDoseAnEstimate(isEstimate: Boolean) {
        this.isDoseAnEstimate = isEstimate
    }

    fun updateEstimatedDoseStandardDeviation(newDeviation: String) {
        estimatedDoseStandardDeviation = newDeviation.replace(',', '.')
    }

    fun updateNotes(newNotes: String) {
        notes = newNotes
    }

    fun isValid(): Boolean {
        return dose.toDoubleOrNull() != null
    }
}