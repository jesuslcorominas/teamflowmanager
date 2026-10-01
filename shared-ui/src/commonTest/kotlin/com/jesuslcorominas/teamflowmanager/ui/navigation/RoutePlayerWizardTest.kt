package com.jesuslcorominas.teamflowmanager.ui.navigation

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * The wizard decides between "create" and "edit" from the id it receives, so the route has to be
 * able to carry no id at all. Routing create through a path argument forced a placeholder, and the
 * placeholder left behind by the Long→String id migration (`player_wizard/0`) sent the wizard
 * looking for a player that does not exist — which bounced the user back to the list in silence.
 */
class RoutePlayerWizardTest {
    @Test
    fun `create carries no player id`() {
        assertEquals("player_wizard", Route.PlayerWizard.createRoute())
        assertEquals("player_wizard", Route.PlayerWizard.createRoute(""))
    }

    @Test
    fun `edit carries the player id as a query argument`() {
        assertEquals("player_wizard?playerId=abc123", Route.PlayerWizard.createRoute("abc123"))
    }

    @Test
    fun `the route pattern declares the id as optional`() {
        assertEquals("player_wizard?playerId={playerId}", Route.PlayerWizard.FULL_ROUTE)
    }

    @Test
    fun `both forms resolve back to the wizard route`() {
        assertEquals(Route.PlayerWizard, Route.fromValue(Route.PlayerWizard.createRoute()))
        assertEquals(Route.PlayerWizard, Route.fromValue(Route.PlayerWizard.createRoute("abc123")))
        assertEquals(Route.PlayerWizard, Route.fromValue(Route.PlayerWizard.FULL_ROUTE))
    }
}
