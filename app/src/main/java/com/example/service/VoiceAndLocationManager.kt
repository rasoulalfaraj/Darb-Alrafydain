package com.example.service

import android.annotation.SuppressLint
import android.content.Context
import android.os.Looper
import android.speech.tts.TextToSpeech
import com.example.data.model.IraqMapCatalog
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

data class GpsTelemetryState(
    val currentIraqLocation: LatLng = IraqMapCatalog.BAGHDAD_CENTER,
    val currentLocationNameAr: String = "بغداد - ساحة التحرير (المركز)",
    val rawDeviceLatLng: LatLng? = null,
    val isPhysicalGpsInsideIraq: Boolean = false,
    val accuracyMeters: Float = 4.5f,
    val speedKmh: Int = 0,
    val hasLocationPermission: Boolean = false
)

class VoiceAndLocationManager(context: Context) {
    private val appContext = context.applicationContext
    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(appContext)

    private var tts: TextToSpeech? = null
    private var isTtsReady = false

    private val _lastSpokenText = MutableStateFlow("التوجيه الصوتي العربي جاهز للعمل في جميع محافظات العراق")
    val lastSpokenText: StateFlow<String> = _lastSpokenText.asStateFlow()

    private val _ttsEngineStatusAr = MutableStateFlow("محرك النطق العربي: متصل")
    val ttsEngineStatusAr: StateFlow<String> = _ttsEngineStatusAr.asStateFlow()

    private val _gpsState = MutableStateFlow(GpsTelemetryState())
    val gpsState: StateFlow<GpsTelemetryState> = _gpsState.asStateFlow()

    private var locationCallback: LocationCallback? = null

    init {
        initArabicTextToSpeech()
    }

    private fun initArabicTextToSpeech() {
        try {
            tts = TextToSpeech(appContext) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    val iraqArabic = Locale.forLanguageTag("ar-IQ")
                    val genericArabic = Locale.forLanguageTag("ar")
                    val res = tts?.setLanguage(iraqArabic) ?: TextToSpeech.LANG_NOT_SUPPORTED
                    if (res == TextToSpeech.LANG_MISSING_DATA || res == TextToSpeech.LANG_NOT_SUPPORTED) {
                        tts?.setLanguage(genericArabic)
                    }
                    tts?.setPitch(1.0f)
                    tts?.setSpeechRate(0.96f)
                    isTtsReady = true
                    _ttsEngineStatusAr.value = "محرك النطق العربي (العراق): جاهز وبصوت واضح"
                } else {
                    _ttsEngineStatusAr.value = "التوجيه الصوتي النصي والاهتزاز الذكي مفعّل"
                }
            }
        } catch (_: Exception) {
            _ttsEngineStatusAr.value = "التوجيه الصوتي النصي المدمج مفعّل"
        }
    }

    fun speakArabic(promptAr: String, voiceEnabled: Boolean) {
        _lastSpokenText.value = promptAr
        if (!voiceEnabled) return
        try {
            if (isTtsReady) {
                tts?.speak(promptAr, TextToSpeech.QUEUE_FLUSH, null, "iraq_nav_${System.currentTimeMillis()}")
            }
        } catch (_: Exception) {
            // يستمر عرض الإرشاد الصوتي على الشاشة حتى لو لم يتوفر محرك صوت في المحاكي
        }
    }

    @SuppressLint("MissingPermission")
    fun startHighAccuracyLocationUpdates(hasPermission: Boolean) {
        _gpsState.value = _gpsState.value.copy(hasLocationPermission = hasPermission)
        if (!hasPermission) return

        try {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    handleIncomingLocation(location.latitude, location.longitude, location.accuracy, location.speed)
                }
            }

            val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 4000L)
                .setMinUpdateIntervalMillis(2000L)
                .build()

            locationCallback?.let { fusedLocationClient.removeLocationUpdates(it) }
            val callback = object : LocationCallback() {
                override fun onLocationResult(result: LocationResult) {
                    val loc = result.lastLocation ?: return
                    handleIncomingLocation(loc.latitude, loc.longitude, loc.accuracy, loc.speed)
                }
            }
            locationCallback = callback
            fusedLocationClient.requestLocationUpdates(request, callback, Looper.getMainLooper())
        } catch (_: Exception) {
            // في حال عدم توفر خدمات الموقع في المحاكي، نعتمد الموقع المرجعي داخل العراق
        }
    }

    private fun handleIncomingLocation(lat: Double, lng: Double, accuracy: Float, speedMetersPerSec: Float) {
        val raw = LatLng(lat, lng)
        val insideIraq = IraqMapCatalog.isInsideIraq(lat, lng)
        if (insideIraq) {
            val nearestPlace = IraqMapCatalog.IRAQ_PLACES.minByOrNull {
                IraqMapCatalog.calculateDistanceKm(lat, lng, it.latitude, it.longitude)
            }
            val label = if (nearestPlace != null) {
                "موقعك الحي قرب ${nearestPlace.nameAr} (${nearestPlace.governorateAr})"
            } else {
                "موقعك الجغرافي الحي داخل العراق"
            }
            _gpsState.value = _gpsState.value.copy(
                currentIraqLocation = raw,
                currentLocationNameAr = label,
                rawDeviceLatLng = raw,
                isPhysicalGpsInsideIraq = true,
                accuracyMeters = accuracy.coerceAtLeast(3.0f),
                speedKmh = (speedMetersPerSec * 3.6f).toInt().coerceAtLeast(0)
            )
        } else {
            // إذا كان الجهاز أو المحاكي خارج حدود العراق، نحافظ على قفل الخريطة داخل العراق
            _gpsState.value = _gpsState.value.copy(
                rawDeviceLatLng = raw,
                isPhysicalGpsInsideIraq = false,
                accuracyMeters = accuracy.coerceAtLeast(4.0f)
            )
        }
    }

    fun setManualIraqAnchor(latLng: LatLng, labelAr: String, simulatedSpeedKmh: Int = 0) {
        val clamped = IraqMapCatalog.clampToIraq(latLng.latitude, latLng.longitude)
        _gpsState.value = _gpsState.value.copy(
            currentIraqLocation = clamped,
            currentLocationNameAr = labelAr,
            speedKmh = simulatedSpeedKmh
        )
    }

    fun shutdown() {
        try {
            locationCallback?.let { fusedLocationClient.removeLocationUpdates(it) }
            tts?.stop()
            tts?.shutdown()
        } catch (_: Exception) {
        }
    }
}
