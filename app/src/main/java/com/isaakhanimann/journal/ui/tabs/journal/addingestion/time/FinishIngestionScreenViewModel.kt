package com.isaakhanimann.journal.ui.tabs.journal.addingestion.time

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.isaakhanimann.journal.data.room.experiences.ExperienceRepository
import com.isaakhanimann.journal.data.room.experiences.entities.AdaptiveColor
import com.isaakhanimann.journal.data.room.experiences.entities.Experience
import com.isaakhanimann.journal.data.room.experiences.entities.Ingestion
import com.isaakhanimann.journal.data.room.experiences.entities.SubstanceColor
import com.isaakhanimann.journal.data.room.experiences.entities.SubstanceCompanion
import com.isaakhanimann.journal.data.room.experiences.entities.getSubstanceColor
import com.isaakhanimann.journal.data.room.experiences.relations.ExperienceWithIngestions
import com.isaakhanimann.journal.data.substances.AdministrationRoute
import com.isaakhanimann.journal.ui.main.navigation.graphs.FinishIngestionRoute
import com.isaakhanimann.journal.ui.tabs.settings.combinations.UserPreferences
import com.isaakhanimann.journal.ui.utils.getInstant
import com.isaakhanimann.journal.ui.utils.getLocalDateTime
import com.isaakhanimann.journal.ui.utils.getStringOfPattern
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.UUID
import javax.inject.Inject
import kotlin.let

const val hourLimitToSeparateIngestions: Long = 12

enum class IngestionTimePickerOption {
    POINT_IN_TIME, TIME_RANGE
}

