package com.isaakhanimann.journal.data.room.experiences.relations

import androidx.room.Embedded
import androidx.room.Relation
import com.isaakhanimann.journal.data.room.experiences.entities.CustomRecipe
import com.isaakhanimann.journal.data.room.experiences.entities.CustomUnit
import com.isaakhanimann.journal.data.room.experiences.entities.RecipeSubcomponent

data class CustomRecipeWithSubcomponents(
    @Embedded val recipe: CustomRecipe,
    @Relation(
        parentColumn = "id",
        entityColumn = "recipeId"
    )
    val subcomponents: List<RecipeSubcomponent>
) {
    fun getSubcomponentsSummary(customUnitsMap: Map<Int, CustomUnit> = emptyMap()): String {
        return subcomponents.joinToString(", ") { subcomponent ->
            val customUnit = subcomponent.customUnitId?.let { customUnitsMap[it] }
            if (customUnit != null) {
                val customUnitDoseStr = subcomponent.customUnitDose?.let {
                    if (it == it.toLong().toDouble()) it.toLong().toString()
                    else it.toString()
                } ?: "?"
                "$customUnitDoseStr ${customUnit.name} (${customUnit.substanceName})"
            } else {
                val substanceName = subcomponent.substanceName ?: "Unknown"
                val doseStr = subcomponent.dose?.let {
                    if (it == it.toLong().toDouble()) it.toLong().toString()
                    else it.toString()
                } ?: "?"
                "$doseStr${subcomponent.originalUnit} $substanceName"
            }
        }
    }
}