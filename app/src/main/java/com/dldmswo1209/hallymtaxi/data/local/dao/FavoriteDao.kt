package com.dldmswo1209.hallymtaxi.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.dldmswo1209.hallymtaxi.data.model.Place

@Dao
interface FavoriteDao {

    @Query("SELECT * FROM favorite")
    fun getFavorites() : List<Place>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveFavorite(place: Place)

    @Delete
    suspend fun deleteFavorite(place: Place)
}