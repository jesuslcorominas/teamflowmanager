package com.jesuslcorominas.teamflowmanager.data.local.datasource

import android.content.Context
import android.content.SharedPreferences
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class PendingSubstitutionsLocalDataSourceImplTest {

    // SharedPreferences.Editor is a builder — relaxed mock justified to handle
    // the fluent chain (putString/remove return the editor itself, apply() is void)
    private val mockEditor: SharedPreferences.Editor = mockk(relaxed = true)
    private val mockSharedPreferences: SharedPreferences = mockk()
    private val mockContext: Context = mockk()

    private lateinit var dataSource: PendingSubstitutionsLocalDataSourceImpl

    @Before
    fun setup() {
        every { mockContext.getSharedPreferences(any(), any()) } returns mockSharedPreferences
        every { mockSharedPreferences.edit() } returns mockEditor
        every { mockEditor.putString(any(), any()) } returns mockEditor
        every { mockEditor.remove(any()) } returns mockEditor

        dataSource = PendingSubstitutionsLocalDataSourceImpl(mockContext)
    }

    @Test
    fun `givenNoValueStored_whenGetRaw_thenReturnsNull`() {
        // Given
        every { mockSharedPreferences.getString("pending_substitutions_match-1", null) } returns null

        // When
        val result = dataSource.getRaw("match-1")

        // Then
        assertNull(result)
    }

    @Test
    fun `givenValueStored_whenGetRaw_thenReturnsStoredJson`() {
        // Given
        val json = """[{"out":"p1","in":"p2"}]"""
        every { mockSharedPreferences.getString("pending_substitutions_match-1", null) } returns json

        // When
        val result = dataSource.getRaw("match-1")

        // Then
        assertEquals(json, result)
    }

    @Test
    fun `givenRawJson_whenSetRaw_thenStoresUnderPrefixedKey`() {
        // Given
        val json = """[{"out":"p1","in":"p2"}]"""

        // When
        dataSource.setRaw("match-1", json)

        // Then
        verify { mockEditor.putString("pending_substitutions_match-1", json) }
        verify { mockEditor.apply() }
    }

    @Test
    fun `givenNullRaw_whenSetRaw_thenRemovesKey`() {
        // Given a null payload meaning "no pending substitutions"
        // When
        dataSource.setRaw("match-1", null)

        // Then
        verify { mockEditor.remove("pending_substitutions_match-1") }
        verify { mockEditor.apply() }
    }

    @Test
    fun `givenTwoMatchIds_whenSetRaw_thenUsesDifferentKeys`() {
        // Given
        val firstJson = """[{"out":"p1","in":"p2"}]"""
        val secondJson = """[{"out":"p3","in":"p4"}]"""

        // When
        dataSource.setRaw("match-1", firstJson)
        dataSource.setRaw("match-2", secondJson)

        // Then
        verify { mockEditor.putString("pending_substitutions_match-1", firstJson) }
        verify { mockEditor.putString("pending_substitutions_match-2", secondJson) }
    }

    @Test
    fun `whenCreated_thenUsesOwnPreferencesFile`() {
        // Given the data source created in setup()
        // When / Then
        verify {
            mockContext.getSharedPreferences("teamflowmanager_pending_substitutions", Context.MODE_PRIVATE)
        }
    }
}
