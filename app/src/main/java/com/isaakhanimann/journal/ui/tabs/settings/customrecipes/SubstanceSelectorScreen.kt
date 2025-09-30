package com.isaakhanimann.journal.ui.tabs.settings.customrecipes

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import com.isaakhanimann.journal.R
import com.isaakhanimann.journal.di.RecipeResultHolder
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SubstanceSelectorViewModel @Inject constructor(
    private val resultHolder: RecipeResultHolder
) : ViewModel() {
    fun selectSubstance(index: Int, name: String) {
        resultHolder.postResult(index, name)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubstanceSelectorScreen(
    subcomponentIndex: Int,
    allSubstances: List<String>,
    onSubstanceSelected: () -> Unit,
    onDismiss: () -> Unit
) {
    val viewModel: SubstanceSelectorViewModel = hiltViewModel()
    var searchText by remember { mutableStateOf("") }

    val filteredSubstances = remember(searchText, allSubstances) {
        if (searchText.isBlank()) {
            allSubstances
        } else {
            allSubstances.filter { it.contains(searchText, ignoreCase = true) }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    OutlinedTextField(
                        value = searchText,
                        onValueChange = { searchText = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text(stringResource(R.string.search_substances)) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        singleLine = true
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize(),
            contentPadding = paddingValues
        ) {
            items(filteredSubstances) { substanceName ->
                ListItem(
                    headlineContent = { Text(substanceName) },
                    modifier = Modifier.clickable {
                        viewModel.selectSubstance(subcomponentIndex, substanceName)
                        onSubstanceSelected()
                    }
                )
            }
        }
    }
}