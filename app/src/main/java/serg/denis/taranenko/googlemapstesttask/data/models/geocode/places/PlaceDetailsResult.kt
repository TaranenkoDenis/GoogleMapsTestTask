package serg.denis.taranenko.googlemapstesttask.data.models.geocode.places

import serg.denis.taranenko.googlemapstesttask.data.models.geocode.places.PlaceDetails

class PlaceDetailsResult {
    var result: PlaceDetails? = null

    override fun toString(): String {
        return "PlaceDetailsResult{" +
                "result=" + result +
                '}'.toString()
    }
}