package com.isaakhanimann.journal.ui.tabs.journal.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import com.isaakhanimann.journal.data.room.experiences.relations.ExperienceWithIngestionsCompanionsAndRatings
import com.isaakhanimann.journal.data.room.experiences.relations.IngestionWithCompanionAndCustomUnit
import com.isaakhanimann.journal.ui.utils.getDateWithWeekdayText

@Composable
fun ExperienceRow(
    experienceWithIngestionsCompanionsAndRatings: ExperienceWithIngestionsCompanionsAndRatings,
    navigateToExperienceScreen: () -> Unit = {},
    isTimeRelativeToNow: Boolean = true
) {
    val experience = experienceWithIngestionsCompanionsAndRatings.experience
    val ingestions = experienceWithIngestionsCompanionsAndRatings.ingestionsWithCompanions.sortedBy { it.ingestion.time }

    ListItem(
        modifier = Modifier.clickable(onClick = navigateToExperienceScreen),
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        headlineContent = {
            Text(
                text = experience.title,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        supportingContent = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                val substanceNames = remember(ingestions) {
                    ingestions.map { it.ingestion.substanceName }.distinct().joinToString(separator = ", ")
                }
                Text(
                    text = substanceNames.ifEmpty { "No substance yet" },
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                val consumerNames = remember(ingestions) {
                    ingestions.mapNotNull { it.ingestion.consumerName }.distinct().joinToString(separator = ", ")
                }
                if (consumerNames.isNotEmpty()) {
                    Text(
                        text = "With: $consumerNames",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        },
        leadingContent = {
            ColorRectangle(ingestions = ingestions)
        },
        overlineContent = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val timeStyle = MaterialTheme.typography.labelMedium
                if (isTimeRelativeToNow) {
                    RelativeDateTextNew(
                        dateTime = experienceWithIngestionsCompanionsAndRatings.sortInstant,
                        style = timeStyle
                    )
                } else {
                    Text(
                        text = experienceWithIngestionsCompanionsAndRatings.sortInstant.getDateWithWeekdayText(),
                        style = timeStyle
                    )
                }
                val rating = experienceWithIngestionsCompanionsAndRatings.rating?.sign
                if (rating != null) {
                    Text(text = rating, style = MaterialTheme.typography.labelLarge)
                }
            }
        },
        trailingContent = {
            if (experience.isFavorite) {
                Icon(
                    imageVector = Icons.Filled.Star,
                    contentDescription = "Is favorite",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    )
}


@Composable
fun ColorRectangle(ingestions: List<IngestionWithCompanionAndCustomUnit>) {
    val isDarkTheme = isSystemInDarkTheme()
    val width = 6.dp
    val cornerRadius = 3.dp

    val modifier = Modifier
        .width(width)
        .height(56.dp)
        .clip(RoundedCornerShape(cornerRadius))

    when {
        ingestions.size >= 2 -> {
            val brush = remember(ingestions, isDarkTheme) {
                val colors = ingestions.map { it.substanceCompanion?.color?.getComposeColor(isDarkTheme) ?: Color.Gray }
                Brush.verticalGradient(colors = colors)
            }
            Box(modifier = modifier.background(brush))
        }
        ingestions.size == 1 -> {
            val color = ingestions.first().substanceCompanion?.color?.getComposeColor(isDarkTheme) ?: Color.Gray
            Box(modifier = modifier.background(color))
        }
        else -> {
            val color = MaterialTheme.colorScheme.surfaceVariant
            Box(modifier = modifier.background(color))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ExperienceRowPreview(
    @PreviewParameter(ExperienceWithIngestionsCompanionsAndRatingsPreviewProvider::class) experienceWithIngestionsCompanionsAndRatings: ExperienceWithIngestionsCompanionsAndRatings
) {
    Column {
        ExperienceRow(
            experienceWithIngestionsCompanionsAndRatings = experienceWithIngestionsCompanionsAndRatings,
            navigateToExperienceScreen = {},
            isTimeRelativeToNow = true
        )
        HorizontalDivider()
        ExperienceRow(
            experienceWithIngestionsCompanionsAndRatings = experienceWithIngestionsCompanionsAndRatings.copy(
                experience = experienceWithIngestionsCompanionsAndRatings.experience.copy(isFavorite = false)
            ),
            navigateToExperienceScreen = {},
            isTimeRelativeToNow = false
        )
    }
}