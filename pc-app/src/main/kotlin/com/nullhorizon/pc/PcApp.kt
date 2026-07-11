package com.nullhorizon.pc

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.nullhorizon.pc.di.PcAppContainer
import com.nullhorizon.pc.feature.mission.MissionListScreen
import com.nullhorizon.pc.feature.mission.MissionListViewModel
import com.nullhorizon.pc.feature.mission.MissionSessionScreen
import com.nullhorizon.pc.feature.mission.MissionSessionViewModel
import com.nullhorizon.pc.feature.onboarding.ProfileSetupScreen
import com.nullhorizon.pc.feature.onboarding.ProfileSetupViewModel
import com.nullhorizon.pc.feature.settings.SettingsScreen
import com.nullhorizon.pc.feature.settings.SettingsViewModel
import com.nullhorizon.pc.feature.shipmap.ShipMapScreen
import com.nullhorizon.pc.feature.shipmap.ShipMapViewModel
import com.nullhorizon.pc.feature.skills.SkillMapScreen
import com.nullhorizon.pc.feature.skills.SkillMapViewModel
import com.nullhorizon.pc.navigation.PcRoutes
import com.nullhorizon.pc.navigation.TopLevelTab
import com.nullhorizon.pc.ui.Strings
import com.nullhorizon.pc.util.PcViewModel

@Composable
fun PcApp(
    appContainer: PcAppContainer,
) {
    val profileSetupViewModel = rememberPcViewModel {
        ProfileSetupViewModel(appContainer.localProfileRepository)
    }
    val profileState by profileSetupViewModel.uiState.collectAsState()
    var lastMainTab by remember { mutableStateOf(TopLevelTab.ShipMap) }
    var route by remember { mutableStateOf<PcRoutes>(PcRoutes.ProfileSetup) }

    LaunchedEffect(profileState.isLoading, profileState.profileConfigured) {
        if (profileState.isLoading) return@LaunchedEffect
        route = if (profileState.profileConfigured) {
            when (route) {
                PcRoutes.ProfileSetup -> PcRoutes.Main(lastMainTab)
                else -> route
            }
        } else {
            PcRoutes.ProfileSetup
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        if (profileState.isLoading) {
            Box(modifier = Modifier.fillMaxSize()) {
                Text(Strings.boot_status)
            }
            return@Surface
        }

        when (val currentRoute = route) {
            PcRoutes.ProfileSetup -> ProfileSetupScreen(
                viewModel = profileSetupViewModel,
                onProfileCreated = {
                    route = PcRoutes.Main(lastMainTab)
                },
            )

            is PcRoutes.Main -> {
                MainShell(
                    appContainer = appContainer,
                    currentTab = currentRoute.tab,
                    onTabSelected = { tab ->
                        lastMainTab = tab
                        route = PcRoutes.Main(tab)
                    },
                    onOpenMission = { missionId ->
                        route = PcRoutes.MissionSession(missionId)
                    },
                )
            }

            is PcRoutes.MissionSession -> MissionSessionRoute(
                appContainer = appContainer,
                missionId = currentRoute.missionId,
                onBack = { route = PcRoutes.Main(lastMainTab) },
            )
        }
    }
}

@Composable
private fun MainShell(
    appContainer: PcAppContainer,
    currentTab: TopLevelTab,
    onTabSelected: (TopLevelTab) -> Unit,
    onOpenMission: (String) -> Unit,
) {
    val shipMapViewModel = rememberPcViewModel { ShipMapViewModel() }
    val missionListViewModel = rememberPcViewModel {
        MissionListViewModel(
            contentRepository = appContainer.contentRepository,
            progressRepository = appContainer.missionProgressRepository,
        )
    }
    val skillMapViewModel = rememberPcViewModel {
        SkillMapViewModel(
            contentRepository = appContainer.contentRepository,
            progressionRepository = appContainer.progressionRepository,
        )
    }
    val settingsViewModel = rememberPcViewModel {
        SettingsViewModel(
            profileRepository = appContainer.localProfileRepository,
            settingsRepository = appContainer.settingsRepository,
            playerDataRepository = appContainer.playerDataRepository,
            crashReporter = appContainer.crashReporter,
        )
    }

    Row(modifier = Modifier.fillMaxSize()) {
        NavigationRail(
            modifier = Modifier.semantics {
                contentDescription = "Primary navigation"
            },
        ) {
            TopLevelTab.entries.forEach { tab ->
                NavigationRailItem(
                    selected = currentTab == tab,
                    onClick = { onTabSelected(tab) },
                    icon = {
                        Icon(
                            imageVector = tab.icon,
                            contentDescription = tab.contentDescription,
                        )
                    },
                    label = { Text(tab.label) },
                    modifier = Modifier.semantics {
                        contentDescription = tab.contentDescription
                    },
                )
            }
        }

        Box(modifier = Modifier.fillMaxSize()) {
            when (currentTab) {
                TopLevelTab.ShipMap -> ShipMapScreen(viewModel = shipMapViewModel)
                TopLevelTab.Missions -> MissionListScreen(
                    viewModel = missionListViewModel,
                    onMissionSelected = onOpenMission,
                )
                TopLevelTab.Skills -> SkillMapScreen(viewModel = skillMapViewModel)
                TopLevelTab.Settings -> SettingsScreen(viewModel = settingsViewModel)
            }
        }
    }
}

@Composable
private fun MissionSessionRoute(
    appContainer: PcAppContainer,
    missionId: String,
    onBack: () -> Unit,
) {
    val viewModel = rememberPcViewModel(missionId) {
        MissionSessionViewModel(
            missionId = missionId,
            contentRepository = appContainer.contentRepository,
            progressRepository = appContainer.missionProgressRepository,
        )
    }
    MissionSessionScreen(
        viewModel = viewModel,
        onBack = onBack,
    )
}

@Composable
private fun <T : PcViewModel> rememberPcViewModel(
    key: Any? = Unit,
    factory: () -> T,
): T {
    val viewModel = remember(key) { factory() }
    DisposableEffect(viewModel) {
        onDispose { viewModel.clear() }
    }
    return viewModel
}
