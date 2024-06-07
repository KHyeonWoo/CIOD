package com.khw.ciod

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Entity
data class OOTD(
    @PrimaryKey val date: String = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")),
    @ColumnInfo(name = "top") val top: String?,
    @ColumnInfo(name = "pants") val pants: String?,
    @ColumnInfo(name = "shoes") val shoes: String?
)