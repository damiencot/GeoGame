package fr.nansty.geogame.map

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import fr.nansty.geogame.R
import fr.nansty.geogame.geofence.GEOFENCE_ID_AKATOR
import fr.nansty.geogame.geofence.GeofenceManager
import fr.nansty.geogame.location.LocationData
import fr.nansty.geogame.location.LocationLiveData
import fr.nansty.geogame.poi.AKATOR
import fr.nansty.geogame.poi.Poi
import kotlinx.android.synthetic.main.activity_main.*
import timber.log.Timber
import java.lang.Exception

private const val REQUEST_PERMISSION_LOCATION_START_UPDATE = 2
private const val REQUEST_CHECK_SETTINGS = 1

class MapActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var viewModel: MapViewModel
    private lateinit var map: GoogleMap
    private lateinit var locationLiveData: LocationLiveData
    private lateinit var userMarker: Marker
    private lateinit var geofenceManager: GeofenceManager

    private var firsLocation = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

    val mapOptions = GoogleMapOptions()
        .mapType(GoogleMap.MAP_TYPE_NORMAL)
        .zoomControlsEnabled(true)
        .zoomGesturesEnabled(true)

        val mapFragment = SupportMapFragment.newInstance(mapOptions)
        mapFragment.getMapAsync(this)

        supportFragmentManager.beginTransaction().replace(R.id.content, mapFragment).commit()

        geofenceManager = GeofenceManager(this)

        locationLiveData = LocationLiveData(this)
        locationLiveData.observe(this, Observer { handleLocationData(it!!) })

        viewModel = ViewModelProvider(this).get(MapViewModel::class.java)
        viewModel.getUiState().observe(this, Observer { updateUiState(it!!) })




    }

    private fun updateUiState(state: MapUiState) {
        Timber.i("$state")
        return when(state){
            is MapUiState.Error -> {
                //loadingProgressBar.hide()
                Toast.makeText(this,"Error ${state.errorMessage}", Toast.LENGTH_SHORT).show()
            }
            MapUiState.Loading -> {
                loadingProgressBar.show()
            }
            is MapUiState.PoiReady -> {
                loadingProgressBar.hide()
                state.userPoi?.let {poi ->
                    userMarker = addPoiToMapMarker(poi,map)
                }
                state.pois?.let {pois ->
                    for (poi in pois){
                        addPoiToMapMarker(poi, map)
                        if (poi.title == AKATOR){
                            geofenceManager.createGeofence(poi, 10000.0f, GEOFENCE_ID_AKATOR)
                        }
                    }
                }
                return
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode){
            REQUEST_CHECK_SETTINGS -> locationLiveData.startRequestLocation()
        }
    }

    //2
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // if no result or no granted permission, do nothing
        if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) return

        when (requestCode) {
            REQUEST_PERMISSION_LOCATION_START_UPDATE -> locationLiveData.startRequestLocation()
        }
    }

    private fun handleLocationData(locationData: LocationData) {
        if(handleLocationException(locationData.exception)){
            return
        }
        locationData.location?.let {
            val latLng = LatLng(it.latitude, it.longitude)

            if(firsLocation && ::map.isInitialized){
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 9f))
                firsLocation = false
                viewModel.loadPois(it.latitude, it.longitude)
            }
            if(::userMarker.isInitialized){
                userMarker.position = latLng
            }
        }
    }

    private fun handleLocationException(exception: Exception?): Boolean {
        exception ?: return false
        when(exception){
            is SecurityException -> checkLocationPermission(
                REQUEST_PERMISSION_LOCATION_START_UPDATE)
            is ResolvableApiException -> exception.startResolutionForResult(this, REQUEST_CHECK_SETTINGS)
        }
        return true
    }

    //Permission accorder ou pas
    private fun checkLocationPermission(requestCode: Int): Boolean {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                requestCode

            )
            return false
        }
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.activity_map_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId){
            R.id.generate_pois -> {
                refreshPoisFromCurrentLocation()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun refreshPoisFromCurrentLocation() {
        geofenceManager.removeAllGeofences()
        map.clear()
        viewModel.loadPois(userMarker.position.latitude, userMarker.position.longitude)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
    }
}

private fun addPoiToMapMarker(poi: Poi, map: GoogleMap) : Marker {
    val options = MarkerOptions()
        .position(LatLng(poi.latitude, poi.longitude))
        .title(poi.title)
        .snippet(poi.description)
    if (poi.iconId > 0) {
        options.icon(BitmapDescriptorFactory.fromResource(poi.iconId))
    } else if (poi.iconColor != 0) {
        val hue = when (poi.iconColor) {
            Color.BLUE -> BitmapDescriptorFactory.HUE_AZURE
            Color.GREEN -> BitmapDescriptorFactory.HUE_GREEN
            Color.YELLOW -> BitmapDescriptorFactory.HUE_YELLOW
            Color.RED -> BitmapDescriptorFactory.HUE_RED
            else -> BitmapDescriptorFactory.HUE_RED
        }
        options.icon(BitmapDescriptorFactory.defaultMarker(hue))
    }
    val marker = map.addMarker(options)
    marker.tag = poi
    return marker
}