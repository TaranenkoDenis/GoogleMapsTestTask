package serg.denis.taranenko.googlemapstesttask.data.models.geocode.places

class Location {
    var lat: Double = 0.toDouble()
    var lng: Double = 0.toDouble()

    override fun toString(): String {
        return "Location{" +
                "lat=" + lat +
                ", lng=" + lng +
                '}'.toString()
    }
}