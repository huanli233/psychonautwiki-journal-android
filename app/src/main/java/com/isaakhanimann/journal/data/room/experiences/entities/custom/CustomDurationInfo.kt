package com.isaakhanimann.journal.data.room.experiences.entities.custom

import com.isaakhanimann.journal.data.substances.classes.roa.RoaDuration
import kotlinx.serialization.Serializable

@Serializable
data class CustomDurationInfo(
    val onset: SerializableDurationRange? = null,
    val comeup: SerializableDurationRange? = null,
    val peak: SerializableDurationRange? = null,
    val offset: SerializableDurationRange? = null,
    val total: SerializableDurationRange? = null,
    val afterglow: SerializableDurationRange? = null
)

fun CustomDurationInfo.toRoaDuration() = RoaDuration(
    onset?.toDurationRange(),
    comeup?.toDurationRange(),
    peak?.toDurationRange(),
    offset?.toDurationRange(),
    total?.toDurationRange(),
    afterglow?.toDurationRange()
)