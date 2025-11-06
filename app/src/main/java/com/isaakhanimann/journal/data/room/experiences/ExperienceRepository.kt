package com.isaakhanimann.journal.data.room.experiences

import com.isaakhanimann.journal.data.room.experiences.entities.CustomSubstance
import com.isaakhanimann.journal.data.room.experiences.entities.CustomUnit
import com.isaakhanimann.journal.data.room.experiences.entities.Experience
import com.isaakhanimann.journal.data.room.experiences.entities.Ingestion
import com.isaakhanimann.journal.data.room.experiences.entities.ShulginRating
import com.isaakhanimann.journal.data.room.experiences.entities.SubstanceCompanion
import com.isaakhanimann.journal.data.room.experiences.entities.TimedNote
import com.isaakhanimann.journal.data.room.experiences.entities.CustomRecipe
import com.isaakhanimann.journal.data.room.experiences.entities.RecipeSubcomponent
import com.isaakhanimann.journal.data.room.experiences.entities.IngestionReminder
import com.isaakhanimann.journal.data.room.experiences.entities.TimedNotePhoto
import com.isaakhanimann.journal.data.room.experiences.relations.CustomUnitWithIngestions
import com.isaakhanimann.journal.data.room.experiences.relations.ExperienceWithIngestions
import com.isaakhanimann.journal.data.room.experiences.relations.ExperienceWithIngestionsAndCompanions
import com.isaakhanimann.journal.data.room.experiences.relations.ExperienceWithIngestionsCompanionsAndRatings
import com.isaakhanimann.journal.data.room.experiences.relations.ExperienceWithIngestionsTimedNotesAndRatings
import com.isaakhanimann.journal.data.room.experiences.relations.IngestionWithCompanion
import com.isaakhanimann.journal.data.room.experiences.relations.IngestionWithExperienceAndCustomUnit
import com.isaakhanimann.journal.data.room.experiences.relations.CustomRecipeWithSubcomponents
import com.isaakhanimann.journal.ui.tabs.settings.JournalExport
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExperienceRepository @Inject constructor(private val experienceDao: ExperienceDao) {
    suspend fun insert(rating: ShulginRating) = experienceDao.insert(rating)
    suspend fun insert(customUnit: CustomUnit) = experienceDao.insert(customUnit).toInt()
    suspend fun insert(timedNote: TimedNote) = experienceDao.insert(timedNote)
    suspend fun update(experience: Experience) = experienceDao.update(experience)
    suspend fun update(ingestion: Ingestion) = experienceDao.update(ingestion)
    suspend fun update(rating: ShulginRating) = experienceDao.update(rating)
    suspend fun update(customUnit: CustomUnit) = experienceDao.update(customUnit)
    suspend fun update(timedNote: TimedNote) = experienceDao.update(timedNote)

    suspend fun migrateBenzydamine() = experienceDao.migrateBenzydamine()
    suspend fun migrateCannabisAndMushroomUnits() = experienceDao.migrateCannabisAndMushroomUnits()
    suspend fun insertIngestionExperienceAndCompanion(
        ingestion: Ingestion,
        experience: Experience,
        substanceCompanion: SubstanceCompanion
    ) = experienceDao.insertIngestionExperienceAndCompanion(
        ingestion,
        experience,
        substanceCompanion
    )

    suspend fun insertEverything(
        journalExport: JournalExport
    ) = experienceDao.insertEverything(journalExport)
    
    suspend fun insertEverythingWithProgress(
        journalExport: JournalExport,
        imageExportHelper: com.isaakhanimann.journal.ui.tabs.settings.ImageExportHelper,
        onProgressUpdate: (Float, String) -> Unit
    ) = experienceDao.insertEverythingWithProgress(journalExport, imageExportHelper, onProgressUpdate)

    suspend fun insertIngestionAndCompanion(
        ingestion: Ingestion,
        substanceCompanion: SubstanceCompanion
    ) = experienceDao.insertIngestionAndCompanion(
        ingestion,
        substanceCompanion
    )

    suspend fun deleteEverything() = experienceDao.deleteEverything()

    suspend fun delete(ingestion: Ingestion) = experienceDao.delete(ingestion)
    suspend fun delete(customUnit: CustomUnit) = experienceDao.delete(customUnit)

    suspend fun deleteIngestionsByRecipeGroupId(recipeGroupId: String) {
        experienceDao.deleteIngestionsByRecipeGroupId(recipeGroupId)
    }

    suspend fun deleteEverythingOfExperience(experienceId: Int) =
        experienceDao.deleteEverythingOfExperience(experienceId)

    suspend fun delete(experience: Experience) =
        experienceDao.delete(experience)

    suspend fun delete(rating: ShulginRating) =
        experienceDao.delete(rating)

    suspend fun delete(timedNote: TimedNote) =
        experienceDao.delete(timedNote)

    suspend fun delete(experienceWithIngestions: ExperienceWithIngestions) =
        experienceDao.deleteExperienceWithIngestions(experienceWithIngestions)

    suspend fun deleteUnusedSubstanceCompanions() =
        experienceDao.deleteUnusedSubstanceCompanions()

    suspend fun getSortedExperiencesWithIngestionsWithSortDateBetween(
        fromInstant: Instant,
        toInstant: Instant
    ): List<ExperienceWithIngestions> =
        experienceDao.getSortedExperiencesWithIngestionsWithSortDateBetween(fromInstant, toInstant)

    fun getSortedExperienceWithIngestionsCompanionsAndRatingsFlow(): Flow<List<ExperienceWithIngestionsCompanionsAndRatings>> =
        experienceDao.getSortedExperienceWithIngestionsCompanionsAndRatingsFlow()
            .flowOn(Dispatchers.IO)
            .conflate()

    fun getSortedExperiencesWithIngestionsFlow(): Flow<List<ExperienceWithIngestions>> =
        experienceDao.getSortedExperiencesWithIngestionsFlow()
            .flowOn(Dispatchers.IO)
            .conflate()

    fun getSortedExperiencesWithIngestionsAndCustomUnitsFlow(): Flow<List<ExperienceWithIngestionsAndCompanions>> =
        experienceDao.getSortedExperiencesWithIngestionsAndCustomUnitsFlow()
            .flowOn(Dispatchers.IO)
            .conflate()

    fun getCustomSubstancesFlow(): Flow<List<CustomSubstance>> =
        experienceDao.getCustomSubstancesFlow()
            .flowOn(Dispatchers.IO)
            .conflate()

    fun getCustomSubstanceFlow(id: Int): Flow<CustomSubstance?> =
        experienceDao.getCustomSubstanceFlow(id)
            .flowOn(Dispatchers.IO)
            .conflate()

    suspend fun getCustomSubstance(name: String): CustomSubstance? =
        experienceDao.getCustomSubstance(name)

    fun getIngestionsWithExperiencesFlow(
        fromInstant: Instant,
        toInstant: Instant
    ): Flow<List<IngestionWithExperienceAndCustomUnit>> =
        experienceDao.getIngestionWithExperiencesFlow(fromInstant, toInstant)
            .flowOn(Dispatchers.IO)
            .conflate()

    suspend fun getIngestionsWithCompanions(
        fromInstant: Instant,
        toInstant: Instant
    ): List<IngestionWithCompanion> =
        experienceDao.getIngestionsWithCompanions(fromInstant, toInstant)

    fun getSortedLastUsedSubstanceNamesFlow(limit: Int): Flow<List<String>> =
        experienceDao.getSortedLastUsedSubstanceNamesFlow(limit).flowOn(Dispatchers.IO).conflate()

    suspend fun getExperience(id: Int): Experience? = experienceDao.getExperience(id)
    suspend fun getExperienceWithIngestionsCompanionsAndRatings(id: Int): ExperienceWithIngestionsCompanionsAndRatings? =
        experienceDao.getExperienceWithIngestionsCompanionsAndRatings(id)

    suspend fun getIngestionsWithCompanions(experienceId: Int) =
        experienceDao.getIngestionsWithCompanions(experienceId)

    suspend fun getRating(id: Int): ShulginRating? = experienceDao.getRating(id)
    suspend fun getTimedNote(id: Int): TimedNote? = experienceDao.getTimedNote(id)
    suspend fun getCustomUnit(id: Int): CustomUnit? = experienceDao.getCustomUnit(id)
    suspend fun getCustomUnitWithIngestions(id: Int): CustomUnitWithIngestions? = experienceDao.getCustomUnitWithIngestions(id)
    fun getIngestionFlow(id: Int) = experienceDao.getIngestionFlow(id)
        .flowOn(Dispatchers.IO)
        .conflate()

    fun getIngestionsWithCompanionsFlow(experienceId: Int) =
        experienceDao.getIngestionsWithCompanionsFlow(experienceId)
            .flowOn(Dispatchers.IO)
            .conflate()

    fun getRatingsFlow(experienceId: Int) =
        experienceDao.getRatingsFlow(experienceId)
            .flowOn(Dispatchers.IO)
            .conflate()

    fun getTimedNotesFlowSorted(experienceId: Int) =
        experienceDao.getTimedNotesFlowSorted(experienceId)
            .flowOn(Dispatchers.IO)
            .conflate()
    
    fun getTimedNotesWithPhotosFlow(experienceId: Int) =
        experienceDao.getTimedNotesWithPhotosFlow(experienceId)
            .flowOn(Dispatchers.IO)
            .conflate()

    fun getExperienceFlow(experienceId: Int) =
        experienceDao.getExperienceFlow(experienceId)
            .flowOn(Dispatchers.IO)
            .conflate()

    suspend fun getLatestIngestionOfEverySubstanceSinceDate(instant: Instant): List<Ingestion> =
        experienceDao.getLatestIngestionOfEverySubstanceSinceDate(instant)

    suspend fun getAllExperiencesWithIngestionsTimedNotesAndRatingsSorted(): List<ExperienceWithIngestionsTimedNotesAndRatings> =
        experienceDao.getAllExperiencesWithIngestionsTimedNotesAndRatingsSorted()

    suspend fun getAllCustomUnitsSorted(): List<CustomUnit> =
        experienceDao.getAllCustomUnitsSorted()

    suspend fun getAllCustomSubstances(): List<CustomSubstance> =
        experienceDao.getAllCustomSubstances()

    suspend fun getAllSubstanceCompanions(): List<SubstanceCompanion> =
        experienceDao.getAllSubstanceCompanions()

    suspend fun getTimedNotes(experienceId: Int): List<TimedNote> =
        experienceDao.getTimedNotes(experienceId)

    suspend fun delete(substanceCompanion: SubstanceCompanion) =
        experienceDao.delete(substanceCompanion)

    suspend fun update(substanceCompanion: SubstanceCompanion) =
        experienceDao.update(substanceCompanion)

    suspend fun insert(customSubstance: CustomSubstance): Int =
        experienceDao.insert(customSubstance).toInt()

    suspend fun importCustomSubstances(customSubstances: List<CustomSubstance>) =
        experienceDao.importCustomSubstances(customSubstances)

    suspend fun delete(customSubstance: CustomSubstance) {
        experienceDao.deleteCustomSubstanceAndRelatedIngestions(customSubstance)
    }

    suspend fun update(customSubstance: CustomSubstance) {
        withContext(Dispatchers.IO) {
            val originalSubstance = experienceDao.getCustomSubstanceById(customSubstance.id)

            if (originalSubstance != null && originalSubstance.name != customSubstance.name) {
                experienceDao.updateCustomSubstanceAndRelatedIngestions(
                    originalName = originalSubstance.name,
                    updatedSubstance = customSubstance
                )
            } else {
                experienceDao.update(customSubstance)
            }
        }
    }

    fun getSortedIngestionsWithSubstanceCompanionsFlow(limit: Int) =
        experienceDao.getSortedIngestionsWithSubstanceCompanionsFlow(limit)
            .flowOn(Dispatchers.IO)
            .conflate()

    fun getSortedIngestions(limit: Int) =
        experienceDao.getSortedIngestions(limit)
            .flowOn(Dispatchers.IO)
            .conflate()

    fun getSortedIngestionsFlow(substanceName: String, limit: Int) =
        experienceDao.getSortedIngestionsFlow(substanceName, limit)
            .flowOn(Dispatchers.IO)
            .conflate()

    fun getSortedIngestionsWithExperienceAndCustomUnitFlow(substanceName: String) =
        experienceDao.getSortedIngestionsWithExperienceAndCustomUnitFlow(substanceName)
            .flowOn(Dispatchers.IO)
            .conflate()

    fun getAllSubstanceCompanionsFlow() = experienceDao.getAllSubstanceCompanionsFlow()
        .flowOn(Dispatchers.IO)
        .conflate()

    fun getCustomUnitsFlow(isArchived: Boolean) = experienceDao.getSortedCustomUnitsFlow(isArchived)
        .flowOn(Dispatchers.IO)
        .conflate()

    fun getUnArchivedCustomUnitsFlow(substanceName: String) =
        experienceDao.getSortedCustomUnitsFlowBasedOnName(substanceName, false)
            .flowOn(Dispatchers.IO)
            .conflate()

    fun getAllCustomUnitsFlow() = experienceDao.getAllCustomUnitsFlow()
        .flowOn(Dispatchers.IO)
        .conflate()

    fun getSubstanceCompanionFlow(substanceName: String) =
        experienceDao.getSubstanceCompanionFlow(substanceName)

    suspend fun insert(customRecipe: CustomRecipe) = experienceDao.insert(customRecipe)
    suspend fun insert(recipeSubcomponent: RecipeSubcomponent) = experienceDao.insert(recipeSubcomponent)
    suspend fun update(customRecipe: CustomRecipe) = experienceDao.update(customRecipe)
    suspend fun update(recipeSubcomponent: RecipeSubcomponent) = experienceDao.update(recipeSubcomponent)
    suspend fun delete(customRecipe: CustomRecipe) = experienceDao.delete(customRecipe)
    suspend fun delete(recipeSubcomponent: RecipeSubcomponent) = experienceDao.delete(recipeSubcomponent)
    suspend fun deleteCustomRecipeWithSubcomponents(customRecipeWithSubcomponents: CustomRecipeWithSubcomponents) = 
        experienceDao.deleteCustomRecipeWithSubcomponents(customRecipeWithSubcomponents)

    suspend fun getCustomRecipe(id: Int) = experienceDao.getCustomRecipe(id)
    suspend fun getCustomRecipeWithSubcomponents(id: Int) = experienceDao.getCustomRecipeWithSubcomponents(id)
    suspend fun getRecipeSubcomponents(recipeId: Int) = experienceDao.getRecipeSubcomponents(recipeId)
    suspend fun getAllCustomRecipesWithSubcomponentsSorted(): List<CustomRecipeWithSubcomponents> =
        experienceDao.getAllCustomRecipesWithSubcomponentsSorted()

    fun getSortedCustomRecipesFlow(isArchived: Boolean) = 
        experienceDao.getSortedCustomRecipesFlow(isArchived)
            .flowOn(Dispatchers.IO)
            .conflate()

    fun getSortedCustomRecipesWithSubcomponentsFlow(isArchived: Boolean) = 
        experienceDao.getSortedCustomRecipesWithSubcomponentsFlow(isArchived)
            .flowOn(Dispatchers.IO)
            .conflate()

    fun getAllCustomRecipesFlow() = experienceDao.getAllCustomRecipesFlow()
        .flowOn(Dispatchers.IO)
    suspend fun insert(substanceCompanion: SubstanceCompanion) = experienceDao.insert(substanceCompanion)

    // TimedNotePhoto methods
    suspend fun insert(timedNotePhoto: TimedNotePhoto): Long = experienceDao.insert(timedNotePhoto)
    fun getPhotosForTimedNoteFlow(timedNoteId: Int) = experienceDao.getPhotosForTimedNoteFlow(timedNoteId)
    suspend fun delete(timedNotePhoto: TimedNotePhoto) = experienceDao.delete(timedNotePhoto)

    // IngestionReminder methods
    suspend fun insert(reminder: IngestionReminder): Long = experienceDao.insert(reminder)
    
    suspend fun getReminder(id: Int): IngestionReminder? = experienceDao.getReminderById(id)
    suspend fun update(reminder: IngestionReminder) = experienceDao.update(reminder)
    suspend fun delete(reminder: IngestionReminder) = experienceDao.delete(reminder)
    fun getAllRemindersFlow() = experienceDao.getAllRemindersFlow()
        .flowOn(Dispatchers.IO)
        .conflate()
    fun getEnabledRemindersFlow() = experienceDao.getEnabledRemindersFlow()
        .flowOn(Dispatchers.IO)
        .conflate()
    suspend fun getReminderById(id: Int) = experienceDao.getReminderById(id)
    suspend fun getAllReminders() = experienceDao.getAllReminders()
    
    suspend fun getTimedNotesWithPhotos(experienceId: Int) = 
        experienceDao.getTimedNotesWithPhotos(experienceId)
}