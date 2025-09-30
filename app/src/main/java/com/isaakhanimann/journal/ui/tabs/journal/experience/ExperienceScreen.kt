package com.isaakhanimann.journal.ui.tabs.journal.experience

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.NoteAdd
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.OpenInFull
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.ExposurePlus2
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.isaakhanimann.journal.R
import com.isaakhanimann.journal.data.room.experiences.entities.AdaptiveColor
import com.isaakhanimann.journal.data.room.experiences.entities.CustomUnit
import com.isaakhanimann.journal.data.room.experiences.entities.SubstanceColor
import com.isaakhanimann.journal.data.room.experiences.entities.TimedNote
import com.isaakhanimann.journal.data.room.experiences.entities.getSubstanceColor
import com.isaakhanimann.journal.data.substances.AdministrationRoute
import com.isaakhanimann.journal.ui.FULL_STOMACH_DISCLAIMER
import com.isaakhanimann.journal.ui.YOU
import com.isaakhanimann.journal.ui.tabs.journal.addingestion.interactions.Interaction
import com.isaakhanimann.journal.ui.tabs.journal.experience.components.CumulativeDoseRow
import com.isaakhanimann.journal.ui.tabs.journal.experience.components.IngestionTimeOrDurationText
import com.isaakhanimann.journal.ui.tabs.journal.experience.components.InteractionRow
import com.isaakhanimann.journal.ui.tabs.journal.experience.components.NoteOrRatingTimeOrDurationText
import com.isaakhanimann.journal.ui.tabs.journal.experience.components.SavedTimeDisplayOption
import com.isaakhanimann.journal.ui.tabs.journal.experience.components.TimeDisplayOption
import com.isaakhanimann.journal.ui.tabs.journal.experience.components.getDurationText
import com.isaakhanimann.journal.ui.tabs.journal.experience.components.ingestion.IngestionRow
import com.isaakhanimann.journal.ui.tabs.journal.experience.components.ingestion.VerticalLine
import com.isaakhanimann.journal.ui.tabs.journal.experience.components.rating.OverallRatingRow
import com.isaakhanimann.journal.ui.tabs.journal.experience.components.rating.TimedRatingRow
import com.isaakhanimann.journal.ui.tabs.journal.experience.components.timednote.TimedNoteRow
import com.isaakhanimann.journal.ui.tabs.journal.experience.models.ConsumerWithIngestions
import com.isaakhanimann.journal.ui.tabs.journal.experience.models.CumulativeDose
import com.isaakhanimann.journal.ui.tabs.journal.experience.models.OneExperienceScreenModel
import com.isaakhanimann.journal.ui.tabs.journal.experience.timeline.AllTimelines
import com.isaakhanimann.journal.ui.theme.JournalTheme
import com.isaakhanimann.journal.ui.utils.getDateWithWeekdayText
import kotlinx.coroutines.delay
import java.time.Instant
import java.time.temporal.ChronoUnit

