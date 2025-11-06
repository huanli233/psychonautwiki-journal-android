/*
 * Copyright (c) 2022. Isaak Hanimann.
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

package com.isaakhanimann.journal.ui.tabs.search.substancerow

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import com.isaakhanimann.journal.ui.tabs.search.CategoryModel
import com.isaakhanimann.journal.ui.tabs.search.SubstanceModel
import com.isaakhanimann.journal.ui.theme.horizontalPadding

@Preview(showBackground = true)
@Composable
fun SubstanceRowPreview(
    @PreviewParameter(SubstanceModelPreviewProvider::class) substanceModel: SubstanceModel
) {
    SubstanceRow(substanceModel = substanceModel, onTap = {})
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SubstanceRow(
    substanceModel: SubstanceModel,
    onTap: (substanceName: String) -> Unit
) {
    ElevatedCard(
        onClick = { onTap(substanceModel.name) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = substanceModel.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            
            if (substanceModel.commonNames.isNotEmpty()) {
                val commaSeparatedNames = substanceModel.commonNames.joinToString(separator = ", ")
                Text(
                    text = commaSeparatedNames,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            if (substanceModel.categories.isNotEmpty()) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    substanceModel.categories.forEach { category ->
                        CategoryChipStatic(categoryModel = category)
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryChipStatic(categoryModel: CategoryModel) {
    // Ensure good contrast by using theme colors for text
    val isDarkTheme = androidx.compose.foundation.isSystemInDarkTheme()
    val backgroundColor = categoryModel.color.copy(alpha = 0.15f)
    
    // Use high contrast text color based on theme
    val textColor = if (isDarkTheme) {
        // In dark theme, use lighter version of the color or white
        if (isColorLight(categoryModel.color)) {
            categoryModel.color
        } else {
            categoryModel.color.copy(
                red = kotlin.math.min(1f, categoryModel.color.red + 0.3f),
                green = kotlin.math.min(1f, categoryModel.color.green + 0.3f),
                blue = kotlin.math.min(1f, categoryModel.color.blue + 0.3f)
            )
        }
    } else {
        // In light theme, use darker version of the color or ensure minimum contrast
        if (isColorLight(categoryModel.color)) {
            categoryModel.color.copy(
                red = kotlin.math.max(0f, categoryModel.color.red - 0.4f),
                green = kotlin.math.max(0f, categoryModel.color.green - 0.4f),
                blue = kotlin.math.max(0f, categoryModel.color.blue - 0.4f)
            )
        } else {
            categoryModel.color
        }
    }
    
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = backgroundColor,
        border = androidx.compose.foundation.BorderStroke(
            width = 0.8.dp,
            color = textColor.copy(alpha = 0.4f)
        ),
        modifier = Modifier
    ) {
        Text(
            text = categoryModel.name,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            color = textColor,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

// Helper function to determine if a color is light
private fun isColorLight(color: androidx.compose.ui.graphics.Color): Boolean {
    val luminance = 0.299 * color.red + 0.587 * color.green + 0.114 * color.blue
    return luminance > 0.6f
}