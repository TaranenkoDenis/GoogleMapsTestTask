package serg.denis.taranenko.googlemapstesttask.domain.interactors

import serg.denis.taranenko.googlemapstesttask.data.repos.geo.LocalGeoRepo
import serg.denis.taranenko.googlemapstesttask.data.repos.geo.RemoteGeoRepo

object InteractorsProvider {

    private var autoCompleteInteractor: AutoCompleteInteractor? = null
    private var routeInteractor: RouteInteractor? = null


    fun provideAutoCompleteInteractor(
            remoteGeocodeRepo: RemoteGeoRepo
    ): AutoCompleteInteractor {

        if (autoCompleteInteractor == null) {
            autoCompleteInteractor = AutoCompleteInteractorImpl(
                    remoteGeocodeRepo
            )
        }
        return autoCompleteInteractor as AutoCompleteInteractor
    }

    fun provideRouteInteractor(
            remoteGeoRepo: RemoteGeoRepo,
            localGeoRepo: LocalGeoRepo
    ): RouteInteractor {

        if (routeInteractor == null) {
            routeInteractor = RouteInteractorImpl(
                    remoteGeoRepo,
                    localGeoRepo
            )
        }

        return routeInteractor as RouteInteractor
    }
}