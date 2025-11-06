package com.isaakhanimann.journal.data.room.experiences

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.isaakhanimann.journal.data.room.experiences.entities.CustomSubstance
import com.isaakhanimann.journal.data.room.experiences.entities.CustomUnit
import com.isaakhanimann.journal.data.room.experiences.entities.Experience
import com.isaakhanimann.journal.data.room.experiences.entities.Ingestion
import com.isaakhanimann.journal.data.room.experiences.entities.Location
import com.isaakhanimann.journal.data.room.experiences.entities.ShulginRating
import com.isaakhanimann.journal.data.room.experiences.entities.SubstanceCompanion
import com.isaakhanimann.journal.data.room.experiences.entities.TimedNote
import com.isaakhanimann.journal.data.room.experiences.entities.CustomRecipe
import com.isaakhanimann.journal.data.room.experiences.entities.IngestionReminder
import com.isaakhanimann.journal.data.room.experiences.entities.RecipeSubcomponent
import com.isaakhanimann.journal.data.room.experiences.entities.TimedNotePhoto
import com.isaakhanimann.journal.data.room.experiences.relations.CustomUnitWithIngestions
import com.isaakhanimann.journal.data.room.experiences.relations.ExperienceWithIngestions
import com.isaakhanimann.journal.data.room.experiences.relations.ExperienceWithIngestionsAndCompanions
import com.isaakhanimann.journal.data.room.experiences.relations.ExperienceWithIngestionsCompanionsAndRatings
import com.isaakhanimann.journal.data.room.experiences.relations.ExperienceWithIngestionsTimedNotesAndRatings
import com.isaakhanimann.journal.data.room.experiences.relations.IngestionWithCompanion
import com.isaakhanimann.journal.data.room.experiences.relations.IngestionWithCompanionAndCustomUnit
import com.isaakhanimann.journal.data.room.experiences.relations.IngestionWithExperienceAndCustomUnit
import com.isaakhanimann.journal.data.room.experiences.relations.CustomRecipeWithSubcomponents
import com.isaakhanimann.journal.data.substances.AdministrationRoute
import com.isaakhanimann.journal.ui.tabs.settings.JournalExport
import kotlinx.coroutines.flow.Flow
import java.time.Instant

@Dao
interface ExperienceDao {

    @Query("SELECT * FROM experience ORDER BY creationDate DESC")
    fun getSortedExperiencesFlow(): Flow<List<Experience>>

    @Query("SELECT * FROM ingestion ORDER BY time DESC")
    fun getIngestionsSortedDescendingFlow(): Flow<List<Ingestion>>

    @Query(
        "SELECT * FROM ingestion as i" +
                " INNER JOIN (SELECT id, MAX(time) AS time FROM ingestion WHERE time > :instant GROUP BY substanceName) as sub" +
                " ON i.id = sub.id AND i.time = sub.time" +
                " ORDER BY time DESC"
    )
    suspend fun getLatestIngestionOfEverySubstanceSinceDate(instant: Instant): List<Ingestion>

    @Transaction
    @Query("SELECT * FROM experience ORDER BY sortDate")
    suspend fun getAllExperiencesWithIngestionsTimedNotesAndRatingsSorted(): List<ExperienceWithIngestionsTimedNotesAndRatings>

    @Query("SELECT * FROM customunit ORDER BY creationDate")
    suspend fun getAllCustomUnitsSorted(): List<CustomUnit>

    @Query("SELECT * FROM customsubstance")
    suspend fun getAllCustomSubstances(): List<CustomSubstance>

    @Query("SELECT * FROM substancecompanion")
    suspend fun getAllSubstanceCompanions(): List<SubstanceCompanion>

    @Transaction
    @Query("SELECT * FROM experience WHERE sortDate > :fromInstant AND sortDate < :toInstant ORDER BY sortDate DESC")
    suspend fun getSortedExperiencesWithIngestionsWithSortDateBetween(fromInstant: Instant, toInstant: Instant): List<ExperienceWithIngestions>

    @Query("SELECT substanceName FROM ingestion ORDER BY time DESC LIMIT :limit")
    fun getSortedLastUsedSubstanceNamesFlow(limit: Int): Flow<List<String>>

    @Transaction
    @Query("SELECT * FROM experience ORDER BY sortDate DESC")
    fun getSortedExperiencesWithIngestionsAndCompanionsFlow(): Flow<List<ExperienceWithIngestionsAndCompanions>>

    @Transaction
    @Query("SELECT * FROM experience ORDER BY sortDate DESC")
    fun getSortedExperienceWithIngestionsCompanionsAndRatingsFlow(): Flow<List<ExperienceWithIngestionsCompanionsAndRatings>>

