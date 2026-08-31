package com.closeby.admin

import com.closeby.admin.data.repository.MockAdminRepository
import com.closeby.admin.domain.model.AdminAccessDeniedException
import com.closeby.admin.domain.model.AdminAction
import com.closeby.trust.domain.model.ReportStatus
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class MockAdminRepositoryTest {

    private lateinit var repository: MockAdminRepository

    @Before
    fun setUp() {
        repository = MockAdminRepository(currentUserIdProvider = { CURRENT_USER })
    }

    @Test
    fun nonAdminIsRejectedForDashboardStats() = runTest {
        val result = repository.getDashboardStats()
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is AdminAccessDeniedException)
    }

    @Test
    fun nonAdminCannotCreateAuditLog() = runTest {
        val result = repository.logAudit(
            action = AdminAction.SUSPEND_USER,
            targetType = "USER",
            targetId = "demo-user",
            reason = "test"
        )
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is AdminAccessDeniedException)
        assertTrue(repository.auditLogs().isEmpty())
    }

    @Test
    fun adminCanReadDashboardStats() = runTest {
        repository.setUserAdmin(CURRENT_USER, isAdmin = true)

        val stats = repository.getDashboardStats().getOrThrow()
        assertTrue(stats.totalProviders >= 1)
        assertTrue(stats.pendingVerifications >= 1)
        assertTrue(stats.openReports >= 1)
        assertTrue(stats.pendingAdvertisements >= 1)
    }

    @Test
    fun adminActionCreatesAuditLog() = runTest {
        repository.setUserAdmin(CURRENT_USER, isAdmin = true)

        repository.suspendUser("demo-user", reason = "Policy violation").getOrThrow()

        val logs = repository.auditLogs()
        assertEquals(1, logs.size)
        assertEquals(AdminAction.SUSPEND_USER, logs.first().action)
        assertEquals("USER", logs.first().targetType)
        assertEquals("demo-user", logs.first().targetId)
        assertEquals(CURRENT_USER, logs.first().adminId)
    }

    @Test
    fun isAdminReadsFromStoredProfileNotHardcoded() = runTest {
        assertFalse(repository.isAdmin(CURRENT_USER))
        repository.setUserAdmin(CURRENT_USER, isAdmin = true)
        assertTrue(repository.isAdmin(CURRENT_USER))
    }

    @Test
    fun adminCanResolveReport() = runTest {
        repository.setUserAdmin(CURRENT_USER, isAdmin = true)

        repository.updateReportStatus("report-001", ReportStatus.RESOLVED, note = "Handled")
            .getOrThrow()

        val report = repository.listReports().getOrThrow().first { it.id == "report-001" }
        assertEquals(ReportStatus.RESOLVED, report.status)
    }

    companion object {
        private const val CURRENT_USER = "admin-user-test"
    }
}
