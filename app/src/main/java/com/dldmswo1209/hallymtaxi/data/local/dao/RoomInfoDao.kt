package com.dldmswo1209.hallymtaxi.data.local.dao

import androidx.room.*
import com.dldmswo1209.hallymtaxi.data.model.RoomInfo

@Dao
interface RoomInfoDao {

    @Query("SELECT * FROM room_info WHERE roomId = :roomId LIMIT 1")
    suspend fun detachRoomInfo(roomId: String) : RoomInfo

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoomInfo(vararg roomInfo: RoomInfo)

    @Update
    suspend fun updateRoomInfo(roomInfo: RoomInfo)

    @Query("SELECT * FROM room_info WHERE isActivate = :isActivate")
    suspend fun detachRoomInfoHistory(isActivate: Boolean = false) : List<RoomInfo>

}