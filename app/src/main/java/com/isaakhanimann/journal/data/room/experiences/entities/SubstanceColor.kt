/*
 * Copyright (c) 2023. Isaak Hanimann.
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

package com.isaakhanimann.journal.data.room.experiences.entities

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

sealed class SubstanceColor {
    data class Predefined(val color: AdaptiveColor) : SubstanceColor()
    data class Custom(val value: Int) : SubstanceColor()

    @Composable
    fun toColor(): Color {
        return when (this) {
            is Predefined -> color.getColor()
            is Custom -> Color(value)
        }
    }

    fun getComposeColor(isDarkTheme: Boolean): Color {
        return when (this) {
            is Predefined -> color.getComposeColor(isDarkTheme)
            is Custom -> Color(value)
        }
    }
}

fun SubstanceCompanion.getSubstanceColor(): SubstanceColor {
    return customColor?.let { SubstanceColor.Custom(it) }
        ?: color?.let { SubstanceColor.Predefined(it) }
        ?: SubstanceColor.Predefined(AdaptiveColor.BLUE)
}