@Composable
fun ExperienceScreen(
    viewModel: ExperienceViewModel = hiltViewModel(),
    navigateToAddIngestionSearch: () -> Unit,
    navigateToEditExperienceScreen: () -> Unit,
    navigateToExplainTimeline: () -> Unit,
    navigateToIngestionScreen: (ingestionId: Int) -> Unit,
    navigateToAddRatingScreen: () -> Unit,
    navigateToAddTimedNoteScreen: () -> Unit,
    navigateToEditRatingScreen: (ratingId: Int) -> Unit,
    navigateToEditTimedNoteScreen: (timedNoteId: Int) -> Unit,
    navigateToTimelineScreen: (consumerName: String) -> Unit,
    navigateBack: () -> Unit,
) {
    val experience = viewModel.experienceFlow.collectAsState().value
    val customUnitsMap = viewModel.customUnitsFlow.collectAsState().value

    val oneExperienceScreenModel = OneExperienceScreenModel(
        isFavorite = viewModel.isFavoriteFlow.collectAsState().value,
        title = experience?.title ?: "",
        firstIngestionTime = viewModel.ingestionsWithCompanionsFlow.collectAsState().value.minOfOrNull { it.ingestion.time }
            ?: experience?.sortDate ?: Instant.now(),
        notes = experience?.text ?: "",
        locationName = experience?.location?.name ?: "",
        isCurrentExperience = viewModel.isCurrentExperienceFlow.collectAsState().value,
        ingestionElements = viewModel.ingestionElementsFlow.collectAsState(emptyList()).value,
        cumulativeDoses = viewModel.cumulativeDosesFlow.collectAsState().value,
        interactions = viewModel.interactionsFlow.collectAsState().value,
        interactionExplanations = viewModel.interactionExplanationsFlow.collectAsState().value,
        ratings = viewModel.ratingsFlow.collectAsState().value,
        timedNotesSorted = viewModel.timedNotesSortedFlow.collectAsState().value,
        consumersWithIngestions = viewModel.consumersWithIngestionsFlow.collectAsState().value,
        dataForEffectLines = viewModel.dataForEffectTimelinesFlow.collectAsState().value
    )
    ExperienceScreen(
        oneExperienceScreenModel = oneExperienceScreenModel,
        customUnitsMap = customUnitsMap,
        timelineDisplayOption = viewModel.timelineDisplayOptionFlow.collectAsState().value,
        isOralDisclaimerHidden = viewModel.isOralTimelineDisclaimerHidden.collectAsState().value,
        onChangeIsOralDisclaimerHidden = viewModel::saveOralDisclaimerIsHidden,
        addIngestion = {
            viewModel.saveLastIngestionTimeOfExperience()
            navigateToAddIngestionSearch()
        },
        deleteExperience = viewModel::deleteExperience,
        navigateToEditExperienceScreen = navigateToEditExperienceScreen,
        navigateToExplainTimeline = navigateToExplainTimeline,
        navigateToIngestionScreen = navigateToIngestionScreen,
        navigateToAddRatingScreen = navigateToAddRatingScreen,
        navigateToAddTimedNoteScreen = navigateToAddTimedNoteScreen,
        navigateBack = navigateBack,
        saveIsFavorite = viewModel::saveIsFavorite,
        navigateToEditRatingScreen = navigateToEditRatingScreen,
        navigateToEditTimedNoteScreen = navigateToEditTimedNoteScreen,
        savedTimeDisplayOption = viewModel.savedTimeDisplayOption.collectAsState().value,
        timeDisplayOption = viewModel.timeDisplayOptionFlow.collectAsState().value,
        onChangeTimeDisplayOption = viewModel::saveTimeDisplayOption,
        navigateToTimelineScreen = navigateToTimelineScreen,
        areDosageDotsHidden = viewModel.areDosageDotsHiddenFlow.collectAsState().value,
        isTimelineHidden = viewModel.isTimelineHiddenFlow.collectAsState().value
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExperienceScreen(
    oneExperienceScreenModel: OneExperienceScreenModel,
    customUnitsMap: Map<Int, CustomUnit>,
    timelineDisplayOption: TimelineDisplayOption,
    isOralDisclaimerHidden: Boolean,
    onChangeIsOralDisclaimerHidden: (Boolean) -> Unit,
    addIngestion: () -> Unit,
    deleteExperience: () -> Unit,
    navigateToEditExperienceScreen: () -> Unit,
    navigateToExplainTimeline: () -> Unit,
    navigateToIngestionScreen: (ingestionId: Int) -> Unit,
    navigateToAddRatingScreen: () -> Unit,
    navigateToAddTimedNoteScreen: () -> Unit,
    navigateBack: () -> Unit,
    saveIsFavorite: (Boolean) -> Unit,
    navigateToEditRatingScreen: (ratingId: Int) -> Unit,
    navigateToEditTimedNoteScreen: (timedNoteId: Int) -> Unit,
    savedTimeDisplayOption: SavedTimeDisplayOption,
    timeDisplayOption: TimeDisplayOption,
    onChangeTimeDisplayOption: (SavedTimeDisplayOption) -> Unit,
    navigateToTimelineScreen: (consumerName: String) -> Unit,
    areDosageDotsHidden: Boolean,
    isTimelineHidden: Boolean,
) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            ExperienceTopBar(
                oneExperienceScreenModel = oneExperienceScreenModel,
                onChangeTimeDisplayOption = onChangeTimeDisplayOption,
                savedTimeDisplayOption = savedTimeDisplayOption,
                deleteExperience = deleteExperience,
                navigateBack = navigateBack,
                navigateToEditExperienceScreen = navigateToEditExperienceScreen,
                saveIsFavorite = saveIsFavorite,
                navigateToAddTimedNoteScreen = navigateToAddTimedNoteScreen,
                navigateToAddRatingScreen = navigateToAddRatingScreen,
                addIngestion = addIngestion,
                scrollBehavior = scrollBehavior
            )
        },
        floatingActionButton = {
            AddIngestionFAB(oneExperienceScreenModel, addIngestion)
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(Modifier.height(0.dp))
            MyTimelineSection(
                timelineDisplayOption = timelineDisplayOption,
                navigateToExplainTimeline = navigateToExplainTimeline,
                navigateToTimelineScreen = navigateToTimelineScreen,
                oneExperienceScreenModel = oneExperienceScreenModel,
                timeDisplayOption = timeDisplayOption,
                isOralDisclaimerHidden = isOralDisclaimerHidden,
                onChangeIsOralDisclaimerHidden = onChangeIsOralDisclaimerHidden,
            )
            if (oneExperienceScreenModel.ingestionElements.isNotEmpty()) {
                MyIngestionList(
                    ingestionListElements = oneExperienceScreenModel.ingestionElements,
                    firstIngestionTime = oneExperienceScreenModel.firstIngestionTime,
                    areDosageDotsHidden = areDosageDotsHidden,
                    navigateToIngestionScreen = navigateToIngestionScreen,
                    timeDisplayOption = timeDisplayOption,
                    customUnitsMap = customUnitsMap
                )
            }
            val cumulativeDoses = oneExperienceScreenModel.cumulativeDoses
            if (cumulativeDoses.isNotEmpty()) {
                CumulativeDosesSection(
                    cumulativeDoses = cumulativeDoses,
                    areDosageDotsHidden = areDosageDotsHidden
                )
            }
            val timedNotesSorted = oneExperienceScreenModel.timedNotesSorted
            if (timedNotesSorted.isNotEmpty()) {
                TimedNotesSection(
                    timedNotesSorted = timedNotesSorted,
                    navigateToEditTimedNoteScreen = navigateToEditTimedNoteScreen,
                    timeDisplayOption = timeDisplayOption,
                    firstIngestionTime = oneExperienceScreenModel.firstIngestionTime
                )
            }
            if (oneExperienceScreenModel.ratings.isNotEmpty()) {
                ShulginRatingsSection(
                    oneExperienceScreenModel = oneExperienceScreenModel,
                    navigateToEditRatingScreen = navigateToEditRatingScreen,
                    timeDisplayOption = timeDisplayOption
                )
            }
            val notes = oneExperienceScreenModel.notes
            if (notes.isNotBlank()) {
                NotesSection(
                    navigateToEditExperienceScreen = navigateToEditExperienceScreen,
                    oneExperienceScreenModel = oneExperienceScreenModel
                )
            }
            oneExperienceScreenModel.consumersWithIngestions.forEach { consumerWithIngestions ->
                ConsumerSection(
                    consumerWithIngestions = consumerWithIngestions,
                    navigateToTimelineScreen = navigateToTimelineScreen,
                    timeDisplayOption = timeDisplayOption,
                    areDosageDotsHidden = areDosageDotsHidden,
                    navigateToIngestionScreen = navigateToIngestionScreen,
                    isTimelineHidden = isTimelineHidden
                )
            }
            val interactions = oneExperienceScreenModel.interactions
            AnimatedVisibility(visible = interactions.isNotEmpty()) {
                ExperienceInteractionsSection(
                    interactions = interactions,
                    oneExperienceScreenModel = oneExperienceScreenModel
                )
            }
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ExperienceInteractionsSection(
    interactions: List<Interaction>,
    oneExperienceScreenModel: OneExperienceScreenModel
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Column(modifier = Modifier.padding(bottom = 16.dp)) {
            Text(
                text = stringResource(R.string.interactions),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(16.dp)
            )
            interactions.forEachIndexed { index, interaction ->
                InteractionRow(interaction = interaction)
                if (index < interactions.size - 1) {
                    HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.explanations),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                oneExperienceScreenModel.interactionExplanations.forEach {
                    val uriHandler = LocalUriHandler.current
                    SuggestionChip(
                        onClick = {
                            uriHandler.openUri(it.url)
                        },
                        label = { Text(it.name) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ConsumerSection(
    consumerWithIngestions: ConsumerWithIngestions,
    navigateToTimelineScreen: (consumerName: String) -> Unit,
    timeDisplayOption: TimeDisplayOption,
    areDosageDotsHidden: Boolean,
    navigateToIngestionScreen: (ingestionId: Int) -> Unit,
    isTimelineHidden: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = consumerWithIngestions.consumerName,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(16.dp)
            )
            if (!isTimelineHidden) {
                IconButton(onClick = { navigateToTimelineScreen(consumerWithIngestions.consumerName) }) {
                    Icon(
                        Icons.Default.OpenInFull,
                        contentDescription = stringResource(R.string.expand_timeline)
                    )
                }
            }
        }
        when (val timelineDisplayOption = consumerWithIngestions.timelineDisplayOption) {
            TimelineDisplayOption.Hidden -> {}
            TimelineDisplayOption.Loading -> LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            TimelineDisplayOption.NotWorthDrawing -> {}
            is TimelineDisplayOption.Shown -> {
                val timelineModel = timelineDisplayOption.allTimelinesModel
                Column(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    AllTimelines(
                        model = timelineModel,
                        timeDisplayOption = timeDisplayOption,
                        isShowingCurrentTime = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                    )
                }
            }
        }
        HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)
        consumerWithIngestions.ingestionElements.forEachIndexed { index, ingestionElement ->
            IngestionRow(
                ingestionElement = ingestionElement,
                areDosageDotsHidden = areDosageDotsHidden,
                modifier = Modifier
                    .clickable {
                        navigateToIngestionScreen(ingestionElement.ingestionWithCompanionAndCustomUnit.ingestion.id)
                    }
                    .fillMaxWidth()
                    .padding(vertical = 5.dp, horizontal = 16.dp)
            ) {
                val ingestion = ingestionElement.ingestionWithCompanionAndCustomUnit.ingestion
                IngestionTimeOrDurationText(
                    time = ingestion.time,
                    endTime = ingestion.endTime,
                    index = index,
                    timeDisplayOption = timeDisplayOption,
                    allTimesSortedMap = consumerWithIngestions.ingestionElements.map { it.ingestionWithCompanionAndCustomUnit.ingestion.time }
                )
            }
            if (index < consumerWithIngestions.ingestionElements.size - 1) {
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    thickness = DividerDefaults.Thickness,
                    color = DividerDefaults.color
                )
            }
        }
    }
}

@Composable
private fun NotesSection(
    navigateToEditExperienceScreen: () -> Unit,
    oneExperienceScreenModel: OneExperienceScreenModel
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { navigateToEditExperienceScreen() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.notes),
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = oneExperienceScreenModel.notes, style = MaterialTheme.typography.bodyMedium)
            if (oneExperienceScreenModel.locationName.isNotBlank()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.location, oneExperienceScreenModel.locationName),
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

@Composable
private fun ShulginRatingsSection(
    oneExperienceScreenModel: OneExperienceScreenModel,
    navigateToEditRatingScreen: (ratingId: Int) -> Unit,
    timeDisplayOption: TimeDisplayOption
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Text(
            text = stringResource(R.string.shulgin_ratings),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(16.dp)
        )
        HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)
        val ratingsWithTime =
            oneExperienceScreenModel.ratings.mapNotNull { rating ->
                rating.time?.let { Pair(it, rating) }
            }.sortedBy { it.first }
        ratingsWithTime.forEachIndexed { index, pair ->
            TimedRatingRow(
                modifier = Modifier
                    .clickable {
                        navigateToEditRatingScreen(pair.second.id)
                    }
                    .fillMaxWidth()
                    .padding(vertical = 8.dp, horizontal = 16.dp),
                ratingSign = pair.second.option.sign
            ) {
                NoteOrRatingTimeOrDurationText(
                    time = pair.first,
                    timeDisplayOption = timeDisplayOption,
                    firstIngestionTime = oneExperienceScreenModel.firstIngestionTime
                )
            }
            if (index < ratingsWithTime.size - 1) {
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    thickness = DividerDefaults.Thickness,
                    color = DividerDefaults.color
                )
            }
        }
        val overallRating =
            oneExperienceScreenModel.ratings.firstOrNull { it.time == null }
        if (overallRating != null) {
            if (ratingsWithTime.isNotEmpty()) {
                HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)
            }
            OverallRatingRow(
                modifier = Modifier
                    .clickable {
                        navigateToEditRatingScreen(overallRating.id)
                    }
                    .fillMaxWidth()
                    .padding(vertical = 8.dp, horizontal = 16.dp),
                ratingSign = overallRating.option.sign
            )
        }
    }
}

