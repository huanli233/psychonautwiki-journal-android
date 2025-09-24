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

package com.isaakhanimann.journal.ui.tabs.journal.experience.timeline

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.isaakhanimann.journal.data.room.experiences.entities.AdaptiveColor
import com.isaakhanimann.journal.data.room.experiences.entities.SubstanceColor
import com.isaakhanimann.journal.data.substances.AdministrationRoute
import com.isaakhanimann.journal.data.substances.classes.roa.DurationRange
import com.isaakhanimann.journal.data.substances.classes.roa.DurationUnits
import com.isaakhanimann.journal.data.substances.classes.roa.RoaDuration
import com.isaakhanimann.journal.ui.tabs.journal.experience.components.DataForOneEffectLine
import java.time.Instant
import java.time.temporal.ChronoUnit

private val sampleRoaDuration = RoaDuration(
    onset = DurationRange(
        min = 20f,
        max = 40f,
        units = DurationUnits.MINUTES
    ),
    comeup = DurationRange(
        min = 15f,
        max = 30f,
        units = DurationUnits.MINUTES
    ),
    peak = DurationRange(
        min = 1.5f,
        max = 2.5f,
        units = DurationUnits.HOURS
    ),
    offset = DurationRange(
        min = 2f,
        max = 4f,
        units = DurationUnits.HOURS
    ),
    total = DurationRange(
        min = 3f,
        max = 5f,
        units = DurationUnits.HOURS
    ),
    afterglow = DurationRange(
        min = 12f,
        max = 48f,
        units = DurationUnits.HOURS
    )
)

class TimelinesPreviewProvider :
    PreviewParameterProvider<List<DataForOneEffectLine>> {
    override val values: Sequence<List<DataForOneEffectLine>> = sequenceOf(
        listOf(
            DataForOneEffectLine(
                substanceName = "name15",
                route = AdministrationRoute.ORAL,
                roaDuration = sampleRoaDuration,
                height = 3f,
                horizontalWeight = 0.5f,
                color = SubstanceColor.Predefined(AdaptiveColor.BLUE),
                startTime = Instant.now().minus(3, ChronoUnit.HOURS),
                endTime = null,
            ),
            DataForOneEffectLine(
                substanceName = "name16",
                route = AdministrationRoute.ORAL,
                roaDuration = sampleRoaDuration,
                height = 2f,
                horizontalWeight = 0.5f,
                color = SubstanceColor.Predefined(AdaptiveColor.GREEN),
                startTime = Instant.now().minus(80, ChronoUnit.MINUTES),
                endTime = Instant.now().plus(100, ChronoUnit.MINUTES),
            ),
            DataForOneEffectLine(
                substanceName = "name17",
                route = AdministrationRoute.ORAL,
                roaDuration = sampleRoaDuration,
                height = 5f,
                horizontalWeight = 0.5f,
                color = SubstanceColor.Predefined(AdaptiveColor.PINK),
                startTime = Instant.now().minus(40, ChronoUnit.MINUTES),
                endTime = Instant.now().plus(150, ChronoUnit.MINUTES),
            ),
            DataForOneEffectLine(
                substanceName = "name17",
                route = AdministrationRoute.ORAL,
                roaDuration = sampleRoaDuration,
                height = 1f,
                horizontalWeight = 0.5f,
                color = SubstanceColor.Predefined(AdaptiveColor.PURPLE),
                startTime = Instant.now().plus(160, ChronoUnit.MINUTES),
                endTime = Instant.now().plus(200, ChronoUnit.MINUTES),
            ),
        ),
        listOf(
            DataForOneEffectLine(
                substanceName = "name",
                route = AdministrationRoute.SUBLINGUAL,
                roaDuration = RoaDuration(
                    onset = DurationRange(
                        min = 20f,
                        max = 40f,
                        units = DurationUnits.MINUTES
                    ),
                    comeup = null,
                    peak = null,
                    offset = null,
                    total = null,
                    afterglow = null
                ),
                height = 1f,
                horizontalWeight = 0.5f,
                color = SubstanceColor.Predefined(AdaptiveColor.BLUE),
                startTime = Instant.now().minus(4, ChronoUnit.HOURS),
                endTime = null,
            )
        ),
    )
}