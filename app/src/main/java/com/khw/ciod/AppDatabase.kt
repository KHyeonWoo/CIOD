package com.khw.ciod

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
@Database(entities = [OOTD::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun FavoriteClothesDao(): FavoriteClothesDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return instance ?:
            Room.databaseBuilder(
                context,
                AppDatabase::class.java, "ciod.db"
            ).build()
                .also { instance = it }

        }
    }
}