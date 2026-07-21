package com.nullhorizon.app.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Per-ship-region accent tokens. Always pair with textual status labels.
 */
enum class ShipRegionId(val id: String, val displayName: String) {
    Emergency("emergency", "Emergency Interface"),
    Maintenance("maintenance", "Maintenance Deck"),
    Archive("archive", "Archive Core"),
    VersionVault("vault", "Version Vault"),
    Automation("automation", "Automation Lab"),
    Drone("drone", "Drone Foundry"),
    Navigation("navigation", "Navigation Array"),
    Comms("comms", "Communications Spire"),
    Verification("verification", "Verification Chamber"),
    BlackVault("black_vault", "Black Vault"),
    DataFoundry("data_foundry", "Data Foundry"),
    Reactor("reactor", "Reactor Kernel"),
    Prediction("prediction", "Prediction Observatory"),
    Horizon("horizon", "Horizon Core"),
    ;

    companion object {
        fun fromRawId(raw: String): ShipRegionId? {
            val normalized = raw.trim().lowercase()
                .replace(' ', '_')
                .replace('-', '_')
            return entries.firstOrNull { it.id == normalized }
                ?: when (normalized) {
                    "version_vault" -> VersionVault
                    "emergency_interface" -> Emergency
                    "maintenance_deck" -> Maintenance
                    "archive_core" -> Archive
                    "automation_lab" -> Automation
                    "drone_foundry" -> Drone
                    "navigation_array" -> Navigation
                    "communications_spire", "communications" -> Comms
                    "verification_chamber" -> Verification
                    "reactor_kernel" -> Reactor
                    "prediction_observatory" -> Prediction
                    "horizon_core" -> Horizon
                    else -> null
                }
        }
    }
}

data class RegionAccent(
    val region: ShipRegionId,
    val accent: Color,
    val labelColor: Color = accent,
)

object NhRegionAccent {
    val catalog: Map<ShipRegionId, RegionAccent> = mapOf(
        ShipRegionId.Emergency to RegionAccent(ShipRegionId.Emergency, NhPalette.GreenPhosphor.accent),
        ShipRegionId.Maintenance to RegionAccent(ShipRegionId.Maintenance, NhPalette.GreenPhosphor.primary),
        ShipRegionId.Archive to RegionAccent(ShipRegionId.Archive, NhPalette.GreenPhosphor.primary),
        ShipRegionId.VersionVault to RegionAccent(ShipRegionId.VersionVault, NhPalette.GreenPhosphor.info),
        ShipRegionId.Automation to RegionAccent(ShipRegionId.Automation, NhPalette.GreenPhosphor.primary),
        ShipRegionId.Drone to RegionAccent(ShipRegionId.Drone, NhPalette.GreenPhosphor.text),
        ShipRegionId.Navigation to RegionAccent(ShipRegionId.Navigation, NhPalette.GreenPhosphor.info),
        ShipRegionId.Comms to RegionAccent(ShipRegionId.Comms, NhPalette.GreenPhosphor.info),
        ShipRegionId.Verification to RegionAccent(ShipRegionId.Verification, NhPalette.GreenPhosphor.accent),
        ShipRegionId.BlackVault to RegionAccent(ShipRegionId.BlackVault, NhPalette.GreenPhosphor.danger),
        ShipRegionId.DataFoundry to RegionAccent(ShipRegionId.DataFoundry, NhPalette.GreenPhosphor.accent),
        ShipRegionId.Reactor to RegionAccent(ShipRegionId.Reactor, NhPalette.GreenPhosphor.danger),
        ShipRegionId.Prediction to RegionAccent(ShipRegionId.Prediction, NhPalette.GreenPhosphor.info),
        ShipRegionId.Horizon to RegionAccent(ShipRegionId.Horizon, NhPalette.GreenPhosphor.text),
    )

    fun forRegionId(rawId: String): RegionAccent {
        val region = ShipRegionId.fromRawId(rawId) ?: ShipRegionId.Horizon
        return catalog.getValue(region)
    }

    fun statusLine(regionName: String, status: String): String =
        "REGION: ${regionName.uppercase()} — ${status.uppercase()}"
}
