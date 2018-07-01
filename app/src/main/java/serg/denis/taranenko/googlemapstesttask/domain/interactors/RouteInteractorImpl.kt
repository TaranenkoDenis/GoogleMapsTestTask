package serg.denis.taranenko.googlemapstesttask.domain.interactors

import android.util.Log
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Action
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import serg.denis.taranenko.googlemapstesttask.TAG
import serg.denis.taranenko.googlemapstesttask.data.net.models.route.ResponseRoute
import serg.denis.taranenko.googlemapstesttask.data.persistance.models.Route
import serg.denis.taranenko.googlemapstesttask.data.repos.geo.LocalGeoRepo
import serg.denis.taranenko.googlemapstesttask.data.repos.geo.RemoteGeoRepo

class RouteInteractorImpl(
        private val remoteGeocodeRepo: RemoteGeoRepo,
        private val localGeoRepo: LocalGeoRepo
) :
        RouteInteractor{

    override fun printContentOfDB() {
        loadRoutesFromDB()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        Consumer {
                            it.forEach {
                                Log.d(TAG, it.toString())
                            }
                        }
                )
    }

    override fun loadRoute(origins: String,
                           destinations: String) =
            remoteGeocodeRepo.loadRoute(origins, destinations)

    override fun loadRoutesFromDB(): Single<List<Route>> {
        return localGeoRepo.getAllRoutes()
    }

    override fun saveRoute(route: Route): Completable {
        return localGeoRepo.insertRoute(route)
    }
}

public interface RouteInteractor{
    fun loadRoute(origins: String, destinations: String): Observable<ResponseRoute>
    fun loadRoutesFromDB(): Single<List<Route>>
    fun saveRoute(route: Route): Completable
    fun printContentOfDB();
}