package fr.nansty.geogame.location

import android.location.Location
import java.lang.Exception

data class LocationData(val location: Location? = null, val exception: Exception? = null)