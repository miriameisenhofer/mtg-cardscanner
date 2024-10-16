package com.example.mtgcardscanner

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Query
import retrofit2.http.GET

import com.example.mtgcardscanner.ScryfallCard
import retrofit2.http.Url

interface ScryfallApiInterface {
    @GET("cards/named")
    fun getCardByExactName(@Query("exact") cardName: String): Call<ScryfallCard>
    fun getCardByFuzzyName(@Query("fuzzy") cardName: String): Call<ScryfallCard>
    @GET
    fun getCardPrintings(@Url printsSearchUri: String): Call<ScryfallCardPrintingsResponse>
}