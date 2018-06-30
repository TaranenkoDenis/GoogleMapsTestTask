package serg.denis.taranenko.googlemapstesttask.presentation.MainMap

import android.app.Activity
import android.content.Context
import android.support.v7.widget.AppCompatAutoCompleteTextView
import io.reactivex.Observable
import serg.denis.taranenko.googlemapstesttask.App
import serg.denis.taranenko.googlemapstesttask.TypeOfElementInPlacesList
import serg.denis.taranenko.googlemapstesttask.data.models.geocode.places.PlaceDetailsResult
import serg.denis.taranenko.googlemapstesttask.data.models.geocode.places.ResponseGeoPlaces

public interface MainMapView{
    fun getApplicationContext(): Context
    fun getApp(): App
    fun getActivityContext(): Context
    fun getActivity(): Activity
    fun showMessageTooMatchPoints()
    fun setEnablinInputFieldIntermediatePlace(isEnabling: Boolean)
}

public interface MainMapPresenter{
    fun detachView()
    fun init()
    fun getGooglePlacesClient(enteredText: String): Observable<ResponseGeoPlaces>

//    fun updateMap(placeDetailsResponse: PlaceDetailsResult)
    fun getDetails(placeId: String): Observable<PlaceDetailsResult>

    fun addOnAutoCompleteTextViewTextChangedObserver(
            et: AppCompatAutoCompleteTextView)

    fun addOnAutoCompleteTextViewItemClickedSubscriber(
            et: AppCompatAutoCompleteTextView,
            typeEt: TypeOfElementInPlacesList)

    fun startTravel()
}