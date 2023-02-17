package com.dldmswo1209.hallymtaxi.di

import com.dldmswo1209.hallymtaxi.data.database.ChatDao
import com.dldmswo1209.hallymtaxi.data.database.RoomInfoDao
import com.dldmswo1209.hallymtaxi.data.repository.*
import com.dldmswo1209.hallymtaxi.data.remote.KakaoApi
import com.dldmswo1209.hallymtaxi.data.remote.MainServerApi
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
        client: MainServerApi,
    ) : ServerRepository = ServerRepositoryImpl(client)

    @Provides
    @Singleton
    fun provideAuthRepository(
        auth: FirebaseAuth,
        fireStore: FirebaseFirestore
    ) : AuthRepository = AuthRepositoryImpl(auth, fireStore)

    @Provides
    @Singleton
    fun provideFireStoreRepository(
        fireStore: FirebaseFirestore,
        auth: FirebaseAuth
    ) : FireStoreRepository = FireStoreRepositoryImpl(fireStore, auth)

    @Provides
    @Singleton
    fun provideRoomRepository(
        chatDao: ChatDao,
        roomInfoDao: RoomInfoDao
    ) : DatabaseRepository = DatabaseRepositoryImpl(chatDao, roomInfoDao)

    @Provides
    @Singleton
    fun provideKakaoRepository(
        client: KakaoApi
    ) : KakaoRepository = KakaoRepositoryImpl(client)

}