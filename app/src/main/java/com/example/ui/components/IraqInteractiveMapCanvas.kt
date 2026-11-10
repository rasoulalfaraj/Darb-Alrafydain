package com.example.ui.components

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import com.example.data.local.OfflineGovernorateEntity
import com.example.data.model.ActiveIraqRoute
import com.example.data.model.IraqMapCatalog
import com.example.data.model.IraqPlace
import com.example.data.model.PlaceCategory
import com.example.ui.theme.AlertCrimson
import com.example.ui.theme.BabylonGold
import com.example.ui.theme.EuphratesBlue
import com.example.ui.theme.RouteEmerald
import com.example.ui.theme.TigrisTurquoise
import com.example.ui.viewmodel.PlaceWithDistance
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import kotlin.math.hypot
import kotlin.math.pow

@Composable
fun IraqDualEngineMapView(
    preferGoogleMaps: Boolean,
    isOfflineMode: Boolean,
    mapStyleMode: Int,
    cameraTarget: LatLng,
    cameraZoom: Float,
    userIraqLocation: LatLng,
    userLocationLabelAr: String,
    selectedPlace: IraqPlace?,
    nearbyPlaces: List<PlaceWithDistance>,
    offlineGovernorates: List<OfflineGovernorateEntity>,
    activeRoute: ActiveIraqRoute?,
    currentStepIndex: Int,
    onSelectPlace: (IraqPlace) -> Unit,
    onTapMapLatLng: (LatLng) -> Unit,
    modifier: Modifier = Modifier
) {
    val useGoogleSdk = preferGoogleMaps && !isOfflineMode

    Box(modifier = modifier.fillMaxSize().testTag("iraq_map_container")) {
        if (useGoogleSdk) {
            GoogleMapsIraqView(
                mapStyleMode = mapStyleMode,
                cameraTarget = cameraTarget,
                cameraZoom = cameraZoom,
                userIraqLocation = userIraqLocation,
                userLocationLabelAr = userLocationLabelAr,
                selectedPlace = selectedPlace,
                nearbyPlaces = nearbyPlaces,
                activeRoute = activeRoute,
                onSelectPlace = onSelectPlace,
                onTapMapLatLng = onTapMapLatLng
            )
        } else {
            IraqVectorOfflineMapCanvas(
                mapStyleMode = mapStyleMode,
                cameraTarget = cameraTarget,
                cameraZoom = cameraZoom,
                userIraqLocation = userIraqLocation,
                selectedPlace = selectedPlace,
                nearbyPlaces = nearbyPlaces,
                offlineGovernorates = offlineGovernorates,
                activeRoute = activeRoute,
                currentStepIndex = currentStepIndex,
                onSelectPlace = onSelectPlace,
                onTapMapLatLng = onTapMapLatLng
            )
        }
    }
}

