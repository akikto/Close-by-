package com.closeby.feature.servicelisting

import com.closeby.feature.servicelisting.domain.repository.SavedServiceRepository
import com.closeby.feature.servicelisting.presentation.viewmodel.SavedServiceToggleViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SavedServiceToggleViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private class FakeSavedRepo : SavedServiceRepository {
        private val ids = MutableStateFlow<Set<String>>(emptySet())
        override fun observeSavedServiceIds() = ids
        override suspend fun getSavedEntries() =
            emptyList<com.closeby.feature.servicelisting.domain.model.SavedServiceEntry>()
        override suspend fun save(serviceId: String) { ids.value = ids.value + serviceId }
        override suspend fun unsave(serviceId: String) { ids.value = ids.value - serviceId }
        override suspend fun isSaved(serviceId: String) = serviceId in ids.value
        override suspend fun migrateLocalToAccount(userId: String, localIds: Set<String>) = Unit
    }

    @Test
    fun toggleSaveAndUnsave() = runTest {
        val vm = SavedServiceToggleViewModel(FakeSavedRepo())
        vm.toggle("svc-1")
        advanceUntilIdle()
        assertTrue(vm.isSaved("svc-1", vm.savedIds.value))
        vm.toggle("svc-1")
        advanceUntilIdle()
        assertFalse(vm.isSaved("svc-1", vm.savedIds.value))
    }
}
