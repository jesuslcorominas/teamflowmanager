package com.jesuslcorominas.teamflowmanager.ui.club

import com.jesuslcorominas.teamflowmanager.domain.model.ClubRole
import teamflowmanager.shared_ui.generated.resources.Res
import teamflowmanager.shared_ui.generated.resources.club_role_coach
import teamflowmanager.shared_ui.generated.resources.club_role_president
import teamflowmanager.shared_ui.generated.resources.club_role_staff
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ClubRoleLabelTest {
    @Test
    fun `president role name maps to president string`() {
        assertEquals(Res.string.club_role_president, clubRoleLabelResource(ClubRole.PRESIDENT.roleName))
    }

    @Test
    fun `coach role name maps to coach string`() {
        assertEquals(Res.string.club_role_coach, clubRoleLabelResource(ClubRole.COACH.roleName))
    }

    @Test
    fun `staff role name maps to staff string`() {
        assertEquals(Res.string.club_role_staff, clubRoleLabelResource(ClubRole.STAFF.roleName))
    }

    @Test
    fun `role name matching is case insensitive`() {
        assertEquals(Res.string.club_role_coach, clubRoleLabelResource("coach"))
    }

    @Test
    fun `unknown role name maps to null so the raw value is shown`() {
        assertNull(clubRoleLabelResource("Delegado"))
    }
}