@Composable
private fun GoogleMapsIraqView(
    mapStyleMode: Int,
    cameraTarget: LatLng,
    cameraZoom: Float,
    userIraqLocation: LatLng,
    userLocationLabelAr: String,
    selectedPlace: IraqPlace?,
    nearbyPlaces: List<PlaceWithDistance>,
    activeRoute: ActiveIraqRoute?,
    onSelectPlace: (IraqPlace) -> Unit,
    onTapMapLatLng: (LatLng) -> Unit
) {
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(cameraTarget, cameraZoom)
    }

    LaunchedEffect(cameraTarget, cameraZoom) {
        try {
            cameraPositionState.animate(
                update = CameraUpdateFactory.newLatLngZoom(cameraTarget, cameraZoom),
                durationMs = 650
            )
        } catch (_: Exception) {
            cameraPositionState.position = CameraPosition.fromLatLngZoom(cameraTarget, cameraZoom)
        }
    }

    val googleMapType = when (mapStyleMode) {
        1 -> MapType.HYBRID
        2 -> MapType.TERRAIN
        else -> MapType.NORMAL
    }

    GoogleMap(
        modifier = Modifier.fillMaxSize().testTag("google_map_iraq"),
        cameraPositionState = cameraPositionState,
        properties = MapProperties(
            mapType = googleMapType,
            isTrafficEnabled = true,
            latLngBoundsForCameraTarget = IraqMapCatalog.IRAQ_LAT_LNG_BOUNDS,
            minZoomPreference = 5.5f,
            maxZoomPreference = 19.0f
        ),
        uiSettings = MapUiSettings(
            zoomControlsEnabled = false,
            compassEnabled = true,
            myLocationButtonEnabled = false,
            mapToolbarEnabled = false
        ),
        onMapClick = { latLng ->
            onTapMapLatLng(latLng)
        }
    ) {
        // حدود العراق الإرشادية على خرائط جوجل
        Polyline(
            points = IraqMapCatalog.IRAQ_BORDER_POLYGON,
            color = BabylonGold.copy(alpha = 0.75f),
            width = 6f
        )

        // مسار الملاحة النشط
        if (activeRoute != null) {
            Polyline(
                points = activeRoute.polylinePoints,
                color = RouteEmerald,
                width = 16f
            )
        }

        // مؤشر موقع المستخدم في العراق
        Marker(
            state = MarkerState(position = userIraqLocation),
            title = "موقعي الحالي",
            snippet = userLocationLabelAr,
            icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)
        )

        // دبابيس الأماكن القريبة في العراق
        nearbyPlaces.take(30).forEach { item ->
            val place = item.place
            val isSelected = selectedPlace?.id == place.id
            val hue = when {
                isSelected -> BitmapDescriptorFactory.HUE_YELLOW
                place.category == PlaceCategory.HOSPITAL -> BitmapDescriptorFactory.HUE_RED
                place.category == PlaceCategory.FUEL -> BitmapDescriptorFactory.HUE_ORANGE
                place.category == PlaceCategory.LANDMARK -> BitmapDescriptorFactory.HUE_CYAN
                else -> BitmapDescriptorFactory.HUE_GREEN
            }
            Marker(
                state = MarkerState(position = LatLng(place.latitude, place.longitude)),
                title = place.nameAr,
                snippet = "${place.governorateAr} • ${item.distanceKm} كم",
                icon = BitmapDescriptorFactory.defaultMarker(hue),
                onClick = {
                    onSelectPlace(place)
                    false
                }
            )
        }
    }
}

