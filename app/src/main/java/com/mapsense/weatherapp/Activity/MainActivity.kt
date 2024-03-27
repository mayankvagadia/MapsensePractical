package com.mapsense.weatherapp.Activity

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.AdapterView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import com.bumptech.glide.Glide
import com.example.lotteryapp.utility.interfaces.onApiCall
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.mapsense.weatherapp.Adapter.PlaceAdapter
import com.mapsense.weatherapp.Factory.Homefactory
import com.mapsense.weatherapp.Model.PlaceModel
import com.mapsense.weatherapp.R
import com.mapsense.weatherapp.Viewmodel.Homeviewmodel
import com.mapsense.weatherapp.databinding.ActivityMainBinding
import com.mapsense.weatherapp.databinding.DialogWeatherInfoBinding
import java.text.DecimalFormat


class MainActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMapLoadedCallback,
    onApiCall, GoogleMap.OnMarkerClickListener {

    lateinit var binding: ActivityMainBinding
    var mapFragment: SupportMapFragment? = null
    var mGoogleMap: GoogleMap? = null
    var mLastLocation: Location? = null
    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    private val permissionId = 123
    private lateinit var mPlacesClient: PlacesClient
    private var placeAdapter: PlaceAdapter? = null
    lateinit var viewmodel: Homeviewmodel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        if (resources.getString(R.string.google_maps_key) == "Your Api Key"){
            Toast.makeText(this@MainActivity,"You have to add your google maps api key in string.xml file",Toast.LENGTH_LONG).show()
        }

        val factory = Homefactory(this.application, this)
        viewmodel = ViewModelProviders.of(this, factory)[Homeviewmodel::class.java]

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        mapFragment =
            supportFragmentManager.findFragmentById(R.id.fragment_map) as SupportMapFragment
        mapFragment?.getMapAsync(this)

        Places.initialize(this, this.resources.getString(R.string.google_maps_key))
        mPlacesClient = Places.createClient(this)

        placeAdapter = PlaceAdapter(this, R.layout.layout_item_places, mPlacesClient)
        binding.autoCompleteEditText.setAdapter(placeAdapter)

        binding.autoCompleteEditText.onItemClickListener =
            AdapterView.OnItemClickListener { parent, _, position, _ ->
                val place = parent.getItemAtPosition(position) as PlaceModel
                binding.autoCompleteEditText.apply {
                    setText(place.fullText)
                    setSelection(binding.autoCompleteEditText.length())
                    Log.d("PlaceId", "onCreate: " + place.placeId)

                    viewmodel.getLatLongFromPlaceId(this@MainActivity, place.placeId!!)
                }
            }

        viewmodel.locationResults.observe(this) {
            mGoogleMap!!.clear()
            val currentMarker =
                LatLng(it.lat, it.lng)
            mGoogleMap!!.addMarker(MarkerOptions().position(currentMarker))
            mGoogleMap!!.moveCamera(CameraUpdateFactory.newLatLng(LatLng(it.lat, it.lng)))
            mGoogleMap!!.moveCamera(
                CameraUpdateFactory.newCameraPosition(
                    CameraPosition.Builder().target(mGoogleMap!!.cameraPosition.target).zoom(10f)
                        .build()
                )
            )
            mGoogleMap!!.setOnMarkerClickListener(this)
        }

        viewmodel.weatherResults.observe(this) {
            val dialog = BottomSheetDialog(this)
            val view = layoutInflater.inflate(R.layout.dialog_weather_info, null)
            val binding: DialogWeatherInfoBinding? = DataBindingUtil.bind(view)
            dialog.setContentView(binding!!.root)
            dialog.show()

            val df = DecimalFormat("#.##")
            val temp: Double = it.main!!.temp - 273.15
            val fahrenheit = (df.format(temp).toDouble() * 9 / 5) + 32
            binding.txtCityName.text = "Weather in " + it.name
            binding.txtWeatherCondition.text = "Weather Condition: " + it.weather!![0].description
            binding.txtTemp.text = "Temp in celsius : " + df.format(temp) + "°C"
            binding.txtFTemp.text = "Temp in fahrenheit : " + fahrenheit + "°F"
            binding.txtHumidity.text = "Humidity : " + it.main!!.humidity + "%"
            binding.txtWindSpeed.text = "Wind Speed : " + it.wind!!.speed + "m/s"
            binding.txtPressure.text = "Pressure : " + it.main!!.pressure + "hPa"

            val imageURl = "https://openweathermap.org/img/wn/" + it.weather!![0].icon + ".png"
            Glide.with(this@MainActivity).load(imageURl).into(binding.imvWeatherCondition)
        }

    }

    public override fun onStart() {
        super.onStart()
        if (!checkPermissions()) {
            requestPermissions()
        } else {
            getLastLocation()
        }
    }

    private fun getLastLocation() {
        if (checkPermissions()) {
            if (isLocationEnabled()) {
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ),
                        permissionId
                    )
                    return
                }
                mFusedLocationClient.lastLocation.addOnCompleteListener(this) { task ->
                    if (task.isSuccessful && task.result != null) {
                        mLastLocation = task.result
                        val currentMarker =
                            LatLng(mLastLocation!!.latitude, mLastLocation!!.longitude)
                        mGoogleMap!!.addMarker(
                            MarkerOptions()
                                .position(currentMarker)
                                .title("You are here")
                        )
                        mGoogleMap!!.moveCamera(
                            CameraUpdateFactory.newLatLng(
                                LatLng(
                                    mLastLocation!!.latitude,
                                    mLastLocation!!.longitude
                                )
                            )
                        )
                        mGoogleMap!!.moveCamera(
                            CameraUpdateFactory.newCameraPosition(
                                CameraPosition.Builder()
                                    .target(mGoogleMap!!.cameraPosition.target)
                                    .zoom(10f)
                                    .build()
                            )
                        )
                        mGoogleMap!!.setOnMarkerClickListener(this)
                    }
                }
            } else {
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }
        } else {
            requestPermissions()
        }
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager: LocationManager =
            getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    private fun checkPermissions(): Boolean {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            return true
        }
        return false
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            ),
            permissionId
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == permissionId) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                getLastLocation()
            }
        }
    }

    override fun onMapReady(p0: GoogleMap) {
        mGoogleMap = p0
    }

    override fun onMapLoaded() {
    }

    override fun onError(type: String, message: String) {
        Toast.makeText(this@MainActivity, message, Toast.LENGTH_LONG).show()
    }

    override fun onMarkerClick(p0: Marker): Boolean {
        viewmodel.getWeatherFromLatLong(
            p0.position.latitude.toString(),
            p0.position.longitude.toString()
        )
        return false
    }
}