package serg.denis.taranenko.googlemapstesttask.data.net.models.places

import com.google.gson.annotations.SerializedName

data class MainTextMatchedSubstring (
        @SerializedName("length")
        val length: Int,
        @SerializedName("offset")
        val offset: Int
)