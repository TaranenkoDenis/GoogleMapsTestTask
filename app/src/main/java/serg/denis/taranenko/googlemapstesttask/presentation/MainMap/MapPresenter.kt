package serg.denis.taranenko.googlemapstesttask.presentation.MainMap

import android.animation.ValueAnimator
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
import serg.denis.taranenko.googlemapstesttask.data.net.models.places.NameAndPlaceId
import serg.denis.taranenko.googlemapstesttask.data.net.models.places.PlaceDetails
import serg.denis.taranenko.googlemapstesttask.data.net.models.places.ResponseGeoPlaces
import serg.denis.taranenko.googlemapstesttask.data.net.models.route.Step
import serg.denis.taranenko.googlemapstesttask.data.repos.geo.RepositoriesProvider
import serg.denis.taranenko.googlemapstesttask.domain.interactors.AutoCompleteInteractor
import serg.denis.taranenko.googlemapstesttask.domain.interactors.InteractorsProvider
import serg.denis.taranenko.googlemapstesttask.domain.interactors.RouteInteractor
import serg.denis.taranenko.googlemapstesttask.utils.KeyboardHelper
import serg.denis.taranenko.googlemapstesttask.utils.PolylineHelper
import java.lang.ref.WeakReference
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList
import android.support.v4.content.ContextCompat
import serg.denis.taranenko.googlemapstesttask.R
import serg.denis.taranenko.googlemapstesttask.data.net.models.route.Leg
import serg.denis.taranenko.googlemapstesttask.data.net.models.route.ResponseRoute
import serg.denis.taranenko.googlemapstesttask.data.persistance.db.RouteDatabase
import serg.denis.taranenko.googlemapstesttask.data.persistance.models.Route
import serg.denis.taranenko.googlemapstesttask.utils.BitmapDescriptor.bitmapDescriptorFromVector


