package com.closeby.app.core.di

import com.closeby.admin.data.repository.MockAdminRepository
import com.closeby.admin.data.repository.SupabaseAdminRepository
import com.closeby.admin.domain.repository.AdminRepository
import com.closeby.app.BuildConfig
import kotlinx.coroutines.runBlocking

object AdminDependenciesFactory {

    private val hasSupabase: Boolean
        get() = BuildConfig.SUPABASE_URL.isNotBlank() && BuildConfig.SUPABASE_ANON_KEY.isNotBlank()

    private val mockRepository: MockAdminRepository by lazy {
        MockAdminRepository(
            currentUserIdProvider = {
                runBlocking {
                    ProviderDependenciesFactory.authRepository().getCurrentSession()?.userId
                }
            }
        )
    }

    fun adminRepository(): AdminRepository =
        if (hasSupabase) {
            SupabaseAdminRepository()
        } else {
            mockRepository
        }

    fun mockAdminRepository(): MockAdminRepository = mockRepository
}
