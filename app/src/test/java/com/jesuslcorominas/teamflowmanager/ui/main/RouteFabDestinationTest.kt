package com.jesuslcorominas.teamflowmanager.ui.main

import com.jesuslcorominas.teamflowmanager.ui.navigation.Route
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * The FAB is the only way into the player wizard, so what it navigates to is worth pinning: it used
 * to pass `0L`, a placeholder left over from when ids were Longs, which opened the wizard in edit
 * mode for a player that does not exist.
 */
class RouteFabDestinationTest {
    @Test
    fun `the players FAB opens the wizard with no player id`() {
        assertEquals(Route.PlayerWizard.createRoute(), Route.Players.toDestination())
    }

    @Test
    fun `no FAB destination carries a numeric placeholder id`() {
        val destinations = Route.all.mapNotNull { it.toDestination() }

        assertEquals(emptyList<String>(), destinations.filter { it.substringAfterLast('/').toLongOrNull() != null })
    }

    @Test
    fun `routes without a FAB have no destination`() {
        assertNull(Route.Settings.toDestination())
        assertNull(Route.Splash.toDestination())
    }
}
