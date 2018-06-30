package serg.denis.taranenko.googlemapstesttask.data.models.geocode.places

import com.google.gson.annotations.SerializedName
import serg.denis.taranenko.googlemapstesttask.data.models.geocode.places.Prediction

data class ResponseGeoPlaces(
        @SerializedName("predictions")
        val predictions: List<Prediction>,
        @SerializedName("status")
        val status: String
)