@Composable
private fun TimedNotesSection(
    timedNotesSorted: List<TimedNote>,
    navigateToEditTimedNoteScreen: (timedNoteId: Int) -> Unit,
    timeDisplayOption: TimeDisplayOption,
    firstIngestionTime: Instant,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Text(
            text = stringResource(R.string.timed_notes),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(16.dp)
        )
        if (timedNotesSorted.isNotEmpty()) {
            HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)
        }
        timedNotesSorted.forEachIndexed { index, timedNote ->
            TimedNoteRow(
                timedNote = timedNote,
                modifier = Modifier
                    .clickable {
                        navigateToEditTimedNoteScreen(timedNote.id)
                    }
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                NoteOrRatingTimeOrDurationText(
                    time = timedNote.time,
                    timeDisplayOption = timeDisplayOption,
                    firstIngestionTime = firstIngestionTime
                )
            }
            if (index < timedNotesSorted.size - 1) {
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    thickness = DividerDefaults.Thickness,
                    color = DividerDefaults.color
                )
            }
        }
    }
}

@Composable
private fun CumulativeDosesSection(
    cumulativeDoses: List<CumulativeDose>,
    areDosageDotsHidden: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Text(
            text = stringResource(R.string.your_cumulative_doses),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(16.dp)
        )
        if (cumulativeDoses.isNotEmpty()) {
            HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)
        }
        cumulativeDoses.forEachIndexed { index, cumulativeDose ->
            CumulativeDoseRow(
                cumulativeDose = cumulativeDose,
                areDosageDotsHidden = areDosageDotsHidden,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 5.dp, horizontal = 16.dp)
            )
            if (index < cumulativeDoses.size - 1) {
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    thickness = DividerDefaults.Thickness,
                    color = DividerDefaults.color
                )
            }
        }
    }
}

