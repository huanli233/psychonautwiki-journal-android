package com.isaakhanimann.journal.di

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

data class RecipeSubstanceSelection(
    val index: Int,
    val substanceName: String,
    val customUnitId: Int?
)

@Singleton
class RecipeResultHolder @Inject constructor() {
    private val _resultFlow = MutableStateFlow<RecipeSubstanceSelection?>(null)
    val resultFlow = _resultFlow.asStateFlow()

    fun postResult(index: Int, substanceName: String, customUnitId: Int?) {
        _resultFlow.value = RecipeSubstanceSelection(index, substanceName, customUnitId)
    }

    fun clearResult() {
        _resultFlow.value = null
    }
}