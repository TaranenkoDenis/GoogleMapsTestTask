package serg.denis.taranenko.googlemapstesttask.presentation.MainMap

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Color
import android.os.Handler
import android.support.v7.widget.AppCompatAutoCompleteTextView
import android.util.Log
import android.view.animation.LinearInterpolator
import android.widget.ArrayAdapter
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.*
import com.jakewharton.rxbinding2.widget.RxAutoCompleteTextView
import com.jakewharton.rxbinding2.widget.RxTextView
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import serg.denis.taranenko.googlemapstesttask.TAG
import serg.denis.taranenko.googlemapstesttask.TypeOfElementInPlacesList
import serg.denis.taranenko.googlemapstesttask.data.models.geocode.places.NameAndPlaceId
import serg.denis.taranenko.googlemapstesttask.data.models.geocode.places.PlaceDetails
import serg.denis.taranenko.googlemapstesttask.data.models.geocode.places.ResponseGeoPlaces
import serg.denis.taranenko.googlemapstesttask.data.models.geocode.route.Step
import serg.denis.taranenko.googlemapstesttask.data.repos.geocode.RepositoriesProvider
import serg.denis.taranenko.googlemapstesttask.domain.interactors.AutoCompleteInteractor
import serg.denis.taranenko.googlemapstesttask.domain.interactors.InteractorsProvider
import serg.denis.taranenko.googlemapstesttask.domain.interactors.RouteInteractor
import serg.denis.taranenko.googlemapstesttask.utils.KeyboardHelper
import serg.denis.taranenko.googlemapstesttask.utils.PolylineHelper
import java.lang.ref.WeakReference
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import android.graphics.Bitmap
import android.graphics.Canvas
import android.support.v4.content.ContextCompat
import android.support.annotation.DrawableRes
import com.google.android.gms.maps.model.BitmapDescriptor
import serg.denis.taranenko.googlemapstesttask.R


