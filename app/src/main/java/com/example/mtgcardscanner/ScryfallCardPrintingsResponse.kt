package com.example.mtgcardscanner

import com.google.gson.annotations.SerializedName

data class ScryfallCardPrintingsResponse (
    @SerializedName("object") val objectType: String,
    @SerializedName("total_cards") val totalCards: Int,
    @SerializedName("data") val printings: List<ScryfallCard>
)