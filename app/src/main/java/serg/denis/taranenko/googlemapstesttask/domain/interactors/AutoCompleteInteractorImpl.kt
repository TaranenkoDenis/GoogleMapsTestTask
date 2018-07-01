package serg.denis.taranenko.googlemapstesttask.domain.interactors

import io.reactivex.Observable
import serg.denis.taranenko.googlemapstesttask.data.net.models.places.PlaceDetailsResult
import serg.denis.taranenko.googlemapstesttask.data.net.models.places.ResponseGeoPlaces
import serg.denis.taranenko.googlemapstesttask.data.repos.geo.RemoteGeoRepo

class AutoCompleteInteractorImpl(
        val remoteGeoRepo: RemoteGeoRepo
): AutoCompleteInteractor {


    override fun loadDetails(placeId: String) =
            remoteGeoRepo.loadDetails(placeId)

    override fun loadVariantsForAutocomplete(
            enteredText: String, types: String,
            location: String, radius: Int
    ): Observable<ResponseGeoPlaces>{
        return remoteGeoRepo.loadVariantsForAutocomplete(
                enteredText, types,
                location, radius
        )
    }

    override fun loadVariantsForAutocomplete(enteredText: String):
            Observable<ResponseGeoPlaces>{

        return remoteGeoRepo.loadVariantsForAutocomplete(
                enteredText
        )
    }
}
interface AutoCompleteInteractor{

    fun loadVariantsForAutocomplete(enteredText: String, types: String,
                                    location: String, radius: Int):
            Observable<ResponseGeoPlaces>

    fun loadVariantsForAutocomplete(enteredText: String): Observable<ResponseGeoPlaces>

    fun loadDetails(placeId: String): Observable<PlaceDetailsResult>
}