package fr.nansty.geogame.location

import android.content.Context
import androidx.lifecycle.LiveData
import com.google.android.gms.location.*
import timber.log.Timber

class LocationLiveData(context: Context) : LiveData<LocationData>() {
    private val appContext = context.applicationContext
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(appContext)
    private var firstSubscriber = true

    private val locationRequest = LocationRequest.create().apply {
        interval = 10000
        fastestInterval = 5000
        priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }


    private val locationCallback= object: LocationCallback(){
        override fun onLocationResult(locationResult: LocationResult) {
            locationResult ?:return
            for (location in locationResult.locations){
                value = LocationData(location = location)
            }
        }
    }

    override fun onActive() {
        super.onActive()
        if (firstSubscriber){
            requestLastLocation()
            requestLocation()
            firstSubscriber = false
        }
    }

    override fun onInactive() {
        super.onInactive()
        fusedLocationClient.removeLocationUpdates(locationCallback)
        firstSubscriber = true
    }

    fun startRequestLocation(){
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        val client = LocationServices.getSettingsClient(appContext)

        val task = client.checkLocationSettings(builder.build())
        task.addOnSuccessListener { locationSettingsResponse ->
            Timber.i("Location settings satified")
            requestLocation()
        }

        task.addOnFailureListener { exception ->
            Timber.e(exception, "Failed to modify location settings")
            value = LocationData(exception = exception)
        }
    }

    private fun requestLocation() {
        try {
            fusedLocationClient.requestLocationUpdates(locationRequest,locationCallback,null)
        }catch (e: SecurityException){
            value = LocationData(exception = e)
        }
    }

    private fun requestLastLocation() {
        try {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                value = LocationData(location = location)
            }
            fusedLocationClient.lastLocation.addOnFailureListener { exception ->
                value = LocationData(exception = exception)
            }
        }catch (e: SecurityException){
            value = LocationData(exception = e)
        }
    }



}