package com.jesuslcorominas.teamflowmanager.data.remote.firestore

import com.jesuslcorominas.teamflowmanager.domain.model.MatchPeriod
import org.junit.Assert.assertEquals
import org.junit.Test

class MatchFirestoreModelTest {

    /**
     * Sentinel test for [MatchPeriod.toPeriodUpdateMap].
     *
     * This test exists because [toPeriodUpdateMap] writes the period data to Firestore using
     * string literals as field names. A typo in a literal (e.g. "startTimeMilis") or a rename
     * of a field in [MatchPeriodFirestoreModel] without a matching change in the map produces a
     * silent divergence: the partial update would write to an unknown Firestore field, and the
     * full model's field would keep its old value. The suite would remain green because no
     * production test asserted on these key names.
     *
     * The expected key set is derived by naming the properties of [MatchPeriodFirestoreModel]
     * explicitly here. If any property is renamed in that model (or a new one is added), the
     * set must be updated — which is exactly the kind of oversight this test catches.
     */
    @Test
    fun `toPeriodUpdateMap keys match MatchPeriodFirestoreModel field names`() {
        val period = MatchPeriod(
            periodNumber = 1,
            periodDuration = 1500000L,
            startTimeMillis = 1000L,
            endTimeMillis = 2000L,
        )

        val map = period.toPeriodUpdateMap()

        // The expected key set must equal the property names of MatchPeriodFirestoreModel:
        // periodNumber, periodDuration, startTimeMillis, endTimeMillis.
        // A rename or typo in toPeriodUpdateMap will cause exactly one of these assertions to fail.
        val expectedKeys = setOf(
            "periodNumber",
            "periodDuration",
            "startTimeMillis",
            "endTimeMillis",
        )
        assertEquals("map keys must match MatchPeriodFirestoreModel field names", expectedKeys, map.keys)
    }

    @Test
    fun `toPeriodUpdateMap values match the period properties`() {
        val period = MatchPeriod(
            periodNumber = 2,
            periodDuration = 900000L,
            startTimeMillis = 5000L,
            endTimeMillis = 905000L,
        )

        val map = period.toPeriodUpdateMap()

        assertEquals(2, map["periodNumber"])
        assertEquals(900000L, map["periodDuration"])
        assertEquals(5000L, map["startTimeMillis"])
        assertEquals(905000L, map["endTimeMillis"])
    }
}
