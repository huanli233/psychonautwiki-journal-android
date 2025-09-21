package com.isaakhanimann.journal.ui.tabs.search.custom

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.isaakhanimann.journal.R
import com.isaakhanimann.journal.data.room.experiences.entities.custom.CustomDoseInfo
import com.isaakhanimann.journal.data.room.experiences.entities.custom.CustomDurationInfo
import com.isaakhanimann.journal.data.room.experiences.entities.custom.CustomRoaInfo
import com.isaakhanimann.journal.data.room.experiences.entities.custom.SerializableDurationRange
import com.isaakhanimann.journal.data.room.experiences.entities.custom.SerializableDurationUnits
import com.isaakhanimann.journal.data.substances.AdministrationRoute

@Composable
fun AddOrEditCustomSubstanceContent(
    padding: PaddingValues,
    name: String,
    onNameChange: (String) -> Unit,
    units: String,
    onUnitsChange: (String) -> Unit,
    description: String,
    onDescriptionChange: (String) -> Unit,
    roaInfos: List<CustomRoaInfo>,
    onAddRoa: (CustomRoaInfo) -> Unit,
    onRemoveRoa: (CustomRoaInfo) -> Unit,
    onUpdateRoa: (Int, CustomRoaInfo) -> Unit
) {
    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier
            .padding(padding)
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            OutlinedTextField(
                value = name,
                onValueChange = onNameChange,
                label = { Text(stringResource(R.string.name)) },
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                keyboardOptions = KeyboardOptions.Default.copy(capitalization = KeyboardCapitalization.Words),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = units,
                onValueChange = onUnitsChange,
                label = { Text(stringResource(R.string.default_units)) },
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SuggestionChip(onClick = { onUnitsChange("µg") }, label = { Text("µg") })
                SuggestionChip(onClick = { onUnitsChange("mg") }, label = { Text("mg") })
                SuggestionChip(onClick = { onUnitsChange("g") }, label = { Text("g") })
                SuggestionChip(onClick = { onUnitsChange("mL") }, label = { Text("mL") })
            }

            OutlinedTextField(
                value = description,
                onValueChange = onDescriptionChange,
                label = { Text(stringResource(R.string.description)) },
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                keyboardOptions = KeyboardOptions.Default.copy(capitalization = KeyboardCapitalization.Sentences),
                modifier = Modifier.fillMaxWidth()
            )
        }


        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(stringResource(R.string.routes_of_administration), style = MaterialTheme.typography.titleLarge)
            roaInfos.forEachIndexed { index, roaInfo ->
                RoaInfoEditor(
                    roaInfo = roaInfo,
                    onUpdate = { onUpdateRoa(index, it) },
                    onRemove = { onRemoveRoa(roaInfo) }
                )
            }
        }

        FilledTonalButton(
            onClick = {
                val defaultRoa = AdministrationRoute.entries.firstOrNull { route ->
                    roaInfos.none { it.administrationRoute == route }
                } ?: AdministrationRoute.ORAL
                onAddRoa(
                    CustomRoaInfo(
                        administrationRoute = defaultRoa,
                        doseInfo = CustomDoseInfo(),
                        durationInfo = CustomDurationInfo()
                    )
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_route))
            Text(stringResource(R.string.add_route_of_administration), modifier = Modifier.padding(start = 8.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RoaInfoEditor(
    roaInfo: CustomRoaInfo,
    onUpdate: (CustomRoaInfo) -> Unit,
    onRemove: () -> Unit
) {
    var isRoaMenuExpanded by remember { mutableStateOf(false) }

    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ExposedDropdownMenuBox(
                    expanded = isRoaMenuExpanded,
                    onExpandedChange = { isRoaMenuExpanded = !isRoaMenuExpanded },
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = roaInfo.administrationRoute.name.lowercase().replaceFirstChar { it.uppercase() },
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.route)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isRoaMenuExpanded) },
                        modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    )
                    ExposedDropdownMenu(
                        expanded = isRoaMenuExpanded,
                        onDismissRequest = { isRoaMenuExpanded = false }
                    ) {
                        AdministrationRoute.entries.forEach { route ->
                            DropdownMenuItem(
                                text = { Text(route.name.lowercase().replaceFirstChar { it.uppercase() }) },
                                onClick = {
                                    onUpdate(roaInfo.copy(administrationRoute = route))
                                    isRoaMenuExpanded = false
                                }
                            )
                        }
                    }
                }
                IconButton(onClick = onRemove) {
                    Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.remove_route))
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stringResource(R.string.dosage), style = MaterialTheme.typography.titleMedium)
                val doseInfo = roaInfo.doseInfo ?: CustomDoseInfo()
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    DoseTextField(stringResource(R.string.dose_light), doseInfo.lightMin) { onUpdate(roaInfo.copy(doseInfo = doseInfo.copy(lightMin = it))) }
                    DoseTextField(stringResource(R.string.dose_common), doseInfo.commonMin) { onUpdate(roaInfo.copy(doseInfo = doseInfo.copy(commonMin = it))) }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    DoseTextField(stringResource(R.string.dose_strong), doseInfo.strongMin) { onUpdate(roaInfo.copy(doseInfo = doseInfo.copy(strongMin = it))) }
                    DoseTextField(stringResource(R.string.dose_heavy), doseInfo.heavyMin) { onUpdate(roaInfo.copy(doseInfo = doseInfo.copy(heavyMin = it))) }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(stringResource(R.string.duration), style = MaterialTheme.typography.titleMedium)
                val durationInfo = roaInfo.durationInfo ?: CustomDurationInfo()
                DurationRangeEditor(stringResource(R.string.duration_onset), durationInfo.onset) { onUpdate(roaInfo.copy(durationInfo = durationInfo.copy(onset = it))) }
                DurationRangeEditor(stringResource(R.string.duration_comeup), durationInfo.comeup) { onUpdate(roaInfo.copy(durationInfo = durationInfo.copy(comeup = it))) }
                DurationRangeEditor(stringResource(R.string.duration_peak), durationInfo.peak) { onUpdate(roaInfo.copy(durationInfo = durationInfo.copy(peak = it))) }
                DurationRangeEditor(stringResource(R.string.duration_offset), durationInfo.offset) { onUpdate(roaInfo.copy(durationInfo = durationInfo.copy(offset = it))) }
                DurationRangeEditor(stringResource(R.string.duration_total), durationInfo.total) { onUpdate(roaInfo.copy(durationInfo = durationInfo.copy(total = it))) }
                DurationRangeEditor(stringResource(R.string.duration_afterglow), durationInfo.afterglow) { onUpdate(roaInfo.copy(durationInfo = durationInfo.copy(afterglow = it))) }
            }
        }
    }
}

