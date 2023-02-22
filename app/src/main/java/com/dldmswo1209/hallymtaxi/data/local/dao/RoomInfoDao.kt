package com.dldmswo1209.hallymtaxi.data.local.dao

import androidx.room.*
import com.dldmswo1209.hallymtaxi.data.model.RoomInfo

@Dao
interface RoomInfoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertRoomInfo(vararg roomInfo: RoomInfo)

    @Query("SELECT * FROM room_info WHERE roomId = :roomId LIMIT 1")
    fun detachRoomInfo(roomId: String) : RoomInfo

    @Update
    fun updateRoomInfo(roomInfo: RoomInfo)

    @Query("SELECT * FROM room_info WHERE isActivate = :isActivate")
    fun detachRoomInfoHistory(isActivate: Boolean = false) : List<RoomInfo>

}