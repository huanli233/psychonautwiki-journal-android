package com.isaakhanimann.journal.data.room.experiences.relations

import androidx.room.Embedded
import androidx.room.Relation
import com.isaakhanimann.journal.data.room.experiences.entities.CustomRecipe
import com.isaakhanimann.journal.data.room.experiences.entities.RecipeSubcomponent

data class CustomRecipeWithSubcomponents(
    @Embedded val recipe: CustomRecipe,
    @Relation(
        parentColumn = "id",
        entityColumn = "recipeId"
    )
    val subcomponents: List<RecipeSubcomponent>
) {
    fun getSubcomponentsSummary(): String {
        return subcomponents.joinToString(", ") { "${it.substanceName} ${it.getDoseDescription()}" }
    }
}
