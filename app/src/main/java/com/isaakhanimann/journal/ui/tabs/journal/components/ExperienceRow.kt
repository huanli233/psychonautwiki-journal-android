package com.isaakhanimann.journal.ui.tabs.journal.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import com.isaakhanimann.journal.data.room.experiences.entities.getSubstanceColor
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
    val ingestions =
        experienceWithIngestionsCompanionsAndRatings.ingestionsWithCompanions.sortedBy { it.ingestion.time }

    ElevatedCard(
        onClick = navigateToExperienceScreen,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .animateContentSize(), // M3 Expressive 动画
        shape = RoundedCornerShape(16.dp), // 显式指定圆角以确保阴影正确裁剪
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = 3.dp,
            pressedElevation = 8.dp // 交互反馈
        ),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ColorRectangle(ingestions = ingestions)

            // Content
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Title with favorite star
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = experience.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    if (experience.isFavorite) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Favorite",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                // Substances
                val substanceNames = remember(ingestions) {
                    ingestions.map { it.ingestion.substanceName }.distinct()
                        .joinToString(separator = " • ")
                }
                Text(
                    text = substanceNames.ifEmpty { "No substance yet" },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // Consumer names
                val consumerNames = remember(ingestions) {
                    ingestions.mapNotNull { it.ingestion.consumerName }.distinct()
                        .joinToString(separator = ", ")
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

            // Time and Rating
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Time
                val timeStyle = MaterialTheme.typography.labelMedium
                if (isTimeRelativeToNow) {
                    RelativeDateTextNew(
                        dateTime = experienceWithIngestionsCompanionsAndRatings.sortInstant,
                        style = timeStyle,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Text(
                        text = experienceWithIngestionsCompanionsAndRatings.sortInstant.getDateWithWeekdayText(),
                        style = timeStyle,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Rating
                val rating = experienceWithIngestionsCompanionsAndRatings.rating?.sign
                if (rating != null) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = rating,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun ColorRectangle(ingestions: List<IngestionWithCompanionAndCustomUnit>) {
    val width = 6.dp
    val cornerRadius = 3.dp

    val modifier = Modifier
        .width(width)
        .height(56.dp)
        .clip(RoundedCornerShape(cornerRadius))

    when {
        ingestions.size >= 2 -> {
            val colors = ingestions.map {
                it.substanceCompanion?.getSubstanceColor()?.toColor() ?: Color.Gray
            }
            val brush = remember(ingestions) {
                Brush.verticalGradient(colors = colors)
            }
            Box(modifier = modifier.background(brush))
        }

        ingestions.size == 1 -> {
            val color =
                ingestions.first().substanceCompanion?.getSubstanceColor()?.toColor() ?: Color.Gray
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