class MapPresenter(
        private val view: WeakReference<MainMapView>
): MainMapPresenter, OnMapReadyCallback {

    private val DELAY_IN_MILLIS = 500L
    private val MIN_LENGTH_TO_START = 3
    private val MAX_PLACES_ON_MAP = 5
    private val DEFAULT_ZOOM = 10f

    private val compositeDisposable = CompositeDisposable()

    private var firstPlaceOnMap: PlaceDetails? = null
    private var lastPlaceOnMap: PlaceDetails? = null
    private val intermediatePlacesDetails = ArrayList<PlaceDetails?>()

    private lateinit var map:GoogleMap

    private lateinit var autoCompleteInteractor: AutoCompleteInteractor
    private lateinit var routeInteractor: RouteInteractor

    override fun init() {
        val view = view.get()

        if (view != null) {
            val remoteGeoRepo = RepositoriesProvider.provideRemoteGeocodeRepo(
                    view.getApp().getGeocodeApi()
            )
            val localGeoRepo = RepositoriesProvider.provideLocalGeocodeRepo(
                    RouteDatabase.getInstance(view.getApplicationContext()).getRouteDao()
            )

            autoCompleteInteractor = InteractorsProvider.provideAutoCompleteInteractor(
                remoteGeoRepo
            )
            routeInteractor = InteractorsProvider.provideRouteInteractor(
                remoteGeoRepo, localGeoRepo
            )

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

    private fun getObservableRouteForPlace(startPlace: PlaceDetails, finishPlace: PlaceDetails):
            Observable<ResponseRoute>{
        val origins = startPlace.geometry?.location?.lat.toString() + "," + startPlace.geometry?.location?.lng
        val destinations = finishPlace.geometry?.location?.lat.toString() + "," + finishPlace.geometry?.location?.lng
        return routeInteractor.loadRoute(origins, destinations)
    }

    override fun startTravel(){
//        val origins = intermediatePlacesDetails[0]?.geometry?.location?.lat.toString() + "," + intermediatePlacesDetails[0]?.geometry?.location?.lng
//        val destinations = intermediatePlacesDetails[intermediatePlacesDetails.size - 1]?.geometry?.location?.lat.toString() + "," + intermediatePlacesDetails[intermediatePlacesDetails.size - 1]?.geometry?.location?.lng

        if (!isFirstAndLastPlacesEntered())
            return

        val listObservable = ArrayList<Observable<ResponseRoute>>()

        if (intermediatePlacesDetails.isEmpty()){
            listObservable.add(getObservableRouteForPlace(
                firstPlaceOnMap!!, lastPlaceOnMap!!
            ))
        } else {
            var firstPlace = firstPlaceOnMap
            var nextPlace: PlaceDetails? = null

            for (i in 0 until intermediatePlacesDetails.size) {
                nextPlace = intermediatePlacesDetails[i]
                listObservable.add(getObservableRouteForPlace(
                        firstPlace!!, nextPlace!!
                ))
                firstPlace = intermediatePlacesDetails[i]
            }
        }

        Observable.zip(listObservable) {

            val result = ArrayList<Leg>()

            for (r in it)
                result.add((r as ResponseRoute).routes[0].legs[0])

            return@zip result }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe( {
                    Log.d(TAG, "Success!")
                    saveRoute(it)

                    val listOfSteps = ArrayList<Step>()

                    for (l in it) {
                        listOfSteps.addAll(l.steps)
                    }

                    carAnimation(listOfSteps)
                }, {
                    Log.e(TAG, "Exception! $it")
                })
    }

    private fun saveRoute(legs: List<Leg>) {
        val nameOfRout = makeNameOfRoute(legs)
        val listOfPoints = makeListOfPointsOfRoute(legs)

        val route = Route(nameOfRout, listOfPoints)

        routeInteractor.saveRoute(route)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        {
                            Log.d(TAG, "Saving to databse done. Check: ")
                            routeInteractor.printContentOfDB()
                        },
                        {
                            Log.e(TAG, "Throwable = $it")
                            it.printStackTrace()
                        }
                )
    }

    private fun makeListOfPointsOfRoute(legs: List<Leg>): List<String> {
        val result = ArrayList<String>()

        for (l in legs) {
            for (step in l.steps)
                result.add(step.polyline.points)
        }

        return result
    }

    private fun makeNameOfRoute(legs: List<Leg>): String {
        val nameOfRoute = StringBuilder(legs[0].start_address)

        if (legs.size == 1){
            nameOfRoute.append(" - " + legs[0].end_address)
        } else {
            for (i in 1 until legs.size) {
                nameOfRoute.append(" - " + legs[i].start_address)
                if (i == legs.size - 1)
                    nameOfRoute.append(" - " + legs[i].end_address)
            }
        }

        return nameOfRoute.toString()
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

    override fun getDetails(placeId: String) = autoCompleteInteractor.loadDetails(placeId)

    private fun addPlace(placeDetails: PlaceDetails,
                         typeElement: TypeOfElementInPlacesList){
        if (!isListOfPlacesComplete()){

            when (typeElement){
                TypeOfElementInPlacesList.FIRST_ELEMENT
                    -> firstPlaceOnMap =  placeDetails

                TypeOfElementInPlacesList.LAST_ELEMENT
                    -> lastPlaceOnMap = placeDetails

                TypeOfElementInPlacesList.INTERMEDIATE_ELEMENT
                    -> intermediatePlacesDetails.add(placeDetails)
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
        return firstPlaceOnMap != null && lastPlaceOnMap != null
    }

    private fun isListOfPlacesComplete(): Boolean {
        if (intermediatePlacesDetails.size >= MAX_PLACES_ON_MAP - 2)
            return true
        return false
    }

    private fun clearListOfPlaces(){
        firstPlaceOnMap = null
        lastPlaceOnMap = null
        intermediatePlacesDetails.clear()
    }

    private fun carAnimation(steps: List<Step>) {
        val view = view.get() ?: return

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
                            ContextCompat.getDrawable(view.getActivityContext(), R.drawable.car_marker)!!
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
}