    @Transaction
    @Query("SELECT * FROM experience ORDER BY sortDate DESC LIMIT :limit")
    fun getSortedExperiencesWithIngestionsAndCompanionsFlow(limit: Int): Flow<List<ExperienceWithIngestionsAndCompanions>>

    @Transaction
    @Query("SELECT * FROM experience ORDER BY sortDate DESC")
    fun getSortedExperiencesWithIngestionsFlow(): Flow<List<ExperienceWithIngestions>>

    @Transaction
    @Query("SELECT * FROM experience ORDER BY sortDate DESC")
    fun getSortedExperiencesWithIngestionsAndCustomUnitsFlow(): Flow<List<ExperienceWithIngestionsAndCompanions>>

    @Transaction
    @Query("SELECT * FROM ingestion ORDER BY time DESC")
    fun getSortedIngestionsWithSubstanceCompanionsFlow(): Flow<List<IngestionWithCompanionAndCustomUnit>>

    @Transaction
    @Query("SELECT * FROM ingestion ORDER BY creationDate DESC LIMIT :limit")
    fun getSortedIngestionsWithSubstanceCompanionsFlow(limit: Int): Flow<List<IngestionWithCompanionAndCustomUnit>>

    @Query("SELECT * FROM ingestion ORDER BY time DESC LIMIT :limit")
    fun getSortedIngestions(limit: Int): Flow<List<Ingestion>>

    @Query("SELECT * FROM ingestion ORDER BY time DESC")
    fun getSortedIngestionsFlow(): Flow<List<Ingestion>>

    @Query("SELECT * FROM ingestion WHERE substanceName = :substanceName ORDER BY time DESC LIMIT :limit")
    fun getSortedIngestionsFlow(substanceName: String, limit: Int): Flow<List<Ingestion>>

    @Query("SELECT * FROM customsubstance ORDER BY name ASC")
    fun getCustomSubstancesFlow(): Flow<List<CustomSubstance>>

    @Query("SELECT * FROM customsubstance WHERE id = :id")
    fun getCustomSubstanceFlow(id: Int): Flow<CustomSubstance?>

    @Query("SELECT * FROM CustomSubstance WHERE id = :id")
    suspend fun getCustomSubstanceById(id: Int): CustomSubstance?

    @Query("UPDATE Ingestion SET substanceName = :newName WHERE substanceName = :oldName")
    suspend fun updateIngestionSubstanceName(oldName: String, newName: String)

    @Query("DELETE FROM Ingestion WHERE substanceName = :substanceName")
    suspend fun deleteIngestionsByName(substanceName: String)

    @Query("UPDATE SubstanceCompanion SET substanceName = :newName WHERE substanceName = :oldName")
    suspend fun updateSubstanceCompanionName(oldName: String, newName: String)

    @Transaction
    suspend fun updateCustomSubstanceAndRelatedIngestions(originalName: String, updatedSubstance: CustomSubstance) {
        updateIngestionSubstanceName(originalName, updatedSubstance.name)
        updateSubstanceCompanionName(originalName, updatedSubstance.name)
        update(updatedSubstance)
    }

    @Transaction
    suspend fun deleteCustomSubstanceAndRelatedIngestions(customSubstance: CustomSubstance) {
        deleteIngestionsByName(customSubstance.name)
        delete(customSubstance)
    }

    @Query("SELECT * FROM customsubstance WHERE name = :name")
    suspend fun getCustomSubstance(name: String): CustomSubstance?

    @Query("SELECT * FROM ingestion WHERE substanceName = :substanceName ORDER BY time DESC")
    fun getSortedIngestionsFlow(substanceName: String): Flow<List<Ingestion>>

    @Transaction
    @Query("SELECT * FROM ingestion WHERE substanceName = :substanceName ORDER BY time DESC")
    fun getSortedIngestionsWithExperienceAndCustomUnitFlow(substanceName: String): Flow<List<IngestionWithExperienceAndCustomUnit>>

    @Query("SELECT * FROM experience WHERE id =:id")
    suspend fun getExperience(id: Int): Experience?

    @Transaction
    @Query("SELECT * FROM experience WHERE id =:id")
    suspend fun getExperienceWithIngestionsCompanionsAndRatings(id: Int): ExperienceWithIngestionsCompanionsAndRatings?

    @Transaction
    @Query("SELECT * FROM ingestion WHERE experienceId =:experienceId")
    suspend fun getIngestionsWithCompanions(experienceId: Int): List<IngestionWithCompanionAndCustomUnit>

