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
        ShipRegionId.Emergency to RegionAccent(ShipRegionId.Emergency, NhColors.PhosphorAmber),
        ShipRegionId.Maintenance to RegionAccent(ShipRegionId.Maintenance, NhColors.PhosphorGreen),
        ShipRegionId.Archive to RegionAccent(ShipRegionId.Archive, NhColors.PhosphorGreen),
        ShipRegionId.VersionVault to RegionAccent(ShipRegionId.VersionVault, NhColors.PhosphorBlue),
        ShipRegionId.Automation to RegionAccent(ShipRegionId.Automation, NhColors.PhosphorGreen),
        ShipRegionId.Drone to RegionAccent(ShipRegionId.Drone, NhColors.PhosphorWhite),
        ShipRegionId.Navigation to RegionAccent(ShipRegionId.Navigation, NhColors.PhosphorBlue),
        ShipRegionId.Comms to RegionAccent(ShipRegionId.Comms, NhColors.PhosphorBlue),
        ShipRegionId.Verification to RegionAccent(ShipRegionId.Verification, NhColors.PhosphorAmber),
        ShipRegionId.BlackVault to RegionAccent(ShipRegionId.BlackVault, NhColors.PhosphorRed),
        ShipRegionId.DataFoundry to RegionAccent(ShipRegionId.DataFoundry, NhColors.PhosphorAmber),
        ShipRegionId.Reactor to RegionAccent(ShipRegionId.Reactor, NhColors.PhosphorRed),
        ShipRegionId.Prediction to RegionAccent(ShipRegionId.Prediction, NhColors.PhosphorBlue),
        ShipRegionId.Horizon to RegionAccent(ShipRegionId.Horizon, NhColors.PhosphorWhite),
    )

    fun forRegionId(rawId: String): RegionAccent {
        val region = ShipRegionId.fromRawId(rawId) ?: ShipRegionId.Horizon
        return catalog.getValue(region)
    }

    fun statusLine(regionName: String, status: String): String =
        "REGION: ${regionName.uppercase()} — ${status.uppercase()}"
}
