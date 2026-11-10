package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.data.local.FavoritePlaceEntity
import com.example.data.local.IraqDatabase
import com.example.data.local.IraqRepository
import com.example.data.local.NavigationHistoryEntity
import com.example.data.local.OfflineGovernorateEntity
import com.example.data.model.ActiveIraqRoute
import com.example.data.model.IraqMapCatalog
import com.example.data.model.IraqPlace
import com.example.data.model.PlaceCategory
import com.example.service.GpsTelemetryState
import com.example.service.VoiceAndLocationManager
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class IraqNavTab(val titleAr: String) {
    MAP_NAV("الخريطة والملاحة"),
    NEARBY_PLACES("الأماكن القريبة"),
    FAVORITES_HISTORY("المفضلة والسجل"),
    OFFLINE_AND_GITHUB("دون اتصال و APK")
}

data class PlaceWithDistance(
    val place: IraqPlace,
    val distanceKm: Double,
    val bearingAr: String,
    val isFavorite: Boolean
)

class IraqNavViewModel(application: Application) : AndroidViewModel(application) {
    private val database = IraqDatabase.getInstance(application)
    private val repository = IraqRepository(database.iraqDao())
    val voiceAndLocation = VoiceAndLocationManager(application)

    val hasConfiguredMapsApiKey: Boolean =
        BuildConfig.MAPS_API_KEY.isNotBlank() &&
            BuildConfig.MAPS_API_KEY != "YOUR_GOOGLE_MAPS_API_KEY_HERE" &&
            !BuildConfig.MAPS_API_KEY.startsWith("YOUR_")

    private val _currentTab = MutableStateFlow(IraqNavTab.MAP_NAV)
    val currentTab: StateFlow<IraqNavTab> = _currentTab.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow(PlaceCategory.ALL)
    val selectedCategory: StateFlow<PlaceCategory> = _selectedCategory.asStateFlow()

    private val _selectedGovernorateAr = MutableStateFlow("كل العراق")
    val selectedGovernorateAr: StateFlow<String> = _selectedGovernorateAr.asStateFlow()

    private val _isOfflineMode = MutableStateFlow(false)
    val isOfflineMode: StateFlow<Boolean> = _isOfflineMode.asStateFlow()

    // محرك الخريطة: يدعم التبديل بين Google Maps SDK والخريطة المتجهة التفاعلية للعراق
    private val _preferGoogleMapsEngine = MutableStateFlow(hasConfiguredMapsApiKey)
    val preferGoogleMapsEngine: StateFlow<Boolean> = _preferGoogleMapsEngine.asStateFlow()

    private val _mapStyleMode = MutableStateFlow(0) // 0: قياسي، 1: قمر صناعي هجين، 2: تضاريس العراق
    val mapStyleMode: StateFlow<Int> = _mapStyleMode.asStateFlow()

    private val _voiceGuidanceEnabled = MutableStateFlow(true)
    val voiceGuidanceEnabled: StateFlow<Boolean> = _voiceGuidanceEnabled.asStateFlow()

    private val _isDarkTheme = MutableStateFlow(true)
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    private val _selectedPlace = MutableStateFlow<IraqPlace?>(IraqMapCatalog.IRAQ_PLACES.first())
    val selectedPlace: StateFlow<IraqPlace?> = _selectedPlace.asStateFlow()

    private val _activeRoute = MutableStateFlow<ActiveIraqRoute?>(null)
    val activeRoute: StateFlow<ActiveIraqRoute?> = _activeRoute.asStateFlow()

    private val _currentStepIndex = MutableStateFlow(0)
    val currentStepIndex: StateFlow<Int> = _currentStepIndex.asStateFlow()

    private val _isLiveSimulating = MutableStateFlow(false)
    val isLiveSimulating: StateFlow<Boolean> = _isLiveSimulating.asStateFlow()

    private val _cameraTargetLatLng = MutableStateFlow(IraqMapCatalog.BAGHDAD_CENTER)
    val cameraTargetLatLng: StateFlow<LatLng> = _cameraTargetLatLng.asStateFlow()

