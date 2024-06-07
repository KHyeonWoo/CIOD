package com.khw.ciod

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Favorite(
    @PrimaryKey(autoGenerate = true) val uid: Int = 0,
    @ColumnInfo(name = "category") val category: String?,
    @ColumnInfo(name = "filename") val fileName: String?,
)