package com.isaakhanimann.journal.ui.tabs.settings.customsubstances

import com.isaakhanimann.journal.data.room.experiences.entities.CustomSubstance
import kotlinx.serialization.Serializable

@Serializable
data class CustomSubstancesExport(
    val customUnits: List<CustomSubstance>
)