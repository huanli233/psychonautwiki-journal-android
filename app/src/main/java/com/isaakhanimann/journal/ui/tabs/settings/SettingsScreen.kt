/*
 * Copyright (c) 2022-2023. Isaak Hanimann.
 * This file is part of PsychonautWiki Journal.
 *
 * PsychonautWiki Journal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version.
 *
 * PsychonautWiki Journal is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with PsychonautWiki Journal.  If not, see https://www.gnu.org/licenses/gpl-3.0.en.html.
 */

package com.isaakhanimann.journal.ui.tabs.settings

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Article
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.outlined.AdUnits
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.DeleteForever
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.FileUpload
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Medication
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Timeline
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material.icons.outlined.VolunteerActivism
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.os.LocaleListCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.isaakhanimann.journal.JournalApp
import com.isaakhanimann.journal.MainActivity
import com.isaakhanimann.journal.R
import com.isaakhanimann.journal.ui.VERSION_NAME
import com.isaakhanimann.journal.ui.utils.getStringOfPattern
import com.isaakhanimann.journal.util.LangList
import com.isaakhanimann.journal.util.LocaleDelegate
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.time.Instant
import java.util.Locale

@Preview
@Composable
fun SettingsPreview() {
    SettingsScreen(
        deleteEverything = {},
        navigateToFAQ = {},
        navigateToComboSettings = {},
        navigateToSubstanceColors = {},
        navigateToCustomUnits = {},
        navigateToCustomSubstances = {},
        navigateToCustomRecipes = {},
        navigateToDonate = {},
        importFile = {},
        exportFile = {},
        language = "SYSTEM",
        saveLanguage = {},
        snackbarHostState = remember { SnackbarHostState() },
        areDosageDotsHidden = false,
        saveDosageDotsAreHidden = {},
        isTimelineHidden = false,
        saveIsTimelineHidden = {},
        areSubstanceHeightsIndependent = false,
        saveAreSubstanceHeightsIndependent = {},
    )
}

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    navigateToFAQ: () -> Unit,
    navigateToComboSettings: () -> Unit,
    navigateToSubstanceColors: () -> Unit,
    navigateToCustomUnits: () -> Unit,
    navigateToCustomRecipes: () -> Unit,
    navigateToCustomSubstances: () -> Unit,
    navigateToDonate: () -> Unit,
) {
    SettingsScreen(
        navigateToFAQ = navigateToFAQ,
        navigateToComboSettings = navigateToComboSettings,
        navigateToSubstanceColors = navigateToSubstanceColors,
        navigateToCustomUnits = navigateToCustomUnits,
        navigateToCustomRecipes = navigateToCustomRecipes,
        navigateToCustomSubstances = navigateToCustomSubstances,
        navigateToDonate = navigateToDonate,
        deleteEverything = viewModel::deleteEverything,
        importFile = viewModel::importFile,
        exportFile = viewModel::exportFile,
        snackbarHostState = viewModel.snackbarHostState,
        language = viewModel.languageFlow.collectAsState().value,
        saveLanguage = viewModel::saveLanguage,
        areDosageDotsHidden = viewModel.areDosageDotsHiddenFlow.collectAsState().value,
        saveDosageDotsAreHidden = viewModel::saveDosageDotsAreHidden,
        isTimelineHidden = viewModel.isTimelineHiddenFlow.collectAsState().value,
        saveIsTimelineHidden = viewModel::saveIsTimelineHidden,
        areSubstanceHeightsIndependent = viewModel.areSubstanceHeightsIndependentFlow.collectAsState().value,
        saveAreSubstanceHeightsIndependent = viewModel::saveAreSubstanceHeightsIndependent,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navigateToFAQ: () -> Unit,
    navigateToComboSettings: () -> Unit,
    navigateToSubstanceColors: () -> Unit,
    navigateToCustomUnits: () -> Unit,
    navigateToCustomRecipes: () -> Unit,
    navigateToCustomSubstances: () -> Unit,
    navigateToDonate: () -> Unit,
    deleteEverything: () -> Unit,
    importFile: (uri: Uri) -> Unit,
    exportFile: (uri: Uri) -> Unit,
    snackbarHostState: SnackbarHostState,
    language: String,
    saveLanguage: (String) -> Unit,
    areDosageDotsHidden: Boolean,
    saveDosageDotsAreHidden: (Boolean) -> Unit,
    isTimelineHidden: Boolean,
    saveIsTimelineHidden: (Boolean) -> Unit,
    areSubstanceHeightsIndependent: Boolean,
    saveAreSubstanceHeightsIndependent: (Boolean) -> Unit,
) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current

    // State for dialogs
    var showExportDialog by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings)) }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            // UI Category
            item { PreferenceCategory(title = stringResource(R.string.ui)) }

            item {
                LanguagePreference(
                    currentLanguageTag = language,
                    onLanguageSelected = { newLanguageTag ->
                        runBlocking { saveLanguage(newLanguageTag) }
                        applyLanguage(newLanguageTag)
                        (context as? MainActivity)?.recreate()
                    }
                )
            }
            item {
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    thickness = DividerDefaults.Thickness,
                    color = DividerDefaults.color
                )
            }

            item {
                Preference(
                    title = stringResource(R.string.custom_units),
                    icon = Icons.Outlined.AdUnits,
                    onClick = navigateToCustomUnits
                )
            }

            item {
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    thickness = DividerDefaults.Thickness,
                    color = DividerDefaults.color
                )
            }

            item {
                Preference(
                    title = stringResource(R.string.custom_recipes),
                    icon = Icons.Outlined.Medication,
                    onClick = navigateToCustomRecipes
                )
            }
            item {
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    thickness = DividerDefaults.Thickness,
                    color = DividerDefaults.color
                )
            }


            item {
                Preference(
                    title = stringResource(R.string.custom_substances),
                    icon = Icons.Outlined.Medication,
                    onClick = navigateToCustomSubstances
                )
            }
            item {
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    thickness = DividerDefaults.Thickness,
                    color = DividerDefaults.color
                )
            }


            item {
                Preference(
                    title = stringResource(R.string.substance_colors),
                    icon = Icons.Outlined.Palette,
                    onClick = navigateToSubstanceColors
                )
            }
            item {
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    thickness = DividerDefaults.Thickness,
                    color = DividerDefaults.color
                )
            }

            item {
                Preference(
                    title = stringResource(R.string.interaction_settings),
                    icon = Icons.Outlined.WarningAmber,
                    onClick = navigateToComboSettings
                )
            }
            item {
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    thickness = DividerDefaults.Thickness,
                    color = DividerDefaults.color
                )
            }

            item {
                SwitchPreference(
                    title = stringResource(R.string.hide_dosage_dots),
                    summary = stringResource(R.string.hide_dosage_dots_summary),
                    icon = Icons.Outlined.VisibilityOff,
                    checked = areDosageDotsHidden,
                    onCheckedChange = saveDosageDotsAreHidden
                )
            }
            item {
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    thickness = DividerDefaults.Thickness,
                    color = DividerDefaults.color
                )
            }

            item {
                SwitchPreference(
                    title = stringResource(R.string.hide_timeline),
                    summary = stringResource(R.string.hide_timeline_summary),
                    icon = Icons.Outlined.Timeline,
                    checked = isTimelineHidden,
                    onCheckedChange = saveIsTimelineHidden
                )
            }
            item {
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    thickness = DividerDefaults.Thickness,
                    color = DividerDefaults.color
                )
            }

            item {
                SwitchPreference(
                    title = stringResource(R.string.independent_substance_heights),
                    summary = stringResource(R.string.independent_substance_heights_description),
                    icon = Icons.Outlined.Info,
                    checked = areSubstanceHeightsIndependent,
                    onCheckedChange = saveAreSubstanceHeightsIndependent
                )
            }

            // App Data Category
            item { PreferenceCategory(title = stringResource(R.string.app_data)) }

            item {
                Preference(
                    title = stringResource(R.string.export_file),
                    summary = stringResource(R.string.export_description_short),
                    icon = Icons.Outlined.FileUpload,
                    onClick = { showExportDialog = true }
                )
            }
            item {
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    thickness = DividerDefaults.Thickness,
                    color = DividerDefaults.color
                )
            }

            item {
                Preference(
                    title = stringResource(R.string.import_file_title),
                    summary = stringResource(R.string.import_description_short),
                    icon = Icons.Outlined.FileDownload,
                    onClick = { showImportDialog = true }
                )
            }
            item {
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    thickness = DividerDefaults.Thickness,
                    color = DividerDefaults.color
                )
            }

            item {
                Preference(
                    title = stringResource(R.string.delete_everything),
                    summary = stringResource(R.string.delete_everything_description_short),
                    icon = Icons.Outlined.DeleteForever,
                    onClick = { showDeleteDialog = true }
                )
            }

            // Feedback Category
            item { PreferenceCategory(title = stringResource(R.string.feedback)) }

            item {
                Preference(
                    title = "FAQ",
                    icon = Icons.AutoMirrored.Outlined.HelpOutline,
                    onClick = navigateToFAQ
                )
            }
            item {
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    thickness = DividerDefaults.Thickness,
                    color = DividerDefaults.color
                )
            }

            item {
                Preference(
                    title = stringResource(R.string.question_bug_report),
                    icon = Icons.AutoMirrored.Outlined.OpenInNew,
                    onClick = { uriHandler.openUri("https://t.me/+ss8uZhBF6g00MTY8") }
                )
            }
            item {
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    thickness = DividerDefaults.Thickness,
                    color = DividerDefaults.color
                )
            }

            item {
                Preference(
                    title = "Donate",
                    icon = Icons.Outlined.VolunteerActivism,
                    onClick = navigateToDonate
                )
            }

            // App Category
            item { PreferenceCategory(title = stringResource(R.string.app)) }

            item {
                Preference(
                    title = stringResource(R.string.source_code),
                    icon = Icons.Outlined.Code,
                    onClick = { uriHandler.openUri("https://github.com/isaakhanimann/psychonautwiki-journal-android") }
                )
            }
            item {
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    thickness = DividerDefaults.Thickness,
                    color = DividerDefaults.color
                )
            }

            item {
                val sendIntent: Intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, SHARE_APP_URL)
                    type = "text/plain"
                }
                val shareIntent = Intent.createChooser(sendIntent, null)
                Preference(
                    title = stringResource(R.string.share),
                    icon = Icons.Outlined.Share,
                    onClick = { context.startActivity(shareIntent) }
                )
            }
            item {
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    thickness = DividerDefaults.Thickness,
                    color = DividerDefaults.color
                )
            }

            item {
                Preference(
                    title = stringResource(R.string.version, VERSION_NAME),
                    icon = Icons.Outlined.Info,
                    onClick = {}
                )
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // --- Dialogs ---
    val jsonMIMEType = "application/json"
    val launcherExport = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument(mimeType = jsonMIMEType)
    ) { uri ->
        if (uri != null) exportFile(uri)
    }
    if (showExportDialog) {
        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            title = { Text(text = stringResource(R.string.want_to_export)) },
            text = { Text(stringResource(R.string.export_description)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showExportDialog = false
                        launcherExport.launch("Journal ${Instant.now().getStringOfPattern("dd MMM yyyy")}.json")
                    }
                ) { Text(stringResource(R.string.export)) }
            },
            dismissButton = {
                TextButton(onClick = { showExportDialog = false }) { Text(stringResource(R.string.cancel)) }
            }
        )
    }

    val launcherImport = rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) importFile(uri)
    }
    if (showImportDialog) {
        AlertDialog(
            onDismissRequest = { showImportDialog = false },
            title = { Text(text = stringResource(R.string.want_to_import_file)) },
            text = { Text(stringResource(R.string.import_description)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showImportDialog = false
                        launcherImport.launch(jsonMIMEType)
                    }
                ) { Text(stringResource(R.string.import_file)) }
            },
            dismissButton = {
                TextButton(onClick = { showImportDialog = false }) { Text(stringResource(R.string.cancel)) }
            }
        )
    }

    val scope = rememberCoroutineScope()
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(text = stringResource(R.string.delete_everything)) },
            text = { Text(stringResource(R.string.delete_everything_description)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        deleteEverything()
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                message = context.getString(R.string.deleted_everything),
                                duration = SnackbarDuration.Short
                            )
                        }
                    }
                ) { Text(stringResource(R.string.delete)) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text(stringResource(R.string.cancel)) }
            }
        )
    }
}

