package com.nullhorizon.pc.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import com.nullhorizon.pc.ui.Strings

enum class TopLevelTab(
    val label: String,
    val contentDescription: String,
    val icon: ImageVector,
) {
    ShipMap(
        label = Strings.nav_ship_map,
        contentDescription = Strings.nav_ship_map_a11y,
        icon = Icons.Filled.Place,
    ),
    Missions(
        label = Strings.nav_missions,
        contentDescription = Strings.nav_missions_a11y,
        icon = Icons.AutoMirrored.Filled.List,
    ),
    Skills(
        label = Strings.nav_skills,
        contentDescription = Strings.nav_skills_a11y,
        icon = Icons.Filled.AccountTree,
    ),
    Settings(
        label = Strings.nav_settings,
        contentDescription = Strings.nav_settings_a11y,
        icon = Icons.Filled.Settings,
    ),
}
