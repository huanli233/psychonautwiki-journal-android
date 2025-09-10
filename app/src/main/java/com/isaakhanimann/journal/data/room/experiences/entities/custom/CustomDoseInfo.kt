package com.isaakhanimann.journal.data.room.experiences.entities.custom

import kotlinx.serialization.Serializable

@Serializable
data class CustomDoseInfo(
    var units: String? = null,
    val lightMin: Double? = null,
    val commonMin: Double? = null,
    val strongMin: Double? = null,
    val heavyMin: Double? = null
)