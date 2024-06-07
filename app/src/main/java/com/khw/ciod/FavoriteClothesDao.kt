package com.khw.ciod

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteClothesDao {
    @Query(
        "SELECT * FROM Favorite WHERE category = :category"
    )
    fun getRank(category: String): Flow<List<Favorite>>

    @Insert
    fun insertAll(vararg users: Favorite)

    @Delete
    fun delete(user: Favorite)
}