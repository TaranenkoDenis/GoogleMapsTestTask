package serg.denis.taranenko.googlemapstesttask.testActivity3

import android.animation.ValueAnimator
import android.graphics.Color
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.animation.LinearInterpolator
import android.widget.Toast
import com.google.android.gms.maps.*

import com.google.android.gms.maps.model.*
import kotlinx.android.synthetic.main.activity_maps.*
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import serg.denis.taranenko.googlemapstesttask.R
import com.google.android.gms.maps.model.LatLng



class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var mapFragment: SupportMapFragment
    private lateinit var polylineList: List<LatLng>
    private lateinit var marker: Marker

    private var v: Float = 0f
    private var lat: Double = 0.0
    private var lng: Double = 0.0
    private var index: Int = 0
    private var next: Int = 0
    private lateinit var destination: String

    private lateinit var handler: Handler
    private lateinit var startPosition: LatLng
    private lateinit var endPosition: LatLng

    private lateinit var polilyneOptions: PolylineOptions
    private lateinit var blackPolilyneOptions: PolylineOptions
    private lateinit var blackPolyline: Polyline
    private lateinit var greyPolyline: Polyline

    private lateinit var myLocation: LatLng

    private lateinit var googleApi: IGoogleApi


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
//        mapFragment.getMapAsync(this)

        polylineList = ArrayList()

        btn_add_intermediate_point.setOnClickListener {
            destination = et_destination.text.toString()
            destination.replace(" ","+")
            mapFragment.getMapAsync(this)
        }

        googleApi = getGoogleApi()
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        mMap.mapType = GoogleMap.MAP_TYPE_NORMAL
        mMap.isTrafficEnabled = false
        mMap.isIndoorEnabled = false
        mMap.isBuildingsEnabled = false
        mMap.uiSettings.isZoomControlsEnabled = true

        // Add a marker in Sydney and move the camera
        val sydney = LatLng(-34.0, 151.0)
        mMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))
        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(CameraPosition.Builder()
                .target(googleMap.cameraPosition.target)
                .zoom(17f)
                .bearing(30f)
                .tilt(45f)
                .build()))

        val strBuilder = StringBuilder()
        try {
            strBuilder.append("https://maps.googleapis.com/maps/api/directions/json?")
                    .append("mode=driving&")
                    .append("transit_routing_preference=less_driving&")
                    .append("origin=${sydney.latitude},${sydney.longitude}&")
                    .append("destination=$destination&")
                    .append("key=${resources.getString(R.string.google_direction_key)}")

            Log.d("MyTag", "Url = $strBuilder")

            googleApi.getDataFromGoogleApi(strBuilder.toString()).enqueue(object : Callback<String>     {

                override fun onFailure(call: Call<String>?, t: Throwable?) {
                    t?.printStackTrace()
                    Toast.makeText(this@MapsActivity, t?.message, Toast.LENGTH_SHORT)
                            .show()
                }

                override fun onResponse(call: Call<String>?, response: Response<String>?) {
                    try {
                        if (response != null) {

                            val jsonObject = JSONObject(response.body().toString())
                            val jsonArray = jsonObject.getJSONArray("routes")
                            for (i in 0 until jsonArray.length()){
                                val route = jsonArray.getJSONObject(i)
                                val poly = route.getJSONObject("overview_polyline")
                                val polyline = poly.getString("points")
                                polylineList = decodePoly(polyline)
                            }

                        } else{
                            Log.e("MyTag", "response == null")
                        }

                        // Adjusting bounds
                        val builder = LatLngBounds.Builder()

                        for (latlng in polylineList){
                            builder.include(latlng)
                        }

                        val bounds = builder.build()
                        val mCameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds,2)
                        mMap.animateCamera(mCameraUpdate)

                        polilyneOptions = PolylineOptions()
                        polilyneOptions.color(Color.GRAY)
                        polilyneOptions.width(5f)
                        polilyneOptions.startCap(SquareCap())
                        polilyneOptions.endCap(SquareCap())
                        polilyneOptions.jointType(JointType.ROUND)
                        polilyneOptions.addAll(polylineList)
                        greyPolyline = mMap.addPolyline(polilyneOptions)

                        blackPolilyneOptions = PolylineOptions()
                        blackPolilyneOptions.color(Color.BLACK)
                        blackPolilyneOptions.width(5f)
                        blackPolilyneOptions.startCap(SquareCap())
                        blackPolilyneOptions.endCap(SquareCap())
                        blackPolilyneOptions.jointType(JointType.ROUND)
                        blackPolilyneOptions.addAll(polylineList)
                        blackPolyline = mMap.addPolyline(blackPolilyneOptions)

                        mMap.addMarker(MarkerOptions().position(
                                polylineList[polylineList.size - 1]
                        ))

                        // Animator
                        val polylineAnimator = ValueAnimator.ofInt(0, 100)
                        polylineAnimator.duration = 2000
                        polylineAnimator.interpolator = LinearInterpolator()
                        polylineAnimator.addUpdateListener {
                            val points = greyPolyline.points
                            val percentValue = it.animatedValue as Int
                            val size = points.size
                            val newPoints = (size * (percentValue / 100f)) as Int
                            val p = points.subList(0, newPoints)
                            blackPolyline.points = p
                        }

                        polylineAnimator.start()

                        // Add car marker
                        marker = mMap.addMarker(MarkerOptions().position(sydney)
                                .flat(true)
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.car)))

                        // Car moving
                        handler = Handler()
                        index = -1
                        next = 1
                        handler.postDelayed({
                            if (index < polylineList.size - 1){
                                ++index
                                next = index + 1
                            }
                            if (index < polylineList.size - 1){
                                startPosition = polylineList[index]
                                endPosition = polylineList[next]
                            }
                        }, 3000)

                        // Animator
                        val valueAnimator = ValueAnimator.ofFloat(0f, 1f)
                        valueAnimator.duration = 3000
                        valueAnimator.interpolator = LinearInterpolator()
                        valueAnimator.addUpdateListener {
                            v = valueAnimator.animatedFraction
                            lng = v * endPosition.longitude + (1 - v) * startPosition.longitude
                            lat = v * endPosition.latitude + (1 - v) * startPosition.latitude

                            val newPos = LatLng(lat, lng)

                            marker.position = newPos
                            marker.setAnchor(0.5f, 0.5f)
                            marker.rotation = getBearing(startPosition, newPos)
                            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(
                                    CameraPosition.Builder()
                                            .target(newPos)
                                            .zoom(15.5f)
                                            .build()
                            ))
                        }

                        valueAnimator.start()
                        handler.postDelayed(this, 3000)

                    } catch (e: Exception){
                        e.printStackTrace()
                    }
                }
            })



        } catch (e: Exception){
            e.printStackTrace()
        }


    }

    private fun getBearing(startPosition: LatLng, newPos: LatLng): Float {
        lat = Math.abs(startPosition.latitude - newPos.latitude)
        lng = Math.abs(startPosition.longitude - newPos.longitude)

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

    private fun decodePoly(polyline: String): List<LatLng> {
        val poly = ArrayList<LatLng>()
        var index = 0
        val len = polyline.length
        var lat = 0
        var lng = 0

        while (index < len) {
            var b: Int
            var shift = 0
            var result = 0

            do {
                b = (polyline.toCharArray()[index] - 63).toInt()
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)

            val dlat = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lat += dlat

            shift = 0
            result = 0

            do {
                b = (polyline.toCharArray()[index] - 63).toInt()
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlng = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lng += dlng

            val p = LatLng(lat.toDouble() / 1E5,
                    lng.toDouble() / 1E5)
            poly.add(p)
        }

        return poly
    }
}
