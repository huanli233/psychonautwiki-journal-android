package com.isaakhanimann.journal.data.room.experiences.relations

import androidx.room.Embedded
import androidx.room.Relation
import com.isaakhanimann.journal.data.room.experiences.entities.CustomRecipe
import com.isaakhanimann.journal.data.room.experiences.entities.Ingestion

data class IngestionWithRecipe(
    @Embedded val ingestion: Ingestion,
    @Relation(
        parentColumn = "customRecipeId",
        entityColumn = "id"
    )
    val customRecipe: CustomRecipe?
)
