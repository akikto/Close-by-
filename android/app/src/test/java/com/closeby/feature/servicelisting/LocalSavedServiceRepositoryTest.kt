package com.closeby.feature.servicelisting

import com.closeby.feature.servicelisting.data.local.LocalSavedServiceRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class LocalSavedServiceRepositoryTest {

    private lateinit var repository: LocalSavedServiceRepository

    @Before
    fun setup() {
        repository = LocalSavedServiceRepository(RuntimeEnvironment.getApplication())
    }

    @Test
    fun saveAndUnsaveService() = runTest {
        repository.save("svc-1")
        assertTrue(repository.isSaved("svc-1"))
        repository.unsave("svc-1")
        assertFalse(repository.isSaved("svc-1"))
    }

    @Test
    fun duplicateSaveIsIdempotent() = runTest {
        repository.save("svc-1")
        repository.save("svc-1")
        assertTrue(repository.getSavedEntries().size == 1)
    }
}
