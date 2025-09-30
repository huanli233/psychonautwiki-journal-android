package com.isaakhanimann.journal.ui.tabs.journal.addingestion.search

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.isaakhanimann.journal.R
import com.isaakhanimann.journal.data.room.experiences.entities.AdaptiveColor
import com.isaakhanimann.journal.data.room.experiences.entities.CustomSubstance
import com.isaakhanimann.journal.data.room.experiences.entities.CustomUnit
import com.isaakhanimann.journal.data.room.experiences.entities.SubstanceColor
import com.isaakhanimann.journal.data.substances.AdministrationRoute
import com.isaakhanimann.journal.ui.tabs.journal.addingestion.search.suggestion.SuggestionRow
import com.isaakhanimann.journal.ui.tabs.search.SubstanceModel
import com.isaakhanimann.journal.data.room.experiences.relations.CustomRecipeWithSubcomponents

@Composable
fun AddIngestionSearchScreen(
    navigateToCheckInteractions: (substanceName: String) -> Unit,
    navigateToCheckSaferUse: (substanceName: String) -> Unit,
    navigateToChooseRoute: (substanceName: String) -> Unit,
    navigateToDose: (substanceName: String, route: AdministrationRoute) -> Unit,
    navigateToChooseCustomSubstanceDose: (customSubstanceName: String, route: AdministrationRoute) -> Unit,
    navigateToChooseTime: (substanceName: String, route: AdministrationRoute, dose: Double?, units: String?, isEstimate: Boolean, estimatedDoseStandardDeviation: Double?, customUnitId: Int?) -> Unit,
    navigateToCustomSubstanceChooseRoute: (customSubstanceName: String) -> Unit,
    navigateToCustomUnitChooseDose: (customUnitId: Int) -> Unit,
    navigateToAddCustomSubstanceScreen: (searchText: String) -> Unit,
    navigateToChooseDoseCustomRecipe: (customRecipeId: Int) -> Unit = {},
    viewModel: AddIngestionSearchViewModel = hiltViewModel()
) {
    val searchText by viewModel.searchTextFlow.collectAsState()
    val customUnitsMap by viewModel.customUnitsMapFlow.collectAsState()

    AddIngestionSearchScreen(
        navigateToCheckInteractions = navigateToCheckInteractions,
        navigateToCheckSaferUse = navigateToCheckSaferUse,
        navigateToChooseRoute = navigateToChooseRoute,
        navigateToCustomDose = navigateToChooseCustomSubstanceDose,
        navigateToCustomSubstanceChooseRoute = navigateToCustomSubstanceChooseRoute,
        navigateToChooseTime = navigateToChooseTime,
        navigateToDose = navigateToDose,
        navigateToAddCustomSubstanceScreen = {
            navigateToAddCustomSubstanceScreen(searchText)
        },
        navigateToCustomUnitChooseDose = navigateToCustomUnitChooseDose,
        quickLogItems = viewModel.quickLogItemsFlow.collectAsState().value,
        searchText = searchText,
        onChangeSearchText = viewModel::updateSearchText,
        filteredSubstances = viewModel.filteredSubstancesFlow.collectAsState().value,
        filteredCustomUnits = viewModel.filteredCustomUnitsFlow.collectAsState().value,
        filteredCustomRecipes = viewModel.filteredCustomRecipesFlow.collectAsState().value,
        filteredCustomSubstances = viewModel.filteredCustomSubstancesFlow.collectAsState().value,
        navigateToChooseDoseCustomRecipe = navigateToChooseDoseCustomRecipe,
        customUnitsMap = customUnitsMap
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AddIngestionSearchScreen(
    navigateToCheckInteractions: (substanceName: String) -> Unit,
    navigateToChooseRoute: (substanceName: String) -> Unit,
    navigateToCheckSaferUse: (substanceName: String) -> Unit,
    navigateToDose: (substanceName: String, route: AdministrationRoute) -> Unit,
    navigateToCustomDose: (customSubstanceName: String, route: AdministrationRoute) -> Unit,
    navigateToChooseTime: (substanceName: String, route: AdministrationRoute, dose: Double?, units: String?, isEstimate: Boolean, estimatedDoseStandardDeviation: Double?, customUnitId: Int?) -> Unit,
    navigateToCustomSubstanceChooseRoute: (customSubstanceName: String) -> Unit,
    navigateToAddCustomSubstanceScreen: () -> Unit,
    navigateToCustomUnitChooseDose: (customUnitId: Int) -> Unit,
    quickLogItems: List<QuickLogItem>,
    searchText: String,
    onChangeSearchText: (searchText: String) -> Unit,
    filteredSubstances: List<SubstanceModel>,
    filteredCustomUnits: List<CustomUnit>,
    filteredCustomRecipes: List<CustomRecipeWithSubcomponents>,
    filteredCustomSubstances: List<CustomSubstance>,
    navigateToChooseDoseCustomRecipe: (customRecipeId: Int) -> Unit,
    customUnitsMap: Map<Int, CustomUnit>
) {
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    Scaffold(
        topBar = {
            Surface(shadowElevation = 2.dp) {
                Column(modifier = Modifier.statusBarsPadding()) {
                    LinearProgressIndicator(
                        progress = { 0.17f },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    OutlinedTextField(
                        value = searchText,
                        onValueChange = onChangeSearchText,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .focusRequester(focusRequester),
                        placeholder = { Text(text = stringResource(R.string.search_substances)) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = stringResource(R.string.search)) },
                        trailingIcon = {
                            if (searchText.isNotEmpty()) {
                                IconButton(onClick = { onChangeSearchText("") }) {
                                    Icon(Icons.Default.Close, contentDescription = stringResource(R.string.close))
                                }
                            }
                        },
                        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                        keyboardOptions = KeyboardOptions.Default.copy(
                            autoCorrectEnabled = false,
                            imeAction = ImeAction.Done,
                            capitalization = KeyboardCapitalization.Words,
                        ),
                        singleLine = true,
                        shape = CircleShape
                    )
                }
            }
        }
    ) { padding ->
        LazyColumn(contentPadding = padding) {
            if (quickLogItems.isNotEmpty()) {
                stickyHeader { SectionHeader(title = stringResource(R.string.quick_logging)) }
            }
            itemsIndexed(quickLogItems) { index, item ->
                when (item) {
                    is QuickLogItem.SuggestionItem -> {
                        SuggestionRow(
                            suggestion = item.suggestion,
                            navigateToDose = navigateToDose,
                            navigateToCustomUnitChooseDose = navigateToCustomUnitChooseDose,
                            navigateToCustomDose = navigateToCustomDose,
                            navigateToChooseTime = navigateToChooseTime
                        )
                    }
                    is QuickLogItem.RecipeItem -> {
                        CustomRecipeRowAddIngestion(
                            customRecipeWithSubcomponents = item.recipe,
                            customUnitsMap = customUnitsMap,
                            navigateToChooseDoseCustomRecipe = navigateToChooseDoseCustomRecipe
                        )
                    }
                }
                if (index < quickLogItems.size - 1) {
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        thickness = DividerDefaults.Thickness,
                        color = DividerDefaults.color
                    )
                }
            }

            if (filteredCustomSubstances.isNotEmpty()) {
                stickyHeader { SectionHeader(title = stringResource(R.string.custom_substances)) }
            }
            items(filteredCustomSubstances) { customSubstance ->
                SubstanceRowAddIngestion(
                    substanceModel = SubstanceModel(
                        name = customSubstance.name,
                        commonNames = emptyList(),
                        categories = emptyList(),
                        hasSaferUse = false,
                        hasInteractions = false
                    ), onTap = { navigateToCustomSubstanceChooseRoute(customSubstance.name) })
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    thickness = DividerDefaults.Thickness,
                    color = DividerDefaults.color
                )
            }

            if (filteredCustomUnits.isNotEmpty()) {
                stickyHeader { SectionHeader(title = stringResource(R.string.custom_units)) }
            }
            items(filteredCustomUnits) { customUnit ->
                CustomUnitRowAddIngestion(
                    customUnit = customUnit,
                    navigateToCustomUnitChooseDose = navigateToCustomUnitChooseDose
                )
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    thickness = DividerDefaults.Thickness,
                    color = DividerDefaults.color
                )
            }

            if (filteredCustomRecipes.isNotEmpty()) {
                stickyHeader { SectionHeader(title = stringResource(R.string.custom_recipes)) }
            }
            items(filteredCustomRecipes) { customRecipe ->
                CustomRecipeRowAddIngestion(
                    customRecipeWithSubcomponents = customRecipe,
                    navigateToChooseDoseCustomRecipe = navigateToChooseDoseCustomRecipe,
                    customUnitsMap = customUnitsMap
                )
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    thickness = DividerDefaults.Thickness,
                    color = DividerDefaults.color
                )
            }

            if (filteredSubstances.isNotEmpty()) {
                stickyHeader { SectionHeader(title = stringResource(R.string.substances)) }
            }
            items(filteredSubstances) { substance ->
                SubstanceRowAddIngestion(substanceModel = substance, onTap = {
                    when {
                        substance.hasSaferUse -> navigateToCheckSaferUse(substance.name)
                        substance.hasInteractions -> navigateToCheckInteractions(substance.name)
                        else -> navigateToChooseRoute(substance.name)
                    }
                })
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    thickness = DividerDefaults.Thickness,
                    color = DividerDefaults.color
                )
            }

            item {
                ListItem(
                    headlineContent = { Text(text = stringResource(R.string.add_custom_substance)) },
                    leadingContent = {
                        Icon(
                            Icons.Outlined.Add,
                            contentDescription = stringResource(R.string.add)
                        )
                    },
                    modifier = Modifier.clickable(onClick = navigateToAddCustomSubstanceScreen)
                )
                HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)
            }

            item {
                if (filteredSubstances.isEmpty() && filteredCustomSubstances.isEmpty() && quickLogItems.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 50.dp), contentAlignment = Alignment.Center
                    ) {
                        Text(stringResource(R.string.no_matching_substance_found))
                    }
                }
            }
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .fillMaxWidth(),
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun ColorCircle(color: SubstanceColor) {
    Surface(
        shape = CircleShape,
        color = color.toColor(),
        modifier = Modifier.size(25.dp)
    ) {}
}