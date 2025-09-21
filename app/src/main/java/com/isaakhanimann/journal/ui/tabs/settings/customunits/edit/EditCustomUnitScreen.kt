package com.isaakhanimann.journal.ui.tabs.settings.customunits.edit

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.hilt.navigation.compose.hiltViewModel
import com.isaakhanimann.journal.R
import com.isaakhanimann.journal.data.substances.AdministrationRoute
import com.isaakhanimann.journal.data.substances.classes.roa.DoseClass
import com.isaakhanimann.journal.data.substances.classes.roa.RoaDose
import com.isaakhanimann.journal.ui.tabs.journal.experience.rating.FloatingDoneButton
import com.isaakhanimann.journal.ui.tabs.search.substance.roa.dose.RoaDosePreviewProvider
import com.isaakhanimann.journal.ui.tabs.settings.customunits.add.EditCustomUnitSections

@Composable
fun EditCustomUnitScreen(
    navigateBack: () -> Unit,
    viewModel: EditCustomUnitViewModel = hiltViewModel()
) {
    EditCustomUnitScreenContent(
        substanceName = viewModel.substanceName,
        administrationRoute = viewModel.administrationRoute,
        numberOfIngestionsWithThisCustomUnit = viewModel.numberOfIngestionsWithThisCustomUnit,
        roaDose = viewModel.roaDose,
        dismiss = { viewModel.updateAndDismissAfter(dismiss = navigateBack) },
        navigateBack = navigateBack,
        name = viewModel.name,
        onChangeOfName = viewModel::onChangeOfName,
        doseText = viewModel.doseText,
        onChangeDoseText = viewModel::onChangeOfDose,
        estimatedDoseStandardDeviationText = viewModel.estimatedDoseDeviationText,
        onChangeEstimatedDoseStandardDeviationText = viewModel::onChangeOfEstimatedDoseDeviation,
        isEstimate = viewModel.isEstimate,
        onChangeIsEstimate = viewModel::onChangeOfIsEstimate,
        currentDoseClass = viewModel.currentDoseClass,
        isShowingUnitsField = viewModel.roaDose?.units?.isBlank() ?: true,
        unit = viewModel.unit,
        onChangeOfUnits = viewModel::onChangeOfUnit,
        unitPlural = viewModel.unitPlural,
        onChangeOfUnitPlural = viewModel::onChangeOfUnitPlural,
        originalUnit = viewModel.originalUnit,
        onChangeOfOriginalUnit = viewModel::onChangeOfOriginalUnit,
        note = viewModel.note,
        onChangeOfNote = viewModel::onChangeOfNote,
        isArchived = viewModel.isArchived,
        onChangeOfIsArchived = viewModel::onChangeOfIsArchived,
        onDelete = { viewModel.deleteCustomUnit(navigateBack) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditCustomUnitScreenContent(
    substanceName: String,
    administrationRoute: AdministrationRoute,
    numberOfIngestionsWithThisCustomUnit: Int?,
    roaDose: RoaDose?,
    dismiss: () -> Unit,
    navigateBack: () -> Unit,
    name: String,
    onChangeOfName: (String) -> Unit,
    doseText: String,
    onChangeDoseText: (String) -> Unit,
    estimatedDoseStandardDeviationText: String,
    onChangeEstimatedDoseStandardDeviationText: (String) -> Unit,
    isEstimate: Boolean,
    onChangeIsEstimate: (Boolean) -> Unit,
    currentDoseClass: DoseClass?,
    isShowingUnitsField: Boolean,
    unit: String,
    onChangeOfUnits: (units: String) -> Unit,
    unitPlural: String,
    onChangeOfUnitPlural: (unitPlural: String) -> Unit,
    originalUnit: String,
    onChangeOfOriginalUnit: (String) -> Unit,
    note: String,
    onChangeOfNote: (String) -> Unit,
    isArchived: Boolean,
    onChangeOfIsArchived: (Boolean) -> Unit,
    onDelete: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.edit_custom_unit)) },
                navigationIcon = {
                    IconButton(onClick = navigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.navigate_back))
                    }
                },
                actions = {
                    var isShowingDeleteDialog by remember { mutableStateOf(false) }
                    IconButton(onClick = { isShowingDeleteDialog = true }) {
                        Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.delete_custom_unit_cd))
                    }
                    if (isShowingDeleteDialog) {
                        AlertDialog(
                            onDismissRequest = { isShowingDeleteDialog = false },
                            title = { Text(text = stringResource(R.string.delete_custom_unit_dialog_title)) },
                            text = { Text(stringResource(R.string.delete_custom_unit_dialog_text)) },
                            confirmButton = {
                                TextButton(onClick = { isShowingDeleteDialog = false; onDelete() }) {
                                    Text(stringResource(R.string.delete))
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { isShowingDeleteDialog = false }) {
                                    Text(stringResource(R.string.cancel))
                                }
                            }
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingDoneButton(onDone = dismiss)
        }
    ) { padding ->
        EditCustomUnitSections(
            substanceName = substanceName,
            administrationRoute = administrationRoute,
            numberOfIngestionsWithThisCustomUnit = numberOfIngestionsWithThisCustomUnit,
            padding = padding,
            roaDose = roaDose,
            name = name,
            onChangeOfName = onChangeOfName,
            doseText = doseText,
            onChangeDoseText = onChangeDoseText,
            estimatedDoseStandardDeviationText = estimatedDoseStandardDeviationText,
            onChangeEstimatedDoseStandardDeviationText = onChangeEstimatedDoseStandardDeviationText,
            isEstimate = isEstimate,
            onChangeIsEstimate = onChangeIsEstimate,
            currentDoseClass = currentDoseClass,
            isShowingUnitsField = isShowingUnitsField,
            unit = unit,
            onChangeOfUnits = onChangeOfUnits,
            unitPlural = unitPlural,
            onChangeOfUnitPlural = onChangeOfUnitPlural,
            originalUnit = originalUnit,
            onChangeOfOriginalUnit = onChangeOfOriginalUnit,
            note = note,
            onChangeOfNote = onChangeOfNote,
            isArchived = isArchived,
            onChangeOfIsArchived = onChangeOfIsArchived
        )
    }
}

@Preview @Composable private fun EditCustomUnitScreenPreview(@PreviewParameter(RoaDosePreviewProvider::class) roaDose: RoaDose) {
    EditCustomUnitScreenContent(
        substanceName = "Example", administrationRoute = AdministrationRoute.ORAL, numberOfIngestionsWithThisCustomUnit = 3,
        roaDose = roaDose, dismiss = {}, navigateBack = {}, name = "Pink rocket", onChangeOfName = {}, doseText = "10", onChangeDoseText = {},
        estimatedDoseStandardDeviationText = "", onChangeEstimatedDoseStandardDeviationText = {}, isEstimate = true,
        onChangeIsEstimate = {}, currentDoseClass = DoseClass.LIGHT, isShowingUnitsField = false, unit = "pill",
        onChangeOfUnits = {}, unitPlural = "pills", onChangeOfUnitPlural = {}, originalUnit = "mg",
        onChangeOfOriginalUnit = {}, note = "", onChangeOfNote = {}, isArchived = false, onChangeOfIsArchived = {}, onDelete = {}
    )
}