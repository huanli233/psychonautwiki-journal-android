package com.isaakhanimann.journal.ui.tabs.search.custom

import androidx.compose.foundation.layout.imePadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.isaakhanimann.journal.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCustomSubstanceScreen(
    navigateBack: () -> Unit,
    initialName: String = "",
    viewModel: AddCustomSubstanceViewModel = hiltViewModel()
) {
    LaunchedEffect(Unit) {
        viewModel.name = initialName
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.add_custom_substance)) },
                navigationIcon = {
                    IconButton(onClick = navigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.navigate_back))
                    }
                }
            )
        },
        floatingActionButton = {
            if (viewModel.isValid) {
                ExtendedFloatingActionButton(
                    modifier = Modifier.imePadding(),
                    onClick = {
                        viewModel.addCustomSubstance {
                            navigateBack()
                        }
                    },
                    icon = { Icon(Icons.Filled.Done, contentDescription = stringResource(R.string.done_cd)) },
                    text = { Text(stringResource(R.string.done)) },
                )
            }
        }
    ) { padding ->
        AddOrEditCustomSubstanceContent(
            padding = padding,
            name = viewModel.name,
            units = viewModel.units,
            description = viewModel.description,
            onNameChange = { viewModel.name = it },
            onUnitsChange = { viewModel.units = it },
            onDescriptionChange = { viewModel.description = it },
            roaInfos = viewModel.roaInfos,
            onAddRoa = viewModel::addRoa,
            onRemoveRoa = viewModel::removeRoa,
            onUpdateRoa = viewModel::updateRoa
        )
    }
}