@Composable
private fun MyIngestionList(
    ingestionListElements: List<ExperienceListElement>,
    firstIngestionTime: Instant,
    areDosageDotsHidden: Boolean,
    navigateToIngestionScreen: (ingestionId: Int) -> Unit,
    timeDisplayOption: TimeDisplayOption,
    customUnitsMap: Map<Int, CustomUnit>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Text(
            text = firstIngestionTime.getDateWithWeekdayText(),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(16.dp)
        )
        if (ingestionListElements.isNotEmpty()) {
            HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)
        }

        val allTimes = ingestionListElements.map { it.time }

        ingestionListElements.forEachIndexed { index, listElement ->
            when (listElement) {
                is SingleIngestionListElement -> {
                    IngestionRow(
                        ingestionElement = listElement.element,
                        areDosageDotsHidden = areDosageDotsHidden,
                        modifier = Modifier
                            .clickable { navigateToIngestionScreen(listElement.element.ingestionWithCompanionAndCustomUnit.ingestion.id) }
                            .fillMaxWidth(),
                        customUnit = listElement.element.ingestionWithCompanionAndCustomUnit.ingestion.customUnitId?.let {
                            customUnitsMap[it]
                        }
                    ) {
                        val ingestion = listElement.element.ingestionWithCompanionAndCustomUnit.ingestion
                        IngestionTimeOrDurationText(
                            time = ingestion.time,
                            endTime = ingestion.endTime,
                            index = allTimes.indexOf(ingestion.time),
                            timeDisplayOption = timeDisplayOption,
                            allTimesSortedMap = allTimes
                        )
                    }
                }
                is GroupedRecipeListElement -> {
                    GroupedIngestionRow(
                        groupedElement = listElement,
                        areDosageDotsHidden = areDosageDotsHidden,
                        navigateToIngestionScreen = navigateToIngestionScreen,
                        timeDisplayOption = timeDisplayOption,
                        allTimesSortedMap = allTimes,
                        customUnitsMap = customUnitsMap
                    )
                }
            }

            val isLastElement = index == ingestionListElements.size - 1
            if (!isLastElement) {
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    thickness = DividerDefaults.Thickness,
                    color = DividerDefaults.color
                )
            }
        }
    }
}