    @Query("SELECT * FROM shulginrating WHERE id =:id")
    suspend fun getRating(id: Int): ShulginRating?

    @Query("SELECT * FROM timednote WHERE id =:id")
    suspend fun getTimedNote(id: Int): TimedNote?

    @Query("SELECT * FROM customunit WHERE id =:id")
    suspend fun getCustomUnit(id: Int): CustomUnit?

    @Transaction
    @Query("SELECT * FROM customunit WHERE id =:id")
    suspend fun getCustomUnitWithIngestions(id: Int): CustomUnitWithIngestions?

    @Query("SELECT * FROM experience WHERE id =:id")
    fun getExperienceFlow(id: Int): Flow<Experience?>

    @Transaction
    @Query("SELECT * FROM ingestion WHERE id =:id")
    fun getIngestionWithExperienceFlow(id: Int): Flow<IngestionWithExperienceAndCustomUnit?>

    @Transaction
    @Query("SELECT * FROM ingestion WHERE time > :fromInstant AND time < :toInstant")
    fun getIngestionWithExperiencesFlow(
        fromInstant: Instant,
        toInstant: Instant
    ): Flow<List<IngestionWithExperienceAndCustomUnit>>

    @Transaction
    @Query("SELECT * FROM ingestion WHERE time > :fromInstant AND time < :toInstant")
    suspend fun getIngestionsWithCompanions(
        fromInstant: Instant,
        toInstant: Instant
    ): List<IngestionWithCompanion>

    @Transaction
    @Query("SELECT * FROM ingestion WHERE id =:id")
    fun getIngestionFlow(id: Int): Flow<IngestionWithCompanionAndCustomUnit?>

    @Transaction
    @Query("SELECT * FROM ingestion WHERE id =:id")
    fun getIngestionWithCompanionFlow(id: Int): Flow<IngestionWithCompanionAndCustomUnit?>

    @Transaction
    @Query("UPDATE ingestion SET units = 'mg', dose = dose * 1000 WHERE substanceName = 'Benzydamine' AND units = 'g'")
    suspend fun migrateBenzydamine()


    @Transaction
    suspend fun migrateCannabisAndMushroomUnits() {
        migrateCannabisIngestionUnits()
        migrateMushroomsIngestionUnits()
        migrateCannabisCustomUnits()
        migrateMushroomsCustomUnits()
    }

    @Query("UPDATE ingestion SET units = 'mg THC' WHERE substanceName = 'Cannabis' AND units = 'mg'")
    suspend fun migrateCannabisIngestionUnits()

    @Query("UPDATE ingestion SET units = 'mg Psilocybin' WHERE substanceName = 'Psilocybin mushrooms' AND units = 'mg'")
    suspend fun migrateMushroomsIngestionUnits()

    @Query("UPDATE customunit SET originalUnit = 'mg THC' WHERE substanceName = 'Cannabis' AND originalUnit = 'mg'")
    suspend fun migrateCannabisCustomUnits()

    @Query("UPDATE customunit SET originalUnit = 'mg Psilocybin' WHERE substanceName = 'Psilocybin mushrooms' AND originalUnit = 'mg'")
    suspend fun migrateMushroomsCustomUnits()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(experience: Experience): Long

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(experience: Experience)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(ingestion: Ingestion)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(rating: ShulginRating)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(customUnit: CustomUnit)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(timedNote: TimedNote)

    @Delete
    suspend fun delete(experience: Experience)

    @Delete
    suspend fun delete(rating: ShulginRating)

    @Delete
    suspend fun delete(timedNote: TimedNote)

    @Transaction
    suspend fun deleteExperienceWithIngestions(experienceWithIngestions: ExperienceWithIngestions) {
        delete(experience = experienceWithIngestions.experience)
        experienceWithIngestions.ingestions.forEach {
            delete(it)
        }
    }

    @Transaction
    @Query("DELETE FROM substancecompanion WHERE substanceName NOT IN (SELECT substanceName FROM ingestion)")
    suspend fun deleteUnusedSubstanceCompanions()

    @Delete
    suspend fun delete(ingestion: Ingestion)

    @Delete
    suspend fun delete(customUnit: CustomUnit)

    @Query("DELETE FROM ingestion WHERE recipeGroupId = :recipeGroupId")
    suspend fun deleteIngestionsByRecipeGroupId(recipeGroupId: String)

    @Transaction
    suspend fun deleteEverything() {
        deleteAllIngestions()
        deleteAllTimedNotes()
        deleteAllExperiences()
        deleteAllSubstanceCompanions()
        deleteAllCustomSubstances()
        deleteAllRatings()
        deleteAllCustomUnits()
        deleteAllRecipeSubcomponents()
        deleteAllCustomRecipes()
    }

