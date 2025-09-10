package com.isaakhanimann.journal.data.room.experiences.entities.custom

import com.isaakhanimann.journal.data.substances.classes.roa.DurationRange
import kotlinx.serialization.Serializable

@Serializable
data class SerializableDurationRange(
    val min: Float? = null,
    val max: Float? = null,
    val units: SerializableDurationUnits? = null
)

fun SerializableDurationRange.toDurationRange() = DurationRange(
    min, max, units?.toDurationUnits()
)