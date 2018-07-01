package serg.denis.taranenko.googlemapstesttask.data.net.models.route

data class ResponseRoute(
    val geocoded_waypoints: List<GeocodedWaypoint>,
    val routes: List<Route>,
    val status: String
)