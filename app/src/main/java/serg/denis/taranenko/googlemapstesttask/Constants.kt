package serg.denis.taranenko.googlemapstesttask

const val TAG = "MyTAg"
const val API_KEY = "AIzaSyBC6hRMA8K9gjeQfrh_6kCUKLdldEBFtnk"
val DELAY_FOR_AUTOCOMPLETE_IN_MILLIS = 500L
val SPEED_OF_CAR = 100L
val MIN_LENGTH_TO_START_AUTOCOMPLETE = 3
val MAX_PLACES_ON_MAP = 5
val DEFAULT_ZOOM = 10f


enum class TypeOfElementInPlacesList{
    FIRST_ELEMENT,
    LAST_ELEMENT,
    INTERMEDIATE_ELEMENT
}