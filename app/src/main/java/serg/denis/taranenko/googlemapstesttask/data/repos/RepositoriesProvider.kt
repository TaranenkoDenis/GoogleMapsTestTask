package serg.denis.taranenko.googlemapstesttask.data.repos.geocode

import serg.denis.taranenko.googlemapstesttask.data.apis.GeoApi

object RepositoriesProvider {

    fun provideRemoteGeocodeRepo(
            geocodeApi:GeoApi
    ): RemoteGeoRepo{
        return RemoteGeoRepoImpl(geocodeApi)
    }
}