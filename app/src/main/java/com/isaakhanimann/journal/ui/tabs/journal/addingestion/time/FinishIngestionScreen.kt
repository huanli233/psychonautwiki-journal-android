package com.isaakhanimann.journal.ui.tabs.journal.addingestion.time

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.isaakhanimann.journal.R
import com.isaakhanimann.journal.data.room.experiences.entities.AdaptiveColor
import com.isaakhanimann.journal.data.room.experiences.relations.ExperienceWithIngestions
import com.isaakhanimann.journal.ui.YOU
import com.isaakhanimann.journal.ui.tabs.journal.experience.components.CardWithTitle
import com.isaakhanimann.journal.ui.tabs.journal.experience.rating.FloatingDoneButton
import java.time.LocalDateTime

@Composable
fun FinishIngestionScreen(
    dismissAddIngestionScreens: () -> Unit,
    viewModel: FinishIngestionScreenViewModel = hiltViewModel()
) {
    val localDateTime = viewModel.localDateTimeStartFlow.collectAsState().value
    FinishIngestionScreen(
        createSaveAndDismissAfter = {
            viewModel.createSaveAndDismissAfter(dismiss = dismissAddIngestionScreens)
        },
        ingestionTimePickerOption = viewModel.ingestionTimePickerOptionFlow.collectAsState().value,
        onChangeTimePickerOption = viewModel::onChangeTimePickerOption,
        onChangeStartDateOrTime = viewModel::onChangeStartDateOrTime,
        localDateTimeStart = localDateTime,
        localDateTimeEnd = viewModel.localDateTimeEndFlow.collectAsState().value,
        onChangeEndDateOrTime = viewModel::onChangeEndDateOrTime,
        isLoadingColor = viewModel.isLoadingColor,
        isShowingColorPicker = viewModel.isShowingColorPicker,
        selectedColor = viewModel.selectedColor,
        onChangeColor = { viewModel.selectedColor = it },
        alreadyUsedColors = viewModel.alreadyUsedColorsFlow.collectAsState().value,
        otherColors = viewModel.otherColorsFlow.collectAsState().value,
        previousNotes = viewModel.previousNotesFlow.collectAsState().value,
        note = viewModel.note,
        onNoteChange = {
            viewModel.note = it
        },
        experiencesInRange = viewModel.experiencesInRangeFlow.collectAsState().value,
        selectedExperience = viewModel.selectedExperienceFlow.collectAsState().value,
        onChangeOfSelectedExperience = viewModel::onChangeOfSelectedExperience,
        substanceName = viewModel.substanceName,
        enteredTitle = viewModel.enteredTitle,
        onChangeOfEnteredTitle = viewModel::changeTitle,
        isEnteredTitleOk = viewModel.isEnteredTitleOk,
        consumerName = viewModel.consumerName,
        onChangeOfConsumerName = viewModel::changeConsumerName,
        consumerNamesSorted = viewModel.sortedConsumerNamesFlow.collectAsState().value
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinishIngestionScreen(
    createSaveAndDismissAfter: () -> Unit,
    ingestionTimePickerOption: IngestionTimePickerOption,
    onChangeTimePickerOption: (option: IngestionTimePickerOption) -> Unit,
    onChangeStartDateOrTime: (LocalDateTime) -> Unit,
    localDateTimeStart: LocalDateTime,
    onChangeEndDateOrTime: (LocalDateTime) -> Unit,
    localDateTimeEnd: LocalDateTime,
    isLoadingColor: Boolean,
    isShowingColorPicker: Boolean,
    selectedColor: AdaptiveColor,
    onChangeColor: (AdaptiveColor) -> Unit,
    alreadyUsedColors: List<AdaptiveColor>,
    otherColors: List<AdaptiveColor>,
    previousNotes: List<String>,
    note: String,
    onNoteChange: (String) -> Unit,
    experiencesInRange: List<ExperienceWithIngestions>,
    selectedExperience: ExperienceWithIngestions?,
    onChangeOfSelectedExperience: (ExperienceWithIngestions?) -> Unit,
    substanceName: String,
    enteredTitle: String,
    onChangeOfEnteredTitle: (String) -> Unit,
    isEnteredTitleOk: Boolean,
    consumerName: String,
    onChangeOfConsumerName: (String) -> Unit,
    consumerNamesSorted: List<String>
) {
    val focusManager = LocalFocusManager.current
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("$substanceName ingestion") }
            )
        },
        floatingActionButton = {
            AnimatedVisibility(
                visible = !isLoadingColor,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                FloatingDoneButton(
                    onDone = createSaveAndDismissAfter,
                    modifier = Modifier.imePadding()
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            LinearProgressIndicator(
                progress = { 1.0f },
                modifier = Modifier.fillMaxWidth(),
            )
            Column(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Spacer(modifier = Modifier.height(0.dp))
                CardWithTitle(title = stringResource(R.string.time)) {
                    TimePointOrRangePicker(
                        onChangeTimePickerOption = onChangeTimePickerOption,
                        ingestionTimePickerOption = ingestionTimePickerOption,
                        localDateTimeStart = localDateTimeStart,
                        onChangeStartDateOrTime = onChangeStartDateOrTime,
                        localDateTimeEnd = localDateTimeEnd,
                        onChangeEndDateOrTime = onChangeEndDateOrTime
                    )
                }

                CardWithTitle(title = stringResource(R.string.experience)) {
                    var isShowingDropDownMenu by remember { mutableStateOf(false) }
                    Box {
                        OutlinedButton(
                            onClick = { isShowingDropDownMenu = true },
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            val selectedExperienceTitle = selectedExperience?.experience?.title
                            Text(
                                text = if (selectedExperienceTitle != null) stringResource(
                                    R.string.part_of,
                                    selectedExperienceTitle
                                ) else stringResource(R.string.part_of_new_experience),
                                modifier = Modifier.weight(1f)
                            )
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                        }
                        DropdownMenu(
                            expanded = isShowingDropDownMenu,
                            onDismissRequest = { isShowingDropDownMenu = false },
                            modifier = Modifier.fillMaxWidth(0.8f)
                        ) {
                            experiencesInRange.forEach { experienceWithIngestions ->
                                val experience = experienceWithIngestions.experience
                                DropdownMenuItem(
                                    text = { Text(experience.title) },
                                    onClick = {
                                        onChangeOfSelectedExperience(experienceWithIngestions)
                                        isShowingDropDownMenu = false
                                    }
                                )
                            }
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.new_experience)) },
                                onClick = {
                                    onChangeOfSelectedExperience(null)
                                    isShowingDropDownMenu = false
                                }
                            )
                        }
                    }
                    AnimatedVisibility(visible = selectedExperience == null) {
                        OutlinedTextField(
                            value = enteredTitle,
                            onValueChange = onChangeOfEnteredTitle,
                            singleLine = true,
                            label = { Text(text = stringResource(R.string.new_experience_title)) },
                            isError = !isEnteredTitleOk,
                            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                            keyboardOptions = KeyboardOptions.Default.copy(
                                imeAction = ImeAction.Done,
                                capitalization = KeyboardCapitalization.Words
                            ),
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                        )
                    }
                }

                CardWithTitle(title = stringResource(id = R.string.consumer)) {
                    var consumerDropdownExpanded by remember { mutableStateOf(false) }
                    var showNewConsumerField by remember { mutableStateOf(false) }

                    Text(
                        text = stringResource(R.string.consumed_by, consumerName.ifBlank { YOU }),
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    if (consumerNamesSorted.isNotEmpty()) {
                        Box {
                            TextButton(onClick = { consumerDropdownExpanded = true }) {
                                Text(text = stringResource(R.string.choose_other_consumer))
                            }
                            DropdownMenu(
                                expanded = consumerDropdownExpanded,
                                onDismissRequest = { consumerDropdownExpanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text(YOU) },
                                    onClick = {
                                        onChangeOfConsumerName("")
                                        consumerDropdownExpanded = false
                                    },
                                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) }
                                )
                                consumerNamesSorted.forEach { name ->
                                    DropdownMenuItem(
                                        text = { Text(name) },
                                        onClick = {
                                            onChangeOfConsumerName(name)
                                            consumerDropdownExpanded = false
                                        },
                                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) }
                                    )
                                }
                            }
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { showNewConsumerField = !showNewConsumerField }
                    ) {
                        Switch(checked = showNewConsumerField, onCheckedChange = { showNewConsumerField = it })
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.enter_new_consumer))
                    }

                    AnimatedVisibility(visible = showNewConsumerField) {
                        OutlinedTextField(
                            value = consumerName,
                            onValueChange = onChangeOfConsumerName,
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                            keyboardOptions = KeyboardOptions.Default.copy(
                                imeAction = ImeAction.Done,
                                capitalization = KeyboardCapitalization.Words
                            ),
                            placeholder = { Text(stringResource(R.string.new_consumer_name)) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                        )
                    }
                }

                CardWithTitle(title = stringResource(R.string.ingestion_note)) {
                    NoteSection(previousNotes, note, onNoteChange)
                }

                if (isShowingColorPicker) {
                    CardWithTitle(title = stringResource(R.string.something_color, substanceName)) {
                        ColorPicker(
                            selectedColor = selectedColor,
                            onChangeOfColor = onChangeColor,
                            alreadyUsedColors = alreadyUsedColors,
                            otherColors = otherColors
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun NoteSection(
    previousNotes: List<String>,
    note: String,
    onNoteChange: (String) -> Unit
) {
    var isShowingSuggestions by remember { mutableStateOf(true) }
    val focusManager = LocalFocusManager.current

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = note,
            onValueChange = onNoteChange,
            label = { Text(text = stringResource(R.string.notes)) },
            keyboardActions = KeyboardActions(onDone = {
                focusManager.clearFocus()
                isShowingSuggestions = false
            }),
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = ImeAction.Done,
                capitalization = KeyboardCapitalization.Sentences
            ),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        if (previousNotes.isNotEmpty() && isShowingSuggestions) {
            Text(
                text = stringResource(R.string.suggestions),
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(top = 8.dp)
            )
            Card {
                Column {
                    previousNotes.forEachIndexed { index, item ->
                        ListItem(
                            headlineContent = {
                                Text(text = item, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            },
                            leadingContent = {
                                Icon(Icons.Filled.ContentCopy, contentDescription = stringResource(R.string.copy))
                            },
                            modifier = Modifier.clickable {
                                focusManager.clearFocus()
                                isShowingSuggestions = false
                                onNoteChange(item)
                            }
                        )
                        if (index < previousNotes.lastIndex) {
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                thickness = DividerDefaults.Thickness,
                                color = DividerDefaults.color
                            )
                        }
                    }
                }
            }
        }
    }
}


@Preview
@Composable
fun FinishIngestionScreenPreview() {
    val alreadyUsedColors = listOf(AdaptiveColor.BLUE, AdaptiveColor.PINK)
    val otherColors = AdaptiveColor.entries.filter { color ->
        !alreadyUsedColors.contains(color)
    }
    FinishIngestionScreen(
        createSaveAndDismissAfter = {},
        ingestionTimePickerOption = IngestionTimePickerOption.POINT_IN_TIME,
        onChangeTimePickerOption = {},
        onChangeStartDateOrTime = {},
        localDateTimeStart = LocalDateTime.now(),
        localDateTimeEnd = LocalDateTime.now(),
        onChangeEndDateOrTime = {},
        isLoadingColor = false,
        isShowingColorPicker = true,
        selectedColor = AdaptiveColor.BLUE,
        onChangeColor = {},
        alreadyUsedColors = alreadyUsedColors,
        otherColors = otherColors,
        previousNotes = listOf(
            "My previous note where I make some remarks",
            "Another previous note and this one is very long, such that it doesn't fit on one line"
        ),
        note = "",
        onNoteChange = {},
        experiencesInRange = emptyList(),
        selectedExperience = null,
        onChangeOfSelectedExperience = {},
        substanceName = "LSD",
        enteredTitle = "This is my title",
        onChangeOfEnteredTitle = {},
        isEnteredTitleOk = true,
        consumerName = "",
        onChangeOfConsumerName = {},
        consumerNamesSorted = listOf("Isaak", "Marc", "Eve")
    )
}