@Composable
private fun GroupedIngestionRow(
    groupedElement: GroupedRecipeListElement,
    areDosageDotsHidden: Boolean,
    navigateToIngestionScreen: (ingestionId: Int) -> Unit,
    timeDisplayOption: TimeDisplayOption,
    allTimesSortedMap: List<Instant>,
    customUnitsMap: Map<Int, CustomUnit>
) {
    var isExpanded by rememberSaveable { mutableStateOf(false) }

    val horizontalPadding = 21.dp
    val verticalPadding = 18.dp

    Column {
        Row(
            modifier = Modifier
                .clickable { isExpanded = !isExpanded }
                .fillMaxWidth()
                .padding(horizontal = horizontalPadding, vertical = verticalPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            val representativeIngestion = groupedElement.representativeElement.ingestionWithCompanionAndCustomUnit
            VerticalLine(
                color = representativeIngestion.substanceCompanion?.getSubstanceColor() ?: SubstanceColor.Predefined(
                    AdaptiveColor.BLUE)
            )

            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = buildAnnotatedString {
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                            append(groupedElement.recipeName)
                        }
                        append(" (${stringResource(id = R.string.recipe)})")
                    },
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = groupedElement.recipeDoseText,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Icon(
                imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = if (isExpanded) stringResource(R.string.collapse) else stringResource(R.string.expand)
            )

            Column(modifier = Modifier.widthIn(min = 60.dp), horizontalAlignment = Alignment.End) {
                IngestionTimeOrDurationText(
                    time = groupedElement.time,
                    endTime = null,
                    index = allTimesSortedMap.indexOf(groupedElement.time),
                    timeDisplayOption = timeDisplayOption,
                    allTimesSortedMap = allTimesSortedMap
                )
            }
        }

        HorizontalDivider(
            modifier = Modifier.padding(horizontal = horizontalPadding),
            thickness = DividerDefaults.Thickness,
            color = DividerDefaults.color
        )

        AnimatedVisibility(visible = isExpanded) {
            Column(modifier = Modifier.padding(start = horizontalPadding, bottom = verticalPadding)) {
                groupedElement.subElements.forEachIndexed { index, subElement ->
                    IngestionRow(
                        ingestionElement = subElement,
                        areDosageDotsHidden = areDosageDotsHidden,
                        modifier = Modifier
                            .clickable { navigateToIngestionScreen(subElement.ingestionWithCompanionAndCustomUnit.ingestion.id) }
                            .fillMaxWidth()
                            .padding(start = 5.dp),
                        customUnit = subElement.ingestionWithCompanionAndCustomUnit.ingestion.customUnitId?.let {
                            customUnitsMap[it]
                        }
                    ) {

                    }
                    if (index < groupedElement.subElements.size - 1) {
                        HorizontalDivider(
                            modifier = Modifier.padding(start = 32.dp, end = 16.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
            }
        }
    }
}


@Composable
private fun MyTimelineSection(
    timelineDisplayOption: TimelineDisplayOption,
    navigateToExplainTimeline: () -> Unit,
    navigateToTimelineScreen: (consumerName: String) -> Unit,
    oneExperienceScreenModel: OneExperienceScreenModel,
    timeDisplayOption: TimeDisplayOption,
    isOralDisclaimerHidden: Boolean,
    onChangeIsOralDisclaimerHidden: (Boolean) -> Unit,
) = when (timelineDisplayOption) {
    TimelineDisplayOption.Hidden -> {}
    TimelineDisplayOption.Loading -> LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
    TimelineDisplayOption.NotWorthDrawing -> {}
    is TimelineDisplayOption.Shown -> {
        val timelineModel = timelineDisplayOption.allTimelinesModel
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(R.string.effect_timeline),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(16.dp)
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TextButton(onClick = navigateToExplainTimeline) {
                        Icon(Icons.Outlined.Info, contentDescription = stringResource(R.string.info))
                        Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                        Text(text = stringResource(R.string.info))
                    }
                    IconButton(onClick = { navigateToTimelineScreen(YOU) }) {
                        Icon(
                            Icons.Default.OpenInFull,
                            contentDescription = stringResource(R.string.expand_timeline)
                        )
                    }
                }
            }
            Column(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 10.dp),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                AllTimelines(
                    model = timelineModel,
                    timeDisplayOption = timeDisplayOption,
                    isShowingCurrentTime = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                )
                val hasOralIngestion =
                    oneExperienceScreenModel.ingestionElements.any {
                        when(it) {
                            is SingleIngestionListElement -> it.element.ingestionWithCompanionAndCustomUnit.ingestion.administrationRoute == AdministrationRoute.ORAL
                            is GroupedRecipeListElement -> it.subElements.any { sub -> sub.ingestionWithCompanionAndCustomUnit.ingestion.administrationRoute == AdministrationRoute.ORAL }
                        }
                    }
                if (hasOralIngestion && !isOralDisclaimerHidden) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = FULL_STOMACH_DISCLAIMER,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { onChangeIsOralDisclaimerHidden(true) }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = stringResource(R.string.close_disclaimer)
                            )
                        }
                    }
                }
            }
        }
    }
}


