package serg.denis.taranenko.googlemapstesttask.data.net.models.route

data class GeocodedWaypoint(
    val geocoder_status: String,
    val place_id: String,
    val types: List<String>,
    val partial_match: Boolean
)