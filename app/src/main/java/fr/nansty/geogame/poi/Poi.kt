package fr.nansty.geogame.poi

/**
 *
 */

data class Poi(val title: String,
               var latitude: Double,
               var longitude: Double,
               var imageId: Int = 0,
               val iconId: Int = 0,
               val iconColor: Int = 0,
               val description: String = "",
               val detailUrl: String = "") {
}