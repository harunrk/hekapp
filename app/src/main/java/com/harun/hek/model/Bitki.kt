package com.harun.hek.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity

data class Bitki(

    @ColumnInfo(name = "isim")
    var isim: String,

    @ColumnInfo(name = "pH")
    var pH: String,

    @ColumnInfo(name = "EC")
    var EC: String,

    @ColumnInfo(name = "süre")
    var sure: String,

    @ColumnInfo(name = "nem")
    var nem: String,

    @ColumnInfo(name = "gorsel")
    var gorsel: ByteArray,

    @PrimaryKey(autoGenerate = true)
    var id : Int = 0
)