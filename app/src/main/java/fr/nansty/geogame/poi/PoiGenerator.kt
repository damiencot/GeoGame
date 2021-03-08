package fr.nansty.geogame.poi

import android.graphics.Color
import fr.nansty.geogame.R

const val AKATOR = "Akator"
const val ANCIENT_EDYPTIAN = "Ancient Egyptian"


private data class Loc(val deltaX: Double,
                       val deltaY: Double)


private val locations = mapOf(
    AKATOR to Loc(
        -13.1,
        4.5
    ),
    ANCIENT_EDYPTIAN to Loc(-10.8, 4.5)
)

fun generateUserPoi(latitude: Double, longitude: Double): Poi {
    return Poi(
        title = "Indiana",
        latitude = latitude,
        longitude = longitude,
        imageId = R.drawable.indianajones,
        iconId = R.drawable.marker_indiana,
        detailUrl = "https://indianajones.fandom.com/wiki/Indiana_Jones",
        description = """
            Dr. Henry Walton Jones, Junior was an American archaeologist most famously known as Indiana Jones or Indy
        """.trimIndent()
    )
}

fun generatePois(latitude: Double, longitude: Double) : List<Poi> {
    return listOf(
        Poi(
            title = AKATOR,
            latitude = distToLat(
                AKATOR,
                latitude
            ),
            longitude = distToLong(
                AKATOR,
                longitude
            ),
            imageId = R.drawable.akator,
            iconColor = Color.BLUE,
            detailUrl = "https://indianajones.fandom.com/wiki/Akator",
            description = """
            Akator, also known as El Dorado or the Lost City of Gold, was a legendary city of knowledge built by Ugha natives with the help of Interdimensional beings, in the western part of the Amazon rain forest, in modern-day Brazil, near the Peruvian border. 
            """.trimIndent()
        ),
        Poi(
            title = ANCIENT_EDYPTIAN,
            latitude = distToLat(
                ANCIENT_EDYPTIAN,
                latitude
            ),
            longitude = distToLong(
                ANCIENT_EDYPTIAN,
                longitude
            ),
            imageId = R.drawable.tanis,
            iconColor = Color.GREEN,
            detailUrl = "https://indianajones.fandom.com/wiki/Tanis",
            description = """
                Tanis was an ancient Egyptian city, ruled by the Pharaoh Shishak, who placed the Ark of the Covenant within a secret chamber called the Well of the Souls.
            """.trimIndent()
        )
    )
}

private const val MULTIPLIER = 10.0
private const val ONE_KM_IN_DEG = 0.009009009009009

private fun distToLat(name: String, coordinate: Double) : Double {
    return coordinate + locations.getValue(name).deltaY * MULTIPLIER * ONE_KM_IN_DEG
}

private fun distToLong(name: String, coordinate: Double) : Double {
    return coordinate + locations.getValue(name).deltaX * MULTIPLIER * ONE_KM_IN_DEG
}