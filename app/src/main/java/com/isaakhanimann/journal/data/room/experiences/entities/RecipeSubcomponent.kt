package com.isaakhanimann.journal.data.room.experiences.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.isaakhanimann.journal.data.room.experiences.ExperienceRepository
import java.time.Instant

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = CustomRecipe::class,
            parentColumns = ["id"],
            childColumns = ["recipeId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = CustomUnit::class,
            parentColumns = ["id"],
            childColumns = ["customUnitId"],
            onDelete = ForeignKey.SET_NULL
        )
    ]
)
data class RecipeSubcomponent(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val recipeId: Int,
    val substanceName: String?,
    var customUnitId: Int? = null,
    val customUnitDose: Double? = 0.0,
    var dose: Double?,
    var estimatedDoseStandardDeviation: Double?,
    var isEstimate: Boolean,
    var unit: String,
    var originalUnit: String,
    val creationDate: Instant = Instant.now()
) {
    fun getDoseDescription(customUnit: CustomUnit? = null): String {
        return customUnit?.getDoseOfOneUnitDescription() ?: run {
            dose?.let { d ->
                if (isEstimate) {
                    estimatedDoseStandardDeviation?.let { sd ->
                        "${d.toReadableString()}±${sd.toReadableString()} $originalUnit"
                    } ?: "~${d.toReadableString()} $originalUnit"
                } else {
                    "${d.toReadableString()} $originalUnit"
                }
            } ?: "Unknown dose"
        }
    }

    companion object {
        var sampleSubcomponent = RecipeSubcomponent(
            recipeId = 1,
            substanceName = "MDMA",
            dose = 80.0,
            estimatedDoseStandardDeviation = null,
            isEstimate = false,
            unit = "mg",
            originalUnit = "mg",
        )
    }
}

suspend fun RecipeSubcomponent.getActualDose(experienceRepository: ExperienceRepository): Double? {
    return if (customUnitId != null && customUnitDose != null) {
        val customUnit = experienceRepository.getCustomUnit(customUnitId ?: 0)
        customUnit?.dose?.let { unitDose ->
            customUnitDose * unitDose
        }
    } else {
        dose
    }
}

fun Double.toReadableString(): String {
    return if (this == this.toLong().toDouble()) {
        this.toLong().toString()
    } else {
        this.toString()
    }
}
