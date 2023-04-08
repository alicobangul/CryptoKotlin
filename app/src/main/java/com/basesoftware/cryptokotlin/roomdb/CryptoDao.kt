package com.basesoftware.cryptokotlin.roomdb

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.basesoftware.cryptokotlin.model.CryptoModel
import io.reactivex.Completable
import io.reactivex.Observable

@Dao
interface CryptoDao {

    @Query("SELECT * FROM Crypto")
    fun getAllDataRxJava() : Observable<List<CryptoModel>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertRxJava(crypto : CryptoModel) : Completable

    @Query("SELECT * FROM Crypto")
    fun getAllDataCoroutines() : List<CryptoModel>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertCoroutines(crypto : CryptoModel)

}