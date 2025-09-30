package com.isaakhanimann.journal.ui.tabs.journal.addingestion.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.isaakhanimann.journal.data.room.experiences.ExperienceRepository
import com.isaakhanimann.journal.data.room.experiences.entities.CustomSubstance
import com.isaakhanimann.journal.data.room.experiences.entities.CustomUnit
import com.isaakhanimann.journal.data.room.experiences.entities.SubstanceColor
import com.isaakhanimann.journal.data.room.experiences.entities.getSubstanceColor
import com.isaakhanimann.journal.data.room.experiences.relations.CustomRecipeWithSubcomponents
import com.isaakhanimann.journal.data.room.experiences.relations.IngestionWithCompanionAndCustomUnit
import com.isaakhanimann.journal.data.substances.AdministrationRoute
import com.isaakhanimann.journal.data.substances.classes.Substance
import com.isaakhanimann.journal.data.substances.repositories.SearchRepository
import com.isaakhanimann.journal.data.substances.repositories.SubstanceRepository
import com.isaakhanimann.journal.ui.tabs.journal.addingestion.search.suggestion.models.CustomUnitDoseSuggestion
import com.isaakhanimann.journal.ui.tabs.journal.addingestion.search.suggestion.models.DoseAndUnit
import com.isaakhanimann.journal.ui.tabs.journal.addingestion.search.suggestion.models.Suggestion
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant
import javax.inject.Inject

sealed class QuickLogItem(open val sortInstant: Instant) {
    data class SuggestionItem(val suggestion: Suggestion) : QuickLogItem(suggestion.sortInstant)
    data class RecipeItem(
        val recipe: CustomRecipeWithSubcomponents,
        override val sortInstant: Instant
    ) : QuickLogItem(sortInstant)
}

