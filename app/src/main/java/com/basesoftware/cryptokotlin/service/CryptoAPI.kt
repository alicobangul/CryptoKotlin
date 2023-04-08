package com.basesoftware.cryptokotlin.service

import com.basesoftware.cryptokotlin.model.CryptoModel
import io.reactivex.Observable
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.GET

interface CryptoAPI {

    @GET("atilsamancioglu/K21-JSONDataSet/master/crypto.json")
    fun getCryptoDataRxJava() : Observable<List<CryptoModel>>

    @GET("atilsamancioglu/K21-JSONDataSet/master/crypto.json")
    suspend fun getCryptoDataCoroutines() : Response<List<CryptoModel>>

}