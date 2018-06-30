package serg.denis.taranenko.googlemapstesttask.data.models.geocode.places

data class NameAndPlaceId(val name: String, val placeId: String) {

    override fun toString(): String {
        return name
    }
}