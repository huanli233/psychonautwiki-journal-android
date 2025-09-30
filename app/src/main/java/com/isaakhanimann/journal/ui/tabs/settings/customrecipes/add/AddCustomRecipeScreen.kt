package com.isaakhanimann.journal.ui.tabs.settings.customrecipes.add

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.isaakhanimann.journal.R
import com.isaakhanimann.journal.data.substances.AdministrationRoute

@Composable
fun AddCustomRecipeScreen(
    viewModel: AddCustomRecipeViewModel = hiltViewModel(),
    navigateBack: () -> Unit,
    navigateToSubstanceSelector: (Int) -> Unit
) {
    val allSubstances by viewModel.allSubstances.collectAsState()

    AddCustomRecipeScreenContent(
        recipeName = viewModel.recipeName,
        onRecipeNameChange = viewModel::updateRecipeName,
        administrationRoute = viewModel.administrationRoute,
        onAdministrationRouteChange = viewModel::updateAdministrationRoute,
        unit = viewModel.unit,
        onUnitChange = viewModel::updateUnit,
        unitPlural = viewModel.unitPlural,
        onUnitPluralChange = viewModel::updateUnitPlural,
        note = viewModel.note,
        onNoteChange = viewModel::updateNote,
        subcomponents = viewModel.subcomponents,
        onAddSubcomponent = viewModel::addSubcomponent,
        onRemoveSubcomponent = viewModel::removeSubcomponent,
        onUpdateSubcomponent = viewModel::updateSubcomponent,
        onSave = { viewModel.saveRecipe(navigateBack) },
        isValid = viewModel.isValid(),
        isLoading = viewModel.isLoading,
        allSubstances = allSubstances,
        onSubstanceNameClick = navigateToSubstanceSelector
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCustomRecipeScreenContent(
    recipeName: String,
    onRecipeNameChange: (String) -> Unit,
    administrationRoute: AdministrationRoute,
    onAdministrationRouteChange: (AdministrationRoute) -> Unit,
    unit: String,
    onUnitChange: (String) -> Unit,
    unitPlural: String,
    onUnitPluralChange: (String) -> Unit,
    note: String,
    onNoteChange: (String) -> Unit,
    subcomponents: List<AddCustomRecipeViewModel.SubcomponentData>,
    onAddSubcomponent: () -> Unit,
    onRemoveSubcomponent: (Int) -> Unit,
    onUpdateSubcomponent: (Int, AddCustomRecipeViewModel.SubcomponentData) -> Unit,
    onSave: () -> Unit,
    isValid: Boolean,
    isLoading: Boolean,
    allSubstances: List<String>,
    onSubstanceNameClick: (Int) -> Unit
) {
    var isRouteMenuExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.add_custom_recipe)) }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                OutlinedTextField(
                    value = recipeName,
                    onValueChange = onRecipeNameChange,
                    label = { Text(stringResource(R.string.recipe_name)) },
                    placeholder = { Text(stringResource(R.string.recipe_name_hint)) },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                ExposedDropdownMenuBox(
                    expanded = isRouteMenuExpanded,
                    onExpandedChange = { isRouteMenuExpanded = !isRouteMenuExpanded },
                ) {
                    OutlinedTextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        readOnly = true,
                        value = administrationRoute.displayText,
                        onValueChange = {},
                        label = { Text(stringResource(R.string.administration_route)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isRouteMenuExpanded) },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                    )
                    ExposedDropdownMenu(
                        expanded = isRouteMenuExpanded,
                        onDismissRequest = { isRouteMenuExpanded = false },
                    ) {
                        AdministrationRoute.values().forEach { selectionOption ->
                            DropdownMenuItem(
                                text = { Text(selectionOption.displayText) },
                                onClick = {
                                    onAdministrationRouteChange(selectionOption)
                                    isRouteMenuExpanded = false
                                },
                                contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                            )
                        }
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = unit,
                        onValueChange = onUnitChange,
                        label = { Text(stringResource(R.string.recipe_unit)) },
                        placeholder = { Text(stringResource(R.string.recipe_unit_hint)) },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = unitPlural,
                        onValueChange = onUnitPluralChange,
                        label = { Text(stringResource(R.string.recipe_unit_plural)) },
                        placeholder = { Text(stringResource(R.string.recipe_unit_plural_hint)) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                OutlinedTextField(
                    value = note,
                    onValueChange = onNoteChange,
                    label = { Text(stringResource(R.string.recipe_note)) },
                    placeholder = { Text(stringResource(R.string.recipe_note_hint)) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.subcomponents),
                        style = MaterialTheme.typography.titleMedium
                    )
                    OutlinedButton(onClick = onAddSubcomponent) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.add_subcomponent))
                    }
                }
            }

            itemsIndexed(subcomponents) { index, subcomponent ->
                SubcomponentCard(
                    unit = unit,
                    subcomponent = subcomponent,
                    onUpdate = { updatedSubcomponent ->
                        onUpdateSubcomponent(index, updatedSubcomponent)
                    },
                    onRemove = { onRemoveSubcomponent(index) },
                    onSubstanceNameClick = { onSubstanceNameClick(index) }
                )
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onSave,
                    enabled = isValid && !isLoading,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.save_recipe))
                }
            }
        }
    }
}

