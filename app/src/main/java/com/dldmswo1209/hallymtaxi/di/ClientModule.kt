package com.dldmswo1209.hallymtaxi.di

import com.dldmswo1209.hallymtaxi.retrofit.MainServerApiClient
import com.dldmswo1209.hallymtaxi.util.Keys
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ClientModule {

    @Provides
    @Singleton
    fun provideMainServerClient() : MainServerApiClient =
        Retrofit.Builder()
            .baseUrl(Keys.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(MainServerApiClient::class.java)
}