    @Transaction
    suspend fun deleteEverythingOfExperience(experienceId: Int) {
        deleteIngestions(experienceId)
        deleteRatings(experienceId)
        deleteExperience(experienceId)
    }


    @Transaction
    @Query("DELETE FROM ingestion")
    suspend fun deleteAllIngestions()

    @Transaction
    @Query("DELETE FROM timedNote")
    suspend fun deleteAllTimedNotes()

    @Transaction
    @Query("DELETE FROM ingestion WHERE experienceId = :experienceId")
    suspend fun deleteIngestions(experienceId: Int)

    @Transaction
    @Query("DELETE FROM shulginrating WHERE experienceId = :experienceId")
    suspend fun deleteRatings(experienceId: Int)

    @Transaction
    @Query("DELETE FROM experience WHERE id = :experienceId")
    suspend fun deleteExperience(experienceId: Int)

    @Transaction
    @Query("DELETE FROM shulginrating")
    suspend fun deleteAllRatings()

    @Transaction
    @Query("DELETE FROM customunit")
    suspend fun deleteAllCustomUnits()

    @Transaction
    @Query("DELETE FROM experience")
    suspend fun deleteAllExperiences()

    @Transaction
    @Query("DELETE FROM substancecompanion")
    suspend fun deleteAllSubstanceCompanions()

    @Transaction
    @Query("DELETE FROM customsubstance")
    suspend fun deleteAllCustomSubstances()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(ingestion: Ingestion)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(rating: ShulginRating)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(customUnit: CustomUnit): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(timedNote: TimedNote): Long

    @Transaction
    suspend fun insertIngestionExperienceAndCompanion(
        ingestion: Ingestion,
        experience: Experience,
        substanceCompanion: SubstanceCompanion
    ) {
        insert(ingestion)
        insert(experience)
        insert(substanceCompanion)
    }

    @Transaction
    @Query("SELECT * FROM customrecipe ORDER BY creationDate")
    suspend fun getAllCustomRecipesWithSubcomponentsSorted(): List<CustomRecipeWithSubcomponents>