@Composable
private fun AddIngestionFAB(
    oneExperienceScreenModel: OneExperienceScreenModel,
    addIngestion: () -> Unit
) {
    val wasAnyIngestionCreatedInLast4Hours =
        oneExperienceScreenModel.ingestionElements.map {
            when(it) {
                is SingleIngestionListElement -> it.element.ingestionWithCompanionAndCustomUnit.ingestion.creationDate
                is GroupedRecipeListElement -> it.representativeElement.ingestionWithCompanionAndCustomUnit.ingestion.creationDate
            }
        }
            .any {
                (it?.compareTo(Instant.now().minus(4, ChronoUnit.HOURS)) ?: 0) < 0
            }
    if (oneExperienceScreenModel.isCurrentExperience || wasAnyIngestionCreatedInLast4Hours) {
        ExtendedFloatingActionButton(
            onClick = addIngestion,
            icon = {
                Icon(
                    Icons.Filled.Add,
                    contentDescription = stringResource(R.string.add)
                )
            },
            text = { Text(stringResource(R.string.ingestion)) }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExperienceTopBar(
    oneExperienceScreenModel: OneExperienceScreenModel,
    onChangeTimeDisplayOption: (SavedTimeDisplayOption) -> Unit,
    savedTimeDisplayOption: SavedTimeDisplayOption,
    deleteExperience: () -> Unit,
    navigateBack: () -> Unit,
    navigateToEditExperienceScreen: () -> Unit,
    saveIsFavorite: (Boolean) -> Unit,
    navigateToAddTimedNoteScreen: () -> Unit,
    navigateToAddRatingScreen: () -> Unit,
    addIngestion: () -> Unit,
    scrollBehavior: TopAppBarScrollBehavior
) {
    TopAppBar(
        title = { Text(oneExperienceScreenModel.title) },
        navigationIcon = {
            IconButton(onClick = navigateBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.navigate_back))
            }
        },
        actions = {
            var areTimeOptionsExpanded by remember { mutableStateOf(false) }
            IconButton(onClick = { areTimeOptionsExpanded = true }) {
                Icon(Icons.Outlined.Timer, contentDescription = stringResource(R.string.time_display_option))
            }
            DropdownMenu(
                expanded = areTimeOptionsExpanded,
                onDismissRequest = { areTimeOptionsExpanded = false }
            ) {
                SavedTimeDisplayOption.entries.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option.text) },
                        onClick = {
                            onChangeTimeDisplayOption(option)
                            areTimeOptionsExpanded = false
                        },
                        leadingIcon = {
                            if (option == savedTimeDisplayOption) {
                                Icon(
                                    Icons.Filled.Check,
                                    contentDescription = stringResource(R.string.check)
                                )
                            }
                        }
                    )
                }
            }
            var areEditOptionsExpanded by remember { mutableStateOf(false) }
            IconButton(onClick = { areEditOptionsExpanded = true }) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = stringResource(R.string.edit_options),
                )
            }
            var isShowingDeleteDialog by remember { mutableStateOf(false) }
            if(isShowingDeleteDialog) {
                AlertDialog(
                    onDismissRequest = { isShowingDeleteDialog = false },
                    title = {
                        Text(text = stringResource(R.string.delete_experience))
                    },
                    text = {
                        Text(stringResource(R.string.this_will_also_delete_all_its_ingestions))
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                isShowingDeleteDialog = false
                                deleteExperience()
                                navigateBack()
                            }
                        ) {
                            Text(stringResource(R.string.delete))
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = { isShowingDeleteDialog = false }
                        ) {
                            Text(stringResource(R.string.cancel))
                        }
                    }
                )
            }
            DropdownMenu(
                expanded = areEditOptionsExpanded,
                onDismissRequest = { areEditOptionsExpanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.edit_title_notes_location)) },
                    onClick = {
                        navigateToEditExperienceScreen()
                        areEditOptionsExpanded = false
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Outlined.Edit,
                            contentDescription = stringResource(R.string.edit_experience)
                        )
                    }
                )
                val isFavorite = oneExperienceScreenModel.isFavorite
                DropdownMenuItem(
                    text = { if(isFavorite) Text(stringResource(R.string.unmark_favorite)) else Text(stringResource(R.string.mark_favorite)) },
                    onClick = {
                        saveIsFavorite(!isFavorite)
                        areEditOptionsExpanded = false
                    },
                    leadingIcon = {
                        if (isFavorite) {
                            Icon(
                                Icons.Filled.Star,
                                contentDescription = stringResource(R.string.unmark_favorite)
                            )
                        } else {
                            Icon(
                                Icons.Outlined.StarOutline,
                                contentDescription = "Mark favorite"
                            )
                        }
                    }
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.delete_experience)) },
                    onClick = {
                        isShowingDeleteDialog = true
                        areEditOptionsExpanded = false
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Outlined.Delete,
                            contentDescription = stringResource(R.string.delete_experience)
                        )
                    }
                )
            }

            var areAddOptionsExpanded by remember { mutableStateOf(false) }
            IconButton(onClick = { areAddOptionsExpanded = true }) {
                Icon(
                    Icons.Outlined.Add,
                    contentDescription = stringResource(R.string.add_options),
                )
            }
            DropdownMenu(
                expanded = areAddOptionsExpanded,
                onDismissRequest = { areAddOptionsExpanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.add_ingestion)) },
                    onClick = {
                        addIngestion()
                        areAddOptionsExpanded = false
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Outlined.Add,
                            contentDescription = "Add Ingestion"
                        )
                    }
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.add_timed_note)) },
                    onClick = {
                        navigateToAddTimedNoteScreen()
                        areAddOptionsExpanded = false
                    },
                    leadingIcon = {
                        Icon(
                            Icons.AutoMirrored.Outlined.NoteAdd,
                            contentDescription = stringResource(R.string.add_timed_note)
                        )
                    }
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.add_shulgin_rating)) },
                    onClick = {
                        navigateToAddRatingScreen()
                        areAddOptionsExpanded = false
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Outlined.ExposurePlus2,
                            contentDescription = stringResource(R.string.add_shulgin_rating)
                        )
                    }
                )
            }
        },
        scrollBehavior = scrollBehavior
    )
}

