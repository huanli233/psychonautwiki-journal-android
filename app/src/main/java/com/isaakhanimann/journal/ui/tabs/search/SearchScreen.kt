package com.isaakhanimann.journal.ui.tabs.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.isaakhanimann.journal.R
import com.isaakhanimann.journal.ui.tabs.search.substancerow.SubstanceRow

@Composable
fun SearchScreen(
    searchViewModel: SearchViewModel = hiltViewModel(),
    onSubstanceTap: (substanceModel: SubstanceModel) -> Unit,
    onCustomSubstanceTap: (customSubstanceId: Int) -> Unit,
    navigateToAddCustomSubstanceScreen: () -> Unit,
) {
    val focusRequester = remember { FocusRequester() }
    var isFocused by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            Surface(shadowElevation = 2.dp) {
                SearchField(
                    modifier = Modifier
                        .statusBarsPadding()
                        .fillMaxWidth()
                        .padding(16.dp)
                        .focusRequester(focusRequester)
                        .onFocusChanged { focusState -> isFocused = focusState.isFocused },
                    searchText = searchViewModel.searchTextFlow.collectAsState().value,
                    onChange = { searchViewModel.filterSubstances(searchText = it) },
                    categories = searchViewModel.chipCategoriesFlow.collectAsState().value,
                    onFilterTapped = searchViewModel::onFilterTapped,
                    isShowingFilter = true
                )
            }
        }
    ) { padding ->
        val activeFilters = searchViewModel.chipCategoriesFlow.collectAsState().value.filter { it.isActive }
        val onFilterTapped = searchViewModel::onFilterTapped
        val filteredSubstances = searchViewModel.filteredSubstancesFlow.collectAsState().value
        val filteredCustomSubstances = searchViewModel.filteredCustomSubstancesFlow.collectAsState().value

        Column(modifier = Modifier.padding(padding)) {
            if (activeFilters.isNotEmpty()) {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    items(activeFilters) { categoryChipModel ->
                        CategoryChipDelete(categoryChipModel = categoryChipModel) {
                            onFilterTapped(categoryChipModel.chipName)
                        }
                    }
                }
            }

            if (filteredSubstances.isEmpty() && filteredCustomSubstances.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = getNoResultsMessage(activeFilters),
                        modifier = Modifier.padding(10.dp),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = navigateToAddCustomSubstanceScreen) {
                        Icon(Icons.Outlined.Add, contentDescription = "Add")
                        Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                        Text(text = stringResource(R.string.add_custom_substance))
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(filteredCustomSubstances) { customSubstance ->
                        SubstanceRow(
                            substanceModel = SubstanceModel(
                                name = customSubstance.name,
                                commonNames = emptyList(),
                                categories = listOf(
                                    CategoryModel(
                                        name = stringResource(R.string.custom), color = customColor
                                    )
                                ),
                                hasSaferUse = false,
                                hasInteractions = false
                            ), onTap = {
                                onCustomSubstanceTap(customSubstance.id)
                            })
                    }
                    items(filteredSubstances) { substance ->
                        SubstanceRow(substanceModel = substance, onTap = {
                            onSubstanceTap(substance)
                        })
                    }
                    item {
                        ListItem(
                            headlineContent = { Text(stringResource(R.string.add_custom_substance)) },
                            leadingContent = {
                                Icon(
                                    Icons.Outlined.Add,
                                    contentDescription = "Add"
                                )
                            },
                            modifier = Modifier.clickable(onClick = navigateToAddCustomSubstanceScreen)
                        )
                        HorizontalDivider(
                            Modifier,
                            DividerDefaults.Thickness,
                            DividerDefaults.color
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun getNoResultsMessage(activeFilters: List<CategoryChipModel>): String {
    val activeCategoryNames = activeFilters.filter { it.isActive }.map { it.chipName }
    return when {
        activeCategoryNames.isEmpty() -> stringResource(R.string.no_matching_substance_found)
        activeCategoryNames.size == 1 -> stringResource(
            R.string.no_matching_substance_with_the_tag_found,
            activeCategoryNames[0]
        )
        else -> {
            val names = activeCategoryNames.joinToString(separator = "', '")
            stringResource(R.string.no_matching_substance_with_tags_found, names)
        }
    }
}