    @Transaction
    suspend fun insertEverythingWithProgress(
        journalExport: JournalExport,
        imageExportHelper: com.isaakhanimann.journal.ui.tabs.settings.ImageExportHelper,
        onProgressUpdate: (Float, String) -> Unit
    ) {
        val totalSteps = journalExport.experiences.size + 
                        journalExport.substanceCompanions.size + 
                        journalExport.customSubstances.size + 
                        journalExport.customUnits.size + 
                        journalExport.customRecipes.size +
                        journalExport.ingestionReminders.size
        var currentStep = 0
        
        // Import experiences with photos
        journalExport.experiences.forEachIndexed { indexExperience, experienceSerializable ->
            currentStep++
            onProgressUpdate(currentStep.toFloat() / totalSteps, "Importing experience ${indexExperience + 1}...")
            
            val newExperience = Experience(
                id = 0, // Let database auto-generate new ID
                title = experienceSerializable.title,
                text = experienceSerializable.text,
                creationDate = experienceSerializable.creationDate,
                sortDate = experienceSerializable.sortDate,
                isFavorite = experienceSerializable.isFavorite,
                location = if (experienceSerializable.location != null) {
                    Location(
                        name = experienceSerializable.location.name,
                        longitude = experienceSerializable.location.longitude,
                        latitude = experienceSerializable.location.latitude
                    )
                } else {
                    null
                }
            )
            val experienceID = insert(newExperience).toInt() // Get the auto-generated ID
            
            // Import ingestions
            experienceSerializable.ingestions.forEach { ingestionSerializable ->
                if (ingestionSerializable.substanceName != null) {
                    val newIngestion = Ingestion(
                        substanceName = ingestionSerializable.substanceName,
                        time = ingestionSerializable.time,
                        endTime = ingestionSerializable.endTime,
                        creationDate = ingestionSerializable.creationDate,
                        administrationRoute = ingestionSerializable.administrationRoute,
                        dose = ingestionSerializable.dose,
                        isDoseAnEstimate = ingestionSerializable.isDoseAnEstimate,
                        estimatedDoseStandardDeviation = ingestionSerializable.estimatedDoseStandardDeviation,
                        units = ingestionSerializable.units,
                        experienceId = experienceID,
                        notes = ingestionSerializable.notes,
                        stomachFullness = ingestionSerializable.stomachFullness,
                        consumerName = ingestionSerializable.consumerName,
                        customUnitId = ingestionSerializable.customUnitId,
                        customRecipeId = ingestionSerializable.customRecipeId,
                        recipeGroupId = ingestionSerializable.recipeGroupId
                    )
                    insert(newIngestion)
                }
            }
            
            // Import timed notes with photos
            experienceSerializable.timedNotes.forEach { timedNoteSerializable ->
                val newTimedNote = TimedNote(
                    time = timedNoteSerializable.time,
                    creationDate = timedNoteSerializable.creationDate,
                    experienceId = experienceID,
                    isPartOfTimeline = timedNoteSerializable.isPartOfTimeline,
                    color = timedNoteSerializable.color,
                    customColor = timedNoteSerializable.customColor,
                    note = timedNoteSerializable.note
                )
                val timedNoteId = insert(newTimedNote)
                
                // Import photos for this timed note
                timedNoteSerializable.photos.forEach { photoSerializable ->
                    if (photoSerializable.imageBase64.isNotEmpty()) {
                        val fileName = imageExportHelper.generateUniqueFileName(photoSerializable.originalFileName)
                        val filePath = imageExportHelper.base64ToImage(
                            photoSerializable.imageBase64,
                            fileName,
                            imageExportHelper.getImagesDirectory()
                        )
                        
                        if (filePath != null) {
                            val newPhoto = TimedNotePhoto(
                                timedNoteId = timedNoteId.toInt(),
                                filePath = filePath,
                                creationDate = photoSerializable.creationDate,
                                caption = photoSerializable.caption
                            )
                            insert(newPhoto)
                        }
                    }
                }
            }
            
            // Import ratings
            experienceSerializable.ratings.forEach { ratingSerializable ->
                val newRating = ShulginRating(
                    time = ratingSerializable.time,
                    creationDate = ratingSerializable.creationDate,
                    option = ratingSerializable.option,
                    experienceId = experienceID
                )
                insert(newRating)
            }
        }
        
        // Import other data
        journalExport.substanceCompanions.forEach { 
            currentStep++
            onProgressUpdate(currentStep.toFloat() / totalSteps, "Importing substance companions...")
            insert(it) 
        }
        
        journalExport.customSubstances.forEach { 
            currentStep++
            onProgressUpdate(currentStep.toFloat() / totalSteps, "Importing custom substances...")
            insert(it) 
        }
        
        journalExport.customUnits.forEach {
            currentStep++
            onProgressUpdate(currentStep.toFloat() / totalSteps, "Importing custom units...")
            if (it.substanceName != null) {
                insert(
                    CustomUnit(
                        id = 0, // Let database auto-generate new ID
                        substanceName = it.substanceName,
                        name = it.name,
                        creationDate = it.creationDate,
                        administrationRoute = it.administrationRoute ?: AdministrationRoute.ORAL,
                        dose = it.dose,
                        estimatedDoseStandardDeviation = it.estimatedDoseStandardDeviation,
                        isEstimate = it.isEstimate,
                        isArchived = it.isArchived,
                        unit = it.unit,
                        unitPlural = it.unitPlural,
                        originalUnit = it.originalUnit ?: "",
                        note = it.note
                    )
                )
            }
        }
        
        journalExport.customRecipes.forEach { recipeSerializable ->
            currentStep++
            onProgressUpdate(currentStep.toFloat() / totalSteps, "Importing custom recipes...")
            val newRecipe = CustomRecipe(
                id = 0, // Let database auto-generate new ID
                name = recipeSerializable.name,
                creationDate = recipeSerializable.creationDate,
                administrationRoute = recipeSerializable.administrationRoute,
                isArchived = recipeSerializable.isArchived,
                unit = recipeSerializable.unit,
                unitPlural = recipeSerializable.unitPlural,
                note = recipeSerializable.note
            )
            val newRecipeId = insert(newRecipe).toInt() // Get the auto-generated ID
            
            recipeSerializable.subcomponents.forEach { subcomponentSerializable ->
                val newSubcomponent = RecipeSubcomponent(
                    id = 0, // Let database auto-generate new ID
                    recipeId = newRecipeId, // Use the new recipe ID
                    substanceName = subcomponentSerializable.substanceName,
                    customUnitId = subcomponentSerializable.customUnitId,
                    dose = subcomponentSerializable.dose,
                    estimatedDoseStandardDeviation = subcomponentSerializable.estimatedDoseStandardDeviation,
                    isEstimate = subcomponentSerializable.isEstimate,
                    originalUnit = subcomponentSerializable.originalUnit,
                    creationDate = subcomponentSerializable.creationDate
                )
                insert(newSubcomponent)
            }
        }
        
        // Import reminders
        journalExport.ingestionReminders.forEach { reminderSerializable ->
            currentStep++
            onProgressUpdate(currentStep.toFloat() / totalSteps, "Importing reminders...")
            val newReminder = IngestionReminder(
                id = 0, // Let database auto-generate new ID
                substanceName = reminderSerializable.substanceName,
                reminderTime = reminderSerializable.reminderTime,
                repeatMode = reminderSerializable.repeatMode,
                dose = reminderSerializable.dose,
                units = reminderSerializable.units,
                note = reminderSerializable.note,
                isEnabled = reminderSerializable.isEnabled,
                createdAt = reminderSerializable.createdAt,
                customRepeatData = reminderSerializable.customRepeatData
            )
            insert(newReminder)
        }
    }

