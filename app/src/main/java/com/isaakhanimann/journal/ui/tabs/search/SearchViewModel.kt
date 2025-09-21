package com.isaakhanimann.journal.ui.tabs.search

import android.app.Application
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.isaakhanimann.journal.R
import com.isaakhanimann.journal.data.room.experiences.ExperienceRepository
import com.isaakhanimann.journal.data.substances.repositories.SearchRepository
import com.isaakhanimann.journal.data.substances.repositories.SubstanceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    application: Application,
    experienceRepo: ExperienceRepository,
    substanceRepo: SubstanceRepository,
    searchRepository: SearchRepository
) : AndroidViewModel(application) {

    private val _searchTextFlow = MutableStateFlow("")
    val searchTextFlow = _searchTextFlow.asStateFlow()

    private val filtersFlow = MutableStateFlow(emptyList<String>())

    private val customChipName: String = application.getString(R.string.custom)

    fun onFilterTapped(filterName: String) {
        viewModelScope.launch {
            if (filterName == customChipName) {
                isShowingCustomSubstancesFlow.emit(isShowingCustomSubstancesFlow.value.not())
            }
            val filters = filtersFlow.value.toMutableList()
            if (filters.contains(filterName)) {
                filters.remove(filterName)
            } else {
                filters.add(filterName)
            }
            filtersFlow.emit(filters)
        }
    }

    private val isShowingCustomSubstancesFlow = MutableStateFlow(false)

    val chipCategoriesFlow: StateFlow<List<CategoryChipModel>> = filtersFlow.map { filters ->
        substanceRepo.getAllCategories().map { category ->
            val isActive = filters.contains(category.name)
            CategoryChipModel(
                chipName = category.name,
                color = category.color,
                isActive = isActive
            )
        }
    }.combine(isShowingCustomSubstancesFlow) { chips, isShowingCustom ->
        val newChips = chips.toMutableList()
        newChips.add(
            0,
            CategoryChipModel(
                chipName = customChipName,
                color = customColor,
                isActive = isShowingCustom
            )
        )
        return@combine newChips
    }.stateIn(
        initialValue = emptyList(),
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000)
    )

    val filteredSubstancesFlow = combine(
        searchTextFlow,
        filtersFlow,
        experienceRepo.getSortedLastUsedSubstanceNamesFlow(limit = 200)
    ) { searchText, filters, recents ->
        return@combine searchRepository.getMatchingSubstances(
            searchText = searchText,
            filterCategories = filters,
            recentlyUsedSubstanceNamesSorted = recents
        ).map { it.toSubstanceModel() }
    }.stateIn(
        initialValue = emptyList(),
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000)
    )

    fun filterSubstances(searchText: String) {
        viewModelScope.launch {
            _searchTextFlow.emit(searchText)
        }
    }

    private val customSubstancesFlow = experienceRepo.getCustomSubstancesFlow()

    val filteredCustomSubstancesFlow =
        customSubstancesFlow.combine(searchTextFlow) { customSubstances, searchText ->
            customSubstances.filter { custom ->
                custom.name.contains(
                    other = searchText,
                    ignoreCase = true
                )
            }
        }.stateIn(
            initialValue = emptyList(),
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000)
        )
}

val customColor = Color.Cyan

data class CategoryChipModel(
    val chipName: String,
    val color: Color,
    val isActive: Boolean
)

data class CategoryModel(
    val name: String,
    val color: Color
)
