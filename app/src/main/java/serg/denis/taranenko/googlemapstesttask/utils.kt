package serg.denis.taranenko.googlemapstesttask

import android.content.Context
import android.location.Address
import android.location.Geocoder
import java.io.IOException

public fun getLocationsString(context: Context, query_text: String): List<GeoSearchResult> {

    val geo_search_results = ArrayList<GeoSearchResult>()

    val geocoder = Geocoder(context, context.resources.configuration.locale)
    var addresses: List<Address>? = null

    try {
        addresses = geocoder.getFromLocationName(query_text, 10)

        if (addresses != null) {
            for (i in addresses.indices) {
                val address = addresses[i]

                if (address.maxAddressLineIndex != -1) {
                    geo_search_results.add(GeoSearchResult(address))
                }
            }
        }

    } catch (e: IOException) {
        e.printStackTrace()
    }

    return geo_search_results
}