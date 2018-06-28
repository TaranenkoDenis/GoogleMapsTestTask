package serg.denis.taranenko.googlemapstesttask.testActivityTwo

import android.location.Address
import android.util.Log

class GeoSearchResult(private val address: Address) {

    public fun getAddress():String{
        var displayAddress = ""

        displayAddress += address.getAddressLine(0) + "\n"

        for (i in 1 until address.maxAddressLineIndex){
            Log.d("GeoSearchResult", "getAddress()")
            displayAddress += address.getAddressLine(i) + ", "
        }

        return displayAddress
    }

    override fun toString(): String {
        var display_address = ""

        if (address.featureName != null) {
            display_address += address.toString() + ", "
        }

        for (i in 0 until address.maxAddressLineIndex) {
            display_address += address.getAddressLine(i)
        }

        return display_address
    }
}