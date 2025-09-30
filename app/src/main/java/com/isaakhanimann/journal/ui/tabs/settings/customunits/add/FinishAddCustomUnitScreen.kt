package com.isaakhanimann.journal.ui.tabs.settings.customunits.add

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.isaakhanimann.journal.R
import com.isaakhanimann.journal.data.room.experiences.entities.CustomUnit
import com.isaakhanimann.journal.data.room.experiences.entities.Ingestion
import com.isaakhanimann.journal.data.room.experiences.entities.PluralizableUnit
import com.isaakhanimann.journal.data.room.experiences.relations.IngestionWithCompanionAndCustomUnit
import com.isaakhanimann.journal.data.substances.AdministrationRoute
import com.isaakhanimann.journal.data.substances.classes.roa.DoseClass
import com.isaakhanimann.journal.data.substances.classes.roa.RoaDose
import com.isaakhanimann.journal.ui.tabs.journal.addingestion.dose.CurrentDoseClassInfo
import com.isaakhanimann.journal.ui.tabs.journal.addingestion.dose.StandardDeviationExplanation
import com.isaakhanimann.journal.ui.tabs.journal.addingestion.search.suggestion.models.toStringWith
import com.isaakhanimann.journal.ui.tabs.journal.experience.components.ingestion.IngestionRow
import com.isaakhanimann.journal.ui.tabs.journal.experience.models.IngestionElement
import com.isaakhanimann.journal.ui.tabs.journal.experience.rating.FloatingDoneButton
import com.isaakhanimann.journal.ui.tabs.search.substance.roa.dose.RoaDosePreviewProvider
import com.isaakhanimann.journal.ui.tabs.search.substance.roa.dose.RoaDoseView
import com.isaakhanimann.journal.ui.utils.getShortTimeWithWeekdayText
import java.time.Instant

