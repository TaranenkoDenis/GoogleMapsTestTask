package serg.denis.taranenko.googlemapstesttask.presentation.MainMap

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_maps_5.*
import com.google.android.gms.maps.SupportMapFragment
import com.jakewharton.rxbinding2.view.RxView
import serg.denis.taranenko.googlemapstesttask.App
import serg.denis.taranenko.googlemapstesttask.R
import serg.denis.taranenko.googlemapstesttask.TypeOfElementInPlacesList
import serg.denis.taranenko.googlemapstesttask.data.persistance.models.Route
import java.lang.ref.WeakReference

class MapAcitivty :
        AppCompatActivity(),
        MainMapView{

    private val presenter = MapPresenter(WeakReference(this))
    private var listOfElementsForSpinner: List<Route> = ArrayList<Route>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps_5)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment

        presenter.init()

        presenter.addOnAutoCompleteTextViewItemClickedSubscriber(
                et_additive_point_name,
                TypeOfElementInPlacesList.INTERMEDIATE_ELEMENT
        )
        presenter.addOnAutoCompleteTextViewTextChangedObserver(et_additive_point_name)

        presenter.addOnAutoCompleteTextViewItemClickedSubscriber(
                et_departure_point,
                TypeOfElementInPlacesList.FIRST_ELEMENT
        )
        presenter.addOnAutoCompleteTextViewTextChangedObserver(et_departure_point)

        presenter.addOnAutoCompleteTextViewItemClickedSubscriber(
                et_destination,
                TypeOfElementInPlacesList.LAST_ELEMENT
        )
        presenter.addOnAutoCompleteTextViewTextChangedObserver(et_destination)

        val spinnerAdapter = ArrayAdapter<Route>(this,
                android.R.layout.simple_spinner_item)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.prompt = getString(R.string.title_used_routes)
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{

            private var isFirstLoad = true

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (isFirstLoad) {
                    isFirstLoad = false
                } else{
                    presenter.showSavedRoute(listOfElementsForSpinner[position])
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        spinner.adapter = spinnerAdapter

        presenter.loadDataForSpinner()

        RxView.clicks(btn_add_intermediate_point)
                .subscribe {et_additive_point_name.setText("")}

        button_go.setOnClickListener {
            presenter.startTravel()
        }

        mapFragment.getMapAsync(presenter)
    }

    override fun setDataForSpinner(data: List<Route>){
        val adapter = (spinner.adapter as ArrayAdapter<Route>)

        listOfElementsForSpinner = data

        adapter.clear()
        adapter.addAll(listOfElementsForSpinner)
        adapter.notifyDataSetChanged()
    }

    override fun setEnablinInputFieldIntermediatePlace(isEnabling: Boolean){
        et_additive_point_name.isEnabled = isEnabling
    }

    override fun showMessageTooMatchPoints() {
        Toast.makeText(this, getString(R.string.too_much_points), Toast.LENGTH_LONG)
                .show()
    }

    override fun clearEditTextes() {
        et_additive_point_name.setText("")
        et_departure_point.setText("")
        et_destination.setText("")
    }

    override fun getApp() = application as App

    override fun getActivityContext() = this

    override fun getActivity() = this

    override fun onDestroy() {
        presenter.detachView()
        super.onDestroy()
    }
}
