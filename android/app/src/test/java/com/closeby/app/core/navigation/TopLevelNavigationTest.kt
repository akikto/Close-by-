package com.closeby.app.core.navigation

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TopLevelNavigationTest {

    @Test
    fun topLevelRoutesAreRecognized() {
        assertTrue(isTopLevelRoute("home"))
        assertTrue(isTopLevelRoute("profile"))
    }

    @Test
    fun detailRoutesAreNotTopLevel() {
        assertFalse(isTopLevelRoute("service/{serviceId}"))
        assertFalse(isTopLevelRoute("saved-services"))
        assertFalse(isTopLevelRoute("admin"))
    }
}
