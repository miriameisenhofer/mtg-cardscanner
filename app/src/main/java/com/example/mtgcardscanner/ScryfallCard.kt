package com.example.mtgcardscanner

import com.google.gson.annotations.SerializedName

data class ScryfallCard (
    @SerializedName("name") val name: String,
    @SerializedName("type_line") val typeLine: String,
    @SerializedName("mana_cost") val manaCost: String,
    @SerializedName("oracle_text") val oracleText: String,
    @SerializedName("image_uris") val imageUris: ImageUris?
)

data class ImageUris(
    @SerializedName("normal") val normal: String
)