package serg.denis.taranenko.googlemapstesttask.data.models.geocode.route

data class GeocodedWaypoint(
    val geocoder_status: String,
    val place_id: String,
    val types: List<String>,
    val partial_match: Boolean
)