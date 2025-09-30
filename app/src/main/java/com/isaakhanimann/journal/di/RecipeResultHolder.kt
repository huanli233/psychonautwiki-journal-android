package com.isaakhanimann.journal.di

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecipeResultHolder @Inject constructor() {
    private val _resultFlow = MutableStateFlow<Pair<Int, String>?>(null)
    val resultFlow = _resultFlow.asStateFlow()

    fun postResult(index: Int, name: String) {
        _resultFlow.value = index to name
    }

    fun clearResult() {
        _resultFlow.value = null
    }
}