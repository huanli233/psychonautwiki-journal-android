package com.isaakhanimann.journal.ui.tabs.journal.addingestion.dose.customrecipe

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.NavigateNext
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.isaakhanimann.journal.R
import com.isaakhanimann.journal.data.room.experiences.entities.CustomUnit
import com.isaakhanimann.journal.data.substances.AdministrationRoute
import com.isaakhanimann.journal.ui.tabs.search.substance.roa.dose.RoaDoseView

@Composable
fun ChooseDoseCustomRecipeScreen(
    viewModel: ChooseDoseCustomRecipeViewModel = hiltViewModel(),
    navigateToFinishScreen: (
        recipeId: Int,
        recipeDose: Double,
        isEstimate: Boolean,
        deviation: Double?,
        notes: String
    ) -> Unit
) {
    ChooseDoseCustomRecipeScreenContent(
        recipeName = viewModel.recipeName,
        customUnits = viewModel.customUnits,
        recipeNote = viewModel.recipeNote,
        recipeUnitPlural = viewModel.recipeUnitPlural,
        administrationRoute = viewModel.administrationRoute,
        subcomponentInfos = viewModel.subcomponentInfos,
        dose = viewModel.dose,
        onDoseChange = viewModel::updateDose,
        isDoseAnEstimate = viewModel.isDoseAnEstimate,
        onIsDoseAnEstimateChange = viewModel::updateIsDoseAnEstimate,
        estimatedDoseStandardDeviation = viewModel.estimatedDoseStandardDeviation,
        onEstimatedDoseStandardDeviationChange = viewModel::updateEstimatedDoseStandardDeviation,
        notes = viewModel.notes,
        onNotesChange = viewModel::updateNotes,
        onNavigateNext = {
            val doseDouble = viewModel.dose.toDoubleOrNull()
            if (doseDouble != null) {
                navigateToFinishScreen(
                    viewModel.recipeId,
                    doseDouble,
                    viewModel.isDoseAnEstimate,
                    if (viewModel.isDoseAnEstimate) viewModel.estimatedDoseStandardDeviation.toDoubleOrNull() else null,
                    viewModel.notes
                )
            }
        },
        isValid = viewModel.isValid(),
        isLoading = viewModel.isLoading
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChooseDoseCustomRecipeScreenContent(
    recipeName: String,
    customUnits: Map<Int, CustomUnit>,
    recipeNote: String,
    recipeUnitPlural: String,
    administrationRoute: AdministrationRoute,
    subcomponentInfos: List<SubcomponentDisplayInfo>,
    dose: String,
    onDoseChange: (String) -> Unit,
    isDoseAnEstimate: Boolean,
    onIsDoseAnEstimateChange: (Boolean) -> Unit,
    estimatedDoseStandardDeviation: String,
    onEstimatedDoseStandardDeviationChange: (String) -> Unit,
    notes: String,
    onNotesChange: (String) -> Unit,
    onNavigateNext: () -> Unit,
    isValid: Boolean,
    isLoading: Boolean
) {
    val focusManager = LocalFocusManager.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = recipeName.ifBlank { stringResource(R.string.choose_recipe) })
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                modifier = Modifier.imePadding(),
                onClick = {
                    if (isValid && !isLoading) onNavigateNext()
                },
                icon = {
                    Icon(
                        Icons.AutoMirrored.Filled.NavigateNext,
                        contentDescription = stringResource(R.string.next)
                    )
                },
                text = { Text(stringResource(R.string.next)) }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            LinearProgressIndicator(
                progress = { 0.67f },
                modifier = Modifier.fillMaxWidth(),
            )
            when {
                isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                subcomponentInfos.isEmpty() && !isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(stringResource(R.string.recipe_not_found))
                    }
                }
                else -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Spacer(modifier = Modifier.height(4.dp))

                        // Recipe Info Card
                        ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(text = recipeName, style = MaterialTheme.typography.titleMedium)
                                if (recipeNote.isNotBlank()) {
                                    Text(
                                        text = recipeNote,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                subcomponentInfos.forEach { info ->
                                    val subcomponent = info.subcomponent
                                    Text(
                                        text = "• ${subcomponent.getDoseDescription(customUnits[info.subcomponent.id])} ${subcomponent.originalUnit} ${subcomponent.substanceName}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        // Dose Input
                        OutlinedTextField(
                            value = dose,
                            onValueChange = onDoseChange,
                            label = { Text(stringResource(R.string.recipe_dose)) },
                            placeholder = { Text(stringResource(R.string.units_of_recipe, recipeUnitPlural)) },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Decimal,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        // Calculated Doses and RoaDoseViews
                        subcomponentInfos.forEach { info ->
                            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    val text = "${info.subcomponent.substanceName} (${administrationRoute.displayText})"
                                    val calculatedDoseText = info.calculatedDose?.let { "%.2f".format(it) } ?: "0.0"

                                    Text(
                                        text = "$text: $calculatedDoseText ${customUnits[info.subcomponent.customUnitId]?.originalUnit ?: info.subcomponent.originalUnit}",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = info.doseClass?.getComposeColor(isSystemInDarkTheme()) ?: Color.Unspecified
                                    )
                                    if (info.roaDose != null) {
                                        RoaDoseView(roaDose = info.roaDose)
                                    }
                                }
                            }
                        }

                        // Estimate Switch
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(stringResource(R.string.is_estimate))
                            Switch(
                                checked = isDoseAnEstimate,
                                onCheckedChange = onIsDoseAnEstimateChange
                            )
                        }

                        // Standard Deviation Input
                        if (isDoseAnEstimate) {
                            OutlinedTextField(
                                value = estimatedDoseStandardDeviation,
                                onValueChange = onEstimatedDoseStandardDeviationChange,
                                label = { Text(stringResource(R.string.estimated_dose_standard_deviation)) },
                                placeholder = { Text(stringResource(R.string.estimated_dose_standard_deviation_hint)) },
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Decimal,
                                    imeAction = ImeAction.Done
                                ),
                                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                        }

                        // Notes Input
                        OutlinedTextField(
                            value = notes,
                            onValueChange = onNotesChange,
                            label = { Text(stringResource(R.string.notes)) },
                            placeholder = { Text(stringResource(R.string.notes_placeholder)) },
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3
                        )

                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }
    }
}