@HiltViewModel
class FinishIngestionScreenViewModel @Inject constructor(
    private val experienceRepo: ExperienceRepository,
    userPreferences: UserPreferences,
    state: SavedStateHandle
) : ViewModel() {
    var substanceName by mutableStateOf("")
    val localDateTimeStartFlow = MutableStateFlow(LocalDateTime.now())
    val localDateTimeEndFlow = MutableStateFlow(LocalDateTime.now().plusMinutes(30))
    val ingestionTimePickerOptionFlow = MutableStateFlow(IngestionTimePickerOption.POINT_IN_TIME)
    val experiencesInRangeFlow = MutableStateFlow<List<ExperienceWithIngestions>>(emptyList())
    val selectedExperienceFlow = MutableStateFlow<ExperienceWithIngestions?>(null)
    var enteredTitle by mutableStateOf(LocalDateTime.now().getStringOfPattern("dd MMMM yyyy"))
    val isEnteredTitleOk get() = enteredTitle.isNotEmpty()
    var consumerName by mutableStateOf("")

    fun onChangeTimePickerOption(ingestionTimePickerOption: IngestionTimePickerOption) =
        viewModelScope.launch {
            ingestionTimePickerOptionFlow.emit(ingestionTimePickerOption)
        }

    private val sortedExperiencesFlow = experienceRepo.getSortedExperiencesWithIngestionsFlow()

    val sortedConsumerNamesFlow =
        experienceRepo.getSortedIngestions(limit = 200).map { ingestions ->
            return@map ingestions.mapNotNull { it.consumerName }.distinct()
        }.stateIn(
            initialValue = emptyList(),
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000)
        )

    var isLoadingColor by mutableStateOf(true)
    var selectedColor by mutableStateOf<SubstanceColor>(SubstanceColor.Predefined(AdaptiveColor.BLUE))
    var note by mutableStateOf("")
    private var hasTitleBeenChanged = false

    fun changeTitle(newTitle: String) {
        enteredTitle = newTitle
        hasTitleBeenChanged = true
    }

    fun changeConsumerName(newName: String) {
        consumerName = newName
    }

    val previousNotesFlow: StateFlow<List<String>> =
        experienceRepo.getSortedIngestionsFlow(substanceName, limit = 10).map { list ->
            list.mapNotNull { it.notes }.filter { it.isNotBlank() }.distinct()
        }.stateIn(
            initialValue = emptyList(),
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000)
        )

    private val route: FinishIngestionRoute = state.toRoute()

    private val companionFlow = experienceRepo.getAllSubstanceCompanionsFlow()

    val alreadyUsedColorsFlow: StateFlow<List<AdaptiveColor>> =
        companionFlow.map { companions ->
            companions.mapNotNull { it.color }.distinct()
        }.stateIn(
            initialValue = emptyList(),
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000)
        )

    val otherColorsFlow: StateFlow<List<AdaptiveColor>> =
        alreadyUsedColorsFlow.map { alreadyUsedColors ->
            AdaptiveColor.entries.filter {
                !alreadyUsedColors.contains(it)
            }
        }.stateIn(
            initialValue = emptyList(),
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000)
        )

    private val newExperienceIdToUseFlow: Flow<Int> = sortedExperiencesFlow.map { experiences ->
        val previousMax = experiences.maxOfOrNull { it.experience.id } ?: 1
        return@map previousMax + 1
    }

    init {
        note = route.ingestionNotes ?: ""

        viewModelScope.launch {
            val nameToUse = if (route.customRecipeId != null) {
                experienceRepo.getCustomRecipe(route.customRecipeId)?.name ?: "Recipe"
            } else {
                route.substanceName ?: "Substance"
            }
            substanceName = nameToUse

            val lastIngestionTimeOfExperience =
                userPreferences.lastIngestionTimeOfExperienceFlow.first()
            val clonedIngestionTime = userPreferences.clonedIngestionTimeFlow.first()
            if (clonedIngestionTime != null) {
                localDateTimeStartFlow.emit(clonedIngestionTime.getLocalDateTime())
                localDateTimeEndFlow.emit(clonedIngestionTime.plus(30, ChronoUnit.MINUTES).getLocalDateTime())
                updateTitleBasedOnTime(clonedIngestionTime)
            } else if (lastIngestionTimeOfExperience != null) {
                val wasLastIngestionOfExperienceMoreThan20HoursAgo =
                    lastIngestionTimeOfExperience < Instant.now().minus(20, ChronoUnit.HOURS)
                if (wasLastIngestionOfExperienceMoreThan20HoursAgo) {
                    localDateTimeStartFlow.emit(lastIngestionTimeOfExperience.getLocalDateTime())
                    localDateTimeEndFlow.emit(lastIngestionTimeOfExperience.plus(30, ChronoUnit.MINUTES).getLocalDateTime())
                    updateTitleBasedOnTime(lastIngestionTimeOfExperience)
                }
            }
            updateExperiencesBasedOnSelectedTime()
            val allCompanions = experienceRepo.getAllSubstanceCompanionsFlow().first()
            val thisCompanion = allCompanions.firstOrNull { it.substanceName == nameToUse }
            selectedColor = thisCompanion?.getSubstanceColor() ?: run {
                val alreadyUsedColors = allCompanions.mapNotNull { it.color }
                val otherColors = AdaptiveColor.entries.filter { !alreadyUsedColors.contains(it) }
                val randomColor = otherColors.filter { it.isPreferred }.randomOrNull()
                    ?: otherColors.randomOrNull() ?: AdaptiveColor.entries.random()
                SubstanceColor.Predefined(randomColor)
            }
            isLoadingColor = false
        }
    }

    fun onChangeOfSelectedExperience(experienceWithIngestions: ExperienceWithIngestions?) =
        viewModelScope.launch {
            selectedExperienceFlow.emit(experienceWithIngestions)
        }

    fun onChangeStartDateOrTime(newLocalDateTime: LocalDateTime) = viewModelScope.launch {
        localDateTimeStartFlow.emit(newLocalDateTime)
        updateExperiencesBasedOnSelectedTime()
        val startTime = newLocalDateTime.atZone(ZoneId.systemDefault()).toInstant()
        if (!hasTitleBeenChanged) {
            updateTitleBasedOnTime(startTime)
        }
        val endTime = localDateTimeEndFlow.first().atZone(ZoneId.systemDefault()).toInstant()
        if (startTime > endTime || Duration.between(startTime, endTime).toHours() > 24) {
            val newEndTime = startTime.plus(30, ChronoUnit.MINUTES)
            localDateTimeEndFlow.emit(newEndTime.getLocalDateTime())
        }
    }

    fun onChangeEndDateOrTime(newLocalDateTime: LocalDateTime) = viewModelScope.launch {
        localDateTimeEndFlow.emit(newLocalDateTime)
        val endTime = newLocalDateTime.atZone(ZoneId.systemDefault()).toInstant()
        val startTime =
            localDateTimeStartFlow.first().atZone(ZoneId.systemDefault()).toInstant()
        if (startTime > endTime) {
            val newStartTime = endTime.minus(30, ChronoUnit.MINUTES)
            localDateTimeStartFlow.emit(newStartTime.getLocalDateTime())
        }
    }

    private fun updateTitleBasedOnTime(time: Instant) {
        enteredTitle = time.getStringOfPattern("dd MMMM yyyy")
    }

    private suspend fun updateExperiencesBasedOnSelectedTime() {
        val selectedInstant = localDateTimeStartFlow.value.getInstant()
        val fromInstant = selectedInstant.minus(3, ChronoUnit.DAYS)
        val toInstant = selectedInstant.plus(1, ChronoUnit.DAYS)
        val experiencesInRange =
            experienceRepo.getSortedExperiencesWithIngestionsWithSortDateBetween(
                fromInstant = fromInstant,
                toInstant = toInstant
            )
        experiencesInRangeFlow.emit(experiencesInRange)
        val closestExperience = experiencesInRange.firstOrNull { experience ->
            val sortedIngestions = experience.ingestions.sortedBy { it.time }
            val firstIngestionTime =
                sortedIngestions.firstOrNull()?.time ?: return@firstOrNull false
            val upperBoundBasedOnFirstIngestion = firstIngestionTime.plus(15, ChronoUnit.HOURS)
            val lastIngestionTime = sortedIngestions.lastOrNull()?.time ?: return@firstOrNull false
            val upperBoundBasedOnLastIngestion = lastIngestionTime.plus(3, ChronoUnit.HOURS)
            val finalUpperBound =
                maxOf(upperBoundBasedOnFirstIngestion, upperBoundBasedOnLastIngestion)
            val lowerBound = firstIngestionTime.minus(3, ChronoUnit.HOURS)
            return@firstOrNull selectedInstant in lowerBound..finalUpperBound
        }
        selectedExperienceFlow.emit(closestExperience)
    }

    fun createSaveAndDismissAfter(dismiss: () -> Unit) {
        viewModelScope.launch {
            createAndSaveIngestion()
            withContext(Dispatchers.Main) {
                dismiss()
            }
        }
    }

    private suspend fun createAndSaveIngestion() {
        val experienceId = selectedExperienceFlow.firstOrNull()?.experience?.id ?: newExperienceIdToUseFlow.first()
        val isNewExperience = selectedExperienceFlow.firstOrNull() == null
        val time = localDateTimeStartFlow.first().getInstant()

        val newExperience = if (isNewExperience) {
            Experience(
                id = experienceId,
                title = enteredTitle,
                text = "",
                creationDate = Instant.now(),
                sortDate = time,
                location = null
            )
        } else null

        // Handle Recipe Ingestion
        if (route.customRecipeId != null && route.recipeDose != null) {
            val recipeWithSubcomponents = experienceRepo.getCustomRecipeWithSubcomponents(route.customRecipeId)
            val subcomponents = recipeWithSubcomponents?.subcomponents

            if (!subcomponents.isNullOrEmpty()) {
                val recipeGroupId = UUID.randomUUID().toString()

                subcomponents.forEachIndexed { index, subcomponent ->
                    val ingestion = createIngestionFromSubcomponent(recipeWithSubcomponents.recipe.administrationRoute, subcomponent, time, experienceId, recipeGroupId)
                    val companion = getOrCreateCompanionFor(subcomponent.substanceName)

                    if (index == 0 && newExperience != null) {
                        experienceRepo.insertIngestionExperienceAndCompanion(ingestion, newExperience, companion)
                    } else {
                        experienceRepo.insertIngestionAndCompanion(ingestion, companion)
                    }
                }
            }
            val recipeCompanion = when(val color = selectedColor) {
                is SubstanceColor.Predefined -> SubstanceCompanion(substanceName = substanceName, color = color.color, customColor = null)
                is SubstanceColor.Custom -> SubstanceCompanion(substanceName = substanceName, color = null, customColor = color.value)
            }
            experienceRepo.insert(recipeCompanion)

            // Handle Standard Ingestion
        } else if (route.substanceName != null) {
            val ingestion = createNewIngestion(experienceId)
            val companion = getOrCreateCompanionFor(ingestion.substanceName)

            if (newExperience != null) {
                experienceRepo.insertIngestionExperienceAndCompanion(ingestion, newExperience, companion)
            } else {
                experienceRepo.insertIngestionAndCompanion(ingestion, companion)
            }
        }
    }

    private fun createIngestionFromSubcomponent(
        route: AdministrationRoute,
        subcomponent: com.isaakhanimann.journal.data.room.experiences.entities.RecipeSubcomponent,
        time: Instant,
        experienceId: Int,
        recipeGroupId: String
    ): Ingestion {
        val recipeDose = this.route.recipeDose ?: 1.0
        val finalDose = (subcomponent.dose ?: 0.0) * recipeDose
        val finalDeviation = this.route.estimatedDoseStandardDeviation?.let { subcomponent.dose?.let { it1 -> it * it1 } }

        return Ingestion(
            substanceName = subcomponent.substanceName,
            time = time,
            endTime = null,
            administrationRoute = route,
            dose = finalDose,
            isDoseAnEstimate = this.route.isEstimate,
            estimatedDoseStandardDeviation = finalDeviation,
            units = subcomponent.originalUnit,
            experienceId = experienceId,
            notes = note,
            stomachFullness = null,
            consumerName = consumerName.ifBlank { null },
            customUnitId = null,
            recipeGroupId = recipeGroupId
        )
    }

    private suspend fun getOrCreateCompanionFor(substance: String): SubstanceCompanion {
        return experienceRepo.getAllSubstanceCompanions().find { it.substanceName == substance }
            ?: SubstanceCompanion(substanceName = substance, color = null, customColor = null)
    }

    private suspend fun createNewIngestion(experienceId: Int): Ingestion {
        val time = localDateTimeStartFlow.first().getInstant()
        val ingestionTimePickerOption = ingestionTimePickerOptionFlow.first()
        val endTime = when (ingestionTimePickerOption) {
            IngestionTimePickerOption.POINT_IN_TIME -> null
            IngestionTimePickerOption.TIME_RANGE -> localDateTimeEndFlow.first().getInstant()
        }
        return Ingestion(
            substanceName = route.substanceName ?: "Unknown",
            time = time,
            endTime = endTime,
            administrationRoute = route.administrationRoute ?: AdministrationRoute.ORAL,
            dose = route.dose,
            isDoseAnEstimate = route.isEstimate,
            estimatedDoseStandardDeviation = route.estimatedDoseStandardDeviation,
            units = route.units,
            experienceId = experienceId,
            notes = note,
            stomachFullness = null,
            consumerName = consumerName.ifBlank { null },
            customUnitId = route.customUnitId,
            customRecipeId = route.customRecipeId,
            recipeGroupId = null
        )
    }
}