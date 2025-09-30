package com.isaakhanimann.journal.ui.main.navigation.graphs

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.navigation
import androidx.navigation.toRoute
import com.isaakhanimann.journal.ui.main.navigation.composableWithTransitions
import com.isaakhanimann.journal.ui.main.navigation.SettingsTopLevelRoute
import com.isaakhanimann.journal.ui.tabs.search.custom.AddCustomSubstanceScreen
import com.isaakhanimann.journal.ui.tabs.search.custom.EditCustomSubstanceScreen
import com.isaakhanimann.journal.ui.tabs.settings.DonateScreen
import com.isaakhanimann.journal.ui.tabs.settings.FAQScreen
import com.isaakhanimann.journal.ui.tabs.settings.SettingsScreen
import com.isaakhanimann.journal.ui.tabs.settings.colors.SubstanceColorsScreen
import com.isaakhanimann.journal.ui.tabs.settings.combinations.CombinationSettingsScreen
import com.isaakhanimann.journal.ui.tabs.settings.customrecipes.CustomRecipesScreen
import com.isaakhanimann.journal.ui.tabs.settings.customrecipes.SubstanceSelectorScreen
import com.isaakhanimann.journal.ui.tabs.settings.customrecipes.add.AddCustomRecipeScreen
import com.isaakhanimann.journal.ui.tabs.settings.customrecipes.add.AddCustomRecipeViewModel
import com.isaakhanimann.journal.ui.tabs.settings.customrecipes.edit.EditCustomRecipeScreen
import com.isaakhanimann.journal.ui.tabs.settings.customsubstances.CustomSubstanceManagementScreen
import com.isaakhanimann.journal.ui.tabs.settings.customunits.CustomUnitsScreen
import com.isaakhanimann.journal.ui.tabs.settings.customunits.archive.CustomUnitArchiveScreen
import com.isaakhanimann.journal.ui.tabs.settings.customunits.edit.EditCustomUnitScreen
import kotlinx.serialization.Serializable

fun NavGraphBuilder.settingsGraph(navController: NavHostController) {
    navigation<SettingsTopLevelRoute>(
        startDestination = SettingsScreenRoute,
    ) {
        composableWithTransitions<SettingsScreenRoute> {
            SettingsScreen(
                navigateToFAQ = {
                    navController.navigate(FAQRoute)
                },
                navigateToComboSettings = {
                    navController.navigate(CombinationSettingsRoute)
                },
                navigateToSubstanceColors = {
                    navController.navigate(SubstanceColorsRoute)
                },
                navigateToCustomUnits = {
                    navController.navigate(CustomUnitsRoute)
                },
                navigateToCustomSubstances = {
                    navController.navigate(CustomSubstancesRoute)
                },
                navigateToDonate = {
                    navController.navigate(DonateRoute)
                },
                navigateToCustomRecipes = {
                    navController.navigate(CustomRecipesRoute)
                }
            )
        }
        composableWithTransitions<FAQRoute> { FAQScreen() }
        composableWithTransitions<DonateRoute> { DonateScreen() }
        composableWithTransitions<CombinationSettingsRoute> { CombinationSettingsScreen() }
        composableWithTransitions<SubstanceColorsRoute> { SubstanceColorsScreen() }
        composableWithTransitions<CustomUnitArchiveRoute> {
            CustomUnitArchiveScreen(navigateToEditCustomUnit = { customUnitId ->
                navController.navigate(EditCustomUnitRoute(customUnitId))
            })
        }
        addCustomUnitGraph(navController)
        composableWithTransitions<CustomUnitsRoute> {
            CustomUnitsScreen(
                navigateToAddCustomUnit = {
                    navController.navigate(AddCustomUnitsParentRoute)
                },
                navigateToEditCustomUnit = { customUnitId ->
                    navController.navigate(EditCustomUnitRoute(customUnitId))
                },
                navigateToCustomUnitArchive = {
                    navController.navigate(CustomUnitArchiveRoute)
                }
            )
        }
        composableWithTransitions<EditCustomUnitRoute> {
            EditCustomUnitScreen(navigateBack = navController::popBackStack)
        }
        composableWithTransitions<CustomSubstancesRoute> {
            CustomSubstanceManagementScreen(
                navigateBack = { navController.popBackStack() },
                navigateToAddCustomSubstance = { navController.navigate(AddCustomSubstanceRoute) },
                navigateToEditCustomSubstance = { id ->
                    navController.navigate(EditCustomSubstanceRoute(id))
                }
            )
        }
        composableWithTransitions<AddCustomSubstanceRoute> {
            AddCustomSubstanceScreen(
                navigateBack = { navController.popBackStack() }
            )
        }
        composableWithTransitions<EditCustomSubstanceRoute> {
            EditCustomSubstanceScreen(
                navigateBack = { navController.popBackStack() }
            )
        }
        composableWithTransitions<CustomRecipesRoute> {
            CustomRecipesScreen(
                navigateToAddCustomRecipe = {
                    navController.navigate(AddCustomRecipeRoute)
                },
                navigateToEditCustomRecipe = { customRecipeId ->
                    navController.navigate(EditCustomRecipeRoute(customRecipeId))
                },
                navigateToCustomRecipeArchive = {
                    navController.navigate(CustomRecipeArchiveRoute)
                }
            )
        }
        composableWithTransitions<AddCustomRecipeRoute> {
            AddCustomRecipeScreen(
                navigateBack = { navController.navigateUp() },
                navigateToSubstanceSelector = { index ->
                    navController.navigate(SubstanceSelectorRoute(subcomponentIndex = index))
                }
            )
        }
        composableWithTransitions<EditCustomRecipeRoute> {
            EditCustomRecipeScreen(
                navigateBack = { navController.navigateUp() },
                navigateToSubstanceSelector = { index ->
                    navController.navigate(SubstanceSelectorRoute(subcomponentIndex = index))
                }
            )
        }
        composableWithTransitions<SubstanceSelectorRoute> { backStackEntry ->
            val args = backStackEntry.toRoute<SubstanceSelectorRoute>()
            val viewModel: AddCustomRecipeViewModel = hiltViewModel()
            val allSubstances by viewModel.allSubstances.collectAsState()

            SubstanceSelectorScreen(
                subcomponentIndex = args.subcomponentIndex,
                allSubstances = allSubstances,
                onSubstanceSelected = {
                    navController.popBackStack()
                },
                onDismiss = { navController.popBackStack() }
            )
        }
    }
}

@Serializable
object SettingsScreenRoute

@Serializable
object FAQRoute

@Serializable
object DonateRoute

@Serializable
object CombinationSettingsRoute

@Serializable
object SubstanceColorsRoute

@Serializable
object CustomUnitArchiveRoute

@Serializable
object CustomUnitsRoute

@Serializable
data class EditCustomUnitRoute(val customUnitId: Int)

@Serializable
object CustomSubstancesRoute

@Serializable
object AddCustomSubstanceRoute

@Serializable
object CustomRecipesRoute

@Serializable
data class EditCustomRecipeRoute(val customRecipeId: Int)

@Serializable
object AddCustomRecipeRoute

@Serializable
object CustomRecipeArchiveRoute

@Serializable
data class SubstanceSelectorRoute(val subcomponentIndex: Int)