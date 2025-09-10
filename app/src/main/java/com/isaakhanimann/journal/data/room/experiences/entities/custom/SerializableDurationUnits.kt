package com.isaakhanimann.journal.data.room.experiences.entities.custom

import com.isaakhanimann.journal.data.substances.classes.roa.DurationUnits
import kotlinx.serialization.Serializable

@Serializable
enum class SerializableDurationUnits(val text: String) {
    SECONDS("seconds"),
    MINUTES("minutes"),
    HOURS("hours"),
    DAYS("days")
}

fun SerializableDurationUnits.toDurationUnits() = when (this) {
    SerializableDurationUnits.SECONDS -> DurationUnits.SECONDS
    SerializableDurationUnits.MINUTES -> DurationUnits.MINUTES
    SerializableDurationUnits.HOURS -> DurationUnits.HOURS
    SerializableDurationUnits.DAYS -> DurationUnits.DAYS
}