    private val _cameraZoomLevel = MutableStateFlow(6.6f)
    val cameraZoomLevel: StateFlow<Float> = _cameraZoomLevel.asStateFlow()

    private val _customAddedPlaces = MutableStateFlow<List<IraqPlace>>(emptyList())

    private val _statusBannerAr = MutableStateFlow<String?>(null)
    val statusBannerAr: StateFlow<String?> = _statusBannerAr.asStateFlow()

    val gpsState: StateFlow<GpsTelemetryState> = voiceAndLocation.gpsState
    val lastSpokenVoiceText: StateFlow<String> = voiceAndLocation.lastSpokenText
    val ttsEngineStatusAr: StateFlow<String> = voiceAndLocation.ttsEngineStatusAr

    val favorites: StateFlow<List<FavoritePlaceEntity>> = repository.favorites.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = IraqMapCatalog.DEFAULT_FAVORITES
    )

    val history: StateFlow<List<NavigationHistoryEntity>> = repository.history.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = IraqMapCatalog.DEFAULT_HISTORY
    )

    val offlineGovernorates: StateFlow<List<OfflineGovernorateEntity>> = repository.offlineGovernorates.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = IraqMapCatalog.IRAQ_GOVERNORATES
    )

    // البحث الذكي الفوري وترتيب الأماكن القريبة حسب المسافة من موقع المستخدم داخل العراق
    val filteredNearbyPlaces: StateFlow<List<PlaceWithDistance>> = combine(
        _searchQuery,
        _selectedCategory,
        _selectedGovernorateAr,
        gpsState,
        favorites
    ) { query, category, gov, gps, favList ->
        val normalizedQuery = IraqMapCatalog.normalizeArabic(query)
        val allPlaces = IraqMapCatalog.IRAQ_PLACES + _customAddedPlaces.value

        allPlaces.filter { place ->
            val matchesCategory = category == PlaceCategory.ALL || place.category == category
            val matchesGov = gov == "كل العراق" || place.governorateAr == gov
            val matchesSearch = if (normalizedQuery.isBlank()) {
                true
            } else {
                val haystacks = listOf(
                    place.nameAr,
                    place.governorateAr,
                    place.addressAr,
                    place.category.labelAr
                ) + place.tagsAr + place.category.keywordsAr
                haystacks.any { IraqMapCatalog.normalizeArabic(it).contains(normalizedQuery) }
            }
            matchesCategory && matchesGov && matchesSearch
        }.map { place ->
            val dist = IraqMapCatalog.calculateDistanceKm(
                gps.currentIraqLocation.latitude,
                gps.currentIraqLocation.longitude,
                place.latitude,
                place.longitude
            )
            val bearing = IraqMapCatalog.calculateBearingAr(
                gps.currentIraqLocation.latitude,
                gps.currentIraqLocation.longitude,
                place.latitude,
                place.longitude
            )
            val isFav = favList.any {
                it.nameAr == place.nameAr ||
                    IraqMapCatalog.calculateDistanceKm(it.latitude, it.longitude, place.latitude, place.longitude) < 0.2
            }
            PlaceWithDistance(place, dist, bearing, isFav)
        }.sortedBy { it.distanceKm }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private var simulationJob: Job? = null

    init {
        viewModelScope.launch {
            repository.ensureSeedData(
                defaultGovernorates = IraqMapCatalog.IRAQ_GOVERNORATES,
                defaultFavorites = IraqMapCatalog.DEFAULT_FAVORITES,
                defaultHistory = IraqMapCatalog.DEFAULT_HISTORY
            )
        }
    }

    fun selectTab(tab: IraqNavTab) {
        _currentTab.value = tab
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectCategory(category: PlaceCategory) {
        _selectedCategory.value = category
    }

    fun selectGovernorate(govNameAr: String) {
        _selectedGovernorateAr.value = govNameAr
        if (govNameAr == "كل العراق") {
            _cameraTargetLatLng.value = IraqMapCatalog.BAGHDAD_CENTER
            _cameraZoomLevel.value = 6.3f
        } else {
            val gov = IraqMapCatalog.IRAQ_GOVERNORATES.find { it.nameAr == govNameAr }
            if (gov != null) {
                val target = LatLng(gov.centerLat, gov.centerLng)
                _cameraTargetLatLng.value = target
                _cameraZoomLevel.value = 11.2f
            }
        }
    }

    fun setCurrentOriginToGovernorate(govNameAr: String) {
        val gov = IraqMapCatalog.IRAQ_GOVERNORATES.find { it.nameAr == govNameAr } ?: return
        val latLng = LatLng(gov.centerLat, gov.centerLng)
        voiceAndLocation.setManualIraqAnchor(latLng, "${gov.nameAr} - ${gov.capitalAr}")
        _cameraTargetLatLng.value = latLng
        _cameraZoomLevel.value = 11.5f
        showTransientBanner("تم تثبيت نقطة الانطلاق في ${gov.nameAr} (${gov.capitalAr})")
    }

    fun selectPlaceOnMap(place: IraqPlace, zoomIn: Boolean = true) {
        _selectedPlace.value = place
        _cameraTargetLatLng.value = LatLng(place.latitude, place.longitude)
        if (zoomIn) {
            _cameraZoomLevel.value = 13.0f
        }
    }

    fun onMapTapInsideIraq(latLng: LatLng) {
        if (!IraqMapCatalog.isInsideIraq(latLng.latitude, latLng.longitude)) {
            showTransientBanner("التطبيق مخصص لخارطة جمهورية العراق فقط")
            return
        }
        val clamped = IraqMapCatalog.clampToIraq(latLng.latitude, latLng.longitude)
        val nearestGov = IraqMapCatalog.IRAQ_GOVERNORATES.minByOrNull {
            IraqMapCatalog.calculateDistanceKm(clamped.latitude, clamped.longitude, it.centerLat, it.centerLng)
        }?.nameAr ?: "بغداد"

        val customPlace = IraqPlace(
            id = "tap_${System.currentTimeMillis()}",
            nameAr = "موقع محدد على خارطة $nearestGov",
            governorateAr = nearestGov,
            category = PlaceCategory.LANDMARK,
            addressAr = "إحداثيات دقيقة: ${String.format("%.4f", clamped.latitude)}° شمالاً، ${String.format("%.4f", clamped.longitude)}° شرقاً",
            latitude = clamped.latitude,
            longitude = clamped.longitude,
            phoneOrCode = "GPS-IRAQ",
            openHoursAr = "متاح للملاحة 24 ساعة",
            rating = 4.8,
            tagsAr = listOf("تحديد يدوي", nearestGov, "إحداثيات")
        )
        _selectedPlace.value = customPlace
        _cameraTargetLatLng.value = clamped
    }

    fun startVoiceNavigationTo(place: IraqPlace) {
        simulationJob?.cancel()
        _isLiveSimulating.value = false

        val origin = gpsState.value.currentIraqLocation
        val originLabel = gpsState.value.currentLocationNameAr
        val route = IraqMapCatalog.buildIraqRoute(
            originLatLng = origin,
            originNameAr = originLabel,
            destination = place,
            isOffline = _isOfflineMode.value
        )
        _selectedPlace.value = place
        _activeRoute.value = route
        _currentStepIndex.value = 0
        _currentTab.value = IraqNavTab.MAP_NAV
        _cameraTargetLatLng.value = origin
        _cameraZoomLevel.value = 9.5f

        val firstVoice = route.steps.firstOrNull()?.voicePromptAr
            ?: "بدأ التوجيه الصوتي إلى ${place.nameAr} في ${place.governorateAr}"
        voiceAndLocation.speakArabic(firstVoice, _voiceGuidanceEnabled.value)

        viewModelScope.launch {
            repository.recordNavigation(
                NavigationHistoryEntity(
                    originNameAr = originLabel,
                    destinationNameAr = place.nameAr,
                    governorateAr = place.governorateAr,
                    destLat = place.latitude,
                    destLng = place.longitude,
                    distanceKm = route.totalDistanceKm,
                    durationMinutes = route.totalDurationMin,
                    highwayNameAr = route.primaryHighwayAr,
                    wasOfflineMode = _isOfflineMode.value
                )
            )
        }
    }

    fun advanceRouteStep() {
        val route = _activeRoute.value ?: return
        val nextIdx = (_currentStepIndex.value + 1).coerceAtMost(route.steps.lastIndex)
        _currentStepIndex.value = nextIdx
        val step = route.steps[nextIdx]
        _cameraTargetLatLng.value = step.location
        voiceAndLocation.setManualIraqAnchor(
            latLng = step.location,
            labelAr = step.roadNameAr,
            simulatedSpeedKmh = step.speedLimitKmh
        )
        voiceAndLocation.speakArabic(step.voicePromptAr, _voiceGuidanceEnabled.value)
    }

    fun previousRouteStep() {
        val route = _activeRoute.value ?: return
        val prevIdx = (_currentStepIndex.value - 1).coerceAtLeast(0)
        _currentStepIndex.value = prevIdx
        val step = route.steps[prevIdx]
        _cameraTargetLatLng.value = step.location
        voiceAndLocation.speakArabic(step.voicePromptAr, _voiceGuidanceEnabled.value)
    }

    fun toggleLiveRouteSimulation() {
        val route = _activeRoute.value ?: return
        if (_isLiveSimulating.value) {
            simulationJob?.cancel()
            _isLiveSimulating.value = false
            voiceAndLocation.setManualIraqAnchor(
                latLng = gpsState.value.currentIraqLocation,
                labelAr = gpsState.value.currentLocationNameAr,
                simulatedSpeedKmh = 0
            )
            return
        }

        _isLiveSimulating.value = true
        simulationJob = viewModelScope.launch {
            var idx = _currentStepIndex.value
            if (idx >= route.steps.lastIndex) idx = 0
            while (idx <= route.steps.lastIndex && _isLiveSimulating.value) {
                _currentStepIndex.value = idx
                val step = route.steps[idx]
                _cameraTargetLatLng.value = step.location
                voiceAndLocation.setManualIraqAnchor(
                    latLng = step.location,
                    labelAr = step.roadNameAr,
                    simulatedSpeedKmh = step.speedLimitKmh
                )
                voiceAndLocation.speakArabic(step.voicePromptAr, _voiceGuidanceEnabled.value)
                if (idx == route.steps.lastIndex) {
                    _isLiveSimulating.value = false
                    break
                }
                delay(4200L)
                idx++
            }
        }
    }

    fun stopActiveNavigation() {
        simulationJob?.cancel()
        _isLiveSimulating.value = false
        _activeRoute.value = null
        _currentStepIndex.value = 0
        voiceAndLocation.setManualIraqAnchor(
            latLng = gpsState.value.currentIraqLocation,
            labelAr = gpsState.value.currentLocationNameAr,
            simulatedSpeedKmh = 0
        )
        voiceAndLocation.speakArabic("تم إنهاء الملاحة الصوتية.", _voiceGuidanceEnabled.value)
    }

    fun toggleVoiceGuidance() {
        val next = !_voiceGuidanceEnabled.value
        _voiceGuidanceEnabled.value = next
        if (next) {
            voiceAndLocation.speakArabic(
                "تم تفعيل التوجيه الصوتي باللغة العربية لخارطة العراق.",
                true
            )
        }
    }

    fun speakTestArabicVoice() {
        val currentStep = _activeRoute.value?.steps?.getOrNull(_currentStepIndex.value)
        val message = currentStep?.voicePromptAr
            ?: "مرحباً بك في تطبيق درب الرافدين لملاحة العراق. أنت الآن في ${gpsState.value.currentLocationNameAr}. التوجيه الصوتي العربي يعمل بدقة عالية."
        voiceAndLocation.speakArabic(message, true)
        showTransientBanner("🔊 جاري نطق التوجيه الصوتي العربي...")
    }

    fun toggleOfflineMode() {
        val next = !_isOfflineMode.value
        _isOfflineMode.value = next
        if (next) {
            _preferGoogleMapsEngine.value = false
            showTransientBanner("تم تفعيل وضع العمل دون اتصال بالإنترنت (خرائط العراق المحفوظة محلياً)")
        } else {
            showTransientBanner("تم تفعيل وضع الاتصال المباشر")
        }
    }

    fun toggleMapEngine() {
        val next = !_preferGoogleMapsEngine.value
        _preferGoogleMapsEngine.value = next
        if (next && !hasConfiguredMapsApiKey) {
            showTransientBanner("ملاحظة: أضف MAPS_API_KEY في لوحة Secrets لعرض بلاطات Google Maps الحية")
        }
    }

    fun cycleMapStyleMode() {
        _mapStyleMode.value = (_mapStyleMode.value + 1) % 3
    }

    fun toggleDarkTheme() {
        _isDarkTheme.value = !_isDarkTheme.value
    }

    fun zoomIn() {
        _cameraZoomLevel.value = (_cameraZoomLevel.value + 1.2f).coerceAtMost(17.5f)
    }

    fun zoomOut() {
        _cameraZoomLevel.value = (_cameraZoomLevel.value - 1.2f).coerceAtLeast(5.4f)
    }

    fun recenterOnIraqOrUser(fullIraqOverview: Boolean = false) {
        if (fullIraqOverview) {
            _cameraTargetLatLng.value = LatLng(33.25, 43.85)
            _cameraZoomLevel.value = 6.1f
            showTransientBanner("عرض خارطة جمهورية العراق بالكامل (من زاخو إلى الفاو)")
        } else {
            _cameraTargetLatLng.value = gpsState.value.currentIraqLocation
            _cameraZoomLevel.value = 12.2f
            showTransientBanner("تم التمركز على موقعك في العراق: ${gpsState.value.currentLocationNameAr}")
        }
    }

    fun toggleFavoritePlace(place: IraqPlace, customNoteAr: String = "") {
        viewModelScope.launch {
            val existing = favorites.value.find {
                it.nameAr == place.nameAr ||
                    IraqMapCatalog.calculateDistanceKm(it.latitude, it.longitude, place.latitude, place.longitude) < 0.2
            }
            if (existing != null) {
                repository.removeFavorite(existing.id)
                showTransientBanner("تمت إزالة «${place.nameAr}» من المفضلة")
            } else {
                repository.addFavorite(
                    FavoritePlaceEntity(
                        nameAr = place.nameAr,
                        governorateAr = place.governorateAr,
                        categoryAr = place.category.labelAr,
                        addressAr = place.addressAr,
                        latitude = place.latitude,
                        longitude = place.longitude,
                        notesAr = customNoteAr.ifBlank { "محفوظ للملاحة السريعة دون اتصال في ${place.governorateAr}" },
                        isOfflineReady = true
                    )
                )
                showTransientBanner("تم حفظ «${place.nameAr}» في سجل المواقع المفضلة")
            }
        }
    }

    fun addCustomPlaceInIraq(
        nameAr: String,
        governorateAr: String,
        category: PlaceCategory,
        addressAr: String,
        latitude: Double,
        longitude: Double,
        saveToFavorites: Boolean
    ) {
        val clamped = IraqMapCatalog.clampToIraq(latitude, longitude)
        val newPlace = IraqPlace(
            id = "custom_${System.currentTimeMillis()}",
            nameAr = nameAr.trim(),
            governorateAr = governorateAr,
            category = category,
            addressAr = addressAr.ifBlank { "محافظة $governorateAr - العراق" },
            latitude = clamped.latitude,
            longitude = clamped.longitude,
            phoneOrCode = "USER-LOC",
            openHoursAr = "متاح دائماً",
            rating = 5.0,
            tagsAr = listOf("مضاف يدوياً", governorateAr, category.labelAr)
        )
        _customAddedPlaces.value = listOf(newPlace) + _customAddedPlaces.value
        _selectedPlace.value = newPlace
        _cameraTargetLatLng.value = clamped
        _cameraZoomLevel.value = 13.0f

        if (saveToFavorites) {
            viewModelScope.launch {
                repository.addFavorite(
                    FavoritePlaceEntity(
                        nameAr = newPlace.nameAr,
                        governorateAr = newPlace.governorateAr,
                        categoryAr = newPlace.category.labelAr,
                        addressAr = newPlace.addressAr,
                        latitude = newPlace.latitude,
                        longitude = newPlace.longitude,
                        notesAr = "موقع مخصص محفوظ محلياً للعمل دون إنترنت",
                        isOfflineReady = true
                    )
                )
            }
        }
        showTransientBanner("تمت إضافة «${newPlace.nameAr}» إلى خارطة $governorateAr")
    }

    fun deleteFavoriteById(id: Int) {
        viewModelScope.launch {
            repository.removeFavorite(id)
        }
    }

    fun navigateToFavorite(fav: FavoritePlaceEntity) {
        val place = IraqPlace(
            id = "fav_${fav.id}",
            nameAr = fav.nameAr,
            governorateAr = fav.governorateAr,
            category = PlaceCategory.entries.find { it.labelAr == fav.categoryAr } ?: PlaceCategory.LANDMARK,
            addressAr = fav.addressAr,
            latitude = fav.latitude,
            longitude = fav.longitude,
            phoneOrCode = "FAV-${fav.id}",
            openHoursAr = "متاح",
            rating = 4.9,
            tagsAr = listOf(fav.governorateAr, "مفضلة")
        )
        startVoiceNavigationTo(place)
    }

    fun navigateFromHistory(entry: NavigationHistoryEntity) {
        val place = IraqPlace(
            id = "hist_${entry.id}",
            nameAr = entry.destinationNameAr,
            governorateAr = entry.governorateAr,
            category = PlaceCategory.LANDMARK,
            addressAr = "عبر ${entry.highwayNameAr}",
            latitude = entry.destLat,
            longitude = entry.destLng,
            phoneOrCode = "HIST-${entry.id}",
            openHoursAr = "متاح",
            rating = 4.8,
            tagsAr = listOf(entry.governorateAr, "سجل الملاحة")
        )
        startVoiceNavigationTo(place)
    }

    fun clearNavigationHistory() {
        viewModelScope.launch {
            repository.clearHistory()
            showTransientBanner("تم مسح سجل المسارات السابقة")
        }
    }

    fun toggleGovernorateOfflineDownload(gov: OfflineGovernorateEntity) {
        viewModelScope.launch {
            val next = !gov.isDownloaded
            repository.setGovernorateDownloaded(gov.code, next)
            val msg = if (next) {
                "تم تنزيل خريطة محافظة ${gov.nameAr} (${gov.sizeMb} ميجابايت) للعمل دون اتصال"
            } else {
                "تمت إزالة حزمة محافظة ${gov.nameAr} المحلية"
            }
            showTransientBanner(msg)
        }
    }

    fun downloadAllIraqOfflineMaps() {
        viewModelScope.launch {
            repository.downloadAllIraqGovernorates(offlineGovernorates.value)
            showTransientBanner("تم تحميل خرائط جميع محافظات العراق الـ 18 بالكامل للعمل دون إنترنت")
        }
    }

    private fun showTransientBanner(messageAr: String) {
        viewModelScope.launch {
            _statusBannerAr.value = messageAr
            delay(3600L)
            if (_statusBannerAr.value == messageAr) {
                _statusBannerAr.value = null
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        simulationJob?.cancel()
        voiceAndLocation.shutdown()
    }
}
