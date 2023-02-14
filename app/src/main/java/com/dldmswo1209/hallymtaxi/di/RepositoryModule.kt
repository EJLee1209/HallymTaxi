package com.dldmswo1209.hallymtaxi.di

import com.dldmswo1209.hallymtaxi.data.repository.AuthRepository
import com.dldmswo1209.hallymtaxi.data.repository.AuthRepositoryImpl
import com.dldmswo1209.hallymtaxi.data.repository.ServerRepository
import com.dldmswo1209.hallymtaxi.data.repository.ServerRepositoryImpl
import com.dldmswo1209.hallymtaxi.retrofit.MainServerApiClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideServerRepository(
        client: MainServerApiClient,
    ) : ServerRepository = ServerRepositoryImpl(client)

    @Provides
    @Singleton
    fun provideAuthRepository(
        auth: FirebaseAuth,
        fireStore: FirebaseFirestore
    ) : AuthRepository = AuthRepositoryImpl(auth, fireStore)
}