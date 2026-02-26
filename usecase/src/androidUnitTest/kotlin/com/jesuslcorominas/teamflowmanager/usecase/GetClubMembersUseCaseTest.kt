package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.ClubMember
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetClubMembersUseCase
import com.jesuslcorominas.teamflowmanager.usecase.repository.ClubMemberRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class GetClubMembersUseCaseTest {
    private lateinit var clubMemberRepository: ClubMemberRepository
    private lateinit var useCase: GetClubMembersUseCase

    @Before
    fun setup() {
        clubMemberRepository = mockk()
        useCase = GetClubMembersUseCaseImpl(clubMemberRepository)
    }

    @Test
    fun `invoke should return club members for valid club id`() = runTest {
        val clubId = "club123"
        val members = listOf(
            ClubMember(id = 1L, userId = "user1", name = "Alice", email = "alice@test.com", clubId = 10L, roles = listOf("PRESIDENT")),
            ClubMember(id = 2L, userId = "user2", name = "Bob", email = "bob@test.com", clubId = 10L, roles = listOf("COACH")),
        )
        every { clubMemberRepository.getClubMembers(clubId) } returns flowOf(members)

        val result = useCase.invoke(clubId).first()

        assertEquals(2, result.size)
        assertEquals("Alice", result[0].name)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `invoke should throw when clubFirestoreId is blank`() {
        useCase.invoke("")
    }

    @Test(expected = IllegalArgumentException::class)
    fun `invoke should throw when clubFirestoreId is whitespace`() {
        useCase.invoke("   ")
    }
}