@Composable
private fun RowScope.DoseTextField(label: String, value: Double?, onValueChange: (Double?) -> Unit) {
    var text by remember { mutableStateOf(value?.toString() ?: "") }
    LaunchedEffect(value) {
        if (value != text.toDoubleOrNull()) {
            text = value?.toString() ?: ""
        }
    }
    OutlinedTextField(
        value = text,
        onValueChange = { newText ->
            text = newText
            onValueChange(newText.replace(',', '.').toDoubleOrNull())
        },
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = Modifier.weight(1f),
        singleLine = true
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DurationRangeEditor(label: String, value: SerializableDurationRange?, onValueChange: (SerializableDurationRange?) -> Unit) {
    var isUnitsMenuExpanded by remember { mutableStateOf(false) }
    val currentRange = value ?: SerializableDurationRange(null, null, null)

    var minText by remember { mutableStateOf(currentRange.min?.toString() ?: "") }
    LaunchedEffect(currentRange.min) {
        if (currentRange.min != minText.toFloatOrNull()) {
            minText = currentRange.min?.toString() ?: ""
        }
    }

    var maxText by remember { mutableStateOf(currentRange.max?.toString() ?: "") }
    LaunchedEffect(currentRange.max) {
        if (currentRange.max != maxText.toFloatOrNull()) {
            maxText = currentRange.max?.toString() ?: ""
        }
    }

    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(vertical = 2.dp)) {
        Text(label, modifier = Modifier.width(80.dp), style = MaterialTheme.typography.bodyMedium)
        OutlinedTextField(
            value = minText,
            onValueChange = { newText ->
                minText = newText
                onValueChange(currentRange.copy(min = newText.replace(',', '.').toFloatOrNull()))
            },
            label = { Text(stringResource(R.string.min)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.weight(1f),
            singleLine = true
        )
        OutlinedTextField(
            value = maxText,
            onValueChange = { newText ->
                maxText = newText
                onValueChange(currentRange.copy(max = newText.replace(',', '.').toFloatOrNull()))
            },
            label = { Text(stringResource(R.string.max)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.weight(1f),
            singleLine = true
        )
        ExposedDropdownMenuBox(
            expanded = isUnitsMenuExpanded,
            onExpandedChange = { isUnitsMenuExpanded = !isUnitsMenuExpanded }
        ) {
            OutlinedButton(onClick = { isUnitsMenuExpanded = true }, modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable)) {
                Text(currentRange.units?.text ?: stringResource(R.string.units_placeholder))
            }
            ExposedDropdownMenu(
                expanded = isUnitsMenuExpanded,
                onDismissRequest = { isUnitsMenuExpanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.disabled)) },
                    onClick = {
                        onValueChange(currentRange.copy(units = null))
                        isUnitsMenuExpanded = false
                    }
                )
                SerializableDurationUnits.entries.forEach { unit ->
                    DropdownMenuItem(
                        text = { Text(unit.text) },
                        onClick = {
                            onValueChange(currentRange.copy(units = unit))
                            isUnitsMenuExpanded = false
                        }
                    )
                }
            }
        }
    }
}