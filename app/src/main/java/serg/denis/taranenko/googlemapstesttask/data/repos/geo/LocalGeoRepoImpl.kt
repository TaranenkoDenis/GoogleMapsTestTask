package serg.denis.taranenko.googlemapstesttask.data.repos.geo

import io.reactivex.Completable
import io.reactivex.Single
import serg.denis.taranenko.googlemapstesttask.data.persistance.dao.polylines.RouteDao
import serg.denis.taranenko.googlemapstesttask.data.persistance.models.Route

class LocalGeoRepoImpl(private val routeDao: RouteDao)
    : LocalGeoRepo{

    override fun insertRoute(route: Route): Completable {
        return Completable.fromAction {
            routeDao.insertRoute(route)
        }
    }

    override fun getAllRoutes(): Single<List<Route>> {
        return routeDao.getAllRoutes()
    }
}

public interface LocalGeoRepo{
    fun insertRoute(route: Route): Completable
    fun getAllRoutes(): Single<List<Route>>
}