package com.harun.hek.roomdb

import androidx.room.Database
import androidx.room.RoomDatabase
import com.harun.hek.model.Bitki

// Virgül konulu birden fazla entitie verilebilir.
// Bitkilere yeni kolonlar eklendiğinde version 2 yapılabilir.

@Database(entities = [Bitki::class], version = 1)
abstract class BitkiDatabase : RoomDatabase(){
    abstract fun bitkiDao(): BitkiDAO
}