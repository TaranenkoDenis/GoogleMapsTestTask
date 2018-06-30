package serg.denis.taranenko.googlemapstesttask.presentation.MainMap

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import com.google.android.gms.maps.SupportMapFragment
import com.jakewharton.rxbinding2.view.RxView
import kotlinx.android.synthetic.main.activity_maps_5.*
import serg.denis.taranenko.googlemapstesttask.App
import serg.denis.taranenko.googlemapstesttask.R
import serg.denis.taranenko.googlemapstesttask.TypeOfElementInPlacesList
import java.lang.ref.WeakReference

class MapAcitivty :
        AppCompatActivity(),
        MainMapView{

    private val presenter = MapPresenter(WeakReference(this))

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

        RxView.clicks(btn_add_intermediate_point)
                .subscribe {et_additive_point_name.setText("")}

        button_go.setOnClickListener {
            presenter.startTravel()
        }

        mapFragment.getMapAsync(presenter)
    }

    override fun setEnablinInputFieldIntermediatePlace(isEnabling: Boolean){
        et_additive_point_name.isEnabled = isEnabling
    }

    override fun showMessageTooMatchPoints() {
        Toast.makeText(this, getString(R.string.too_much_points), Toast.LENGTH_LONG)
                .show()
    }

    override fun getApp() = application as App

    override fun getActivityContext() = this

    override fun getActivity() = this

    override fun onDestroy() {
        presenter.detachView()
        super.onDestroy()
    }
}
