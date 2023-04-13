package com.dldmswo1209.hallymtaxi.di

import android.content.Context
import com.dldmswo1209.hallymtaxi.data.local.AppDatabase
import com.dldmswo1209.hallymtaxi.data.local.dao.ChatDao
import com.dldmswo1209.hallymtaxi.data.local.dao.FavoriteDao
import com.dldmswo1209.hallymtaxi.data.local.dao.RoomInfoDao
import com.dldmswo1209.hallymtaxi.data.repository.*
import com.dldmswo1209.hallymtaxi.data.remote.KakaoApi
import com.dldmswo1209.hallymtaxi.data.remote.MainServerApi
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.crashlytics.internal.common.AppData
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
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
        fireStore: FirebaseFirestore,
        @ApplicationContext context: Context
    ) : AuthRepository = AuthRepositoryImpl(auth, fireStore, context)

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
        roomInfoDao: RoomInfoDao,
        favoriteDao: FavoriteDao,
        db: AppDatabase
    ) : DatabaseRepository = DatabaseRepositoryImpl(chatDao, roomInfoDao, favoriteDao, db)

    @Provides
    @Singleton
    fun provideKakaoRepository(
        client: KakaoApi
    ) : KakaoRepository = KakaoRepositoryImpl(client)

    @Provides
    @Singleton
    fun provideInAppUpdateRepository(
        @ApplicationContext context: Context
    ) : InAppRepository = InAppRepositoryImpl(context)

}