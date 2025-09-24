package com.isaakhanimann.journal.ui.tabs.journal.experience.components.ingestion

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.isaakhanimann.journal.data.room.experiences.entities.AdaptiveColor
import com.isaakhanimann.journal.data.room.experiences.entities.SubstanceColor

@Composable
fun VerticalLine(color: SubstanceColor) {
    Surface(
        shape = RoundedCornerShape(percent = 50),
        color = color.toColor(),
        modifier = Modifier
            .height(40.dp)
            .width(4.dp)
    ) {}
}