const val SHARE_APP_URL = "https://psychonautwiki.org/wiki/PsychonautWiki_Journal"

/**
 * A composable for displaying a category title in a preferences screen.
 */
@Composable
private fun PreferenceCategory(title: String) {
    Text(
        text = title,
        color = MaterialTheme.colorScheme.primary,
        style = MaterialTheme.typography.labelLarge,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp)
    )
}

/**
 * A standard preference item with an icon, title, summary, and click action.
 */
@Composable
private fun Preference(
    title: String,
    summary: String? = null,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp)
        )
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge)
            summary?.let {
                Spacer(Modifier.height(2.dp))
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * A preference item with a switch for boolean settings.
 */
@Composable
private fun SwitchPreference(
    title: String,
    summary: String? = null,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp)
        )
        Spacer(Modifier.width(16.dp))
        Column(
            modifier = Modifier.weight(1f),
        ) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge)
            summary?.let {
                Spacer(Modifier.height(2.dp))
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = null, // Click is handled by the Row
            modifier = Modifier.padding(start = 16.dp)
        )
    }
}


@Composable
private fun LanguagePreference(
    currentLanguageTag: String,
    onLanguageSelected: (String) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }

    Preference(
        title = stringResource(R.string.settings_language),
        summary = getLanguageDisplayName(tag = currentLanguageTag),
        icon = Icons.Outlined.Language,
        onClick = { showDialog = true }
    )

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(stringResource(R.string.settings_language)) },
            text = {
                LazyColumn {
                    items(LangList.LOCALES) { tag ->
                        val displayName = getLanguageDisplayName(tag)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onLanguageSelected(tag)
                                    showDialog = false
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = currentLanguageTag == tag,
                                onClick = null
                            )
                            Spacer(Modifier.width(16.dp))
                            Text(displayName)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text(stringResource(android.R.string.cancel))
                }
            }
        )
    }
}


@Composable
private fun getLanguageDisplayName(tag: String): String {
    return if (tag == "SYSTEM") {
        stringResource(id = R.string.follow_system)
    } else {
        val locale = Locale.forLanguageTag(tag)
        locale.getDisplayName(locale)
            .replaceFirstChar { if (it.isLowerCase()) it.titlecase(locale) else it.toString() }
    }
}


fun applyLanguage(tag: String) {
    val localeList = if (tag == "SYSTEM") {
        LocaleListCompat.getEmptyLocaleList()
    } else {
        LocaleListCompat.forLanguageTags(tag)
    }
    val app = JournalApp.instance
    val locale = app.getLocale(tag)
    val res = app.resources
    val config = res.configuration
    config.setLocale(locale)
    if (locale != null) {
        LocaleDelegate.defaultLocale = locale
    }
    @Suppress("DEPRECATION")
    res.updateConfiguration(config, res.displayMetrics);
    AppCompatDelegate.setApplicationLocales(localeList)
}