// Main Screen Composable
@Composable
fun FinishAddCustomUnitScreen(
    dismissAddCustomUnit: (customUnitId: Int) -> Unit,
    navigateBack: () -> Unit,
    viewModel: FinishAddCustomUnitViewModel = hiltViewModel()
) {
    FinishAddCustomUnitScreenContent(
        substanceName = viewModel.substanceName,
        administrationRoute = viewModel.administrationRoute,
        roaDose = viewModel.roaDose,
        dismiss = { viewModel.createSaveAndDismissAfter(dismiss = dismissAddCustomUnit) },
        navigateBack = navigateBack,
        name = viewModel.name,
        onChangeOfName = viewModel::onChangeOfName,
        doseText = viewModel.doseText,
        onChangeDoseText = viewModel::onChangeOfDose,
        estimatedDoseStandardDeviationText = viewModel.estimatedDoseDeviationText,
        onChangeEstimatedDoseDeviationText = viewModel::onChangeOfEstimatedDoseDeviation,
        isEstimate = viewModel.isEstimate,
        onChangeIsEstimate = viewModel::onChangeOfIsEstimate,
        currentDoseClass = viewModel.currentDoseClass,
        isUnitsFieldShown = viewModel.isUnitsFieldShown,
        unit = viewModel.unit,
        onChangeOfUnits = viewModel::onChangeOfUnit,
        unitPlural = viewModel.unitPlural,
        onChangeOfUnitPlural = viewModel::onChangeOfUnitPlural,
        originalUnit = viewModel.originalUnit,
        onChangeOfOriginalUnit = viewModel::onChangeOfOriginalUnit,
        note = viewModel.note,
        onChangeOfNote = viewModel::onChangeOfNote,
        isArchived = viewModel.isArchived,
        onChangeOfIsArchived = viewModel::onChangeOfIsArchived
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FinishAddCustomUnitScreenContent(
    substanceName: String,
    administrationRoute: AdministrationRoute,
    roaDose: RoaDose?,
    dismiss: () -> Unit,
    navigateBack: () -> Unit,
    name: String,
    onChangeOfName: (String) -> Unit,
    doseText: String,
    onChangeDoseText: (String) -> Unit,
    estimatedDoseStandardDeviationText: String,
    onChangeEstimatedDoseDeviationText: (String) -> Unit,
    isEstimate: Boolean,
    onChangeIsEstimate: (Boolean) -> Unit,
    currentDoseClass: DoseClass?,
    isUnitsFieldShown: Boolean,
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
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.substance_unit_title, substanceName)) },
                navigationIcon = {
                    IconButton(onClick = navigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.navigate_back))
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingDoneButton(
                onDone = dismiss,
                modifier = Modifier.imePadding()
            )
        }
    ) { padding ->
        EditCustomUnitSections(
            padding = padding,
            substanceName = substanceName,
            administrationRoute = administrationRoute,
            numberOfIngestionsWithThisCustomUnit = null,
            roaDose = roaDose,
            name = name,
            onChangeOfName = onChangeOfName,
            doseText = doseText,
            onChangeDoseText = onChangeDoseText,
            estimatedDoseStandardDeviationText = estimatedDoseStandardDeviationText,
            onChangeEstimatedDoseStandardDeviationText = onChangeEstimatedDoseDeviationText,
            isEstimate = isEstimate,
            onChangeIsEstimate = onChangeIsEstimate,
            currentDoseClass = currentDoseClass,
            isShowingUnitsField = isUnitsFieldShown,
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

// Shared Content Composable
@Composable
fun EditCustomUnitSections(
    padding: PaddingValues,
    substanceName: String,
    administrationRoute: AdministrationRoute,
    numberOfIngestionsWithThisCustomUnit: Int?,
    roaDose: RoaDose?,
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
) {
    Column(
        modifier = Modifier
            .padding(padding)
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        val focusRequesterName = remember { FocusRequester() }
        val focusManager = LocalFocusManager.current
        LaunchedEffect(Unit) {
            focusRequesterName.requestFocus()
        }

        if (numberOfIngestionsWithThisCustomUnit != null) {
            InfoCard {
                val pluralizableUnit = PluralizableUnit(
                    singular = stringResource(R.string.ingestion_singular),
                    plural = stringResource(R.string.ingestion_plural)
                )
                val text = if (numberOfIngestionsWithThisCustomUnit > 0) {
                    stringResource(
                        R.string.ingestions_affected_by_edit,
                        numberOfIngestionsWithThisCustomUnit.toStringWith(pluralizableUnit)
                    )
                } else {
                    stringResource(R.string.no_ingestions_using_unit)
                }
                Text(text)
            }
        }

        // Substance-specific info cards
        val substanceInfoResId = when (substanceName) {
            "Cannabis" -> if (administrationRoute == AdministrationRoute.SMOKED) R.array.cannabis_info else null
            "Psilocybin mushrooms" -> R.array.psilocybin_info
            "Alcohol" -> R.array.alcohol_info
            else -> null
        }
        if (substanceInfoResId != null) {
            InfoCard {
                stringArrayResource(id = substanceInfoResId).forEach {
                    Text(it, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        // Main input card
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                val prompt = getPromptFor(substanceName, administrationRoute)
                OutlinedTextField(
                    value = name,
                    onValueChange = onChangeOfName,
                    label = { Text(stringResource(R.string.name)) },
                    placeholder = { Text(stringResource(prompt.name)) },
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next, capitalization = KeyboardCapitalization.Words),
                    modifier = Modifier.fillMaxWidth().focusRequester(focusRequesterName),
                    singleLine = true
                )
                OutlinedTextField(
                    value = unit,
                    onValueChange = onChangeOfUnits,
                    label = { Text(stringResource(R.string.unit_singular)) },
                    placeholder = { Text(stringResource(prompt.unit)) },
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = unitPlural,
                    onValueChange = onChangeOfUnitPlural,
                    label = { Text(stringResource(R.string.unit_plural)) },
                    placeholder = { Text(stringResource(prompt.unitPlural)) },
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = note,
                    onValueChange = onChangeOfNote,
                    label = { Text(stringResource(R.string.note)) },
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next, capitalization = KeyboardCapitalization.Sentences),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Dose calculation card
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (roaDose != null) {
                    RoaDoseView(roaDose = roaDose)
                    AnimatedVisibility(visible = currentDoseClass != null) {
                        if (currentDoseClass != null) {
                            CurrentDoseClassInfo(currentDoseClass, roaDose)
                        }
                    }
                }
                OutlinedTextField(
                    value = doseText,
                    onValueChange = { onChangeDoseText(it.replace(',', '.')) },
                    label = { Text(stringResource(R.string.dose_per_unit, unit.ifBlank { stringResource(R.string.unit) })) },
                    trailingIcon = { Text(originalUnit) },
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                    isError = doseText.toDoubleOrNull() == null,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                if (isShowingUnitsField) {
                    OutlinedTextField(
                        value = originalUnit,
                        onValueChange = onChangeOfOriginalUnit,
                        label = { Text(stringResource(R.string.units)) },
                        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        SuggestionChip(onClick = { onChangeOfOriginalUnit("µg") }, label = { Text("µg") })
                        SuggestionChip(onClick = { onChangeOfOriginalUnit("mg") }, label = { Text("mg") })
                        SuggestionChip(onClick = { onChangeOfOriginalUnit("g") }, label = { Text("g") })
                        SuggestionChip(onClick = { onChangeOfOriginalUnit("mL") }, label = { Text("mL") })
                    }
                }

                Row(
                    modifier = Modifier.toggleable(value = isEstimate, onValueChange = onChangeIsEstimate, role = Role.Switch),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(stringResource(R.string.estimate), style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                    Switch(checked = isEstimate, onCheckedChange = null)
                }
                AnimatedVisibility(visible = isEstimate) {
                    Column {
                        OutlinedTextField(
                            value = estimatedDoseStandardDeviationText,
                            onValueChange = { onChangeEstimatedDoseStandardDeviationText(it.replace(',', '.')) },
                            label = { Text(stringResource(R.string.estimated_standard_deviation_per_unit, unit.ifBlank { stringResource(R.string.unit) })) },
                            trailingIcon = { Text(originalUnit) },
                            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                            isError = estimatedDoseStandardDeviationText.toDoubleOrNull() == null,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        val mean = doseText.toDoubleOrNull()
                        val standardDeviation = estimatedDoseStandardDeviationText.toDoubleOrNull()
                        if (mean != null && standardDeviation != null) {
                            StandardDeviationExplanation(mean = mean, standardDeviation = standardDeviation, unit = originalUnit)
                        }
                    }
                }
            }
        }

        // Preview Card
        if (name.isNotBlank() && unit.isNotBlank()) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        stringResource(R.string.ingestion_sample_preview),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp)
                    )
                    HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)
                    val customUnit = CustomUnit(
                        id = 123, substanceName = substanceName, name = name, administrationRoute = administrationRoute,
                        unit = unit, unitPlural = unitPlural, originalUnit = originalUnit, dose = doseText.toDoubleOrNull(),
                        estimatedDoseStandardDeviation = estimatedDoseStandardDeviationText.toDoubleOrNull(),
                        isEstimate = isEstimate, isArchived = isArchived, note = ""
                    )
                    IngestionRow(
                        ingestionElement = IngestionElement(
                            ingestionWithCompanionAndCustomUnit = IngestionWithCompanionAndCustomUnit(
                                ingestion = Ingestion(
                                    substanceName = substanceName,
                                    dose = 3.0,
                                    time = Instant.now(),
                                    customUnitId = customUnit.id,
                                    administrationRoute = administrationRoute,
                                    units = unit,
                                    experienceId = 1,
                                    notes = null,
                                    consumerName = null,
                                    stomachFullness = null,
                                    isDoseAnEstimate = false,
                                    endTime = null,
                                    estimatedDoseStandardDeviation = null
                                ),
                                substanceCompanion = null, customUnit = customUnit
                            ), roaDuration = null, numDots = null
                        ),
                        areDosageDotsHidden = true,
                    ) {
                        Text(
                            text = Instant.now().getShortTimeWithWeekdayText(),
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            }
        }

        // Archive Card
        ListItem(
            headlineContent = { Text(stringResource(R.string.archive), style = MaterialTheme.typography.titleMedium) },
            supportingContent = { Text(stringResource(R.string.archive_description)) },
            trailingContent = { Switch(checked = isArchived, onCheckedChange = onChangeOfIsArchived) },
            modifier = Modifier.toggleable(value = isArchived, onValueChange = onChangeOfIsArchived, role = Role.Switch)
        )
    }
}

// Helper composable for info cards
@Composable
private fun InfoCard(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.onTertiaryContainer)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                content()
            }
        }
    }
}

