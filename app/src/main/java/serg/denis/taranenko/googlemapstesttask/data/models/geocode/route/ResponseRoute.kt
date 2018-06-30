package serg.denis.taranenko.googlemapstesttask.data.models.geocode.route

data class ResponseRoute(
    val geocoded_waypoints: List<GeocodedWaypoint>,
    val routes: List<Route>,
    val status: String
)