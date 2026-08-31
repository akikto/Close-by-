package com.closeby.trust

import com.closeby.request.data.mock.InMemoryServiceRequestRepository
import com.closeby.trust.data.repository.MockTrustRepository
import com.closeby.trust.domain.model.ReportInput
import com.closeby.trust.domain.model.ReportReason
import com.closeby.trust.domain.model.ReportTargetType
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class MockTrustRepositoryTest {

    private lateinit var requestRepository: InMemoryServiceRequestRepository
    private lateinit var trustRepository: MockTrustRepository

    @Before
    fun setUp() {
        requestRepository = InMemoryServiceRequestRepository()
        trustRepository = MockTrustRepository(requestRepository)
    }

    @Test
    fun submitReportCreatesOpenReport() = runTest {
        val result = trustRepository.submitReport(
            reporterId = "user-1",
            input = ReportInput(
                targetType = ReportTargetType.SERVICE,
                targetId = "service-1",
                reason = ReportReason.ABUSE,
                description = "Misleading listing"
            )
        )

        assertTrue(result.isSuccess)
        val report = result.getOrThrow()
        assertEquals(ReportTargetType.SERVICE, report.targetType)
        assertEquals("service-1", report.targetId)
        assertEquals(ReportReason.ABUSE, report.reason)
    }

    @Test
    fun blockProviderHidesProviderFromBlockedList() = runTest {
        val blockerId = "user-1"
        val providerId = "11111111-1111-1111-1111-111111111101"

        assertFalse(trustRepository.isProviderBlocked(blockerId, providerId).getOrThrow())

        val blockResult = trustRepository.blockProvider(blockerId, providerId)
        assertTrue(blockResult.isSuccess)

        assertTrue(trustRepository.isProviderBlocked(blockerId, providerId).getOrThrow())
        assertEquals(
            setOf(providerId),
            trustRepository.getBlockedProviderIds(blockerId).getOrThrow()
        )

        trustRepository.unblockProvider(blockerId, providerId).getOrThrow()
        assertFalse(trustRepository.isProviderBlocked(blockerId, providerId).getOrThrow())
    }
}