// Helper function to get prompts
@Composable
private fun getPromptFor(substanceName: String, route: AdministrationRoute): Prompt {
    return when (substanceName) {
        "Cannabis" -> Prompt(R.string.prompt_cannabis_name, R.string.prompt_cannabis_unit, R.string.prompt_cannabis_unit_plural)
        "Psilocybin mushrooms" -> Prompt(R.string.prompt_mushrooms_name, R.string.prompt_mushrooms_unit, R.string.prompt_mushrooms_unit_plural)
        "Alcohol" -> Prompt(R.string.prompt_alcohol_name, R.string.prompt_alcohol_unit, R.string.prompt_alcohol_unit_plural)
        "Caffeine" -> Prompt(R.string.prompt_caffeine_name, R.string.prompt_caffeine_unit, R.string.prompt_caffeine_unit_plural)
        else -> when (route) {
            AdministrationRoute.ORAL -> Prompt(R.string.prompt_oral_name, R.string.prompt_oral_unit, R.string.prompt_oral_unit_plural)
            AdministrationRoute.SMOKED -> Prompt(R.string.prompt_smoked_name, R.string.prompt_smoked_unit, R.string.prompt_smoked_unit_plural)
            AdministrationRoute.INSUFFLATED -> Prompt(R.string.prompt_insufflated_name, R.string.prompt_insufflated_unit, R.string.prompt_insufflated_unit_plural)
            AdministrationRoute.BUCCAL -> Prompt(R.string.prompt_buccal_name, R.string.prompt_buccal_unit, R.string.prompt_buccal_unit_plural)
            AdministrationRoute.TRANSDERMAL -> Prompt(R.string.prompt_transdermal_name, R.string.prompt_transdermal_unit, R.string.prompt_transdermal_unit_plural)
            else -> Prompt(R.string.prompt_default_name, R.string.prompt_default_unit, R.string.prompt_default_unit_plural)
        }
    }.let { resIds ->
        Prompt(resIds.name, resIds.unit, resIds.unitPlural)
    }
}

data class Prompt(val name: Int, val unit: Int, val unitPlural: Int)
private data class PromptStrings(val name: String, val unit: String, val unitPlural: String)

// Previews
@Preview @Composable private fun FinishAddCustomUnitScreenPreview(@PreviewParameter(RoaDosePreviewProvider::class) roaDose: RoaDose) {
    FinishAddCustomUnitScreenContent(
        substanceName = "Example", administrationRoute = AdministrationRoute.ORAL, roaDose = roaDose,
        dismiss = {}, navigateBack = {}, name = "Pink rocket", onChangeOfName = {}, doseText = "10", onChangeDoseText = {},
        estimatedDoseStandardDeviationText = "", onChangeEstimatedDoseDeviationText = {}, isEstimate = true,
        onChangeIsEstimate = {}, currentDoseClass = DoseClass.LIGHT, isUnitsFieldShown = false, unit = "pill",
        onChangeOfUnits = {}, unitPlural = "pills", onChangeOfUnitPlural = {}, originalUnit = "mg",
        onChangeOfOriginalUnit = {}, note = "", onChangeOfNote = {}, isArchived = false, onChangeOfIsArchived = {}
    )
}