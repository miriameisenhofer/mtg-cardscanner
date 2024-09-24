package com.example.mtgcardscanner

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import  kotlinx.parcelize.Parcelize

@Parcelize
data class ScryfallCard (
    @SerializedName("name") val name: String,
    @SerializedName("type_line") val typeLine: String,
    @SerializedName("mana_cost") val manaCost: String,
    @SerializedName("oracle_text") val oracleText: String,
    @SerializedName("image_uris") val imageUris: ImageUris?,
    @SerializedName("prints_search_uri") val printsSearchUri: String?,
    @SerializedName("set_name") val setName: String?, // Only in printings
    @SerializedName("set") val set: String?, // Only in printings
) : Parcelable

@Parcelize
data class ImageUris(
    @SerializedName("normal") val normal: String?,
    @SerializedName("large") val large: String?
) : Parcelable