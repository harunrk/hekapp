package com.harun.hek.roomdb

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.harun.hek.model.Bitki
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Flowable

@Dao
interface BitkiDAO {
    @Query("SELECT * FROM Bitki")
    fun getAll() : Flowable<List<Bitki>>

    @Query("SELECT * FROM Bitki WHERE id = :id")
    fun findById(id : Int) : Flowable<Bitki>

    @Insert
    fun insert(bitki: Bitki) : Completable

    @Delete
    fun delete(bitki: Bitki) : Completable

    @Update
    fun update(bitki: Bitki) : Completable
}