package com.isaakhanimann.journal.ui.tabs.settings.customsubstances

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.isaakhanimann.journal.R
import com.isaakhanimann.journal.ui.utils.getStringOfPattern
import java.time.Instant

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun CustomSubstanceManagementScreen(
    navigateBack: () -> Unit,
    navigateToAddCustomSubstance: () -> Unit,
    navigateToEditCustomSubstance: (Int) -> Unit,
    viewModel: CustomSubstanceManagementViewModel = hiltViewModel()
) {
    val substances by viewModel.allSubstances.collectAsState()
    val selectionMode by viewModel.selectionMode.collectAsState()
    val selectedIds by viewModel.selectedSubstances.collectAsState()
    val context = LocalContext.current

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json"),
        onResult = { uri ->
            if (uri != null) {
                viewModel.exportSelection(context, uri) { }
            }
        }
    )

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            if (uri != null) {
                viewModel.importSubstances(context, uri) { }
            }
        }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (selectionMode == SelectionMode.None) stringResource(R.string.custom_substances) else "${selectedIds.size} selected") },
                navigationIcon = {
                    if (selectionMode != SelectionMode.None) {
                        IconButton(onClick = viewModel::clearSelection) {
                            Icon(Icons.Default.Close, contentDescription = stringResource(R.string.clear_selection))
                        }
                    } else {
                        IconButton(onClick = navigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(
                                R.string.back
                            ))
                        }
                    }
                },
                actions = {
                    if (selectionMode != SelectionMode.None) {
                        IconButton(onClick = viewModel::selectAll) {
                            Icon(Icons.Default.CheckCircle, contentDescription = stringResource(R.string.select_all))
                        }
                        IconButton(
                            onClick = { exportLauncher.launch("Custom Substances ${Instant.now().getStringOfPattern("dd MMM yyyy")}.json") },
                            enabled = selectedIds.isNotEmpty()
                        ) {
                            Icon(Icons.Default.Save, contentDescription = stringResource(R.string.export))
                        }
                    } else {
                        var menuExpanded by remember { mutableStateOf(false) }
                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = stringResource(R.string.more))
                        }
                        DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                            DropdownMenuItem(
                                text = { Text("Import from file") },
                                onClick = {
                                    importLauncher.launch("application/json")
                                    menuExpanded = false
                                }
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            if (selectionMode == SelectionMode.None) {
                FloatingActionButton(onClick = navigateToAddCustomSubstance) {
                    Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_custom_substance))
                }
            }
        }
    ) { padding ->
        if (substances.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(stringResource(R.string.no_custom_substances_found))
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(padding),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 8.dp)
            ) {
                items(substances, key = { it.id }) { substance ->
                    val isSelected = selectedIds.contains(substance.id)
                    
                    // Gradient background card
                    androidx.compose.material3.Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .combinedClickable(
                                onClick = {
                                    if (selectionMode != SelectionMode.None) {
                                        viewModel.toggleSelection(substance.id)
                                    } else {
                                        navigateToEditCustomSubstance(substance.id)
                                    }
                                },
                                onLongClick = {
                                    viewModel.toggleSelection(substance.id)
                                }
                            ),
                        colors = androidx.compose.material3.CardDefaults.cardColors(
                            containerColor = if (isSelected) 
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                            else 
                                MaterialTheme.colorScheme.surfaceContainerHighest
                        )
                    ) {
                        androidx.compose.foundation.layout.Box {
                            // Gradient overlay
                            androidx.compose.foundation.layout.Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(80.dp)
                                    .background(
                                        androidx.compose.ui.graphics.Brush.horizontalGradient(
                                            colors = listOf(
                                                MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                                MaterialTheme.colorScheme.secondary.copy(alpha = 0.05f),
                                                androidx.compose.ui.graphics.Color.Transparent
                                            )
                                        )
                                    )
                            )
                            
                            androidx.compose.foundation.layout.Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                // Leading content
                                if (selectionMode != SelectionMode.None) {
                                    Checkbox(
                                        checked = isSelected,
                                        onCheckedChange = { viewModel.toggleSelection(substance.id) }
                                    )
                                } else {
                                    androidx.compose.material3.Surface(
                                        shape = androidx.compose.foundation.shape.CircleShape,
                                        color = MaterialTheme.colorScheme.tertiaryContainer,
                                        modifier = Modifier.padding(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = androidx.compose.material.icons.Icons.Default.Science,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onTertiaryContainer,
                                            modifier = Modifier.padding(8.dp)
                                        )
                                    }
                                }
                                
                                // Content
                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Text(
                                        text = substance.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
                                    )
                                    if (substance.description.isNotBlank()) {
                                        Text(
                                            text = substance.description,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 2,
                                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                        )
                                    }
                                }
                                
                                // Trailing content
                                if (selectionMode == SelectionMode.None) {
                                    Icon(
                                        Icons.AutoMirrored.Filled.ArrowRight, 
                                        contentDescription = "Arrow Right",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}