    @Transaction
    suspend fun insertEverything(
        journalExport: JournalExport
    ) {
        journalExport.experiences.forEachIndexed { indexExperience, experienceSerializable ->
            val experienceID = indexExperience + 1
            val newExperience = Experience(
                id = experienceID,
                title = experienceSerializable.title,
                text = experienceSerializable.text,
                creationDate = experienceSerializable.creationDate,
                sortDate = experienceSerializable.sortDate,
                isFavorite = experienceSerializable.isFavorite,
                location = if (experienceSerializable.location != null) {
                    Location(
                        name = experienceSerializable.location.name,
                        longitude = experienceSerializable.location.longitude,
                        latitude = experienceSerializable.location.latitude
                    )
                } else {
                    null
                }
            )
            insert(newExperience)
            experienceSerializable.ingestions.forEach { ingestionSerializable ->
                if (ingestionSerializable.substanceName != null) {
                    val newIngestion = Ingestion(
                        substanceName = ingestionSerializable.substanceName,
                        time = ingestionSerializable.time,
                        endTime = ingestionSerializable.endTime,
                        creationDate = ingestionSerializable.creationDate,
                        administrationRoute = ingestionSerializable.administrationRoute,
                        dose = ingestionSerializable.dose,
                        isDoseAnEstimate = ingestionSerializable.isDoseAnEstimate,
                        estimatedDoseStandardDeviation = ingestionSerializable.estimatedDoseStandardDeviation,
                        units = ingestionSerializable.units,
                        experienceId = experienceID,
                        notes = ingestionSerializable.notes,
                        stomachFullness = ingestionSerializable.stomachFullness,
                        consumerName = ingestionSerializable.consumerName,
                        customUnitId = ingestionSerializable.customUnitId,
                        customRecipeId = ingestionSerializable.customRecipeId,
                        recipeGroupId = ingestionSerializable.recipeGroupId
                    )
                    insert(newIngestion)
                }
            }
            experienceSerializable.timedNotes.forEach { timedNoteSerializable ->
                val newTimedNote = TimedNote(
                    time = timedNoteSerializable.time,
                    creationDate = timedNoteSerializable.creationDate,
                    experienceId = experienceID,
                    isPartOfTimeline = timedNoteSerializable.isPartOfTimeline,
                    color = timedNoteSerializable.color,
                    customColor = timedNoteSerializable.customColor,
                    note = timedNoteSerializable.note
                )
                insert(newTimedNote)
            }
            experienceSerializable.ratings.forEach { ratingSerializable ->
                val newRating = ShulginRating(
                    time = ratingSerializable.time,
                    creationDate = ratingSerializable.creationDate,
                    option = ratingSerializable.option,
                    experienceId = experienceID
                )
                insert(newRating)
            }
        }
        journalExport.substanceCompanions.forEach { insert(it) }
        journalExport.customSubstances.forEach { insert(it) }
        journalExport.customUnits.forEach {
            if (it.substanceName != null) {
                insert(
                    CustomUnit(
                        id = it.id,
                        substanceName = it.substanceName,
                        name = it.name,
                        creationDate = it.creationDate,
                        administrationRoute = it.administrationRoute ?: AdministrationRoute.ORAL,
                        dose = it.dose,
                        estimatedDoseStandardDeviation = it.estimatedDoseStandardDeviation,
                        isEstimate = it.isEstimate,
                        isArchived = it.isArchived,
                        unit = it.unit,
                        unitPlural = it.unitPlural,
                        originalUnit = it.originalUnit ?: "",
                        note = it.note
                    )
                )
            }
        }
        journalExport.customRecipes.forEach { recipeSerializable ->
            val newRecipe = CustomRecipe(
                id = recipeSerializable.id,
                name = recipeSerializable.name,
                creationDate = recipeSerializable.creationDate,
                administrationRoute = recipeSerializable.administrationRoute,
                isArchived = recipeSerializable.isArchived,
                unit = recipeSerializable.unit,
                unitPlural = recipeSerializable.unitPlural,
                note = recipeSerializable.note
            )
            insert(newRecipe)

            recipeSerializable.subcomponents.forEach { subcomponentSerializable ->
                val newSubcomponent = RecipeSubcomponent(
                    id = subcomponentSerializable.id,
                    recipeId = recipeSerializable.id,
                    substanceName = subcomponentSerializable.substanceName,
                    dose = subcomponentSerializable.dose,
                    estimatedDoseStandardDeviation = subcomponentSerializable.estimatedDoseStandardDeviation,
                    isEstimate = subcomponentSerializable.isEstimate,
                    originalUnit = subcomponentSerializable.originalUnit,
                    creationDate = subcomponentSerializable.creationDate
                )
                insert(newSubcomponent)
            }
        }
    }

