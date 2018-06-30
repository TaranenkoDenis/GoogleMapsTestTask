package serg.denis.taranenko.googlemapstesttask.domain.interactors

import io.reactivex.Observable
import serg.denis.taranenko.googlemapstesttask.data.models.geocode.route.ResponseRoute
import serg.denis.taranenko.googlemapstesttask.data.repos.geocode.RemoteGeoRepo

class RouteInteractorImpl(private val remoteGeocodeRepo: RemoteGeoRepo) :
        RouteInteractor{

    override fun loadRoute(origins: String,
                           destinations: String,
                           mode: String) =
            remoteGeocodeRepo.loadRoute(origins, destinations, mode)
}

public interface RouteInteractor{

    fun loadRoute(origins: String, destinations: String, mode: String): Observable<ResponseRoute>
}