class MapPresenter(
        private val view: WeakReference<MainMapView>
): MainMapPresenter, OnMapReadyCallback {

    private val DELAY_IN_MILLIS = 500L
    private val MIN_LENGTH_TO_START = 3
    private val MAX_PLACES_ON_MAP = 5
    private val DEFAULT_ZOOM = 10f

    private val compositeDisposable = CompositeDisposable()

    private val placesDetails = ArrayList<PlaceDetails?>()
//    private val placesDetails = ArrayList<NameAndPlaceId?>()
    private lateinit var map:GoogleMap

    private lateinit var autoCompleteInteractor: AutoCompleteInteractor
    private lateinit var routeInteractor: RouteInteractor

    override fun init() {
        if (view.get() != null) {
            val remoteGeoRepo = RepositoriesProvider.provideRemoteGeocodeRepo(
                    view.get()!!.getApp().getGeocodeApi()
            )

            autoCompleteInteractor = InteractorsProvider.provideAutoCompleteInteractor(
                remoteGeoRepo
            )
            routeInteractor = InteractorsProvider.provideRouteInteractor(
                remoteGeoRepo
            )

            for (i in 0 until MAX_PLACES_ON_MAP)
                placesDetails.add(null)

            checkStateOfIntermediatePlacesET()
        }
    }

    override fun getGooglePlacesClient(enteredText: String): Observable<ResponseGeoPlaces> {
        return autoCompleteInteractor.loadVariantsForAutocomplete(enteredText)
    }

    override fun onMapReady(p0: GoogleMap?) {
        if (p0 != null) {
            map = p0
        }
    }

    override fun detachView() {
        compositeDisposable.clear()
        view.clear()
    }

    override fun startTravel(){


        val origins = placesDetails[0]?.geometry?.location?.lat.toString() + "," + placesDetails[0]?.geometry?.location?.lng
        val destinations = placesDetails[placesDetails.size - 1]?.geometry?.location?.lat.toString() + "," + placesDetails[placesDetails.size - 1]?.geometry?.location?.lng
        val mode = "driving"

        Log.d(TAG,"start location -> $origins")
        Log.d(TAG,"end location -> $destinations")


        routeInteractor.loadRoute(origins, destinations, mode)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe( {
                    if (it.status.toUpperCase().equals("ok".toUpperCase())){
                        Log.d(TAG, "Success!")
                        carAnimation(it.routes[0].legs[0].steps)
                    } else{
                        Log.e(TAG, "Exception! status = ${it.status}")
                    }
                }, {
                    Log.e(TAG, "Exception! $it")
                })
    }



    private fun carAnimation(steps: List<Step>) {


        // draw route
        val options = PolylineOptions()
        for (step in steps) {
            options.color(Color.RED)
            options.width(10f)
            options.addAll(PolylineHelper.decodePoly(step.polyline.points))
        }
        map.addPolyline(options)
        val polyline = map.addPolyline(options)

        Log.d(TAG, "After drawing line")

        // car animation
        try{
            val polylineAnimator = ValueAnimator.ofInt(0, 100)
            polylineAnimator.duration = 1000
            polylineAnimator.interpolator = LinearInterpolator()
            polylineAnimator.addUpdateListener {
                val points = options.points
                val percentValue = it.animatedValue as Int
                val size = points.size
                val newPoints = (size * (percentValue / 100f)).toInt()
                val p = points.subList(0, newPoints)
                polyline.points = p
            }

            polylineAnimator.start()

            // Add car marker
            val marker = map.addMarker(MarkerOptions().position(options.points[0])
                    .flat(true)
                    .icon(bitmapDescriptorFromVector(
                            view.get()!!.getActivityContext(),
                            R.drawable.car_marker
                    )))

            // Car moving
            val handler = Handler()
            var index = -1
            var next = 1
            var startPosition: LatLng? = null
            var endPosition: LatLng? = null

            handler.postDelayed(object : Runnable {
                override fun run() {
                    if (index < options.points.size - 1){
                        ++index
                        next = index + 1
                    }
                    if (index < options.points.size - 1){
                        startPosition = options.points[index]
                        endPosition = options.points[next]
                    }

                    // Animator
                    val valueAnimator = ValueAnimator.ofFloat(0f, 1f)
                    valueAnimator.duration = 500
                    valueAnimator.interpolator = LinearInterpolator()
                    valueAnimator.addUpdateListener {
                        val v = valueAnimator.animatedFraction

                        val lng = v * endPosition!!.longitude + (1 - v) * startPosition!!.longitude
                        val lat = v * endPosition?.latitude!! + (1 - v) * startPosition!!.latitude

                        val newPos = LatLng(lat, lng)

                        marker.position = newPos
                        marker.setAnchor(0.5f, 0.5f)
//                        marker.rotation = getBearing(startPosition!!, newPos)
                        map.moveCamera(CameraUpdateFactory.newCameraPosition(
                                CameraPosition.Builder()
                                        .target(newPos)
                                        .zoom(10f)
                                        .build()
                        ))
                    }

                    valueAnimator.start()
                    handler.postDelayed(this, 500)
                }
            }, 500)
        } catch (e: Exception){
            Log.e(TAG, "EXCEPTION: $e")
            e.printStackTrace()
        }
    }

    private fun getBearing(startPosition: LatLng, newPos: LatLng): Float {
        val lat = Math.abs(startPosition.latitude - newPos.latitude)
        val lng = Math.abs(startPosition.longitude - newPos.longitude)

        if (startPosition.latitude < newPos.latitude &&
                startPosition.longitude < newPos.longitude){
            return Math.toDegrees(Math.atan(lng/lat)).toFloat()
        }
        else if (startPosition.latitude >= newPos.latitude &&
                startPosition.longitude < newPos.longitude) {
            return ((90 - Math.toDegrees(Math.atan(lng/lat))) + 90).toFloat()
        }
        else if (startPosition.latitude >= newPos.latitude &&
                startPosition.longitude >= newPos.longitude) {
            return (Math.toDegrees(Math.atan(lng/lat)) + 180).toFloat()
        }
        else if (startPosition.latitude < newPos.latitude &&
                startPosition.longitude >= newPos.longitude) {
            return ((90 - Math.toDegrees(Math.atan(lng/lat))) + 270).toFloat()
        }
        return -1f
    }

    override fun addOnAutoCompleteTextViewItemClickedSubscriber(
            et: AppCompatAutoCompleteTextView,
            typeEt: TypeOfElementInPlacesList) {

        val adapterViewItemClickEventObservable = RxAutoCompleteTextView.itemClickEvents(et)
                .map { adapterViewItemClickEvent ->
                    val item = et.adapter
                            .getItem(adapterViewItemClickEvent.position()) as NameAndPlaceId
                    item.placeId
                }
                .observeOn(Schedulers.io())
                .switchMap { placeId -> getDetails(placeId) }
                .observeOn(AndroidSchedulers.mainThread())
                .retry()


        compositeDisposable.add(adapterViewItemClickEventObservable.subscribe(
                { placeDetailsResult ->
                    Log.i(TAG, placeDetailsResult.toString())
                    if (placeDetailsResult.result != null) {

                        addPlace(
                                placeDetailsResult.result!!,
                                typeEt
                        )
                    }
                },
                { throwable -> Log.e(TAG, "onError", throwable) },
                { Log.i(TAG, "onCompleted") })
        )
    }

    override fun addOnAutoCompleteTextViewTextChangedObserver(et: AppCompatAutoCompleteTextView) {
        val autocompleteResponseObservable = RxTextView.textChangeEvents(et)
                .debounce(DELAY_IN_MILLIS, TimeUnit.MILLISECONDS)
                .map { textViewTextChangeEvent -> textViewTextChangeEvent.text().toString() }
                .filter { s -> (s as CharSequence).length >= MIN_LENGTH_TO_START }
                .observeOn(Schedulers.io())
                .switchMap { s -> getGooglePlacesClient(s) }
                .observeOn(AndroidSchedulers.mainThread())
                .retry()

        compositeDisposable.add(autocompleteResponseObservable.subscribe(
                { placeAutocompleteResult ->
                    val list = ArrayList<NameAndPlaceId>()
                    for (prediction in placeAutocompleteResult.predictions) {
                        list.add(NameAndPlaceId(prediction.description, prediction.placeId))
                    }

                    val itemsAdapter = ArrayAdapter(
                            view.get()?.getActivityContext(),
                            android.R.layout.simple_list_item_1, list)

                    et.setAdapter(itemsAdapter)

                    val enteredText = et.text.toString()

                    if (list.size >= 1 && enteredText == list.get(0).name) {
                        et.dismissDropDown()
                    } else {
                        et.showDropDown()
                    }
                },
                { e -> Log.e(TAG, "onError", e) },
                { Log.i(TAG, "onCompleted") })
        )
    }

    private fun bitmapDescriptorFromVector(context: Context, @DrawableRes drawableResourceId: Int): BitmapDescriptor {

        val vectorDrawable = ContextCompat.getDrawable(context, drawableResourceId)
        vectorDrawable!!.setBounds(0, 0, vectorDrawable.intrinsicWidth, vectorDrawable.intrinsicHeight)
        val bitmap = Bitmap.createBitmap(vectorDrawable.intrinsicWidth, vectorDrawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        vectorDrawable.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(Bitmap.createScaledBitmap(bitmap, 100, 100, false))
    }

    override fun getDetails(placeId: String) = autoCompleteInteractor.loadDetails(placeId)

    private fun addPlace(placeDetails: PlaceDetails,
                         typeElement: TypeOfElementInPlacesList){
        if (!isListOfPlacesComplete()){

            when (typeElement){
                TypeOfElementInPlacesList.FIRST_ELEMENT
                    -> placesDetails[0] = placeDetails

                TypeOfElementInPlacesList.LAST_ELEMENT
                    -> placesDetails[placesDetails.size - 1] = placeDetails

                TypeOfElementInPlacesList.INTERMEDIATE_ELEMENT
                    -> {
                    var index: Int = placesDetails.indexOfFirst { p -> p == null}

                    if (index != -1)
                        placesDetails[index] = placeDetails
                }
            }

            addPlaceOnMap(placeDetails)

            checkStateOfIntermediatePlacesET()
        }
        else {
            if (view.get() != null)
                view.get()!!.showMessageTooMatchPoints()
        }
    }

    private fun checkStateOfIntermediatePlacesET() {
        val view = view.get()
        if (view != null) {
            if ((isListOfPlacesComplete() || !isFirstAndLastPlacesEntered())) {
                view.setEnablinInputFieldIntermediatePlace(false)
            } else {
                view.setEnablinInputFieldIntermediatePlace(true)
            }
        }
    }

    private fun addPlaceOnMap(placeDetails: PlaceDetails) {
        val view = view.get()

        if (map != null && view != null) {

            val location = placeDetails.geometry?.location
            val latLng = LatLng(location!!.lat, location.lng)

            val title = Regex("[^-.?!_)(,:]").replace(placeDetails.name!!, "*")

            val markerOptions = MarkerOptions()
                    .position(latLng)
                    .title(title)

            val marker = map.addMarker(markerOptions)
            marker.showInfoWindow()


            map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_ZOOM))

            KeyboardHelper.hideKeyboard(view.getActivity())
        }
    }

    private fun isFirstAndLastPlacesEntered(): Boolean {
        return placesDetails[0] != null &&
                placesDetails[placesDetails.size - 1] != null
    }


    private fun isListOfPlacesComplete(): Boolean {
        var result = true

        for (i in placesDetails) {
            if (i == null){
                result = false
                break
            }
        }

        return result
    }

    private fun clearListOfPlaces(){
        for (i in 0 until placesDetails.size)
            placesDetails[i] = null
    }
}