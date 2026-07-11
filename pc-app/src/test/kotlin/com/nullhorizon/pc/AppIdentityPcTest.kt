package com.nullhorizon.pc

import com.google.common.truth.Truth.assertThat
import com.nullhorizon.app.AppIdentity
import org.junit.Test

class AppIdentityPcTest {
    @Test
    fun bootMessageMatchesDesktopBootCopy() {
        assertThat(AppIdentity.bootMessage(systemsOnline = false))
            .isEqualTo("Systems offline. Awaiting emergency interface.")
        assertThat(AppIdentity.bootMessage(systemsOnline = true))
            .isEqualTo("Emergency interface online.")
    }
}
