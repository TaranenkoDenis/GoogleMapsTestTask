package serg.denis.taranenko.googlemapstesttask.data.models.geocode.places

class PlaceDetails {
    var geometry: Geometry? = null
    var name: String? = null

    override fun toString(): String {
        return "PlaceDetails{" +
                "geometry=" + geometry +
                ", name='" + name + '\''.toString() +
                '}'.toString()
    }
}