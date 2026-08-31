package com.closeby.app.core.navigation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NavigationRoutesTest {

    @Test
    fun topLevelRoutesAreReachable() {
        val routes = topLevelDestinations.map { it.route }
        assertTrue(routes.contains("home"))
        assertTrue(routes.contains("explore"))
        assertTrue(routes.contains("requests"))
        assertTrue(routes.contains("notifications"))
        assertTrue(routes.contains("profile"))
    }

    @Test
    fun serviceDetailsRouteBuildsValidPath() {
        val route = AppRoutes.serviceDetails("abc-123")
        assertTrue(route.startsWith("service/"))
        assertFalse(route.contains(" "))
    }

    @Test
    fun requestDetailsRouteBuildsValidPath() {
        assertEquals("request/req-1", AppRoutes.requestDetails("req-1"))
    }
}
