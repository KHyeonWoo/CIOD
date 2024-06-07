package com.khw.ciod

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteClothesDao {
    @Query(
        "SELECT * FROM OOTD WHERE date = :today"
    )
    fun getOOTD(today: String): OOTD

    @Insert
    fun insertAll(vararg favoriteClothes: OOTD)

    @Delete
    fun delete(user: OOTD)

}