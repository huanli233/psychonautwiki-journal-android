package com.isaakhanimann.journal.ui.tabs.journal.experience.editingestion

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.isaakhanimann.journal.R
import com.isaakhanimann.journal.data.room.experiences.entities.CustomUnit
import com.isaakhanimann.journal.ui.YOU
import com.isaakhanimann.journal.ui.tabs.journal.addingestion.dose.StandardDeviationExplanation
import com.isaakhanimann.journal.ui.tabs.journal.addingestion.time.IngestionTimePickerOption
import com.isaakhanimann.journal.ui.tabs.journal.addingestion.time.TimePointOrRangePicker
import com.isaakhanimann.journal.ui.tabs.journal.experience.components.CardWithTitle
import com.isaakhanimann.journal.ui.theme.JournalTheme
import kotlinx.coroutines.launch
import java.time.LocalDateTime

@Composable
fun EditIngestionScreen(
    viewModel: EditIngestionViewModel = hiltViewModel(),
    navigateToAddIngestion: () -> Unit,
    navigateBack: () -> Unit
) {
    EditIngestionScreen(
        note = viewModel.note,
        onNoteChange = { viewModel.note = it },
        isEstimate = viewModel.isEstimate,
        onChangeIsEstimate = viewModel::onChangeIsEstimate,
        isKnown = viewModel.isKnown,
        toggleIsKnown = viewModel::toggleIsKnown,
        dose = viewModel.dose,
        onDoseChange = viewModel::onDoseChange,
        estimatedDoseStandardDeviation = viewModel.estimatedDoseStandardDeviation,
        onEstimatedDoseStandardDeviationChange = viewModel::onChangeEstimatedDoseStandardDeviation,
        units = viewModel.units,
        onUnitsChange = { viewModel.units = it },
        experiences = viewModel.relevantExperiences.collectAsState().value,
        selectedExperienceId = viewModel.experienceId,
        onChangeId = { viewModel.experienceId = it },
        navigateBack = navigateBack,
        deleteIngestion = viewModel::deleteIngestion,
        onDone = {
            viewModel.onDoneTap()
            navigateBack()
        },
        ingestionTimePickerOption = viewModel.ingestionTimePickerOptionFlow.collectAsState().value,
        onChangeTimePickerOption = viewModel::onChangeTimePickerOption,
        onChangeStartDateOrTime = viewModel::onChangeStartTime,
        localDateTimeStart = viewModel.localDateTimeStartFlow.collectAsState().value,
        localDateTimeEnd = viewModel.localDateTimeEndFlow.collectAsState().value,
        onChangeEndDateOrTime = viewModel::onChangeEndTime,
        consumerName = viewModel.consumerName,
        onChangeConsumerName = viewModel::onChangeConsumerName,
        consumerNamesSorted = viewModel.sortedConsumerNamesFlow.collectAsState().value,
        customUnit = viewModel.customUnit,
        onCustomUnitChange = viewModel::onChangeCustomUnit,
        otherCustomUnits = viewModel.otherCustomUnits.collectAsState().value,
        addIngestionWithClonedTime = {
            viewModel.saveClonedIngestionTime()
            navigateBack()
            navigateToAddIngestion()
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditIngestionScreen(
    note: String,
    onNoteChange: (String) -> Unit,
    isEstimate: Boolean,
    onChangeIsEstimate: (Boolean) -> Unit,
    isKnown: Boolean,
    toggleIsKnown: () -> Unit,
    dose: String,
    onDoseChange: (String) -> Unit,
    estimatedDoseStandardDeviation: String,
    onEstimatedDoseStandardDeviationChange: (String) -> Unit,
    units: String,
    onUnitsChange: (String) -> Unit,
    experiences: List<ExperienceOption>,
    selectedExperienceId: Int,
    onChangeId: (Int) -> Unit,
    navigateBack: () -> Unit,
    deleteIngestion: () -> Unit,
    onDone: () -> Unit,
    ingestionTimePickerOption: IngestionTimePickerOption,
    onChangeTimePickerOption: (option: IngestionTimePickerOption) -> Unit,
    onChangeStartDateOrTime: (LocalDateTime) -> Unit,
    localDateTimeStart: LocalDateTime,
    onChangeEndDateOrTime: (LocalDateTime) -> Unit,
    localDateTimeEnd: LocalDateTime,
    consumerName: String,
    onChangeConsumerName: (String) -> Unit,
    consumerNamesSorted: List<String>,
    customUnit: CustomUnit?,
    onCustomUnitChange: (CustomUnit?) -> Unit,
    otherCustomUnits: List<CustomUnit>,
    addIngestionWithClonedTime: () -> Unit
) {
    var isPresentingBottomSheet by rememberSaveable { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    var isShowingDeleteDialog by remember { mutableStateOf(false) }

    if (isShowingDeleteDialog) {
        AlertDialog(
            onDismissRequest = { isShowingDeleteDialog = false },
            title = { Text(text = stringResource(R.string.delete_ingestion_dialog_title)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        isShowingDeleteDialog = false
                        deleteIngestion()
                        navigateBack()
                    }
                ) { Text(stringResource(R.string.delete)) }
            },
            dismissButton = {
                TextButton(onClick = { isShowingDeleteDialog = false }) { Text(stringResource(R.string.cancel)) }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.edit_ingestion)) },
                actions = {
                    IconButton(onClick = { isShowingDeleteDialog = true }) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = stringResource(R.string.delete_ingestion_content_description)
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                modifier = Modifier.imePadding(),
                onClick = onDone,
                icon = { Icon(Icons.Filled.Done, contentDescription = stringResource(R.string.done_icon_content_description)) },
                text = { Text(stringResource(R.string.done)) }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            val focusManager = LocalFocusManager.current
            val title = customUnit?.let { stringResource(R.string.dose_with_unit_name, it.name) } ?: stringResource(R.string.dose)

            Spacer(modifier = Modifier.height(0.dp))
            CardWithTitle(title = title) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .toggleable(
                                value = isKnown,
                                onValueChange = { toggleIsKnown() },
                                role = Role.Checkbox
                            )
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(checked = isKnown, onCheckedChange = null)
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.dose_is_known))
                    }
                    AnimatedVisibility(visible = isKnown) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            if (customUnit == null) {
                                OutlinedTextField(
                                    value = units,
                                    onValueChange = onUnitsChange,
                                    label = { Text(text = stringResource(R.string.units)) },
                                    modifier = Modifier.fillMaxWidth(),
                                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                                    singleLine = true
                                )
                            }
                            OutlinedTextField(
                                value = dose,
                                onValueChange = { onDoseChange(it.replace(',', '.')) },
                                label = { Text(text = stringResource(R.string.dose)) },
                                trailingIcon = { Text(text = units) },
                                modifier = Modifier.fillMaxWidth(),
                                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                                isError = dose.toDoubleOrNull() == null,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .toggleable(
                                        value = isEstimate,
                                        onValueChange = onChangeIsEstimate,
                                        role = Role.Switch
                                    )
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Switch(checked = isEstimate, onCheckedChange = null)
                                Spacer(Modifier.width(8.dp))
                                Text(stringResource(R.string.estimate), style = MaterialTheme.typography.bodyLarge)
                            }
                            AnimatedVisibility(visible = isEstimate) {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedTextField(
                                        value = estimatedDoseStandardDeviation,
                                        onValueChange = { onEstimatedDoseStandardDeviationChange(it.replace(',', '.')) },
                                        label = { Text(stringResource(R.string.estimated_standard_deviation)) },
                                        trailingIcon = { Text(text = units, modifier = Modifier.padding(horizontal = 16.dp)) },
                                        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                                        isError = estimatedDoseStandardDeviation.toDoubleOrNull() == null,
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    val mean = dose.toDoubleOrNull()
                                    val standardDeviation = estimatedDoseStandardDeviation.toDoubleOrNull()
                                    if (mean != null && standardDeviation != null) {
                                        StandardDeviationExplanation(
                                            mean = mean,
                                            standardDeviation = standardDeviation,
                                            unit = units
                                        )
                                    }
                                }
                            }
                            if (otherCustomUnits.isNotEmpty()) {
                                var isShowingDropDownMenu by remember { mutableStateOf(false) }
                                Box {
                                    OutlinedButton(
                                        onClick = { isShowingDropDownMenu = true },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(text = stringResource(R.string.unit_display, customUnit?.name ?: stringResource(R.string.default_unit)))
                                    }
                                    DropdownMenu(
                                        expanded = isShowingDropDownMenu,
                                        onDismissRequest = { isShowingDropDownMenu = false }
                                    ) {
                                        otherCustomUnits.forEach { unit ->
                                            DropdownMenuItem(
                                                text = { Text(unit.name) },
                                                onClick = {
                                                    onCustomUnitChange(unit)
                                                    isShowingDropDownMenu = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    customUnit?.let {
                        if (it.note.isNotBlank()) {
                            Text("${it.name}: ${it.note}", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }

            CardWithTitle(title = stringResource(R.string.ingestion_notes)) {
                OutlinedTextField(
                    value = note,
                    onValueChange = onNoteChange,
                    label = { Text(text = stringResource(R.string.notes)) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        capitalization = KeyboardCapitalization.Sentences
                    ),
                    singleLine = true
                )
            }

            CardWithTitle(title = stringResource(R.string.time)) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    TimePointOrRangePicker(
                        onChangeTimePickerOption = onChangeTimePickerOption,
                        ingestionTimePickerOption = ingestionTimePickerOption,
                        localDateTimeStart = localDateTimeStart,
                        onChangeStartDateOrTime = onChangeStartDateOrTime,
                        localDateTimeEnd = localDateTimeEnd,
                        onChangeEndDateOrTime = onChangeEndDateOrTime
                    )
                    var isShowingDropDownMenu by remember { mutableStateOf(false) }
                    Box {
                        val selectedOption = experiences.firstOrNull { it.id == selectedExperienceId }
                        OutlinedButton(
                            onClick = { isShowingDropDownMenu = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(text = if (selectedOption?.title != null) stringResource(R.string.part_of, selectedOption.title) else stringResource(
                                R.string.part_of_unknown_experience
                            ))
                        }
                        DropdownMenu(
                            expanded = isShowingDropDownMenu,
                            onDismissRequest = { isShowingDropDownMenu = false }
                        ) {
                            experiences.forEach { experience ->
                                DropdownMenuItem(
                                    text = { Text(experience.title) },
                                    onClick = {
                                        onChangeId(experience.id)
                                        isShowingDropDownMenu = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
            CardWithTitle(title = stringResource(R.string.consumer)) {
                var showNewConsumerTextField by remember { mutableStateOf(false) }
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = stringResource(R.string.consumed_by, consumerName.ifBlank { YOU }),
                        style = MaterialTheme.typography.bodyLarge
                    )
                    if (consumerNamesSorted.isNotEmpty() || consumerName.isNotBlank()) {
                        TextButton(onClick = { isPresentingBottomSheet = true }) {
                            Text(text = stringResource(R.string.choose_other_consumer))
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .toggleable(
                                value = showNewConsumerTextField,
                                onValueChange = { showNewConsumerTextField = it },
                                role = Role.Switch
                            )
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Switch(checked = showNewConsumerTextField, onCheckedChange = null)
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.enter_new_consumer))
                    }

                    AnimatedVisibility(visible = showNewConsumerTextField) {
                        OutlinedTextField(
                            value = consumerName,
                            onValueChange = onChangeConsumerName,
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = stringResource(R.string.consumer_icon_content_description)) },
                            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                            keyboardOptions = KeyboardOptions.Default.copy(
                                imeAction = ImeAction.Done,
                                capitalization = KeyboardCapitalization.Words
                            ),
                            placeholder = { Text(stringResource(R.string.new_consumer_name)) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            FilledTonalButton(
                onClick = addIngestionWithClonedTime,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Outlined.Add, contentDescription = stringResource(R.string.add_content_description))
                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                Text(text = stringResource(R.string.add_ingestion_at_same_time))
            }
        }
    }

    if (isPresentingBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { isPresentingBottomSheet = false },
            sheetState = bottomSheetState,
        ) {
            LazyColumn(modifier = Modifier.padding(bottom = 32.dp)) {
                item {
                    ListItem(
                        headlineContent = { Text(YOU) },
                        leadingContent = { Icon(Icons.Default.Person, contentDescription = stringResource(R.string.consumer_icon_content_description)) },
                        modifier = Modifier.clickable {
                            onChangeConsumerName("")
                            scope.launch { bottomSheetState.hide() }.invokeOnCompletion {
                                if (!bottomSheetState.isVisible) isPresentingBottomSheet = false
                            }
                        }
                    )
                }
                items(consumerNamesSorted) { name ->
                    ListItem(
                        headlineContent = { Text(name) },
                        leadingContent = { Icon(Icons.Default.Person, contentDescription = stringResource(R.string.consumer_icon_content_description)) },
                        modifier = Modifier.clickable {
                            onChangeConsumerName(name)
                            scope.launch { bottomSheetState.hide() }.invokeOnCompletion {
                                if (!bottomSheetState.isVisible) isPresentingBottomSheet = false
                            }
                        }
                    )
                }
            }
        }
    }
}


@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun EditIngestionScreenPreview() {
    JournalTheme {
        EditIngestionScreen(
            note = "This is my note",
            onNoteChange = {},
            isEstimate = false,
            onChangeIsEstimate = {},
            isKnown = true,
            toggleIsKnown = {},
            dose = "5",
            onDoseChange = {},
            estimatedDoseStandardDeviation = "",
            onEstimatedDoseStandardDeviationChange = {},
            units = "mg",
            onUnitsChange = {},
            experiences = emptyList(),
            selectedExperienceId = 2,
            onChangeId = {},
            navigateBack = {},
            deleteIngestion = {},
            onDone = {},
            ingestionTimePickerOption = IngestionTimePickerOption.POINT_IN_TIME,
            onChangeTimePickerOption = {},
            onChangeStartDateOrTime = {},
            localDateTimeStart = LocalDateTime.now(),
            localDateTimeEnd = LocalDateTime.now(),
            onChangeEndDateOrTime = {},
            consumerName = "",
            onChangeConsumerName = {},
            consumerNamesSorted = listOf("Dave", "Ali"),
            customUnit = null,
            onCustomUnitChange = {},
            otherCustomUnits = emptyList(),
            addIngestionWithClonedTime = {}
        )
    }
}