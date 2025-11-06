package com.isaakhanimann.journal.ui.tabs.stats

import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.PrimaryTabRow
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.isaakhanimann.journal.R
import com.isaakhanimann.journal.ui.YOU
import com.isaakhanimann.journal.ui.tabs.search.substance.roa.toReadableString
import com.isaakhanimann.journal.ui.theme.JournalTheme

@Composable
fun StatsScreen(
    viewModel: StatsViewModel = hiltViewModel(),
    navigateToSubstanceCompanion: (substanceName: String, consumerName: String?) -> Unit,
) {
    StatsScreen(
        navigateToSubstanceCompanion = navigateToSubstanceCompanion,
        onTapOption = viewModel::onTapOption,
        statsModel = viewModel.statsModelFlow.collectAsState().value,
        onChangeConsumerName = viewModel::onChangeConsumer,
        consumerNamesSorted = viewModel.sortedConsumerNamesFlow.collectAsState().value,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    navigateToSubstanceCompanion: (substanceName: String, consumerName: String?) -> Unit,
    onTapOption: (option: TimePickerOption) -> Unit,
    statsModel: StatsModel,
    onChangeConsumerName: (String?) -> Unit,
    consumerNamesSorted: List<String>,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (statsModel.consumerName == null) stringResource(R.string.statistics) else stringResource(
                            R.string.statistics_for, statsModel.consumerName
                        )
                    )
                },
                actions = {
                    if (consumerNamesSorted.isNotEmpty()) {
                        var isConsumerSelectionExpanded by remember { mutableStateOf(false) }
                        IconButton(onClick = { isConsumerSelectionExpanded = true }) {
                            Icon(Icons.Outlined.Person, contentDescription = "Consumer")
                        }
                        DropdownMenu(
                            expanded = isConsumerSelectionExpanded,
                            onDismissRequest = { isConsumerSelectionExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(YOU) },
                                onClick = {
                                    onChangeConsumerName(null)
                                    isConsumerSelectionExpanded = false
                                },
                                leadingIcon = {
                                    if (statsModel.consumerName == null) {
                                        Icon(
                                            Icons.Filled.Check,
                                            contentDescription = "Check"
                                        )
                                    }
                                }
                            )
                            consumerNamesSorted.forEach { consumerName ->
                                DropdownMenuItem(
                                    text = { Text(consumerName) },
                                    onClick = {
                                        onChangeConsumerName(consumerName)
                                        isConsumerSelectionExpanded = false
                                    },
                                    leadingIcon = {
                                        if (statsModel.consumerName == consumerName) {
                                            Icon(
                                                Icons.Filled.Check,
                                                contentDescription = "Check"
                                            )
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            )
        },
    ) { padding ->
        if (!statsModel.areThereAnyIngestions) {
            EmptyScreenDisclaimer(
                title = stringResource(R.string.nothing_to_show_yet),
                description = stringResource(R.string.stats_empty_description)
            )
        } else {
            Column(modifier = Modifier.padding(padding)) {
                PrimaryTabRow(
                    selectedTabIndex = statsModel.selectedOption.tabIndex
                ) {
                    TimePickerOption.entries.forEachIndexed { index, option ->
                        Tab(
                            text = { Text(option.displayText) },
                            selected = statsModel.selectedOption.tabIndex == index,
                            onClick = { onTapOption(option) }
                        )
                    }
                }
                if (statsModel.statItems.isNotEmpty()) {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        item {
                            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                                Text(
                                    text = stringResource(
                                        R.string.experiences_since,
                                        statsModel.startDateText
                                    ),
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = stringResource(R.string.substance_counted_once_per_experience),
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }

                        item {
                            BarChart(
                                buckets = statsModel.chartBuckets,
                                startDateText = statsModel.startDateText,
                            )
                        }

                        item {
                            // Spacer instead of full-width divider
                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        items(statsModel.statItems) { subStat ->
                            StatItemRow(
                                subStat = subStat,
                                onClick = {
                                    navigateToSubstanceCompanion(
                                        subStat.substanceName,
                                        statsModel.consumerName
                                    )
                                }
                            )
                        }
                    }
                } else {
                    EmptyScreenDisclaimer(
                        title = stringResource(
                            R.string.no_ingestions_since,
                            statsModel.selectedOption.longDisplayText
                        ),
                        description = stringResource(R.string.no_ingestions_description)
                    )
                }
            }
        }
    }
}

@Composable
private fun StatItemRow(subStat: StatItem, onClick: () -> Unit) {
    ElevatedCard(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Color indicator
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = subStat.color.toColor(),
                modifier = Modifier
                    .width(6.dp)
                    .height(48.dp)
            ) {}
            
            // Content
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = subStat.substanceName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                val addOn = if (subStat.experienceCount == 1) stringResource(R.string.experience_count)
                else stringResource(R.string.experiences_count)
                Text(
                    text = "${subStat.experienceCount}$addOn",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Dose info
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                val cumulativeDose = subStat.totalDose
                val doseTextValue = if (cumulativeDose != null) {
                    if (cumulativeDose.isEstimate) {
                        if (cumulativeDose.estimatedDoseStandardDeviation != null) {
                            "~${cumulativeDose.dose.toReadableString()}±${cumulativeDose.estimatedDoseStandardDeviation.toReadableString()} ${cumulativeDose.units}"
                        } else {
                            "~${cumulativeDose.dose.toReadableString()} ${cumulativeDose.units}"
                        }
                    } else {
                        "${cumulativeDose.dose.toReadableString()} ${cumulativeDose.units}"
                    }
                } else {
                    stringResource(R.string.dose_unknown)
                }
                
                Text(
                    text = stringResource(R.string.total, doseTextValue),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )

                if (subStat.routeCounts.isNotEmpty()) {
                    Text(
                        text = subStat.routeCounts.joinToString { "${it.administrationRoute.displayText.lowercase()} ${it.count}x" },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}


@Composable
fun EmptyScreenDisclaimer(title: String, description: String) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center
            )
            Text(
                text = description,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Preview
@Composable
fun StatsPreview(
    @PreviewParameter(
        StatsPreviewProvider::class,
    ) statsModel: StatsModel
) {
    JournalTheme {
        StatsScreen(
            navigateToSubstanceCompanion = { _, _ -> },
            onTapOption = {},
            statsModel = statsModel,
            onChangeConsumerName = {},
            consumerNamesSorted = listOf("You", "Someone else"),
        )
    }
}