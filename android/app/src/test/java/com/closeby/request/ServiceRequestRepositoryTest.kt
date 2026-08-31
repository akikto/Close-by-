package com.closeby.request

import com.closeby.request.data.mock.InMemoryServiceRequestRepository
import com.closeby.request.domain.model.ServiceRequestStatus
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Test

class ServiceRequestRepositoryTest {

    private val repository = InMemoryServiceRequestRepository()

    @Test
    fun providerCannotAcceptAnotherProvidersRequest() = runTest {
        val result = repository.acceptRequest("req_001", "other-provider")
        assertTrue(result.isFailure)
    }

    @Test
    fun customerCanCancelOwnSessionRequest() = runTest {
        val result = repository.cancelRequest("req_001", null, "demo-session")
        assertTrue(result.isSuccess)
        val updated = repository.getRequestById("req_001").getOrThrow()
        assertTrue(updated.status == ServiceRequestStatus.CANCELLED)
    }
}
