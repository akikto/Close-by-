package com.closeby.feature.servicelisting

import com.closeby.feature.servicelisting.data.local.LocalRecentlyViewedRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class LocalRecentlyViewedRepositoryTest {

    private lateinit var repository: LocalRecentlyViewedRepository

    @Before
    fun setup() {
        repository = LocalRecentlyViewedRepository(RuntimeEnvironment.getApplication())
    }

    @Test
    fun recordsViewAndLimitsHistory() = runTest {
        repeat(25) { index ->
            repository.recordView("svc-$index")
        }
        assertEquals(20, repository.getRecentlyViewed().size)
    }

    @Test
    fun duplicateViewMovesToTop() = runTest {
        repository.recordView("svc-a")
        repository.recordView("svc-b")
        repository.recordView("svc-a")
        assertEquals("svc-a", repository.getRecentlyViewed().first().serviceId)
    }
}
