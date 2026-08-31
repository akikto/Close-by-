package com.closeby.app.data.auth

import com.closeby.app.domain.auth.AuthState
import com.closeby.feature.provider.presentation.AccountAuthViewModel
import com.closeby.feature.provider.data.repository.MockProviderManagementRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AccountAuthViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private lateinit var authRepository: MockAuthRepository
    private lateinit var viewModel: AccountAuthViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(dispatcher)
        authRepository = MockAuthRepository()
        viewModel = AccountAuthViewModel(authRepository, MockProviderManagementRepository())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun startsSignedOut() = runTest {
        advanceUntilIdle()
        assertTrue(viewModel.authState.value is AuthState.SignedOut)
    }

    @Test
    fun invalidEmailSetsError() = runTest {
        viewModel.sendOtp("bad-email")
        advanceUntilIdle()
        assertTrue(viewModel.authState.value is AuthState.Error)
    }

    @Test
    fun otpRequestTransitionsState() = runTest {
        viewModel.sendOtp("provider@example.com")
        advanceUntilIdle()
        assertTrue(viewModel.authState.value is AuthState.OtpRequested)
    }

    @Test
    fun signOutReturnsToSignedOut() = runTest {
        viewModel.sendOtp("provider@example.com")
        advanceUntilIdle()
        viewModel.verifyOtp("123456")
        advanceUntilIdle()
        viewModel.signOut()
        advanceUntilIdle()
        assertTrue(viewModel.authState.value is AuthState.SignedOut)
    }

    @Test
    fun accountDeletionRequiresSignIn() = runTest {
        viewModel.requestAccountDeletion()
        advanceUntilIdle()
        assertTrue(viewModel.authState.value is AuthState.SignedOut || viewModel.authState.value is AuthState.Error)
    }
}
