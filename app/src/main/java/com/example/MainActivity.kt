package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.IraqMapCatalog
import com.example.ui.components.ActiveNavigationCockpitCard
import com.example.ui.components.FavoritesAndHistoryScreen
import com.example.ui.components.FloatingIraqMapControlsColumn
import com.example.ui.components.IraqDualEngineMapView
import com.example.ui.components.NearbySmartSearchScreen
import com.example.ui.components.OfflineMapsAndGitHubScreen
import com.example.ui.components.SelectedIraqPlaceBottomCard
import com.example.ui.components.TopIraqSearchAndFiltersOverlay
import com.example.ui.theme.BabylonGold
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.RouteEmerald
import com.example.ui.theme.TigrisTurquoise
import com.example.ui.viewmodel.IraqNavTab
import com.example.ui.viewmodel.IraqNavViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val navViewModel: IraqNavViewModel = viewModel()
            val isDarkTheme by navViewModel.isDarkTheme.collectAsStateWithLifecycle()

            MyApplicationTheme(darkTheme = isDarkTheme) {
                IraqNavigationAppRoot(viewModel = navViewModel)
            }
        }
    }
}

@Composable
fun IraqNavigationAppRoot(
    viewModel: IraqNavViewModel
) {
    val context = LocalContext.current
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val selectedGovernorateAr by viewModel.selectedGovernorateAr.collectAsStateWithLifecycle()
    val isOfflineMode by viewModel.isOfflineMode.collectAsStateWithLifecycle()
    val preferGoogleMaps by viewModel.preferGoogleMapsEngine.collectAsStateWithLifecycle()
    val mapStyleMode by viewModel.mapStyleMode.collectAsStateWithLifecycle()
    val voiceEnabled by viewModel.voiceGuidanceEnabled.collectAsStateWithLifecycle()
    val isDarkTheme by viewModel.isDarkTheme.collectAsStateWithLifecycle()
    val selectedPlace by viewModel.selectedPlace.collectAsStateWithLifecycle()
    val activeRoute by viewModel.activeRoute.collectAsStateWithLifecycle()
    val currentStepIndex by viewModel.currentStepIndex.collectAsStateWithLifecycle()
    val isLiveSimulating by viewModel.isLiveSimulating.collectAsStateWithLifecycle()
    val cameraTarget by viewModel.cameraTargetLatLng.collectAsStateWithLifecycle()
    val cameraZoom by viewModel.cameraZoomLevel.collectAsStateWithLifecycle()
    val gpsState by viewModel.gpsState.collectAsStateWithLifecycle()
    val lastSpokenVoiceText by viewModel.lastSpokenVoiceText.collectAsStateWithLifecycle()
    val ttsStatusAr by viewModel.ttsEngineStatusAr.collectAsStateWithLifecycle()
    val favorites by viewModel.favorites.collectAsStateWithLifecycle()
    val history by viewModel.history.collectAsStateWithLifecycle()
    val offlineGovernorates by viewModel.offlineGovernorates.collectAsStateWithLifecycle()
    val filteredNearbyPlaces by viewModel.filteredNearbyPlaces.collectAsStateWithLifecycle()
    val statusBannerAr by viewModel.statusBannerAr.collectAsStateWithLifecycle()

    // طلب صلاحية الموقع الجغرافي عالي الدقة في وقت التشغيل
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        val granted = result[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            result[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        viewModel.voiceAndLocation.startHighAccuracyLocationUpdates(granted)
    }

    LaunchedEffect(Unit) {
        val hasFine = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val hasCoarse = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (hasFine || hasCoarse) {
            viewModel.voiceAndLocation.startHighAccuracyLocationUpdates(true)
        } else {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    // معالجة زر الرجوع للعودة إلى شاشة الخريطة والملاحة الرئيسية
    BackHandler(enabled = currentTab != IraqNavTab.MAP_NAV) {
        viewModel.selectTab(IraqNavTab.MAP_NAV)
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isExpandedScreen = maxWidth >= 700.dp

        Scaffold(
            contentWindowInsets = WindowInsets.safeDrawing,
            topBar = {
                IraqHeaderStatusBar(
                    currentLocationLabelAr = gpsState.currentLocationNameAr,
                    isOfflineMode = isOfflineMode,
                    hasLocationPermission = gpsState.hasLocationPermission,
                    isDarkTheme = isDarkTheme,
                    onRequestPermission = {
                        permissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    },
                    onToggleTheme = { viewModel.toggleDarkTheme() }
                )
            },
            bottomBar = {
                if (!isExpandedScreen) {
                    IraqBottomNavigationBar(
                        currentTab = currentTab,
                        onSelectTab = { viewModel.selectTab(it) }
                    )
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (isExpandedScreen) {
                    IraqSideNavigationRail(
                        currentTab = currentTab,
                        onSelectTab = { viewModel.selectTab(it) }
                    )
                }

                Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                    when (currentTab) {
                        IraqNavTab.MAP_NAV -> {
                            Box(modifier = Modifier.fillMaxSize()) {
                                // الخريطة التفاعلية المزدوجة لجمهورية العراق
                                IraqDualEngineMapView(
                                    preferGoogleMaps = preferGoogleMaps,
                                    isOfflineMode = isOfflineMode,
                                    mapStyleMode = mapStyleMode,
                                    cameraTarget = cameraTarget,
                                    cameraZoom = cameraZoom,
                                    userIraqLocation = gpsState.currentIraqLocation,
                                    userLocationLabelAr = gpsState.currentLocationNameAr,
                                    selectedPlace = selectedPlace,
                                    nearbyPlaces = filteredNearbyPlaces,
                                    offlineGovernorates = offlineGovernorates,
                                    activeRoute = activeRoute,
                                    currentStepIndex = currentStepIndex,
                                    onSelectPlace = { place ->
                                        viewModel.selectPlaceOnMap(place, zoomIn = false)
                                    },
                                    onTapMapLatLng = { latLng ->
                                        viewModel.onMapTapInsideIraq(latLng)
                                    }
                                )

                                // شريط البحث الذكي والفلاتر العلوي فوق الخريطة
                                TopIraqSearchAndFiltersOverlay(
                                    searchQuery = searchQuery,
                                    selectedCategory = selectedCategory,
                                    selectedGovernorateAr = selectedGovernorateAr,
                                    isOfflineMode = isOfflineMode,
                                    voiceEnabled = voiceEnabled,
                                    onSearchChange = { viewModel.updateSearchQuery(it) },
                                    onSelectCategory = { viewModel.selectCategory(it) },
                                    onSelectGovernorate = { viewModel.selectGovernorate(it) },
                                    onToggleOffline = { viewModel.toggleOfflineMode() },
                                    onToggleVoice = { viewModel.toggleVoiceGuidance() },
                                    onTestVoice = { viewModel.speakTestArabicVoice() },
                                    modifier = Modifier.align(Alignment.TopCenter)
                                )

                                // أزرار التحكم بالخريطة العائمة
                                FloatingIraqMapControlsColumn(
                                    preferGoogleMaps = preferGoogleMaps && !isOfflineMode,
                                    mapStyleMode = mapStyleMode,
                                    onZoomIn = { viewModel.zoomIn() },
                                    onZoomOut = { viewModel.zoomOut() },
                                    onRecenterMyGps = { viewModel.recenterOnIraqOrUser(false) },
                                    onRecenterFullIraq = { viewModel.recenterOnIraqOrUser(true) },
                                    onCycleMapStyle = { viewModel.cycleMapStyleMode() },
                                    onToggleMapEngine = { viewModel.toggleMapEngine() },
                                    modifier = Modifier.align(Alignment.CenterEnd)
                                )

                                // البطاقة السفلية: إما قمرة الملاحة الصوتية النشطة أو بطاقة المكان المحدد
                                val currentRoute = activeRoute
                                val currentSelected = selectedPlace
                                if (currentRoute != null) {
                                    ActiveNavigationCockpitCard(
                                        route = currentRoute,
                                        currentStepIndex = currentStepIndex,
                                        isLiveSimulating = isLiveSimulating,
                                        lastSpokenText = lastSpokenVoiceText,
                                        currentSpeedKmh = gpsState.speedKmh,
                                        onPreviousStep = { viewModel.previousRouteStep() },
                                        onNextStep = { viewModel.advanceRouteStep() },
                                        onToggleLiveSimulation = { viewModel.toggleLiveRouteSimulation() },
                                        onSpeakCurrentStep = { viewModel.speakTestArabicVoice() },
                                        onStopNavigation = { viewModel.stopActiveNavigation() },
                                        modifier = Modifier.align(Alignment.BottomCenter)
                                    )
                                } else if (currentSelected != null) {
                                    val distKm = IraqMapCatalog.calculateDistanceKm(
                                        gpsState.currentIraqLocation.latitude,
                                        gpsState.currentIraqLocation.longitude,
                                        currentSelected.latitude,
                                        currentSelected.longitude
                                    )
                                    val bearingAr = IraqMapCatalog.calculateBearingAr(
                                        gpsState.currentIraqLocation.latitude,
                                        gpsState.currentIraqLocation.longitude,
                                        currentSelected.latitude,
                                        currentSelected.longitude
                                    )
                                    val isFav = favorites.any {
                                        it.nameAr == currentSelected.nameAr ||
                                            IraqMapCatalog.calculateDistanceKm(
                                                it.latitude,
                                                it.longitude,
                                                currentSelected.latitude,
                                                currentSelected.longitude
                                            ) < 0.2
                                    }
                                    SelectedIraqPlaceBottomCard(
                                        place = currentSelected,
                                        distanceKm = distKm,
                                        bearingAr = bearingAr,
                                        isFavorite = isFav,
                                        onStartVoiceNavigation = {
                                            viewModel.startVoiceNavigationTo(currentSelected)
                                        },
                                        onToggleFavorite = {
                                            viewModel.toggleFavoritePlace(currentSelected)
                                        },
                                        modifier = Modifier.align(Alignment.BottomCenter)
                                    )
                                }
                            }
                        }

                        IraqNavTab.NEARBY_PLACES -> {
                            NearbySmartSearchScreen(
                                searchQuery = searchQuery,
                                selectedCategory = selectedCategory,
                                selectedGovernorateAr = selectedGovernorateAr,
                                currentOriginLabelAr = gpsState.currentLocationNameAr,
                                nearbyPlaces = filteredNearbyPlaces,
                                onSearchChange = { viewModel.updateSearchQuery(it) },
                                onSelectCategory = { viewModel.selectCategory(it) },
                                onSelectGovernorate = { viewModel.selectGovernorate(it) },
                                onStartVoiceNav = { place ->
                                    viewModel.startVoiceNavigationTo(place)
                                },
                                onShowOnMap = { place ->
                                    viewModel.selectPlaceOnMap(place, zoomIn = true)
                                    viewModel.selectTab(IraqNavTab.MAP_NAV)
                                },
                                onToggleFavorite = { place ->
                                    viewModel.toggleFavoritePlace(place)
                                },
                                onAddCustomPlace = { name, gov, cat, addr, lat, lng, saveFav ->
                                    viewModel.addCustomPlaceInIraq(name, gov, cat, addr, lat, lng, saveFav)
                                }
                            )
                        }

                        IraqNavTab.FAVORITES_HISTORY -> {
                            FavoritesAndHistoryScreen(
                                favorites = favorites,
                                history = history,
                                onNavigateToFavorite = { fav ->
                                    viewModel.navigateToFavorite(fav)
                                },
                                onDeleteFavorite = { id ->
                                    viewModel.deleteFavoriteById(id)
                                },
                                onNavigateFromHistory = { entry ->
                                    viewModel.navigateFromHistory(entry)
                                },
                                onClearHistory = {
                                    viewModel.clearNavigationHistory()
                                }
                            )
                        }

                        IraqNavTab.OFFLINE_AND_GITHUB -> {
                            OfflineMapsAndGitHubScreen(
                                isOfflineMode = isOfflineMode,
                                offlineGovernorates = offlineGovernorates,
                                ttsStatusAr = ttsStatusAr,
                                onToggleOfflineMode = { viewModel.toggleOfflineMode() },
                                onDownloadAllIraq = { viewModel.downloadAllIraqOfflineMaps() },
                                onToggleGovernorateDownload = { gov ->
                                    viewModel.toggleGovernorateOfflineDownload(gov)
                                },
                                onSetOriginGovernorate = { govName ->
                                    viewModel.setCurrentOriginToGovernorate(govName)
                                    viewModel.selectTab(IraqNavTab.MAP_NAV)
                                }
                            )
                        }
                    }

                    // إشعار فوري عائم في أعلى الشاشة
                    val currentBanner = statusBannerAr
                    if (currentBanner != null) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color(0xFF06202E),
                            tonalElevation = 10.dp,
                            shadowElevation = 10.dp,
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .padding(top = 8.dp, start = 16.dp, end = 16.dp)
                        ) {
                            Text(
                                text = currentBanner,
                                style = MaterialTheme.typography.labelLarge,
                                color = TigrisTurquoise,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun IraqHeaderStatusBar(
    currentLocationLabelAr: String,
    isOfflineMode: Boolean,
    hasLocationPermission: Boolean,
    isDarkTheme: Boolean,
    onRequestPermission: () -> Unit,
    onToggleTheme: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 4.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = TigrisTurquoise.copy(alpha = 0.18f)
                ) {
                    Text(
                        text = "🇮🇶 درب الرافدين",
                        style = MaterialTheme.typography.labelLarge,
                        color = TigrisTurquoise,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.GpsFixed,
                            contentDescription = null,
                            tint = if (isOfflineMode) BabylonGold else RouteEmerald,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = currentLocationLabelAr,
                            style = MaterialTheme.typography.labelMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Text(
                        text = if (isOfflineMode) {
                            "وضع عدم الاتصال مفعّل (خارطة العراق المحلية)"
                        } else {
                            "تغطية حصرية لخارطة جمهورية العراق (18 محافظة)"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            if (!hasLocationPermission) {
                TextButton(
                    onClick = onRequestPermission,
                    modifier = Modifier.testTag("grant_gps_permission_button")
                ) {
                    Text("تفعيل GPS", color = BabylonGold, fontWeight = FontWeight.Bold)
                }
            }

            IconButton(
                onClick = onToggleTheme,
                modifier = Modifier.size(48.dp).testTag("toggle_theme_button")
            ) {
                Icon(
                    imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                    contentDescription = "تبديل المظهر الليلي / النهاري",
                    tint = BabylonGold
                )
            }
        }
    }
}

@Composable
private fun IraqBottomNavigationBar(
    currentTab: IraqNavTab,
    onSelectTab: (IraqNavTab) -> Unit
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp,
        modifier = Modifier.testTag("iraq_bottom_nav_bar")
    ) {
        IraqNavTab.entries.forEach { tab ->
            val selected = currentTab == tab
            val icon = when (tab) {
                IraqNavTab.MAP_NAV -> Icons.Default.Map
                IraqNavTab.NEARBY_PLACES -> Icons.Default.Explore
                IraqNavTab.FAVORITES_HISTORY -> Icons.Default.Favorite
                IraqNavTab.OFFLINE_AND_GITHUB -> Icons.Default.CloudDownload
            }
            NavigationBarItem(
                selected = selected,
                onClick = { onSelectTab(tab) },
                icon = { Icon(icon, contentDescription = tab.titleAr) },
                label = {
                    Text(
                        text = tab.titleAr,
                        style = MaterialTheme.typography.labelMedium,
                        maxLines = 1
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color(0xFF04221E),
                    selectedTextColor = TigrisTurquoise,
                    indicatorColor = TigrisTurquoise
                ),
                modifier = Modifier.testTag("nav_tab_${tab.name}")
            )
        }
    }
}

@Composable
private fun IraqSideNavigationRail(
    currentTab: IraqNavTab,
    onSelectTab: (IraqNavTab) -> Unit
) {
    NavigationRail(
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxHeight().background(MaterialTheme.colorScheme.surface)
    ) {
        IraqNavTab.entries.forEach { tab ->
            val selected = currentTab == tab
            val icon = when (tab) {
                IraqNavTab.MAP_NAV -> Icons.Default.Map
                IraqNavTab.NEARBY_PLACES -> Icons.Default.Explore
                IraqNavTab.FAVORITES_HISTORY -> Icons.Default.Favorite
                IraqNavTab.OFFLINE_AND_GITHUB -> Icons.Default.CloudDownload
            }
            NavigationRailItem(
                selected = selected,
                onClick = { onSelectTab(tab) },
                icon = { Icon(icon, contentDescription = tab.titleAr) },
                label = { Text(tab.titleAr, style = MaterialTheme.typography.labelMedium) }
            )
        }
    }
}
