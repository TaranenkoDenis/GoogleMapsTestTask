package serg.denis.taranenko.googlemapstesttask

import android.app.Application
import serg.denis.taranenko.googlemapstesttask.data.apis.GeoApi

class App: Application(){

    private lateinit var geocodeApi: GeoApi

    override fun onCreate() {
        super.onCreate()

        geocodeApi = GeoApi.Factory
                .create(getString(R.string.geocode_api_base_url))
    }

    public fun getGeocodeApi(): GeoApi{
        return geocodeApi
    }
}