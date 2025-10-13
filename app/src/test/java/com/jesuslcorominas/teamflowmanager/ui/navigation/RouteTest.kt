//package com.jesuslcorominas.teamflowmanager.ui.navigation
//
//import com.jesuslcorominas.teamflowmanager.R
//import org.junit.Assert.assertEquals
//import org.junit.Assert.assertFalse
//import org.junit.Assert.assertNotNull
//import org.junit.Assert.assertNull
//import org.junit.Assert.assertTrue
//import org.junit.Test
//
//class RouteTest {
//
//    @Test
//    fun `Splash route should have correct configuration`() {
//        // Given
//        val route = Route.Splash
//
//        // Then
//        assertEquals("splash", route.path)
//        assertNull(route.icon)
//        assertNull(route.label)
//        assertFalse(route.showTopBar)
//        assertFalse(route.showBottomBar)
//        assertFalse(route.canGoBack)
//    }
//
//    @Test
//    fun `CreateTeam route should have correct configuration`() {
//        // Given
//        val route = Route.CreateTeam
//
//        // Then
//        assertEquals("create_team", route.path)
//        assertNull(route.icon)
//        assertNull(route.label)
//        assertFalse(route.showTopBar)
//        assertFalse(route.showBottomBar)
//        assertFalse(route.canGoBack)
//    }
//
//    @Test
//    fun `Players route should show in bottom bar`() {
//        // Given
//        val route = Route.Players
//
//        // Then
//        assertEquals("players", route.path)
//        assertNotNull(route.icon)
//        assertEquals(R.string.nav_players, route.label)
//        assertTrue(route.showTopBar)
//        assertTrue(route.showBottomBar)
//        assertFalse(route.canGoBack)
//    }
//
//    @Test
//    fun `TeamDetail route should show in bottom bar`() {
//        // Given
//        val route = Route.TeamDetail
//
//        // Then
//        assertEquals("team_detail", route.path)
//        assertNotNull(route.icon)
//        assertEquals(R.string.nav_team, route.label)
//        assertTrue(route.showTopBar)
//        assertTrue(route.showBottomBar)
//        assertFalse(route.canGoBack)
//    }
//
//    @Test
//    fun `Matches route should show in bottom bar`() {
//        // Given
//        val route = Route.Matches
//
//        // Then
//        assertEquals("matches", route.path)
//        assertNotNull(route.icon)
//        assertEquals(R.string.nav_matches, route.label)
//        assertTrue(route.showTopBar)
//        assertTrue(route.showBottomBar)
//        assertFalse(route.canGoBack)
//    }
//
//    @Test
//    fun `CurrentMatch route should not show in bottom bar`() {
//        // Given
//        val route = Route.CurrentMatch
//
//        // Then
//        assertEquals("current_match", route.path)
//        assertNull(route.icon)
//        assertNull(route.label)
//        assertTrue(route.showTopBar)
//        assertFalse(route.showBottomBar)
//        assertTrue(route.canGoBack)
//    }
//
//    @Test
//    fun `MatchDetail route should not show in bottom bar`() {
//        // Given
//        val route = Route.MatchDetail
//
//        // Then
//        assertEquals("match_detail", route.path)
//        assertNull(route.icon)
//        assertNull(route.label)
//        assertTrue(route.showTopBar)
//        assertFalse(route.showBottomBar)
//        assertTrue(route.canGoBack)
//    }
//
//    @Test
//    fun `fromValue should return correct route for valid path`() {
//        // When
//        val playersRoute = Route.fromValue("players")
//        val teamDetailRoute = Route.fromValue("team_detail")
//        val matchesRoute = Route.fromValue("matches")
//
//        // Then
//        assertEquals(Route.Players, playersRoute)
//        assertEquals(Route.TeamDetail, teamDetailRoute)
//        assertEquals(Route.Matches, matchesRoute)
//    }
//
//    @Test
//    fun `fromValue should handle path with parameters`() {
//        // When
//        val matchDetailRoute = Route.fromValue("match_detail/123")
//
//        // Then
//        assertEquals(Route.MatchDetail, matchDetailRoute)
//    }
//
//    @Test
//    fun `fromValue should return null for invalid path`() {
//        // When
//        val route = Route.fromValue("invalid_path")
//
//        // Then
//        assertNull(route)
//    }
//
//    @Test
//    fun `fromValue should return null for null path`() {
//        // When
//        val route = Route.fromValue(null)
//
//        // Then
//        assertNull(route)
//    }
//
//    @Test
//    fun `createRoute should return correct route for Splash`() {
//        // When
//        val routePath = Route.Splash.createRoute()
//
//        // Then
//        assertEquals("splash", routePath)
//    }
//
//    @Test
//    fun `createRoute should return correct route for Players`() {
//        // When
//        val routePath = Route.Players.createRoute()
//
//        // Then
//        assertEquals("players", routePath)
//    }
//
//    @Test
//    fun `MatchDetail createRoute with matchId should include id in path`() {
//        // Given
//        val matchId = 123L
//
//        // When
//        val routePath = Route.MatchDetail.createRoute(matchId)
//
//        // Then
//        assertEquals("match_detail/123", routePath)
//    }
//
//    @Test
//    fun `MatchDetail createRoute with null matchId should return base path`() {
//        // When
//        val routePath = Route.MatchDetail.createRoute(null)
//
//        // Then
//        assertEquals("match_detail", routePath)
//    }
//
//    @Test
//    fun `uiConfig should return correct configuration`() {
//        // Given
//        val route = Route.Players
//
//        // When
//        val config = route.uiConfig(null)
//
//        // Then
//        assertTrue(config.showTopBar)
//        assertTrue(config.showBottomBar)
//        assertFalse(config.canGoBack)
//    }
//
//    @Test
//    fun `all routes should be accessible`() {
//        // When
//        val allRoutes = Route.all
//
//        // Then
//        assertEquals(7, allRoutes.size)
//        assertTrue(allRoutes.contains(Route.Splash))
//        assertTrue(allRoutes.contains(Route.CreateTeam))
//        assertTrue(allRoutes.contains(Route.Players))
//        assertTrue(allRoutes.contains(Route.TeamDetail))
//        assertTrue(allRoutes.contains(Route.Matches))
//        assertTrue(allRoutes.contains(Route.CurrentMatch))
//        assertTrue(allRoutes.contains(Route.MatchDetail))
//    }
//}

