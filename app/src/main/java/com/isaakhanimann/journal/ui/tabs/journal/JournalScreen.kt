package com.isaakhanimann.journal.ui.tabs.journal

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.isaakhanimann.journal.R
import com.isaakhanimann.journal.data.room.experiences.relations.ExperienceWithIngestionsCompanionsAndRatings
import com.isaakhanimann.journal.ui.tabs.journal.components.ExperienceRow
import com.isaakhanimann.journal.ui.tabs.stats.EmptyScreenDisclaimer
import com.isaakhanimann.journal.ui.theme.JournalTheme
import kotlinx.coroutines.launch

@Composable
fun JournalScreen(
    navigateToExperiencePopNothing: (experienceId: Int) -> Unit,
    navigateToAddIngestion: () -> Unit,
    navigateToCalendar: () -> Unit,
    viewModel: JournalViewModel = hiltViewModel()
) {
    val experiences = viewModel.experiences.collectAsState().value
    LaunchedEffect(Unit) {
        viewModel.maybeMigrate()
    }
    JournalScreen(
        navigateToExperiencePopNothing = navigateToExperiencePopNothing,
        navigateToAddIngestion = {
            viewModel.resetAddIngestionTimes()
            navigateToAddIngestion()
        },
        navigateToCalendar = navigateToCalendar,
        isFavoriteEnabled = viewModel.isFavoriteEnabledFlow.collectAsState().value,
        onChangeIsFavorite = viewModel::onChangeFavorite,
        isTimeRelativeToNow = viewModel.isTimeRelativeToNow.value,
        onChangeIsRelative = viewModel::onChangeRelative,
        searchText = viewModel.searchTextFlow.collectAsState().value,
        onChangeSearchText = viewModel::search,
        isSearchEnabled = viewModel.isSearchEnabled.value,
        onChangeIsSearchEnabled = viewModel::onChangeOfIsSearchEnabled,
        experiences = experiences,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JournalScreen(
    navigateToExperiencePopNothing: (experienceId: Int) -> Unit,
    navigateToAddIngestion: () -> Unit,
    navigateToCalendar: () -> Unit,
    isFavoriteEnabled: Boolean,
    onChangeIsFavorite: (Boolean) -> Unit,
    isTimeRelativeToNow: Boolean,
    onChangeIsRelative: (Boolean) -> Unit,
    searchText: String,
    onChangeSearchText: (String) -> Unit,
    isSearchEnabled: Boolean,
    onChangeIsSearchEnabled: (Boolean) -> Unit,
    experiences: List<ExperienceWithIngestionsCompanionsAndRatings>,
) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.journal)) },
                actions = {
                    IconToggleButton(
                        checked = isTimeRelativeToNow,
                        onCheckedChange = onChangeIsRelative
                    ) {
                        if (isTimeRelativeToNow) {
                            Icon(Icons.Filled.Timer, contentDescription = stringResource(R.string.regular_time))
                        } else {
                            Icon(Icons.Outlined.Timer, contentDescription = stringResource(R.string.time_relative_to_now))
                        }
                    }
                    IconToggleButton(
                        checked = isFavoriteEnabled,
                        onCheckedChange = onChangeIsFavorite
                    ) {
                        if (isFavoriteEnabled) {
                            Icon(Icons.Filled.Star, contentDescription = stringResource(R.string.is_favorite))
                        } else {
                            Icon(Icons.Outlined.StarOutline, contentDescription = stringResource(R.string.is_not_favorite))
                        }
                    }
                    IconToggleButton(
                        checked = isSearchEnabled,
                        onCheckedChange = onChangeIsSearchEnabled
                    ) {
                        if (isSearchEnabled) {
                            Icon(Icons.Outlined.SearchOff, contentDescription = stringResource(R.string.search_off))
                        } else {
                            Icon(Icons.Filled.Search, contentDescription = stringResource(R.string.search))
                        }
                    }
                    IconButton(onClick = navigateToCalendar) {
                        Icon(
                            Icons.Default.CalendarMonth,
                            contentDescription = stringResource(R.string.navigate_to_calendar)
                        )
                    }
                },
                scrollBehavior = scrollBehavior
            )
        },
        floatingActionButton = {
            if (!isSearchEnabled) {
                ExtendedFloatingActionButton(
                    onClick = navigateToAddIngestion,
                    icon = { Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.add)) },
                    text = { Text(stringResource(R.string.ingestion)) },
                )
            }
        },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            AnimatedVisibility(visible = isSearchEnabled) {
                val focusManager = LocalFocusManager.current
                OutlinedTextField(
                    value = searchText,
                    onValueChange = onChangeSearchText,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = CircleShape,
                    placeholder = { Text(text = stringResource(R.string.search_experiences)) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = stringResource(R.string.search)) },
                    trailingIcon = {
                        if (searchText.isNotEmpty()) {
                            IconButton(onClick = { onChangeSearchText("") }) {
                                Icon(Icons.Default.Close, contentDescription = stringResource(R.string.close))
                            }
                        }
                    },
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        capitalization = KeyboardCapitalization.Sentences
                    ),
                    singleLine = true
                )
            }

            if (experiences.isEmpty()) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)
                ) {
                    when {
                        isSearchEnabled && searchText.isNotEmpty() -> {
                            val title = stringResource(R.string.no_results)
                            val description = if(isFavoriteEnabled) stringResource(R.string.no_favorite_experience_titles_match_your_search)
                            else stringResource(R.string.no_experience_titles_match_your_search)
                            EmptyScreenDisclaimer(title = title, description = description)
                        }
                        !isSearchEnabled && isFavoriteEnabled -> {
                            EmptyScreenDisclaimer(
                                title = stringResource(R.string.no_favorites),
                                description = stringResource(R.string.no_favorites_description)
                            )
                        }
                        !isSearchEnabled && !isFavoriteEnabled -> {
                            EmptyScreenDisclaimer(
                                title = stringResource(R.string.no_experiences_yet),
                                description = stringResource(R.string.add_your_first_ingestion)
                            )
                        }
                    }
                }
            } else {
                Box(modifier = Modifier.fillMaxSize()) {
                    val listState = rememberLazyListState()
                    LazyColumn(state = listState) {
                        items(experiences, key = { it.experience.id }) { experienceWithIngestions ->
                            ExperienceRow(
                                experienceWithIngestions,
                                navigateToExperienceScreen = {
                                    navigateToExperiencePopNothing(experienceWithIngestions.experience.id)
                                },
                                isTimeRelativeToNow = isTimeRelativeToNow
                            )
                            HorizontalDivider()
                        }
                    }

                    val isScrollUpButtonShown by remember { derivedStateOf { listState.firstVisibleItemIndex > 2 } }
                    this@Column.AnimatedVisibility(
                        visible = isScrollUpButtonShown,
                        modifier = Modifier.align(Alignment.BottomEnd)
                    ) {
                        val scope = rememberCoroutineScope()
                        SmallFloatingActionButton(
                            modifier = Modifier.padding(16.dp),
                            onClick = { scope.launch { listState.animateScrollToItem(index = 0) } }
                        ) {
                            Icon(Icons.Default.ArrowUpward, contentDescription = stringResource(R.string.scroll_to_top))
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun ExperiencesScreenPreview(
    @PreviewParameter(
        JournalScreenPreviewProvider::class,
    ) experiences: List<ExperienceWithIngestionsCompanionsAndRatings>,
) {
    JournalTheme {
        JournalScreen(
            navigateToExperiencePopNothing = {},
            navigateToAddIngestion = {},
            navigateToCalendar = {},
            isFavoriteEnabled = false,
            onChangeIsFavorite = {},
            isTimeRelativeToNow = true,
            onChangeIsRelative = {},
            searchText = "",
            onChangeSearchText = {},
            isSearchEnabled = true,
            onChangeIsSearchEnabled = {},
            experiences = experiences,
        )
    }
}