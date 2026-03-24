package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.MatchReportData
import com.jesuslcorominas.teamflowmanager.domain.usecase.ExportMatchReportToPdfUseCase
import com.jesuslcorominas.teamflowmanager.domain.utils.MatchReportPdfExporter
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class ExportMatchReportToPdfUseCaseTest {
    private lateinit var matchReportPdfExporter: MatchReportPdfExporter
    private lateinit var useCase: ExportMatchReportToPdfUseCase

    @Before
    fun setup() {
        matchReportPdfExporter = mockk()
        useCase = ExportMatchReportToPdfUseCaseImpl(matchReportPdfExporter)
    }

    @Test
    fun `invoke should delegate to matchReportPdfExporter and return file path`() = runTest {
        val matchReportData = mockk<MatchReportData>()
        val expectedPath = "/data/exports/match_report.pdf"
        every { matchReportPdfExporter.exportMatchReportToPdf(matchReportData) } returns expectedPath

        val result = useCase.invoke(matchReportData)

        assertEquals(expectedPath, result)
        verify { matchReportPdfExporter.exportMatchReportToPdf(matchReportData) }
    }

    @Test
    fun `invoke should return null when export fails`() = runTest {
        val matchReportData = mockk<MatchReportData>()
        every { matchReportPdfExporter.exportMatchReportToPdf(matchReportData) } returns null

        val result = useCase.invoke(matchReportData)

        assertNull(result)
    }
}
