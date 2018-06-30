package serg.denis.taranenko.googlemapstesttask.data.repos.geocode

import io.reactivex.Observable
import serg.denis.taranenko.googlemapstesttask.data.apis.GeoApi
import serg.denis.taranenko.googlemapstesttask.data.models.geocode.places.PlaceDetailsResult
import serg.denis.taranenko.googlemapstesttask.data.models.geocode.places.ResponseGeoPlaces
import serg.denis.taranenko.googlemapstesttask.data.models.geocode.route.ResponseRoute

class RemoteGeoRepoImpl(
        val geoApi: GeoApi) : RemoteGeoRepo{

    override fun loadVariantsForAutocomplete(
            enteredText: String, types: String,
            location: String, radius: Int
            ): Observable<ResponseGeoPlaces> = geoApi.autocomplete(
                    enteredText,
                    types,
                    location,
                    radius
            )

    override fun loadVariantsForAutocomplete(
            enteredText: String
    ): Observable<ResponseGeoPlaces> = geoApi.autocomplete(
            enteredText
    )

    override fun loadDetails(
            placeId: String
    ) = geoApi.details(placeId)

    override fun loadRoute(
            origins: String,
            destinations: String,
            mode: String
    ) = geoApi.getRoute(origins,destinations,mode)
}

interface RemoteGeoRepo{
    fun loadVariantsForAutocomplete(enteredText: String, types: String,
                                    location: String, radius: Int):
            Observable<ResponseGeoPlaces>

    fun loadVariantsForAutocomplete(enteredText: String): Observable<ResponseGeoPlaces>
    fun loadDetails(placeId: String): Observable<PlaceDetailsResult>
    fun loadRoute(origins: String, destinations: String, mode: String): Observable<ResponseRoute>
}