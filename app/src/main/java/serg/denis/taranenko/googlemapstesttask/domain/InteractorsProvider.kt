package serg.denis.taranenko.googlemapstesttask.domain.interactors

import serg.denis.taranenko.googlemapstesttask.data.repos.geocode.RemoteGeoRepo

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
            remoteGeocodeRepo: RemoteGeoRepo
    ): RouteInteractor {

        if (routeInteractor == null) {
            routeInteractor = RouteInteractorImpl(
                    remoteGeocodeRepo
            )
        }

        return routeInteractor as RouteInteractor
    }
}