    @Transaction
    suspend fun insertIngestionAndCompanion(
        ingestion: Ingestion,
        substanceCompanion: SubstanceCompanion
    ) {
        insert(ingestion)
        insert(substanceCompanion)
    }

    @Transaction
    @Query("SELECT * FROM experience WHERE id = :experienceId")
    fun getExperienceWithIngestionsAndCompanionsFlow(experienceId: Int): Flow<ExperienceWithIngestionsAndCompanions?>

    @Transaction
    @Query("SELECT * FROM ingestion WHERE experienceId = :experienceId")
    fun getIngestionsWithCompanionsFlow(experienceId: Int): Flow<List<IngestionWithCompanionAndCustomUnit>>

    @Query("SELECT * FROM shulginrating WHERE experienceId = :experienceId")
    fun getRatingsFlow(experienceId: Int): Flow<List<ShulginRating>>

    @Query("SELECT * FROM timednote WHERE experienceId = :experienceId ORDER BY time")
    fun getTimedNotesFlowSorted(experienceId: Int): Flow<List<TimedNote>>

    @Query("SELECT * FROM ingestion WHERE substanceName = :substanceName ORDER BY time DESC LIMIT 1")
    suspend fun getLastIngestion(substanceName: String): Ingestion?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(substanceCompanion: SubstanceCompanion)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(customSubstance: CustomSubstance): Long

    @Transaction
    suspend fun importCustomSubstances(customSubstances: List<CustomSubstance>) {
        customSubstances.forEach { substance ->
            val existing = getCustomSubstance(substance.name)
            if (existing != null) {
                update(substance.copy(id = existing.id))
            } else {
                insert(substance.copy(id = 0))
            }
        }
    }

    @Delete
    suspend fun delete(customSubstance: CustomSubstance)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(customSubstance: CustomSubstance)

    @Delete
    suspend fun delete(substanceCompanion: SubstanceCompanion)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(substanceCompanion: SubstanceCompanion)

    @Query("SELECT * FROM substancecompanion WHERE substanceName =:substanceName")
    fun getSubstanceCompanionFlow(substanceName: String): Flow<SubstanceCompanion?>

    @Query("SELECT * FROM substancecompanion")
    fun getAllSubstanceCompanionsFlow(): Flow<List<SubstanceCompanion>>

    @Query("SELECT * FROM timednote")
    fun getAllTimedNotesFlow(): Flow<List<TimedNote>>

    @Query("SELECT * FROM customunit WHERE isArchived = :isArchived ORDER BY creationDate DESC")
    fun getSortedCustomUnitsFlow(isArchived: Boolean): Flow<List<CustomUnit>>

    @Query("SELECT * FROM customunit WHERE isArchived = :isArchived AND substanceName = :substanceName ORDER BY creationDate DESC")
    fun getSortedCustomUnitsFlowBasedOnName(substanceName: String, isArchived: Boolean): Flow<List<CustomUnit>>

    @Query("SELECT * FROM customunit ORDER BY creationDate DESC")
    fun getAllCustomUnitsFlow(): Flow<List<CustomUnit>>

    @Query("SELECT * FROM timednote WHERE experienceId =:experienceId")
    suspend fun getTimedNotes(experienceId: Int): List<TimedNote>

    @Query("SELECT * FROM customrecipe WHERE isArchived = :isArchived ORDER BY creationDate DESC")
    fun getSortedCustomRecipesFlow(isArchived: Boolean): Flow<List<CustomRecipe>>

