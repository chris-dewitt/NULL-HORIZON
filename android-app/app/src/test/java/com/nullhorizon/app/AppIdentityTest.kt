package com.nullhorizon.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class AppIdentityTest {
    @Test
    fun displayName_isBrand() {
        assertEquals("NULL HORIZON", AppIdentity.DISPLAY_NAME)
    }

    @Test
    fun bootMessage_offline_mentionsEmergencyInterface() {
        val message = AppIdentity.bootMessage(systemsOnline = false)
        assertEquals("Systems offline. Awaiting emergency interface.", message)
        assertFalse(message.contains("account", ignoreCase = true))
    }

    @Test
    fun bootMessage_online_isDeterministic() {
        assertEquals(
            "Emergency interface online.",
            AppIdentity.bootMessage(systemsOnline = true),
        )
    }
}
