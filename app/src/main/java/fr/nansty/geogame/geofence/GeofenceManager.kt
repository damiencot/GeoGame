package fr.nansty.geogame.geofence

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import fr.nansty.geogame.poi.Poi
import timber.log.Timber

const val GEOFENCE_ID_AKATOR = "Akator"

class GeofenceManager(context: Context) {

    private val appContext = context.applicationContext

    private val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(appContext, GeofenceIntentService::class.java)
        PendingIntent.getService(
            appContext, 0,intent,PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    private val geofencingClient = LocationServices.getGeofencingClient(appContext)
    private val geofenceList = mutableListOf<Geofence>()

    @SuppressLint("MissingPermission")
    fun createGeofence(poi: Poi, radiusMeter: Float, requestId: String){
        Timber.d("Creating geofence at coordinates ${poi.latitude}, ${poi.longitude}")

        geofenceList.add(
            Geofence.Builder()
                .setRequestId(requestId)
                .setExpirationDuration(10 * 60 * 1000)
                .setCircularRegion(poi.latitude, poi.longitude, radiusMeter)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT)
                .build()
        )

        val task = geofencingClient.addGeofences(getGeofencingReuquest(), geofencePendingIntent)
        task.addOnSuccessListener {
            Timber.i("Geofence added")
        }
        task.addOnFailureListener {
            Timber.i("Geofence cannot add")

        }
    }

    private fun getGeofencingReuquest(): GeofencingRequest {
        return GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofences(geofenceList)
            .build()
    }

    fun removeAllGeofences(){
        geofencingClient.removeGeofences(geofencePendingIntent)
        geofenceList.clear()
    }
}