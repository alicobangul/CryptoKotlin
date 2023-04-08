package com.basesoftware.cryptokotlin.model

import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "Crypto")
data class CryptoModel(
    @PrimaryKey
    @ColumnInfo(name = "currency")
    @SerializedName("currency")
    @NonNull var currency: String,

    @ColumnInfo(name = "price")
    @SerializedName("price")
    var price: String
)
