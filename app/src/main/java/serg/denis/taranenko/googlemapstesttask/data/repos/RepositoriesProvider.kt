package serg.denis.taranenko.googlemapstesttask.data.repos.geo

import serg.denis.taranenko.googlemapstesttask.data.net.apis.GeoApi
import serg.denis.taranenko.googlemapstesttask.data.persistance.dao.polylines.RouteDao

object RepositoriesProvider {

    fun provideRemoteGeocodeRepo(
            geocodeApi:GeoApi
    ): RemoteGeoRepo{
        return RemoteGeoRepoImpl(geocodeApi)
    }

    fun provideLocalGeocodeRepo(
            routeDao:RouteDao
    ): LocalGeoRepo{
        return LocalGeoRepoImpl(routeDao)
    }
}