    @Query("SELECT * FROM customrecipe ORDER BY creationDate DESC")
    fun getAllCustomRecipesFlow(): Flow<List<CustomRecipe>>

    @Query("SELECT * FROM customrecipe WHERE id = :id")
    suspend fun getCustomRecipe(id: Int): CustomRecipe?

    @Transaction
    @Query("SELECT * FROM customrecipe WHERE id = :id")
    suspend fun getCustomRecipeWithSubcomponents(id: Int): CustomRecipeWithSubcomponents?

    @Transaction
    @Query("SELECT * FROM customrecipe WHERE isArchived = :isArchived ORDER BY creationDate DESC")
    fun getSortedCustomRecipesWithSubcomponentsFlow(isArchived: Boolean): Flow<List<CustomRecipeWithSubcomponents>>

    @Query("SELECT * FROM recipesubcomponent WHERE recipeId = :recipeId ORDER BY creationDate")
    suspend fun getRecipeSubcomponents(recipeId: Int): List<RecipeSubcomponent>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(customRecipe: CustomRecipe): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(recipeSubcomponent: RecipeSubcomponent): Long

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(customRecipe: CustomRecipe)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(recipeSubcomponent: RecipeSubcomponent)

    @Delete
    suspend fun delete(customRecipe: CustomRecipe)

    @Delete
    suspend fun delete(recipeSubcomponent: RecipeSubcomponent)

    @Transaction
    suspend fun deleteCustomRecipeWithSubcomponents(customRecipeWithSubcomponents: CustomRecipeWithSubcomponents) {
        customRecipeWithSubcomponents.subcomponents.forEach {
            delete(it)
        }
        delete(customRecipeWithSubcomponents.recipe)
    }

    @Transaction
    @Query("DELETE FROM customrecipe")
    suspend fun deleteAllCustomRecipes()

    @Transaction
    @Query("DELETE FROM recipesubcomponent")
    suspend fun deleteAllRecipeSubcomponents()

    // TimedNotePhoto queries
    @Insert
    suspend fun insert(timedNotePhoto: com.isaakhanimann.journal.data.room.experiences.entities.TimedNotePhoto): Long

    @Update
    suspend fun update(timedNotePhoto: com.isaakhanimann.journal.data.room.experiences.entities.TimedNotePhoto)

    @Delete
    suspend fun delete(timedNotePhoto: com.isaakhanimann.journal.data.room.experiences.entities.TimedNotePhoto)

    @Query("SELECT * FROM TimedNotePhoto WHERE timedNoteId = :timedNoteId")
    fun getPhotosForTimedNoteFlow(timedNoteId: Int): Flow<List<com.isaakhanimann.journal.data.room.experiences.entities.TimedNotePhoto>>

    @Transaction
    @Query("SELECT * FROM TimedNote WHERE experienceId = :experienceId ORDER BY time ASC")
    fun getTimedNotesWithPhotosFlow(experienceId: Int): Flow<List<com.isaakhanimann.journal.data.room.experiences.relations.TimedNoteWithPhotos>>

    // IngestionReminder queries
    @Insert
    suspend fun insert(reminder: com.isaakhanimann.journal.data.room.experiences.entities.IngestionReminder): Long

    @Update
    suspend fun update(reminder: com.isaakhanimann.journal.data.room.experiences.entities.IngestionReminder)

    @Delete
    suspend fun delete(reminder: com.isaakhanimann.journal.data.room.experiences.entities.IngestionReminder)

    @Query("SELECT * FROM ingestion_reminders ORDER BY reminderTime ASC")
    fun getAllRemindersFlow(): Flow<List<com.isaakhanimann.journal.data.room.experiences.entities.IngestionReminder>>

    @Query("SELECT * FROM ingestion_reminders WHERE isEnabled = 1 ORDER BY reminderTime ASC")
    fun getEnabledRemindersFlow(): Flow<List<com.isaakhanimann.journal.data.room.experiences.entities.IngestionReminder>>

    @Query("SELECT * FROM ingestion_reminders WHERE id = :id")
    suspend fun getReminderById(id: Int): com.isaakhanimann.journal.data.room.experiences.entities.IngestionReminder?
    
    @Query("SELECT * FROM ingestion_reminders ORDER BY reminderTime ASC")
    suspend fun getAllReminders(): List<com.isaakhanimann.journal.data.room.experiences.entities.IngestionReminder>
    
    @Query("SELECT * FROM TimedNote WHERE experienceId = :experienceId")
    suspend fun getTimedNotesWithPhotos(experienceId: Int): List<com.isaakhanimann.journal.data.room.experiences.relations.TimedNoteWithPhotos>
}