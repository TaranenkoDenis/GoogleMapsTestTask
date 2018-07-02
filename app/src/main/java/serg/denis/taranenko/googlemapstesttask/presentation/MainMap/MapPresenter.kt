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
import serg.denis.taranenko.googlemapstesttask.data.net.models.places.NameAndPlaceId
import serg.denis.taranenko.googlemapstesttask.data.net.models.places.PlaceDetails
import serg.denis.taranenko.googlemapstesttask.data.net.models.places.ResponseGeoPlaces
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
import io.reactivex.functions.Consumer
import serg.denis.taranenko.googlemapstesttask.*
import serg.denis.taranenko.googlemapstesttask.data.net.models.route.Leg
import serg.denis.taranenko.googlemapstesttask.data.persistance.db.RouteDatabase
import serg.denis.taranenko.googlemapstesttask.data.persistance.models.Route
import serg.denis.taranenko.googlemapstesttask.utils.BitmapDescriptor.bitmapDescriptorFromVector


class MapPresenter(
        private val view: WeakReference<MainMapView>
): MainMapPresenter, OnMapReadyCallback {

    private val compositeDisposable = CompositeDisposable()

    private var firstPlaceOnMap: PlaceDetails? = null
    private var lastPlaceOnMap: PlaceDetails? = null
    private val wayPoints = ArrayList<PlaceDetails?>()

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

        if (!isFirstAndLastPlacesEntered())
            return

        val route = buildRoute()

        routeInteractor.loadRoute(route)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    startCarAnimation(it.routes[0].legs)
                    saveRoute(route)
                    clearListOfPlaces()
                }, {
                    Log.e(TAG, "Error!")
                    it.printStackTrace()
                })
    }

    private fun buildRoute(): Route {

        val name = StringBuilder()
        name.append(firstPlaceOnMap?.name)

        val waypoint = StringBuilder()
        val origins = firstPlaceOnMap?.geometry?.location?.lat.toString() + "," + firstPlaceOnMap?.geometry?.location?.lng
        val destinations = lastPlaceOnMap?.geometry?.location?.lat.toString() + "," + lastPlaceOnMap?.geometry?.location?.lng

        if (!wayPoints.isEmpty()){
            for (i in 0 until wayPoints.size) {
                name.append(" - " + wayPoints[i]?.name)

                waypoint.append(wayPoints[i]?.geometry?.location?.lat.toString())
                        .append(",")
                        .append(wayPoints[i]?.geometry?.location?.lng.toString())

                if (i != wayPoints.size - 1)
                    waypoint.append("|")
            }
        }

        name.append(" - " + lastPlaceOnMap?.name)

        return Route(name.toString(), origins, destinations, waypoint.toString())
    }

    private fun saveRoute(route: Route){
        routeInteractor.saveRoute(route)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        {
                            loadDataForSpinner()
                        },
                        {
                            Log.e(TAG, "Throwable = $it")
                            it.printStackTrace()
                        }
                )
    }

    override fun showSavedRoute(route: Route) {
        routeInteractor.loadRoute(route)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    Log.d(TAG, "Success!")
                    map.clear()
                    clearListOfPlaces()
                    startCarAnimation(it.routes[0].legs)
                }, {
                    Log.e(TAG, "Error!")
                    it.printStackTrace()
                })
    }

    private fun startCarAnimation(legs: List<Leg>) {
        val view = view.get() ?: return

        // draw route
        val options = PolylineOptions()

        for (l in legs) {
            for (step in l.steps) {
                options.color(Color.RED)
                options.width(10f)
                options.addAll(PolylineHelper.decodePoly(step.polyline.points))
            }
        }
        map.addPolyline(options)
        val polyline = map.addPolyline(options)

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

                    if (index >= options.points.size - 1){
                        map.clear()
                        loadDataForSpinner()
                        return
                    }

                    if (index < options.points.size - 1){
                        startPosition = options.points[index]
                        endPosition = options.points[next]
                    }

                    // Animator
                    val valueAnimator = ValueAnimator.ofFloat(0f, 1f)
                    valueAnimator.duration = SPEED_OF_CAR
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
                    handler.postDelayed(this, SPEED_OF_CAR)
                }
            }, SPEED_OF_CAR)
        } catch (e: Exception){
            Log.e(TAG, "EXCEPTION: $e")
            e.printStackTrace()
        }
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
                .debounce(DELAY_FOR_AUTOCOMPLETE_IN_MILLIS, TimeUnit.MILLISECONDS)
                .map { textViewTextChangeEvent -> textViewTextChangeEvent.text().toString() }
                .filter { s -> (s as CharSequence).length >= MIN_LENGTH_TO_START_AUTOCOMPLETE }
                .observeOn(Schedulers.io())
                .switchMap { s -> getGooglePlacesObservable(s) }
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

    private fun getGooglePlacesObservable(enteredText: String): Observable<ResponseGeoPlaces> {
        return autoCompleteInteractor.loadVariantsForAutocomplete(enteredText)
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
                    -> wayPoints.add(placeDetails)
            }

            addPlaceOnMap(placeDetails)

            checkStateOfIntermediatePlacesET()
        }
        else {
            if (view.get() != null)
                view.get()!!.showMessageTooMatchPoints()
        }
    }

    override fun loadDataForSpinner() {
        routeInteractor.loadRoutesFromDB()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(Consumer {
                    val view = view.get()?: return@Consumer
                    view.setDataForSpinner(it)
                })
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

    private fun isFirstAndLastPlacesEntered(): Boolean
            = firstPlaceOnMap != null && lastPlaceOnMap != null

    private fun isListOfPlacesComplete(): Boolean
            = wayPoints.size >= MAX_PLACES_ON_MAP - 2

    private fun clearListOfPlaces(){
        firstPlaceOnMap = null
        lastPlaceOnMap = null
        wayPoints.clear()

        if (view.get() != null)
            view.get()!!.clearEditTextes()

        checkStateOfIntermediatePlacesET()
    }
}