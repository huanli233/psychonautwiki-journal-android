package com.isaakhanimann.journal.data.room.experiences.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.isaakhanimann.journal.data.room.experiences.entities.custom.CustomRoaInfo
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element

@Entity
@Serializable
data class CustomSubstance(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    @SerialName("unit")
    var units: String,
    var description: String = "",
    @ColumnInfo(defaultValue = "[]")
    val roaInfos: List<CustomRoaInfo> = emptyList()
)