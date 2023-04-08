package com.basesoftware.cryptokotlin.roomdb

import androidx.room.Database
import androidx.room.RoomDatabase
import com.basesoftware.cryptokotlin.model.CryptoModel

@Database(entities = [CryptoModel::class], version = 1)
abstract class CryptoDatabase : RoomDatabase() { abstract fun cryptoDao() : CryptoDao }