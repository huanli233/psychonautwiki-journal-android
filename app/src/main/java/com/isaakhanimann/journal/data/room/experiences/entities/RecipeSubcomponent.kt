package com.isaakhanimann.journal.data.room.experiences.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = CustomRecipe::class,
            parentColumns = ["id"],
            childColumns = ["recipeId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class RecipeSubcomponent(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val recipeId: Int,
    val substanceName: String,
    var dose: Double?,
    var estimatedDoseStandardDeviation: Double?,
    var isEstimate: Boolean,
    var originalUnit: String,
    val creationDate: Instant = Instant.now()
) {
    fun getDoseDescription(): String {
        return this.dose?.let { unwrappedDose ->
            if (this.isEstimate) {
                this.estimatedDoseStandardDeviation?.let { estimatedDoseStandardDeviationUnwrapped ->
                    "${unwrappedDose.toReadableString()}±${estimatedDoseStandardDeviationUnwrapped.toReadableString()} ${this.originalUnit}"
                } ?: "~${unwrappedDose.toReadableString()} ${this.originalUnit}"
            } else {
                "${unwrappedDose.toReadableString()} ${this.originalUnit}"
            }
        } ?: "Unknown dose"
    }

    companion object {
        var sampleSubcomponent = RecipeSubcomponent(
            recipeId = 1,
            substanceName = "MDMA",
            dose = 80.0,
            estimatedDoseStandardDeviation = null,
            isEstimate = false,
            originalUnit = "mg"
        )
    }
}

fun Double.toReadableString(): String {
    return if (this == this.toLong().toDouble()) {
        this.toLong().toString()
    } else {
        this.toString()
    }
}