@Composable
private fun IraqVectorOfflineMapCanvas(
    mapStyleMode: Int,
    cameraTarget: LatLng,
    cameraZoom: Float,
    userIraqLocation: LatLng,
    selectedPlace: IraqPlace?,
    nearbyPlaces: List<PlaceWithDistance>,
    offlineGovernorates: List<OfflineGovernorateEntity>,
    activeRoute: ActiveIraqRoute?,
    currentStepIndex: Int,
    onSelectPlace: (IraqPlace) -> Unit,
    onTapMapLatLng: (LatLng) -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "iraq_map_pulse")
    val pulseRadiusFactor by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "gps_pulse"
    )

    // حالة التحريك والتكبير باللمس داخل خارطة العراق
    var centerLat by remember { mutableFloatStateOf(cameraTarget.latitude.toFloat()) }
    var centerLng by remember { mutableFloatStateOf(cameraTarget.longitude.toFloat()) }
    var zoomScale by remember { mutableFloatStateOf(2.0f.pow((cameraZoom - 6.0f).coerceIn(0f, 6.5f))) }
    var canvasWidthPx by remember { mutableFloatStateOf(1080f) }
    var canvasHeightPx by remember { mutableFloatStateOf(1920f) }
    var hitTargets by remember { mutableStateOf<List<Pair<Offset, IraqPlace>>>(emptyList()) }

    LaunchedEffect(cameraTarget, cameraZoom) {
        centerLat = cameraTarget.latitude.toFloat().coerceIn(
            IraqMapCatalog.IRAQ_MIN_LAT.toFloat(),
            IraqMapCatalog.IRAQ_MAX_LAT.toFloat()
        )
        centerLng = cameraTarget.longitude.toFloat().coerceIn(
            IraqMapCatalog.IRAQ_MIN_LNG.toFloat(),
            IraqMapCatalog.IRAQ_MAX_LNG.toFloat()
        )
        zoomScale = 2.0f.pow(((cameraZoom - 6.0f) * 0.55f).coerceIn(0f, 4.5f))
    }

    val bgBrush = when (mapStyleMode) {
        1 -> Brush.verticalGradient(listOf(Color(0xFF040D14), Color(0xFF091B29), Color(0xFF05111B)))
        2 -> Brush.verticalGradient(listOf(Color(0xFF1D1912), Color(0xFF282218), Color(0xFF17140E)))
        else -> Brush.verticalGradient(listOf(Color(0xFF061624), Color(0xFF0B2336), Color(0xFF071826)))
    }

    val iraqLandColor = when (mapStyleMode) {
        1 -> Color(0xFF0E293D)
        2 -> Color(0xFF382F21)
        else -> Color(0xFF112C42)
    }

    val textPaintAr = remember {
        Paint().apply {
            isAntiAlias = true
            color = android.graphics.Color.WHITE
            textSize = 28f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            setShadowLayer(6f, 0f, 2f, android.graphics.Color.BLACK)
        }
    }

    val subTextPaintAr = remember {
        Paint().apply {
            isAntiAlias = true
            color = android.graphics.Color.argb(215, 180, 225, 245)
            textSize = 22f
            textAlign = Paint.Align.CENTER
            setShadowLayer(4f, 0f, 1f, android.graphics.Color.BLACK)
        }
    }

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .background(bgBrush)
            .testTag("iraq_vector_map_canvas")
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    zoomScale = (zoomScale * zoom).coerceIn(0.85f, 18.0f)
                    val baseSpan = 9.6f / zoomScale
                    val dLng = -(pan.x / canvasWidthPx.coerceAtLeast(1f)) * baseSpan
                    val dLat = (pan.y / canvasHeightPx.coerceAtLeast(1f)) * baseSpan
                    centerLng = (centerLng + dLng).coerceIn(
                        IraqMapCatalog.IRAQ_MIN_LNG.toFloat() + 0.3f,
                        IraqMapCatalog.IRAQ_MAX_LNG.toFloat() - 0.3f
                    )
                    centerLat = (centerLat + dLat).coerceIn(
                        IraqMapCatalog.IRAQ_MIN_LAT.toFloat() + 0.3f,
                        IraqMapCatalog.IRAQ_MAX_LAT.toFloat() - 0.3f
                    )
                }
            }
            .pointerInput(nearbyPlaces, selectedPlace) {
                detectTapGestures { tapOffset ->
                    val tappedPlace = hitTargets.minByOrNull { (pos, _) ->
                        hypot(pos.x - tapOffset.x, pos.y - tapOffset.y)
                    }
                    if (tappedPlace != null && hypot(tappedPlace.first.x - tapOffset.x, tappedPlace.first.y - tapOffset.y) <= 58f) {
                        onSelectPlace(tappedPlace.second)
                    } else {
                        val minDim = minOf(canvasWidthPx, canvasHeightPx).coerceAtLeast(1f)
                        val degPerPx = (9.5f / zoomScale) / minDim
                        val tappedLng = centerLng + ((tapOffset.x - canvasWidthPx / 2f) * degPerPx) / 0.84f
                        val tappedLat = centerLat - (tapOffset.y - canvasHeightPx / 2f) * degPerPx
                        onTapMapLatLng(LatLng(tappedLat.toDouble(), tappedLng.toDouble()))
                    }
                }
            }
    ) {
        canvasWidthPx = size.width
        canvasHeightPx = size.height
        val minDim = minOf(size.width, size.height).coerceAtLeast(1f)
        val degPerPx = (9.5f / zoomScale) / minDim

        fun project(lat: Double, lng: Double): Offset {
            val x = (size.width / 2f) + (((lng.toFloat() - centerLng) * 0.84f) / degPerPx)
            val y = (size.height / 2f) - ((lat.toFloat() - centerLat) / degPerPx)
            return Offset(x, y)
        }

        // 1. شبكة خطوط الطول والعرض لجمهورية العراق
        val gridColor = Color.White.copy(alpha = 0.06f)
        for (latInt in 29..38) {
            val y = project(latInt.toDouble(), centerLng.toDouble()).y
            if (y in 0f..size.height) {
                drawLine(gridColor, Offset(0f, y), Offset(size.width, y), strokeWidth = 1.2f)
            }
        }
        for (lngInt in 39..49) {
            val x = project(centerLat.toDouble(), lngInt.toDouble()).x
            if (x in 0f..size.width) {
                drawLine(gridColor, Offset(x, 0f), Offset(x, size.height), strokeWidth = 1.2f)
            }
        }

        // 2. رسم مضلع خارطة جمهورية العراق
        val borderPath = Path()
        IraqMapCatalog.IRAQ_BORDER_POLYGON.forEachIndexed { idx, pt ->
            val pos = project(pt.latitude, pt.longitude)
            if (idx == 0) borderPath.moveTo(pos.x, pos.y) else borderPath.lineTo(pos.x, pos.y)
        }
        borderPath.close()

        drawPath(path = borderPath, color = iraqLandColor)
        drawPath(
            path = borderPath,
            color = TigrisTurquoise.copy(alpha = 0.45f),
            style = Stroke(width = 6f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
        drawPath(
            path = borderPath,
            color = BabylonGold.copy(alpha = 0.85f),
            style = Stroke(
                width = 2.5f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(18f, 10f), 0f)
            )
        )

        // 3. رسم نهري دجلة والفرات الخالدين
        val tigrisPath = Path()
        IraqMapCatalog.TIGRIS_RIVER_POINTS.forEachIndexed { idx, pt ->
            val pos = project(pt.latitude, pt.longitude)
            if (idx == 0) tigrisPath.moveTo(pos.x, pos.y) else tigrisPath.lineTo(pos.x, pos.y)
        }
        drawPath(
            path = tigrisPath,
            color = TigrisTurquoise.copy(alpha = 0.55f),
            style = Stroke(width = (5.5f * zoomScale.coerceAtMost(2.5f)), cap = StrokeCap.Round, join = StrokeJoin.Round)
        )

        val euphratesPath = Path()
        IraqMapCatalog.EUPHRATES_RIVER_POINTS.forEachIndexed { idx, pt ->
            val pos = project(pt.latitude, pt.longitude)
            if (idx == 0) euphratesPath.moveTo(pos.x, pos.y) else euphratesPath.lineTo(pos.x, pos.y)
        }
        drawPath(
            path = euphratesPath,
            color = EuphratesBlue.copy(alpha = 0.55f),
            style = Stroke(width = (5.0f * zoomScale.coerceAtMost(2.5f)), cap = StrokeCap.Round, join = StrokeJoin.Round)
        )

        // 4. رسم شبكة الطرق السريعة الوطنية في العراق
        IraqMapCatalog.IRAQ_HIGHWAYS.forEach { highway ->
            val hwPath = Path()
            highway.points.forEachIndexed { idx, pt ->
                val pos = project(pt.latitude, pt.longitude)
                if (idx == 0) hwPath.moveTo(pos.x, pos.y) else hwPath.lineTo(pos.x, pos.y)
            }
            drawPath(
                path = hwPath,
                color = BabylonGold.copy(alpha = 0.30f),
                style = Stroke(width = 3.5f, cap = StrokeCap.Round)
            )
        }

        // 5. رسم مراكز محافظات العراق الـ 18
        offlineGovernorates.forEach { gov ->
            val pos = project(gov.centerLat, gov.centerLng)
            if (pos.x in -100f..(size.width + 100f) && pos.y in -100f..(size.height + 100f)) {
                val dotColor = if (gov.isDownloaded) TigrisTurquoise else BabylonGold.copy(alpha = 0.7f)
                drawCircle(
                    color = dotColor.copy(alpha = 0.22f),
                    radius = 16f,
                    center = pos
                )
                drawCircle(
                    color = dotColor,
                    radius = 6.5f,
                    center = pos
                )
                drawContext.canvas.nativeCanvas.drawText(
                    gov.nameAr,
                    pos.x,
                    pos.y + 30f,
                    subTextPaintAr
                )
            }
        }

        // 6. رسم مسار الملاحة النشط وخطوات التوجيه الصوتي
        if (activeRoute != null && activeRoute.polylinePoints.size >= 2) {
            val routePath = Path()
            activeRoute.polylinePoints.forEachIndexed { idx, pt ->
                val pos = project(pt.latitude, pt.longitude)
                if (idx == 0) routePath.moveTo(pos.x, pos.y) else routePath.lineTo(pos.x, pos.y)
            }
            // توهج خارجي للمسار
            drawPath(
                path = routePath,
                color = RouteEmerald.copy(alpha = 0.32f),
                style = Stroke(width = 22f, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
            // خط المسار الرئيسي
            drawPath(
                path = routePath,
                color = RouteEmerald,
                style = Stroke(width = 9f, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
            // خط متقطع ذهبي فوق المسار للدلالة على اتجاه السير
            drawPath(
                path = routePath,
                color = Color.White.copy(alpha = 0.85f),
                style = Stroke(
                    width = 3.5f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(16f, 18f), pulseRadiusFactor * 34f)
                )
            )

            // نقاط المناورات على طول المسار
            activeRoute.steps.forEachIndexed { stepIdx, step ->
                val stepPos = project(step.location.latitude, step.location.longitude)
                val isCurrentStep = stepIdx == currentStepIndex
                drawCircle(
                    color = if (isCurrentStep) BabylonGold else Color.White,
                    radius = if (isCurrentStep) 13f else 7f,
                    center = stepPos
                )
            }
        }

        // 7. رسم دبابيس الأماكن القريبة في العراق
        val computedTargets = mutableListOf<Pair<Offset, IraqPlace>>()
        nearbyPlaces.take(28).forEach { item ->
            val place = item.place
            val pos = project(place.latitude, place.longitude)
            if (pos.x in -60f..(size.width + 60f) && pos.y in -60f..(size.height + 60f)) {
                computedTargets.add(pos to place)
                val isSelected = selectedPlace?.id == place.id
                val pinColor = when (place.category) {
                    PlaceCategory.HOSPITAL -> AlertCrimson
                    PlaceCategory.FUEL -> BabylonGold
                    PlaceCategory.LANDMARK -> TigrisTurquoise
                    PlaceCategory.AIRPORT_UNI -> EuphratesBlue
                    else -> RouteEmerald
                }

                if (isSelected) {
                    drawCircle(
                        color = BabylonGold.copy(alpha = 0.35f),
                        radius = 34f * (0.75f + pulseRadiusFactor * 0.4f),
                        center = pos
                    )
                }

                drawCircle(
                    color = Color(0xFF061522),
                    radius = if (isSelected) 16f else 11f,
                    center = pos
                )
                drawCircle(
                    color = if (isSelected) BabylonGold else pinColor,
                    radius = if (isSelected) 12.5f else 8.5f,
                    center = pos
                )

                if (isSelected || zoomScale >= 1.75f) {
                    drawContext.canvas.nativeCanvas.drawText(
                        place.nameAr.take(22),
                        pos.x,
                        pos.y - 20f,
                        textPaintAr
                    )
                }
            }
        }
        hitTargets = computedTargets

        // 8. رسم مؤشر موقع المستخدم الحالي في العراق (GPS Beacon)
        val userPos = project(userIraqLocation.latitude, userIraqLocation.longitude)
        drawCircle(
            color = EuphratesBlue.copy(alpha = (1f - pulseRadiusFactor) * 0.45f),
            radius = 18f + pulseRadiusFactor * 42f,
            center = userPos
        )
        drawCircle(
            color = Color.White,
            radius = 13f,
            center = userPos
        )
        drawCircle(
            color = EuphratesBlue,
            radius = 9.5f,
            center = userPos
        )
    }
}
