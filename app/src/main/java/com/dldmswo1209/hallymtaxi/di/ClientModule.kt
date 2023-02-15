package com.dldmswo1209.hallymtaxi.di

import android.content.Context
import com.dldmswo1209.hallymtaxi.data.database.AppDatabase
import com.dldmswo1209.hallymtaxi.retrofit.KakaoApiClient
import com.dldmswo1209.hallymtaxi.retrofit.MainServerApiClient
import com.dldmswo1209.hallymtaxi.util.Keys
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ActivityContext
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Inject
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

    @Provides
    @Singleton
    fun provideKakaoApiClient() : KakaoApiClient =
         Retrofit.Builder()
            .baseUrl(Keys.KAKAO_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(KakaoApiClient::class.java)
}