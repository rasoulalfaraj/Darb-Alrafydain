package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddLocationAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.local.FavoritePlaceEntity
import com.example.data.local.NavigationHistoryEntity
import com.example.data.local.OfflineGovernorateEntity
import com.example.data.model.IraqMapCatalog
import com.example.data.model.IraqPlace
import com.example.data.model.PlaceCategory
import com.example.ui.theme.AlertCrimson
import com.example.ui.theme.BabylonGold
import com.example.ui.theme.EuphratesBlue
import com.example.ui.theme.RouteEmerald
import com.example.ui.theme.TigrisTurquoise
import com.example.ui.viewmodel.PlaceWithDistance

@Composable
fun NearbySmartSearchScreen(
    searchQuery: String,
    selectedCategory: PlaceCategory,
    selectedGovernorateAr: String,
    currentOriginLabelAr: String,
    nearbyPlaces: List<PlaceWithDistance>,
    onSearchChange: (String) -> Unit,
    onSelectCategory: (PlaceCategory) -> Unit,
    onSelectGovernorate: (String) -> Unit,
    onStartVoiceNav: (IraqPlace) -> Unit,
    onShowOnMap: (IraqPlace) -> Unit,
    onToggleFavorite: (IraqPlace) -> Unit,
    onAddCustomPlace: (String, String, PlaceCategory, String, Double, Double, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddDialog by remember { mutableStateOf(false) }
    val governorateOptions = listOf("كل العراق") + IraqMapCatalog.IRAQ_GOVERNORATES.map { it.nameAr }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "البحث الذكي عن الأماكن القريبة",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "نقطة القياس الحالية: $currentOriginLabelAr",
                    style = MaterialTheme.typography.bodySmall,
                    color = TigrisTurquoise
                )
            }

            Button(
                onClick = { showAddDialog = true },
                colors = ButtonDefaults.buttonColors(
                    containerColor = BabylonGold,
                    contentColor = Color(0xFF261700)
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.height(48.dp).testTag("open_add_place_dialog_button")
            ) {
                Icon(Icons.Default.AddLocationAlt, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("إضافة موقع", fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            placeholder = { Text("ابحث بالاسم أو المحافظة أو الخدمة (مثال: مستشفى، وقود، النجف، الموصل...)") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TigrisTurquoise) },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("nearby_search_field")
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(PlaceCategory.entries) { category ->
                FilterChip(
                    selected = selectedCategory == category,
                    onClick = { onSelectCategory(category) },
                    label = { Text(category.labelAr) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = TigrisTurquoise.copy(alpha = 0.22f),
                        selectedLabelColor = TigrisTurquoise
                    )
                )
            }
        }

        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(governorateOptions) { gov ->
                FilterChip(
                    selected = selectedGovernorateAr == gov,
                    onClick = { onSelectGovernorate(gov) },
                    label = { Text(gov) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = BabylonGold.copy(alpha = 0.24f),
                        selectedLabelColor = BabylonGold
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 24.dp),
            modifier = Modifier.fillMaxSize().testTag("nearby_places_list")
        ) {
            if (nearbyPlaces.isEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth().padding(24.dp)
                        ) {
                            Icon(
                                Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = BabylonGold,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "لم يتم العثور على أماكن مطابقة للبحث في هذه المحافظة",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                "جرّب اختيار «كل العراق» أو إضافة موقع جديد يدوياً",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(nearbyPlaces, key = { it.place.id }) { item ->
                    val place = item.place
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("nearby_place_card_${place.id}")
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = place.nameAr,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "محافظة ${place.governorateAr} • ${place.category.labelAr}",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = TigrisTurquoise
                                    )
                                }

                                IconButton(
                                    onClick = { onToggleFavorite(place) },
                                    modifier = Modifier.size(48.dp)
                                ) {
                                    Icon(
                                        imageVector = if (item.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                        contentDescription = "حفظ في المفضلة",
                                        tint = if (item.isFavorite) AlertCrimson else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Text(
                                text = place.addressAr,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = BabylonGold.copy(alpha = 0.16f)
                                ) {
                                    Text(
                                        text = "📍 ${item.distanceKm} كم (${item.bearingAr})",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = BabylonGold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = RouteEmerald.copy(alpha = 0.16f)
                                ) {
                                    Text(
                                        text = "🕒 ${place.openHoursAr}",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = RouteEmerald,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.Star,
                                        contentDescription = null,
                                        tint = BabylonGold,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text(
                                        text = "${place.rating}",
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Button(
                                    onClick = { onStartVoiceNav(place) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = TigrisTurquoise,
                                        contentColor = Color(0xFF04221E)
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.weight(1f).height(48.dp)
                                ) {
                                    Icon(Icons.Default.Directions, contentDescription = null)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("توجيه صوتي", fontWeight = FontWeight.Bold)
                                }

                                OutlinedButton(
                                    onClick = { onShowOnMap(place) },
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.height(48.dp)
                                ) {
                                    Icon(Icons.Default.Map, contentDescription = null)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("عرض بالخريطة")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddCustomIraqPlaceDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, gov, cat, addr, lat, lng, saveFav ->
                onAddCustomPlace(name, gov, cat, addr, lat, lng, saveFav)
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun AddCustomIraqPlaceDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, PlaceCategory, String, Double, Double, Boolean) -> Unit
) {
    var nameAr by remember { mutableStateOf("") }
    var selectedGov by remember { mutableStateOf("بغداد") }
    var addressAr by remember { mutableStateOf("") }
    var latText by remember { mutableStateOf("33.3152") }
    var lngText by remember { mutableStateOf("44.3661") }
    var saveToFav by remember { mutableStateOf(true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("إضافة موقع جديد داخل العراق", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = nameAr,
                    onValueChange = { nameAr = it },
                    label = { Text("اسم المكان أو النقطة الجغرافية") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = selectedGov,
                    onValueChange = { selectedGov = it },
                    label = { Text("المحافظة العراقية (مثال: بغداد، البصرة، أربيل)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = addressAr,
                    onValueChange = { addressAr = it },
                    label = { Text("العنوان التفصيلي أو أقرب نقطة دالة") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = latText,
                        onValueChange = { latText = it },
                        label = { Text("خط العرض (29-37)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = lngText,
                        onValueChange = { lngText = it },
                        label = { Text("خط الطول (39-48)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("حفظ تلقائياً في سجل المواقع المفضلة")
                    Switch(checked = saveToFav, onCheckedChange = { saveToFav = it })
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (nameAr.isNotBlank()) {
                        val lat = latText.toDoubleOrNull() ?: 33.3152
                        val lng = lngText.toDoubleOrNull() ?: 44.3661
                        onConfirm(
                            nameAr,
                            selectedGov.ifBlank { "بغداد" },
                            PlaceCategory.LANDMARK,
                            addressAr,
                            lat,
                            lng,
                            saveToFav
                        )
                    }
                }
            ) {
                Text("حفظ وإضافة للخريطة")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        }
    )
}

@Composable
fun FavoritesAndHistoryScreen(
    favorites: List<FavoritePlaceEntity>,
    history: List<NavigationHistoryEntity>,
    onNavigateToFavorite: (FavoritePlaceEntity) -> Unit,
    onDeleteFavorite: (Int) -> Unit,
    onNavigateFromHistory: (NavigationHistoryEntity) -> Unit,
    onClearHistory: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedSegment by remember { mutableIntStateOf(0) } // 0 = المفضلة, 1 = سجل المسارات

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "المواقع المفضلة وسجل الملاحة",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "محفوظة محلياً في قاعدة بيانات الجهاز للعمل دون اتصال بالإنترنت في جميع أنحاء العراق",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(
                onClick = { selectedSegment = 0 },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedSegment == 0) TigrisTurquoise else MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = if (selectedSegment == 0) Color(0xFF04221E) else MaterialTheme.colorScheme.onSurfaceVariant
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.weight(1f).height(48.dp).testTag("segment_favorites_button")
            ) {
                Icon(Icons.Default.Favorite, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("المفضلة (${favorites.size})", fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = { selectedSegment = 1 },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedSegment == 1) BabylonGold else MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = if (selectedSegment == 1) Color(0xFF231500) else MaterialTheme.colorScheme.onSurfaceVariant
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.weight(1f).height(48.dp).testTag("segment_history_button")
            ) {
                Icon(Icons.Default.History, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("سجل المسارات (${history.size})", fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (selectedSegment == 0) {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 24.dp),
                modifier = Modifier.fillMaxSize().testTag("favorites_lazy_list")
            ) {
                items(favorites, key = { it.id }) { fav ->
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = fav.nameAr,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "محافظة ${fav.governorateAr} • ${fav.categoryAr}",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = TigrisTurquoise
                                    )
                                }

                                IconButton(
                                    onClick = { onDeleteFavorite(fav.id) },
                                    modifier = Modifier.size(48.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "حذف من المفضلة",
                                        tint = AlertCrimson
                                    )
                                }
                            }

                            Text(
                                text = fav.addressAr,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            if (fav.notesAr.isNotBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "📌 ${fav.notesAr}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = BabylonGold
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = RouteEmerald.copy(alpha = 0.16f)
                                ) {
                                    Text(
                                        text = "متاح دون إنترنت (${String.format("%.3f", fav.latitude)}, ${String.format("%.3f", fav.longitude)})",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = RouteEmerald,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }

                                Button(
                                    onClick = { onNavigateToFavorite(fav) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = TigrisTurquoise,
                                        contentColor = Color(0xFF04221E)
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.height(48.dp)
                                ) {
                                    Icon(Icons.Default.Directions, contentDescription = null)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("انطلق صوتياً", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        } else {
            Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (history.isNotEmpty()) {
                    TextButton(
                        onClick = onClearHistory,
                        modifier = Modifier.testTag("clear_history_button")
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, tint = AlertCrimson)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("مسح السجل بالكامل", color = AlertCrimson)
                    }
                }
            }

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 24.dp),
                modifier = Modifier.fillMaxSize().testTag("history_lazy_list")
            ) {
                items(history, key = { it.id }) { entry ->
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "من: ${entry.originNameAr}  ⬅  إلى: ${entry.destinationNameAr}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "المحافظة: ${entry.governorateAr} • المسار: ${entry.highwayNameAr}",
                                style = MaterialTheme.typography.bodySmall,
                                color = TigrisTurquoise
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "المسافة: ${entry.distanceKm} كم • الوقت: ${entry.durationMinutes} دقيقة",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = BabylonGold
                                )

                                OutlinedButton(
                                    onClick = { onNavigateFromHistory(entry) },
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.height(48.dp)
                                ) {
                                    Icon(Icons.Default.Directions, contentDescription = null)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("إعادة الملاحة")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun OfflineMapsAndGitHubScreen(
    isOfflineMode: Boolean,
    offlineGovernorates: List<OfflineGovernorateEntity>,
    ttsStatusAr: String,
    onToggleOfflineMode: () -> Unit,
    onDownloadAllIraq: () -> Unit,
    onToggleGovernorateDownload: (OfflineGovernorateEntity) -> Unit,
    onSetOriginGovernorate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val clipboardManager = LocalClipboardManager.current
    var copiedWorkflowBanner by remember { mutableStateOf(false) }
    val downloadedCount = offlineGovernorates.count { it.isDownloaded }
    val totalMbDownloaded = offlineGovernorates.filter { it.isDownloaded }.sumOf { it.sizeMb }

    val workflowSummarySnippet = """
name: بناء تطبيق ملاحة العراق (Android APK CI/CD)
# الملف المدمج في المشروع: .github/workflows/android-build.yml
# يقوم تلقائياً عند مشاركة المشروع إلى GitHub ببناء:
# 1) iraq-navigation-darb-debug.apk
# 2) iraq-navigation-darb-release.apk
# ورفعهما مباشرة في GitHub Actions Artifacts و GitHub Releases.
    """.trimIndent()

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(16.dp),
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("offline_and_github_list")
    ) {
        // بانر خارطة العراق الرقمية
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(modifier = Modifier.fillMaxWidth().height(175.dp)) {
                    Image(
                        painter = painterResource(id = R.drawable.img_iraq_hero_banner_1794291617206),
                        contentDescription = "خارطة العراق الرقمية",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        Color.Black.copy(alpha = 0.25f),
                                        Color(0xFF061522).copy(alpha = 0.92f)
                                    )
                                )
                            )
                    )
                    Column(
                        verticalArrangement = Arrangement.Bottom,
                        modifier = Modifier.fillMaxSize().padding(16.dp)
                    ) {
                        Text(
                            text = "منظومة خرائط العراق دون اتصال + بناء APK التلقائي",
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "المحافظات المحملة: $downloadedCount من 18 محافظة عراقية ($totalMbDownloaded ميجابايت) • $ttsStatusAr",
                            style = MaterialTheme.typography.bodySmall,
                            color = TigrisTurquoise
                        )
                    }
                }
            }
        }

        // بطاقة تفعيل وضع العمل دون اتصال بالإنترنت وتحميل كل العراق
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = if (isOfflineMode) Icons.Default.CloudOff else Icons.Default.DownloadDone,
                                contentDescription = null,
                                tint = if (isOfflineMode) BabylonGold else TigrisTurquoise,
                                modifier = Modifier.size(28.dp)
                            )
                            Column {
                                Text(
                                    text = "وضع العمل دون اتصال بالإنترنت (Offline Mode)",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "اعتماد الخريطة المتجهة العراقية وحساب المسارات والتوجيه الصوتي محلياً 100%",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Switch(
                            checked = isOfflineMode,
                            onCheckedChange = { onToggleOfflineMode() },
                            modifier = Modifier.testTag("offline_master_switch")
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = onDownloadAllIraq,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = TigrisTurquoise,
                            contentColor = Color(0xFF04221E)
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("download_all_iraq_button")
                    ) {
                        Icon(Icons.Default.CloudDownload, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("تنزيل خرائط جميع محافظات العراق الـ 18 بالكامل", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // بطاقة البناء التلقائي لملف APK على GitHub Actions
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f)
                ),
                modifier = Modifier.fillMaxWidth().testTag("github_actions_apk_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Code,
                            contentDescription = null,
                            tint = BabylonGold,
                            modifier = Modifier.size(28.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "البناء التلقائي لملف APK على GitHub (مفعّل)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "تم تضمين ملف .github/workflows/android-build.yml للبناء الفوري عند المشاركة إلى GitHub",
                                style = MaterialTheme.typography.bodySmall,
                                color = TigrisTurquoise
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "مفعّل",
                            tint = RouteEmerald
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF05101A),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = workflowSummarySnippet,
                            style = MaterialTheme.typography.bodySmall,
                            color = EuphratesBlue,
                            modifier = Modifier.padding(12.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedButton(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(workflowSummarySnippet))
                            copiedWorkflowBanner = true
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (copiedWorkflowBanner) "تم نسخ معلومات مسار البناء التلقائي ✓" else "نسخ ملخص إعداد GitHub Actions APK")
                    }
                }
            }
        }

        item {
            Text(
                text = "حزم خرائط محافظات العراق الـ 18 (للعمل دون إنترنت):",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        items(offlineGovernorates, key = { it.code }) { gov ->
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "محافظة ${gov.nameAr}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (gov.isDownloaded) RouteEmerald.copy(alpha = 0.18f) else BabylonGold.copy(alpha = 0.18f)
                            ) {
                                Text(
                                    text = if (gov.isDownloaded) "محمّلة ✓" else "${gov.sizeMb} م.ب",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (gov.isDownloaded) RouteEmerald else BabylonGold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "${gov.capitalAr} • ${gov.landmarksCount} معلم وخدمة • ${gov.roadNetworkKm} كم طرق",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        IconButton(
                            onClick = { onSetOriginGovernorate(gov.nameAr) },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                Icons.Default.MyLocation,
                                contentDescription = "تثبيت كنقطة انطلاق",
                                tint = TigrisTurquoise
                            )
                        }

                        Button(
                            onClick = { onToggleGovernorateDownload(gov) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (gov.isDownloaded) MaterialTheme.colorScheme.surfaceVariant else TigrisTurquoise,
                                contentColor = if (gov.isDownloaded) MaterialTheme.colorScheme.onSurfaceVariant else Color(0xFF04221E)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.height(48.dp)
                        ) {
                            Text(if (gov.isDownloaded) "محدثة" else "تنزيل")
                        }
                    }
                }
            }
        }
    }
}
