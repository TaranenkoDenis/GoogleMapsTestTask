package serg.denis.taranenko.googlemapstesttask.data.net.models.places

import com.google.gson.annotations.SerializedName

data class ResponseGeoPlaces(
        @SerializedName("predictions")
        val predictions: List<Prediction>,
        @SerializedName("status")
        val status: String
)