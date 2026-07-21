package com.nullhorizon.pc

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.dp
import com.nullhorizon.app.ui.chrome.BootSequenceScreen
import com.nullhorizon.app.ui.chrome.TuiNavColumn
import com.nullhorizon.app.ui.chrome.TuiNavItem
import com.nullhorizon.app.progression.ProgressionSnapshot
import com.nullhorizon.app.ui.theme.NhColors
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
import com.nullhorizon.pc.feature.signals.SignalsScreen
import com.nullhorizon.pc.feature.signals.SignalsViewModel
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
    var bootComplete by remember { mutableStateOf(false) }

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
        if (!bootComplete) {
            BootSequenceScreen(onFinished = { bootComplete = true })
            return@Surface
        }

        if (profileState.isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(24.dp)) {
                Text(
                    text = Strings.boot_status.uppercase(),
                    color = NhColors.PhosphorAmber,
                )
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
    val signalsViewModel = rememberPcViewModel {
        SignalsViewModel(
            contentRepository = appContainer.contentRepository,
            progressRepository = appContainer.missionProgressRepository,
        )
    }
    val progression by appContainer.progressionRepository.snapshot
        .collectAsState(initial = ProgressionSnapshot())

    Row(modifier = Modifier.fillMaxSize()) {
        TuiNavColumn(
            title = "NH OS",
            subtitle = Strings.nav_keybind_hint,
            items = TopLevelTab.entries.mapIndexed { index, tab ->
                TuiNavItem(
                    id = tab.name,
                    label = tab.label,
                    contentDescription = tab.contentDescription,
                    keyHint = (index + 1).toString(),
                )
            },
            selectedId = currentTab.name,
            onSelect = { id ->
                TopLevelTab.entries.firstOrNull { it.name == id }?.let(onTabSelected)
            },
            modifier = Modifier.padding(8.dp),
        )

        Box(modifier = Modifier.fillMaxSize()) {
            when (currentTab) {
                TopLevelTab.ShipMap -> ShipMapScreen(viewModel = shipMapViewModel)
                TopLevelTab.Missions -> MissionListScreen(
                    viewModel = missionListViewModel,
                    onMissionSelected = onOpenMission,
                )
                TopLevelTab.Skills -> SkillMapScreen(viewModel = skillMapViewModel)
                TopLevelTab.Signals -> SignalsScreen(viewModel = signalsViewModel)
                TopLevelTab.Settings -> SettingsScreen(
                    viewModel = settingsViewModel,
                    clearance = progression.clearancePoints,
                )
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
