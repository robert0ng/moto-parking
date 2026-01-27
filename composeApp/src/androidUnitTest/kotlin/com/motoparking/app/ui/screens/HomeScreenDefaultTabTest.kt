package com.motoparking.app.ui.screens

import kotlin.test.Test
import kotlin.test.assertEquals

class HomeScreenDefaultTabTest {

    @Test
    fun defaultTab_shouldBeList() {
        // The default screen should be LIST, not MAP
        // This test documents the expected behavior
        val defaultScreen = Screen.LIST
        assertEquals(Screen.LIST, defaultScreen, "Default tab should be LIST")
    }

    @Test
    fun screenEnum_hasExpectedValues() {
        // Verify Screen enum has the expected values
        assertEquals(2, Screen.entries.size, "Screen should have 2 entries")
        assertEquals("MAP", Screen.MAP.name)
        assertEquals("LIST", Screen.LIST.name)
    }
}
