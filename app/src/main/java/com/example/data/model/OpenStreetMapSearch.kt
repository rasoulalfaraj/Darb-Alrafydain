package com.example.data.model

import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

internal object OpenStreetMapSearch {
    suspend fun findPlaces(query: String): List<IraqPlace> = withContext(Dispatchers.IO) {
        val url = Uri.parse("https://photon.komoot.io/api/").buildUpon()
            .appendQueryParameter("q", query)
            .appendQueryParameter(
                "bbox",
                "${IraqMapCatalog.IRAQ_MIN_LNG},${IraqMapCatalog.IRAQ_MIN_LAT}," +
                    "${IraqMapCatalog.IRAQ_MAX_LNG},${IraqMapCatalog.IRAQ_MAX_LAT}"
            )
            .appendQueryParameter("limit", "20")
            .build()
        val connection = URL(url.toString()).openConnection() as HttpURLConnection
        try {
            connection.connectTimeout = 6000
            connection.readTimeout = 6000
            connection.setRequestProperty("User-Agent", "Darb-Alrafydain/1.0")
            connection.setRequestProperty("Accept", "application/json")
            if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                throw IOException("Photon HTTP ${connection.responseCode}")
            }
            val response = connection.inputStream.bufferedReader().use { it.readText() }
            val features = JSONObject(response).getJSONArray("features")
            buildList {
                for (index in 0 until features.length()) {
                    val feature = features.getJSONObject(index)
                    val properties = feature.getJSONObject("properties")
                    if (!properties.optString("countrycode").equals("IQ", ignoreCase = true)) continue
                    val geometry = feature.getJSONObject("geometry")
                    val coordinates = geometry.getJSONArray("coordinates")
                    val latitude = coordinates.getDouble(1)
                    val longitude = coordinates.getDouble(0)
                    if (!IraqMapCatalog.isInsideIraq(latitude, longitude)) continue

                    val name = properties.optString("name").ifBlank { properties.optString("street") }
                    if (name.isBlank()) continue
                    val category = when (properties.optString("osm_value")) {
                        "hospital", "clinic", "doctors" -> PlaceCategory.HOSPITAL
                        "fuel", "charging_station" -> PlaceCategory.FUEL
                        "aerodrome", "university", "college" -> PlaceCategory.AIRPORT_UNI
                        "restaurant", "marketplace", "mall", "cafe" -> PlaceCategory.MARKET_FOOD
                        else -> PlaceCategory.LANDMARK
                    }
                    val state = properties.optString("state")
                    val governorate = IraqMapCatalog.IRAQ_GOVERNORATES.firstOrNull { governorate ->
                        IraqMapCatalog.normalizeArabic(state.removePrefix("محافظة").trim()) ==
                            IraqMapCatalog.normalizeArabic(governorate.nameAr)
                    }?.nameAr ?: state
                    add(
                        IraqPlace(
                            id = "osm_${properties.optString("osm_type")}_${properties.getLong("osm_id")}",
                            nameAr = name,
                            governorateAr = governorate,
                            category = category,
                            addressAr = listOf(
                                properties.optString("street"),
                                properties.optString("city"),
                                state
                            ).filter { it.isNotBlank() && it != name }.distinct().joinToString("، "),
                            latitude = latitude,
                            longitude = longitude,
                            phoneOrCode = "",
                            openHoursAr = "ساعات العمل غير متوفرة",
                            rating = 0.0,
                            tagsAr = emptyList()
                        )
                    )
                }
            }.distinctBy { it.id }
        } finally {
            connection.disconnect()
        }
    }
}
