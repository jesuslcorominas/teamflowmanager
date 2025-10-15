package com.jesuslcorominas.teamflowmanager.ui.navigation

import io.mockk.mockk
import org.junit.Assert.assertTrue
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertFalse
import org.junit.Test

class RouteTest {

    @Test
    fun `fromValue returns correct route for valid paths`() {
        assertEquals(Route.Players, Route.fromValue("players"))
        assertEquals(Route.MatchDetail, Route.fromValue("match_detail/42"))
        assertEquals(Route.Splash, Route.fromValue("splash"))
    }

    @Test
    fun `fromValue returns null for unknown or null path`() {


        assertNull(Route.fromValue("unknown"))
        assertNull(Route.fromValue(null))
    }

    @Test
    fun `createRoute returns correct path`() {
        assertEquals("splash", Route.Splash.createRoute())
        assertEquals("players", Route.Players.createRoute())
        assertEquals("matches", Route.Matches.createRoute())
    }

    @Test
    fun `uiConfig returns correct configuration for each route`() {
        val splashConfig = Route.Splash.uiConfig(null)
        assertFalse(splashConfig.showTopBar)
        assertFalse(splashConfig.showBottomBar)
        assertFalse(splashConfig.canGoBack)

        val playersConfig = Route.Players.uiConfig(null)
        assertTrue(playersConfig.showTopBar)
        assertTrue(playersConfig.showBottomBar)
        assertFalse(playersConfig.canGoBack)

        val currentMatchConfig = Route.CurrentMatch.uiConfig(null)
        assertTrue(currentMatchConfig.showTopBar)
        assertFalse(currentMatchConfig.showBottomBar)
        assertTrue(currentMatchConfig.canGoBack)
    }

    @Test
    fun `MatchDetail createRoute returns correct values`() {
        assertEquals("match_detail/123", Route.MatchDetail.createRoute(123))
        assertEquals("match_detail", Route.MatchDetail.createRoute(null))
    }

    @Test
    fun `Route all contains all expected route instances by path`() {
        val expectedPaths = setOf(
            "splash",
            "create_team",
            "players",
            "team_detail",
            "matches",
            "current_match",
            "match_detail"
        )

        val actualPaths = Route.all.map { it.createRoute() }.toSet()
        assertEquals(expectedPaths, actualPaths)
    }

    @Test
    fun `UiConfig equality and copy work as expected`() {
        val config = Route.UiConfig(
            showTopBar = true,
            showBottomBar = false,
            canGoBack = true
        )

        val copy = config.copy()
        assertEquals(config, copy)
        assertEquals(config.hashCode(), copy.hashCode())
        assertTrue(copy.toString().contains("showTopBar=true"))
    }

    @Test
    fun `mockk sanity check`() {
        val mockRoute = mockk<Route>(relaxed = true)
        assertNotNull(mockRoute)
    }
}
