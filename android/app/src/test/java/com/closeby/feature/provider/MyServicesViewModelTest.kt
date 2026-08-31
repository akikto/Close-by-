package com.closeby.feature.provider

import com.closeby.feature.provider.data.repository.MockProviderManagementRepository
import com.closeby.feature.provider.presentation.MyServicesViewModel
import com.closeby.util.UiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MyServicesViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun loadTransitionsToSuccess() = runTest {
        val vm = MyServicesViewModel(
            providerId = "11111111-1111-1111-1111-111111111101",
            repository = MockProviderManagementRepository()
        )
        vm.load()
        assertTrue(vm.uiState.value is UiState.Success)
    }
}