@Composable
private fun LastIngestionRelativeToNowText(lastIngestionTime: Instant) {
    val now: MutableState<Instant> = remember { mutableStateOf(Instant.now()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(10000L) // update every 10 seconds
            now.value = Instant.now()
        }
    }
    val isInPast = lastIngestionTime < now.value
    val relativeTime = if (isInPast) {
        stringResource(R.string.last_ingestion_was) + getDurationText(
            fromInstant = lastIngestionTime,
            toInstant = now.value
        ) + " ago"
    } else {
        stringResource(R.string.last_ingestion_in) + getDurationText(
            fromInstant = lastIngestionTime,
            toInstant = now.value
        )
    }
    Text(
        text = relativeTime,
        style = MaterialTheme.typography.labelMedium,
        modifier = Modifier.padding(vertical = 5.dp, horizontal = 16.dp)
    )
}

@Composable
private fun NowRelativeToStartTimeText(startTime: Instant) {
    val now: MutableState<Instant> = remember { mutableStateOf(Instant.now()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(10000L) // update every 10 seconds
            now.value = Instant.now()
        }
    }
    val isStartInPast = startTime < now.value
    val relativeTime = if (isStartInPast) {
        stringResource(R.string.now) + " " + getDurationText(
            fromInstant = startTime,
            toInstant = now.value
        ) + " " + stringResource(R.string.in_since_start)
    } else {
        stringResource(R.string.start_is_in) + " " + getDurationText(
            fromInstant = startTime,
            toInstant = now.value
        )
    }
    Text(
        text = relativeTime,
        style = MaterialTheme.typography.labelMedium,
        modifier = Modifier.padding(vertical = 5.dp, horizontal = 16.dp)
    )
}


