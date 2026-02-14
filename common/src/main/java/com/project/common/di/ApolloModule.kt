package com.project.common.di

import com.apollographql.apollo3.ApolloClient
import com.project.common.repo.api.apollo.ApiService
import com.project.common.repo.api.apollo.NetworkCallRepo
import com.project.common.repo.api.apollo.helper.ApiConstants.BASE_URL
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ApolloModule {

    @Provides
    @Singleton
    fun provideApolloClient(): ApolloClient {
        return ApolloClient.Builder()
            .serverUrl(BASE_URL)
            .build()
    }

    @Provides
    @Singleton
    fun provideCountryClient(apolloClient: ApolloClient): ApiService {
        return NetworkCallRepo(apolloClient)
    }
}