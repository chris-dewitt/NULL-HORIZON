package com.nullhorizon.app.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.nullhorizon.app.di.AppContainer
import com.nullhorizon.app.feature.mission.MissionListScreen
import com.nullhorizon.app.feature.mission.MissionListViewModel
import com.nullhorizon.app.feature.mission.MissionSessionScreen
import com.nullhorizon.app.feature.mission.MissionSessionViewModel
import com.nullhorizon.app.feature.onboarding.ProfileSetupScreen
import com.nullhorizon.app.feature.onboarding.ProfileSetupViewModel
import com.nullhorizon.app.feature.settings.SettingsScreen
import com.nullhorizon.app.feature.settings.SettingsViewModel
import com.nullhorizon.app.feature.shipmap.ShipMapScreen
import com.nullhorizon.app.feature.shipmap.ShipMapViewModel
import com.nullhorizon.app.feature.skills.SkillMapScreen
import com.nullhorizon.app.feature.skills.SkillMapViewModel
import com.nullhorizon.app.progression.ProgressionSnapshot
import com.nullhorizon.app.ui.chrome.TuiNavItem
import com.nullhorizon.app.ui.chrome.TuiTabLine

@Composable
fun NullHorizonNavHost(
    appContainer: AppContainer,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    val profileSetupViewModel: ProfileSetupViewModel = viewModel(
        factory = ProfileSetupViewModel.factory(appContainer.localProfileRepository),
    )
    val profileState by profileSetupViewModel.uiState.collectAsStateWithLifecycle()

    val startDestination = if (profileState.profileConfigured) {
        Routes.Main
    } else {
        Routes.ProfileSetup
    }

    if (profileState.isLoading) {
        return
    }

    // After Settings → Delete local data, return to onboarding without requiring an app restart.
    LaunchedEffect(profileState.profileConfigured) {
        if (!profileState.profileConfigured &&
            navController.currentDestination?.route != Routes.ProfileSetup
        ) {
            navController.navigate(Routes.ProfileSetup) {
                popUpTo(navController.graph.id) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
    ) {
        composable(Routes.ProfileSetup) {
            ProfileSetupScreen(
                viewModel = profileSetupViewModel,
                onProfileCreated = {
                    navController.navigate(Routes.Main) {
                        popUpTo(Routes.ProfileSetup) { inclusive = true }
                    }
                },
            )
        }
        composable(Routes.Main) {
            MainShell(
                appContainer = appContainer,
                onOpenMission = { missionId ->
                    navController.navigate(Routes.missionSession(missionId))
                },
            )
        }
        composable(
            route = Routes.MissionSession,
            arguments = listOf(navArgument("missionId") { type = NavType.StringType }),
        ) { entry ->
            val missionId = entry.arguments?.getString("missionId").orEmpty()
            val viewModel: MissionSessionViewModel = viewModel(
                factory = MissionSessionViewModel.factory(
                    missionId = missionId,
                    contentRepository = appContainer.contentRepository,
                    progressRepository = appContainer.missionProgressRepository,
                ),
            )
            MissionSessionScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
            )
        }
    }
}

@Composable
private fun MainShell(
    appContainer: AppContainer,
    onOpenMission: (String) -> Unit,
) {
    val tabNavController = rememberNavController()
    val navBackStackEntry by tabNavController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val progression by appContainer.progressionRepository.snapshot
        .collectAsStateWithLifecycle(initialValue = ProgressionSnapshot())
    val navItems = TopLevelDestination.entries.map { destination ->
        TuiNavItem(
            id = destination.route,
            label = stringResource(destination.labelRes),
            contentDescription = stringResource(destination.contentDescriptionRes),
        )
    }

    Scaffold(
        bottomBar = {
            TuiTabLine(
                sessionTag = "[NH-0.1]",
                items = navItems,
                selectedId = currentRoute ?: TopLevelDestination.ShipMap.route,
                statusText = progression.rank,
                onSelect = { route ->
                    tabNavController.navigate(route) {
                        popUpTo(tabNavController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
            )
        },
    ) { innerPadding ->
        NavHost(
            navController = tabNavController,
            startDestination = TopLevelDestination.ShipMap.route,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(TopLevelDestination.ShipMap.route) {
                val viewModel: ShipMapViewModel = viewModel(
                    factory = ShipMapViewModel.factory(
                        contentRepository = appContainer.contentRepository,
                        progressRepository = appContainer.missionProgressRepository,
                    ),
                )
                ShipMapScreen(
                    viewModel = viewModel,
                    onOpenMission = onOpenMission,
                )
            }
            composable(TopLevelDestination.Missions.route) {
                val viewModel: MissionListViewModel = viewModel(
                    factory = MissionListViewModel.factory(
                        contentRepository = appContainer.contentRepository,
                        progressRepository = appContainer.missionProgressRepository,
                    ),
                )
                MissionListScreen(
                    viewModel = viewModel,
                    onMissionSelected = onOpenMission,
                )
            }
            composable(TopLevelDestination.Skills.route) {
                val viewModel: SkillMapViewModel = viewModel(
                    factory = SkillMapViewModel.factory(
                        contentRepository = appContainer.contentRepository,
                        progressionRepository = appContainer.progressionRepository,
                    ),
                )
                SkillMapScreen(viewModel = viewModel)
            }
            composable(TopLevelDestination.Settings.route) {
                val viewModel: SettingsViewModel = viewModel(
                    factory = SettingsViewModel.factory(
                        profileRepository = appContainer.localProfileRepository,
                        settingsRepository = appContainer.settingsRepository,
                        playerDataRepository = appContainer.playerDataRepository,
                        crashReporter = appContainer.crashReporter,
                    ),
                )
                SettingsScreen(viewModel = viewModel)
            }
        }
    }
}