@Preview
@Composable
fun ExperienceScreenPreview(
    @PreviewParameter(
        OneExperienceScreenPreviewProvider::class,
        limit = 1
    ) oneExperienceScreenModel: OneExperienceScreenModel
) {
    JournalTheme {
        ExperienceScreen(
            oneExperienceScreenModel = oneExperienceScreenModel,
            customUnitsMap = emptyMap(),
            timelineDisplayOption = TimelineDisplayOption.Loading,
            isOralDisclaimerHidden = false,
            onChangeIsOralDisclaimerHidden = {},
            addIngestion = {},
            deleteExperience = {},
            navigateToEditExperienceScreen = {},
            navigateToExplainTimeline = {},
            navigateToIngestionScreen = {},
            navigateToAddRatingScreen = {},
            navigateToAddTimedNoteScreen = {},
            navigateBack = {},
            saveIsFavorite = {},
            navigateToEditRatingScreen = {},
            navigateToEditTimedNoteScreen = {},
            savedTimeDisplayOption = SavedTimeDisplayOption.RELATIVE_TO_START,
            timeDisplayOption = TimeDisplayOption.RELATIVE_TO_START,
            onChangeTimeDisplayOption = {},
            navigateToTimelineScreen = {},
            areDosageDotsHidden = false,
            isTimelineHidden = false
        )
    }
}