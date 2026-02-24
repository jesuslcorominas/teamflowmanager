package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.ExportData
import com.jesuslcorominas.teamflowmanager.domain.usecase.ExportToPdfUseCase
import com.jesuslcorominas.teamflowmanager.domain.utils.PdfExporter
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class ExportToPdfUseCaseTest {
    private lateinit var pdfExporter: PdfExporter
    private lateinit var useCase: ExportToPdfUseCase

    @Before
    fun setup() {
        pdfExporter = mockk()
        useCase = ExportToPdfUseCaseImpl(pdfExporter)
    }

    @Test
    fun `invoke should delegate to pdfExporter and return file path`() = runTest {
        val exportData = mockk<ExportData>()
        val teamName = "Team A"
        val expectedPath = "/data/exports/report.pdf"
        every { pdfExporter.exportToPdf(exportData, teamName) } returns expectedPath

        val result = useCase.invoke(exportData, teamName)

        assertEquals(expectedPath, result)
        verify { pdfExporter.exportToPdf(exportData, teamName) }
    }

    @Test
    fun `invoke should return null when pdf export fails`() = runTest {
        val exportData = mockk<ExportData>()
        every { pdfExporter.exportToPdf(exportData, any()) } returns null

        val result = useCase.invoke(exportData, "Team A")

        assertNull(result)
    }
}