@HiltViewModel
class AddIngestionSearchViewModel @Inject constructor(
    experienceRepo: ExperienceRepository,
    val substanceRepo: SubstanceRepository,
    private val searchRepo: SearchRepository,
) : ViewModel() {

    private val _searchTextFlow = MutableStateFlow("")
    val searchTextFlow = _searchTextFlow.asStateFlow()

    fun updateSearchText(searchText: String) {
        viewModelScope.launch {
            _searchTextFlow.emit(searchText)
        }
    }

    val filteredSubstancesFlow = combine(
        searchTextFlow,
        experienceRepo.getSortedLastUsedSubstanceNamesFlow(limit = 200)
    ) { searchText, recents ->
        return@combine searchRepo.getMatchingSubstances(
            searchText = searchText,
            filterCategories = emptyList(),
            recentlyUsedSubstanceNamesSorted = recents
        ).map { it.toSubstanceModel() }
    }.stateIn(
        initialValue = emptyList(),
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000)
    )

    private val customUnitsFlow = experienceRepo.getCustomUnitsFlow(false)

    val customUnitsMapFlow: StateFlow<Map<Int, CustomUnit>> = customUnitsFlow
        .map { units -> units.associateBy { it.id } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyMap()
        )

    val filteredCustomUnitsFlow = combine(
        customUnitsFlow,
        filteredSubstancesFlow,
        searchTextFlow
    ) { customUnit, filteredSubstances, searchText ->
        customUnit.filter { custom ->
            filteredSubstances.any { it.name == custom.substanceName } || custom.name.contains(
                other = searchText,
                ignoreCase = true
            ) || custom.substanceName.contains(
                other = searchText,
                ignoreCase = true
            ) || custom.unit.contains(
                other = searchText,
                ignoreCase = true
            ) || custom.note.contains(other = searchText, ignoreCase = true)
        }
    }.stateIn(
        initialValue = emptyList(),
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000)
    )

    private val customRecipesFlow = experienceRepo.getSortedCustomRecipesWithSubcomponentsFlow(false)

    val filteredCustomRecipesFlow = combine(
        customRecipesFlow,
        searchTextFlow
    ) { customRecipes, searchText ->
        customRecipes.filter { recipeWithSubcomponents ->
            recipeWithSubcomponents.recipe.name.contains(searchText, ignoreCase = true) ||
                    recipeWithSubcomponents.recipe.note.contains(searchText, ignoreCase = true) ||
                    recipeWithSubcomponents.subcomponents.any { subcomponent ->
                        subcomponent.substanceName?.contains(searchText, ignoreCase = true) == true
                    }
        }
    }.stateIn(
        initialValue = emptyList(),
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000)
    )

    private val customSubstancesFlow = experienceRepo.getCustomSubstancesFlow()

    val filteredCustomSubstancesFlow =
        customSubstancesFlow.combine(searchTextFlow) { customSubstances, searchText ->
            customSubstances.filter { custom ->
                custom.name.contains(other = searchText, ignoreCase = true)
            }
        }.stateIn(
            initialValue = emptyList(),
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000)
        )

    val quickLogItemsFlow: StateFlow<List<QuickLogItem>> = combine(
        experienceRepo.getSortedIngestionsWithSubstanceCompanionsFlow(limit = 1000),
        customSubstancesFlow,
        customRecipesFlow,
        filteredSubstancesFlow,
        searchTextFlow
    ) { ingestions, customSubstances, customRecipes, filteredSubstances, searchText ->
        // Ingestions for regular substances and custom units
        val substanceIngestions = ingestions.filter { it.ingestion.customRecipeId == null }
        val suggestionItems = getSuggestions(substanceIngestions, customSubstances)
            .map { QuickLogItem.SuggestionItem(it) }

        // Find the last used time for each recipe
        val recipeLastUsedMap = ingestions
            .filter { it.ingestion.customRecipeId != null }
            .groupBy { it.ingestion.customRecipeId!! }
            .mapValues { entry ->
                entry.value.maxOfOrNull { it.ingestion.creationDate ?: Instant.MIN } ?: Instant.MIN
            }

        // Create recipe items, using last used time for sorting
        val recipeItems = customRecipes.map { recipe ->
            val lastUsed = recipeLastUsedMap[recipe.recipe.id] ?: Instant.MIN
            QuickLogItem.RecipeItem(recipe, lastUsed)
        }

        val allItems = (suggestionItems + recipeItems)
            .sortedByDescending { it.sortInstant }

        return@combine allItems.filter { item ->
            when (item) {
                is QuickLogItem.SuggestionItem -> item.suggestion.isInSearch(
                    searchText = searchText,
                    substanceNames = filteredSubstances.map { it.name }
                )
                is QuickLogItem.RecipeItem -> {
                    val recipeWithSubcomponents = item.recipe
                    searchText.isEmpty() ||
                            recipeWithSubcomponents.recipe.name.contains(searchText, ignoreCase = true) ||
                            recipeWithSubcomponents.recipe.note.contains(searchText, ignoreCase = true) ||
                            recipeWithSubcomponents.subcomponents.any { subcomponent ->
                                subcomponent.substanceName?.contains(searchText, ignoreCase = true) == true
                            }
                }
            }
        }
    }.stateIn(
        initialValue = emptyList(),
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000)
    )


    private fun getSuggestions(
        ingestions: List<IngestionWithCompanionAndCustomUnit>,
        customSubstances: List<CustomSubstance>
    ): List<Suggestion> {
        val groupedBySubstance = ingestions.groupBy { it.ingestion.substanceName }
        val suggestions = groupedBySubstance.flatMap { entry ->
            return@flatMap getSuggestionsForSubstance(
                substanceName = entry.key,
                ingestionsGroupedBySubstance = entry.value,
                customSubstances = customSubstances
            )
        }
        return suggestions.sortedByDescending { it.sortInstant }
    }

    private fun getSuggestionsForSubstance(
        substanceName: String,
        ingestionsGroupedBySubstance: List<IngestionWithCompanionAndCustomUnit>,
        customSubstances: List<CustomSubstance>
    ): List<Suggestion> {
        val color =
            ingestionsGroupedBySubstance.firstOrNull()?.substanceCompanion?.getSubstanceColor()
                ?: return emptyList()
        val substance = substanceRepo.getSubstance(substanceName)
        val isPredefinedSubstance = substance != null
        val customSubstance = customSubstances.firstOrNull { it.name == substanceName }
        val groupedRoute =
            ingestionsGroupedBySubstance.groupBy { it.ingestion.administrationRoute }
        if (!isPredefinedSubstance && customSubstance == null) {
            return emptyList()
        } else {
            val suggestions = groupedRoute.flatMap { routeEntry ->
                val administrationRoute = routeEntry.key
                val ingestionsForSubstanceAndRoute = routeEntry.value

                val customUnitSuggestions = getCustomUnitSuggestionsForSubstance(
                    ingestionsForSubstanceAndRoute = ingestionsForSubstanceAndRoute,
                    color = color
                )
                val results = mutableListOf<Suggestion>()
                results.addAll(customUnitSuggestions)
                if (substance != null) {
                    val pureSubstanceSuggestion = getPureSubstanceSuggestion(
                        ingestionsForSubstanceAndRoute = ingestionsForSubstanceAndRoute,
                        substance = substance,
                        administrationRoute = administrationRoute,
                        color = color
                    )
                    if (pureSubstanceSuggestion != null) {
                        results.add(pureSubstanceSuggestion)
                    }
                }
                if (customSubstance != null) {
                    val customSubstanceSuggestion = getCustomSubstanceSuggestion(
                        ingestionsForSubstanceAndRoute = ingestionsForSubstanceAndRoute,
                        customSubstance = customSubstance,
                        administrationRoute = administrationRoute,
                        color = color
                    )
                    if (customSubstanceSuggestion != null) {
                        results.add(customSubstanceSuggestion)
                    }
                }
                return@flatMap results
            }
            return suggestions
        }
    }

    private fun getCustomUnitSuggestionsForSubstance(
        ingestionsForSubstanceAndRoute: List<IngestionWithCompanionAndCustomUnit>,
        color: SubstanceColor
    ): List<Suggestion.CustomUnitSuggestion> {
        val groupedByUnit = ingestionsForSubstanceAndRoute.filter { it.customUnit != null && !(it.customUnit?.isArchived ?: true) }
            .groupBy { it.customUnit }
        return groupedByUnit.mapNotNull { unitGroup ->
            val customUnit = unitGroup.key ?: return@mapNotNull null
            val ingestionsWithUnit = unitGroup.value
            val dosesAndUnit = ingestionsWithUnit.map { it.ingestion }.map { ingestion ->
                CustomUnitDoseSuggestion(
                    dose = ingestion.dose,
                    isEstimate = ingestion.isDoseAnEstimate,
                    estimatedDoseStandardDeviation = ingestion.estimatedDoseStandardDeviation
                )
            }.distinctBy { it.comparatorValue }.take(8)
            if (dosesAndUnit.isEmpty()) {
                return@mapNotNull null
            }
            return@mapNotNull Suggestion.CustomUnitSuggestion(
                customUnit = customUnit,
                color = color,
                dosesAndUnit = dosesAndUnit,
                sortInstant = ingestionsWithUnit.mapNotNull { it.ingestion.creationDate }
                    .maxOfOrNull { it } ?: Instant.MIN
            )
        }
    }

    private fun getPureSubstanceSuggestion(
        ingestionsForSubstanceAndRoute: List<IngestionWithCompanionAndCustomUnit>,
        substance: Substance,
        administrationRoute: AdministrationRoute,
        color: SubstanceColor
    ): Suggestion.PureSubstanceSuggestion? {
        val ingestionsToConsider = ingestionsForSubstanceAndRoute.filter { it.customUnit == null }
            .map { it.ingestion }
        if (ingestionsToConsider.isEmpty()) {
            return null
        }
        val dosesAndUnit = ingestionsToConsider
            .mapNotNull { ingestion ->
                val unit = ingestion.units ?: return@mapNotNull null
                return@mapNotNull DoseAndUnit(
                    dose = ingestion.dose,
                    unit = unit,
                    isEstimate = ingestion.isDoseAnEstimate,
                    estimatedDoseStandardDeviation = ingestion.estimatedDoseStandardDeviation
                )
            }.distinctBy { it.comparatorValue }.take(8)
        return Suggestion.PureSubstanceSuggestion(
            administrationRoute = administrationRoute,
            substanceName = substance.name,
            color = color,
            dosesAndUnit = dosesAndUnit,
            sortInstant = ingestionsToConsider.mapNotNull { it.creationDate }
                .maxOfOrNull { it } ?: Instant.MIN
        )
    }

    private fun getCustomSubstanceSuggestion(
        ingestionsForSubstanceAndRoute: List<IngestionWithCompanionAndCustomUnit>,
        customSubstance: CustomSubstance,
        administrationRoute: AdministrationRoute,
        color: SubstanceColor
    ): Suggestion.CustomSubstanceSuggestion? {
        val ingestionsToConsider = ingestionsForSubstanceAndRoute.filter { it.customUnit == null }
            .map { it.ingestion }
        if (ingestionsToConsider.isEmpty()) {
            return null
        }
        val dosesAndUnit = ingestionsToConsider
            .mapNotNull { ingestion ->
                val unit = ingestion.units ?: return@mapNotNull null
                return@mapNotNull DoseAndUnit(
                    dose = ingestion.dose,
                    unit = unit,
                    isEstimate = ingestion.isDoseAnEstimate,
                    estimatedDoseStandardDeviation = ingestion.estimatedDoseStandardDeviation
                )
            }.distinctBy { it.comparatorValue }.take(8)
        return Suggestion.CustomSubstanceSuggestion(
            administrationRoute = administrationRoute,
            customSubstance = customSubstance,
            color = color,
            dosesAndUnit = dosesAndUnit,
            sortInstant = ingestionsToConsider.mapNotNull { it.creationDate }
                .maxOfOrNull { it } ?: Instant.MIN
        )
    }
}