package com.example.ui.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.data.model.ActiveIraqRoute
import com.example.data.model.IraqMapCatalog
import com.example.data.model.IraqPlace
import com.example.ui.theme.RouteEmerald
import com.example.ui.viewmodel.PlaceWithDistance
import com.google.android.gms.maps.model.LatLng
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.FolderOverlay
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline

@Composable
internal fun OpenStreetMapIraqView(
    cameraTarget: LatLng,
    cameraZoom: Float,
    userLocation: LatLng,
    selectedPlace: IraqPlace?,
    nearbyPlaces: List<PlaceWithDistance>,
    activeRoute: ActiveIraqRoute?,
    onSelectPlace: (IraqPlace) -> Unit,
    onTapMap: (LatLng) -> Unit
) {
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val selectPlace = rememberUpdatedState(onSelectPlace)
    val tapMap = rememberUpdatedState(onTapMap)
    val contentOverlay = remember { FolderOverlay() }
    val map = remember(context) {
        val config = Configuration.getInstance()
        config.osmdroidBasePath = context.cacheDir.resolve("osm")
        config.osmdroidTileCache = config.osmdroidBasePath.resolve("tiles")
        config.userAgentValue = "Darb-Alrafydain/1.0 (+https://github.com/rasoulalfaraj/Darb-Alrafydain)"
        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            setHorizontalMapRepetitionEnabled(false)
            setVerticalMapRepetitionEnabled(false)
            setScrollableAreaLimitDouble(
                BoundingBox(
                    IraqMapCatalog.IRAQ_MAX_LAT, IraqMapCatalog.IRAQ_MAX_LNG,
                    IraqMapCatalog.IRAQ_MIN_LAT, IraqMapCatalog.IRAQ_MIN_LNG
                )
            )
            minZoomLevel = 5.0
            maxZoomLevel = 19.0
            overlays.add(MapEventsOverlay(object : MapEventsReceiver {
                override fun singleTapConfirmedHelper(point: GeoPoint): Boolean {
                    tapMap.value(LatLng(point.latitude, point.longitude))
                    return true
                }

                override fun longPressHelper(point: GeoPoint): Boolean = false
            }))
            overlays.add(contentOverlay)
        }
    }

    AndroidView(
        factory = { map },
        modifier = Modifier.fillMaxSize().testTag("openstreetmap_iraq")
    )

    DisposableEffect(map, lifecycle) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> map.onResume()
                Lifecycle.Event.ON_PAUSE -> map.onPause()
                else -> Unit
            }
        }
        lifecycle.addObserver(observer)
        onDispose {
            lifecycle.removeObserver(observer)
            map.onPause()
            map.onDetach()
        }
    }

    LaunchedEffect(map, cameraZoom) {
        map.controller.setZoom(cameraZoom.toDouble())
    }
    LaunchedEffect(map, cameraTarget) {
        map.controller.setCenter(cameraTarget.toGeoPoint())
    }

    LaunchedEffect(map, userLocation, selectedPlace, nearbyPlaces, activeRoute) {
        contentOverlay.items.filterIsInstance<Marker>().forEach { it.closeInfoWindow() }
        contentOverlay.items.clear()
        if (activeRoute != null) {
            contentOverlay.add(Polyline(map).apply {
                setPoints(activeRoute.polylinePoints.map { it.toGeoPoint() })
                outlinePaint.color = RouteEmerald.toArgb()
                outlinePaint.strokeWidth = 10f
            })
        }
        contentOverlay.add(Marker(map).apply {
            position = userLocation.toGeoPoint()
            title = "موقعي الحالي"
        })
        val places = nearbyPlaces.take(30).map { it.place }.filter { it.id != selectedPlace?.id }
        (places + listOfNotNull(selectedPlace)).forEach { place ->
            contentOverlay.add(Marker(map).apply {
                position = GeoPoint(place.latitude, place.longitude)
                title = place.nameAr
                setOnMarkerClickListener { marker, _ ->
                    selectPlace.value(place)
                    marker.showInfoWindow()
                    true
                }
            })
        }
        map.invalidate()
    }
}

private fun LatLng.toGeoPoint() = GeoPoint(latitude, longitude)
