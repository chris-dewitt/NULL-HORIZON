package com.nullhorizon.pc.navigation

sealed interface PcRoutes {
    data object ProfileSetup : PcRoutes
    data class Main(val tab: TopLevelTab) : PcRoutes
    data class MissionSession(val missionId: String) : PcRoutes
}
