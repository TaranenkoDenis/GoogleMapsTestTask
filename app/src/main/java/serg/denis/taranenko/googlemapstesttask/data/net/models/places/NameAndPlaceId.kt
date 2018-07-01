package serg.denis.taranenko.googlemapstesttask.data.net.models.places

data class NameAndPlaceId(val name: String, val placeId: String) {

    override fun toString(): String {
        return name
    }
}