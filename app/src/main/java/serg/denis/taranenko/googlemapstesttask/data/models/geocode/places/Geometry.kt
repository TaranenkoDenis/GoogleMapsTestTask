package serg.denis.taranenko.googlemapstesttask.data.models.geocode.places


class Geometry {
    var location: Location? = null

    override fun toString(): String {
        return "Geometry{" +
                "location=" + location +
                '}'.toString()
    }
}