package com.isaakhanimann.journal.data.room.experiences.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.isaakhanimann.journal.data.substances.AdministrationRoute
import java.time.Instant

@Entity
data class CustomRecipe(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    var name: String,
    val creationDate: Instant = Instant.now(),
    val administrationRoute: AdministrationRoute,
    var isArchived: Boolean,
    var unit: String,
    var unitPlural: String? = null,
    var note: String
) {
    fun getPluralizableUnit(): PluralizableUnit {
        val plural = unitPlural
        if (plural == null) {
            val calculatedPlural =
                if (unit != "mg" && unit != "g" && unit.lowercase() != "ml" && unit.lastOrNull() != 's') {
                    unit + "s"
                } else {
                    unit
                }
            return PluralizableUnit(singular = unit, plural = calculatedPlural)
        } else {
            return PluralizableUnit(singular = unit, plural = plural)
        }
    }

    companion object {
        var sampleRecipe = CustomRecipe(
            name = "Mixed Capsule",
            administrationRoute = AdministrationRoute.ORAL,
            isArchived = false,
            unit = "capsule",
            unitPlural = "capsules",
            note = "Custom mixed substance capsule"
        )
    }
}
