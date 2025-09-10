package com.isaakhanimann.journal.data.room.experiences.entities.custom

import com.isaakhanimann.journal.data.substances.AdministrationRoute
import com.isaakhanimann.journal.data.substances.classes.roa.RoaDose
import com.isaakhanimann.journal.data.substances.classes.roa.RoaDuration
import kotlinx.serialization.Serializable

@Serializable
data class CustomRoaInfo(
    val administrationRoute: AdministrationRoute,
    val doseInfo: CustomDoseInfo? = null,
    val durationInfo: CustomDurationInfo? = null
)

fun CustomRoaInfo.toRoaDose() = doseInfo?.units?.let {
    RoaDose(
        lightMin = doseInfo.lightMin,
        commonMin = doseInfo.commonMin,
        strongMin = doseInfo.strongMin,
        heavyMin = doseInfo.heavyMin,
        units = it
    )
}

fun CustomRoaInfo.toRoaDuration(): RoaDuration? {
    return durationInfo?.toRoaDuration()
}