package serg.denis.taranenko.googlemapstesttask.data.net.models.places

import com.google.gson.annotations.SerializedName

data class Term (
        @SerializedName("offset")
        val offset: Int,
        @SerializedName("value")
        val value: String
)