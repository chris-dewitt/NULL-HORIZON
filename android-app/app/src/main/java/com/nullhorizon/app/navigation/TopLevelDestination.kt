package com.nullhorizon.app.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import com.nullhorizon.app.R

enum class TopLevelDestination(
    val route: String,
    @StringRes val labelRes: Int,
    @StringRes val contentDescriptionRes: Int,
    val icon: ImageVector,
) {
    ShipMap(
        route = "ship_map",
        labelRes = R.string.nav_ship_map,
        contentDescriptionRes = R.string.nav_ship_map_a11y,
        icon = Icons.Filled.Place,
    ),
    Missions(
        route = "missions",
        labelRes = R.string.nav_missions,
        contentDescriptionRes = R.string.nav_missions_a11y,
        icon = Icons.AutoMirrored.Filled.List,
    ),
    Skills(
        route = "skills",
        labelRes = R.string.nav_skills,
        contentDescriptionRes = R.string.nav_skills_a11y,
        icon = Icons.Filled.AccountTree,
    ),
    Settings(
        route = "settings",
        labelRes = R.string.nav_settings,
        contentDescriptionRes = R.string.nav_settings_a11y,
        icon = Icons.Filled.Settings,
    ),
}

object Routes {
    const val ProfileSetup = "profile_setup"
    const val Main = "main"
    const val MissionSession = "mission/{missionId}"

    fun missionSession(missionId: String): String = "mission/$missionId"
}