@Composable
fun SubcomponentCard(
    unit: String,
    subcomponent: AddCustomRecipeViewModel.SubcomponentData,
    onUpdate: (AddCustomRecipeViewModel.SubcomponentData) -> Unit,
    onRemove: () -> Unit,
    onSubstanceNameClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.subcomponents),
                    style = MaterialTheme.typography.titleSmall
                )
                IconButton(onClick = onRemove) {
                    Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.delete_subcomponent))
                }
            }

            OutlinedTextField(
                value = subcomponent.substanceName,
                onValueChange = { /* No-op */ },
                label = { Text(stringResource(R.string.substance_name)) },
                placeholder = { Text(stringResource(R.string.substance_name_hint)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onSubstanceNameClick),
                readOnly = true,
                enabled = false, // To prevent focus and keyboard
                colors = OutlinedTextFieldDefaults.colors(
                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                    disabledBorderColor = MaterialTheme.colorScheme.outline,
                    disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = subcomponent.dose,
                    onValueChange = { onUpdate(subcomponent.copy(dose = it)) },
                    label = { Text(stringResource(R.string.dose_per_unit, unit)) },
                    placeholder = { Text(stringResource(R.string.dose_per_unit_hint)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = subcomponent.originalUnit,
                    onValueChange = { onUpdate(subcomponent.copy(originalUnit = it)) },
                    label = { Text(stringResource(R.string.original_unit)) },
                    placeholder = { Text(stringResource(R.string.original_unit_hint)) },
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = subcomponent.isEstimate,
                    onCheckedChange = { onUpdate(subcomponent.copy(isEstimate = it)) }
                )
                Text(stringResource(R.string.is_estimate))
            }

            if (subcomponent.isEstimate) {
                OutlinedTextField(
                    value = subcomponent.estimatedDoseStandardDeviation,
                    onValueChange = { onUpdate(subcomponent.copy(estimatedDoseStandardDeviation = it)) },
                    label = { Text(stringResource(R.string.estimated_dose_standard_deviation)) },
                    placeholder = { Text(stringResource(R.string.estimated_dose_standard_deviation_hint)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}


@Preview
@Composable
fun AddCustomRecipeScreenPreview() {
    AddCustomRecipeScreenContent(
        recipeName = "Mixed Capsule",
        onRecipeNameChange = {},
        administrationRoute = AdministrationRoute.ORAL,
        onAdministrationRouteChange = {},
        unit = "capsule",
        onUnitChange = {},
        unitPlural = "capsules",
        onUnitPluralChange = {},
        note = "Sample note",
        onNoteChange = {},
        subcomponents = listOf(
            AddCustomRecipeViewModel.SubcomponentData(
                substanceName = "MDMA",
                dose = "80",
                originalUnit = "mg"
            )
        ),
        onAddSubcomponent = {},
        onRemoveSubcomponent = {},
        onUpdateSubcomponent = { _, _ -> },
        onSave = {},
        isValid = true,
        isLoading = false,
        allSubstances = listOf("MDMA", "LSD", "Caffeine"),
        onSubstanceNameClick = {}
    )
}