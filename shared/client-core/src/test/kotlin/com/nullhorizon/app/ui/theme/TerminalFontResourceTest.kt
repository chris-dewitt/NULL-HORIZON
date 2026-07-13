package com.nullhorizon.app.ui.theme

import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TerminalFontResourceTest {
    @Test
    fun terminalFont_isOnClasspath() {
        val stream = javaClass.classLoader.getResourceAsStream("fonts/NhTerminal-Regular.ttf")
        assertNotNull("NhTerminal-Regular.ttf must be on the test classpath", stream)
        stream!!.use { bytes ->
            val header = ByteArray(4)
            assertTrue(bytes.read(header) == 4)
            // TrueType / OpenType typically starts with 0x00010000 or "OTTO"
            val isTtf = header[0] == 0.toByte() && header[1] == 1.toByte()
            val isOtto = header.contentEquals("OTTO".toByteArray())
            assertTrue("Expected TTF/OTF signature", isTtf || isOtto)
        }
    }
}
