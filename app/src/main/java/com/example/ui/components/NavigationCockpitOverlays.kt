package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CompassCalibration
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ActiveIraqRoute
import com.example.data.model.IraqMapCatalog
import com.example.data.model.IraqPlace
import com.example.data.model.PlaceCategory
import com.example.ui.theme.AlertCrimson
import com.example.ui.theme.BabylonGold
import com.example.ui.theme.RouteEmerald
import com.example.ui.theme.TigrisTurquoise

@Composable
fun TopIraqSearchAndFiltersOverlay(
    searchQuery: String,
    selectedCategory: PlaceCategory,
    selectedGovernorateAr: String,
    isOfflineMode: Boolean,
    voiceEnabled: Boolean,
    onSearchChange: (String) -> Unit,
    onSelectCategory: (PlaceCategory) -> Unit,
    onSelectGovernorate: (String) -> Unit,
    onToggleOffline: () -> Unit,
    onToggleVoice: () -> Unit,
    onTestVoice: () -> Unit,
    modifier: Modifier = Modifier
) {
    val governorateOptions = listOf("كل العراق") + IraqMapCatalog.IRAQ_GOVERNORATES.map { it.nameAr }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        // شريط البحث الذكي العلوي مع أزرار التوجيه الصوتي ووضع عدم الاتصال
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
            tonalElevation = 8.dp,
            shadowElevation = 8.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = onSearchChange,
                        placeholder = {
                            Text(
                                "ابحث في العراق (بغداد، مستشفى، وقود، مرقد...)",
                                style = MaterialTheme.typography.bodyMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "بحث ذكي",
                                tint = TigrisTurquoise
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(
                                    onClick = { onSearchChange("") },
                                    modifier = Modifier.testTag("clear_search_button")
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = "مسح البحث")
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = TigrisTurquoise,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("iraq_smart_search_input")
                    )

                    // زر تفعيل/كتم التوجيه الصوتي العربي
                    FilledIconButton(
                        onClick = onToggleVoice,
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = if (voiceEnabled) TigrisTurquoise else MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (voiceEnabled) Color(0xFF04221E) else MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("toggle_voice_button")
                    ) {
                        Icon(
                            imageVector = if (voiceEnabled) Icons.AutoMirrored.Filled.VolumeUp else Icons.AutoMirrored.Filled.VolumeOff,
                            contentDescription = "التوجيه الصوتي العربي"
                        )
                    }

                    // زر وضع العمل دون اتصال بالإنترنت
                    FilledIconButton(
                        onClick = onToggleOffline,
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = if (isOfflineMode) BabylonGold else MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (isOfflineMode) Color(0xFF241600) else MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("toggle_offline_button")
                    ) {
                        Icon(
                            imageVector = if (isOfflineMode) Icons.Default.CloudOff else Icons.Default.CloudDone,
                            contentDescription = "وضع العمل دون اتصال"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // شريط المحافظات العراقية السريع
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    contentPadding = PaddingValues(horizontal = 2.dp)
                ) {
                    items(governorateOptions) { govName ->
                        val selected = selectedGovernorateAr == govName
                        FilterChip(
                            selected = selected,
                            onClick = { onSelectGovernorate(govName) },
                            label = {
                                Text(
                                    text = govName,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = TigrisTurquoise.copy(alpha = 0.22f),
                                selectedLabelColor = TigrisTurquoise
                            ),
                            modifier = Modifier.testTag("gov_chip_$govName")
                        )
                    }
                }

                // شريط تصنيفات الأماكن القريبة الذكية
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    contentPadding = PaddingValues(horizontal = 2.dp)
                ) {
                    items(PlaceCategory.entries) { category ->
                        val selected = selectedCategory == category
                        FilterChip(
                            selected = selected,
                            onClick = { onSelectCategory(category) },
                            label = {
                                Text(
                                    text = category.labelAr,
                                    style = MaterialTheme.typography.labelMedium
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = BabylonGold.copy(alpha = 0.24f),
                                selectedLabelColor = BabylonGold
                            ),
                            modifier = Modifier.testTag("cat_chip_${category.name}")
                        )
                    }

                    item {
                        FilterChip(
                            selected = false,
                            onClick = onTestVoice,
                            label = { Text("🔊 نطق صوتي الآن", style = MaterialTheme.typography.labelMedium) },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.RecordVoiceOver,
                                    contentDescription = "اختبار النطق",
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            modifier = Modifier.testTag("test_arabic_tts_chip")
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FloatingIraqMapControlsColumn(
    preferGoogleMaps: Boolean,
    mapStyleMode: Int,
    onZoomIn: () -> Unit,
    onZoomOut: () -> Unit,
    onRecenterMyGps: () -> Unit,
    onRecenterFullIraq: () -> Unit,
    onCycleMapStyle: () -> Unit,
    onToggleMapEngine: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.padding(12.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
            tonalElevation = 6.dp,
            shadowElevation = 6.dp
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(
                    onClick = onZoomIn,
                    modifier = Modifier.size(48.dp).testTag("map_zoom_in_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "تكبير الخريطة")
                }
                IconButton(
                    onClick = onZoomOut,
                    modifier = Modifier.size(48.dp).testTag("map_zoom_out_button")
                ) {
                    Icon(Icons.Default.Remove, contentDescription = "تصغير الخريطة")
                }
            }
        }

        FilledIconButton(
            onClick = onRecenterMyGps,
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
                contentColor = TigrisTurquoise
            ),
            modifier = Modifier.size(48.dp).testTag("recenter_gps_button")
        ) {
            Icon(Icons.Default.MyLocation, contentDescription = "موقعي الحالي في العراق")
        }

        FilledIconButton(
            onClick = onRecenterFullIraq,
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
                contentColor = BabylonGold
            ),
            modifier = Modifier.size(48.dp).testTag("recenter_full_iraq_button")
        ) {
            Icon(Icons.Default.CompassCalibration, contentDescription = "عرض خارطة العراق كاملة")
        }

        FilledIconButton(
            onClick = onCycleMapStyle,
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
                contentColor = MaterialTheme.colorScheme.onSurface
            ),
            modifier = Modifier.size(48.dp).testTag("cycle_map_style_button")
        ) {
            Icon(
                Icons.Default.Layers,
                contentDescription = "تبديل نمط الخريطة ($mapStyleMode)"
            )
        }

        FilledIconButton(
            onClick = onToggleMapEngine,
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = if (preferGoogleMaps) RouteEmerald else MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
                contentColor = if (preferGoogleMaps) Color.White else TigrisTurquoise
            ),
            modifier = Modifier.size(48.dp).testTag("toggle_map_engine_button")
        ) {
            Icon(Icons.Default.Map, contentDescription = "تبديل محرك Google Maps / خريطة العراق المتجهة")
        }
    }
}

@Composable
fun ActiveNavigationCockpitCard(
    route: ActiveIraqRoute,
    currentStepIndex: Int,
    isLiveSimulating: Boolean,
    lastSpokenText: String,
    currentSpeedKmh: Int,
    onPreviousStep: () -> Unit,
    onNextStep: () -> Unit,
    onToggleLiveSimulation: () -> Unit,
    onSpeakCurrentStep: () -> Unit,
    onStopNavigation: () -> Unit,
    modifier: Modifier = Modifier
) {
    val step = route.steps.getOrElse(currentStepIndex) { route.steps.first() }
    val remainingSteps = route.steps.drop(currentStepIndex)
    val remainingDistKm = remainingSteps.sumOf { it.distanceKm }
    val remainingMin = remainingSteps.sumOf { it.durationMin }

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.97f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .testTag("active_navigation_hud_card")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // الشريط العلوي: الخطوة الحالية وحد السرعة
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
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(50.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(RouteEmerald.copy(alpha = 0.2f))
                            .clickable { onSpeakCurrentStep() }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Navigation,
                            contentDescription = "اتجاه الملاحة",
                            tint = RouteEmerald,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${step.maneuver.titleAr} • بعد ${step.distanceKm} كم",
                            style = MaterialTheme.typography.labelLarge,
                            color = RouteEmerald
                        )
                        Text(
                            text = step.instructionAr,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = step.roadNameAr,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // شارة السرعة المحددة على الطريق العراقي
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .border(4.dp, AlertCrimson, CircleShape)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${step.speedLimitKmh}",
                                color = Color.Black,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 16.sp
                            )
                            Text(
                                text = "كم/س",
                                color = Color.DarkGray,
                                fontSize = 9.sp
                            )
                        }
                    }
                    if (currentSpeedKmh > 0) {
                        Text(
                            text = "سرعتك: $currentSpeedKmh",
                            style = MaterialTheme.typography.labelMedium,
                            color = TigrisTurquoise
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // شريط النطق الصوتي العربي الفعلي
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSpeakCurrentStep() }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.RecordVoiceOver,
                        contentDescription = "التوجيه الصوتي",
                        tint = BabylonGold,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = lastSpokenText,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // ملخص المسافة المتبقية والوقت وأزرار التحكم بالمسار
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text(
                        text = "المتبقي: ${String.format("%.1f", remainingDistKm)} كم • $remainingMin دقيقة",
                        style = MaterialTheme.typography.titleSmall,
                        color = BabylonGold,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "الوجهة: ${route.destinationPlace.nameAr} (${route.destinationPlace.governorateAr})",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    IconButton(
                        onClick = onPreviousStep,
                        enabled = currentStepIndex > 0,
                        modifier = Modifier.size(48.dp).testTag("nav_prev_step_button")
                    ) {
                        Icon(Icons.Default.SkipPrevious, contentDescription = "الخطوة السابقة")
                    }

                    FilledIconButton(
                        onClick = onToggleLiveSimulation,
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = if (isLiveSimulating) BabylonGold else TigrisTurquoise,
                            contentColor = Color(0xFF04221E)
                        ),
                        modifier = Modifier.size(48.dp).testTag("nav_live_sim_button")
                    ) {
                        Icon(
                            imageVector = if (isLiveSimulating) Icons.Default.Stop else Icons.Default.PlayArrow,
                            contentDescription = "محاكاة السير الصوتي على المسار"
                        )
                    }

                    IconButton(
                        onClick = onNextStep,
                        enabled = currentStepIndex < route.steps.lastIndex,
                        modifier = Modifier.size(48.dp).testTag("nav_next_step_button")
                    ) {
                        Icon(Icons.Default.SkipNext, contentDescription = "الخطوة التالية")
                    }

                    FilledIconButton(
                        onClick = onStopNavigation,
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = AlertCrimson,
                            contentColor = Color.White
                        ),
                        modifier = Modifier.size(48.dp).testTag("nav_stop_button")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "إنهاء الملاحة")
                    }
                }
            }
        }
    }
}

@Composable
fun SelectedIraqPlaceBottomCard(
    place: IraqPlace,
    distanceKm: Double,
    bearingAr: String,
    isFavorite: Boolean,
    onStartVoiceNavigation: () -> Unit,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .testTag("selected_place_card")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
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
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(TigrisTurquoise.copy(alpha = 0.18f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = TigrisTurquoise
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = place.nameAr,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${place.governorateAr} • ${place.category.labelAr} • يبعد $distanceKm كم ($bearingAr)",
                            style = MaterialTheme.typography.bodySmall,
                            color = BabylonGold
                        )
                    }
                }

                IconButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier.size(48.dp).testTag("selected_place_favorite_button")
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "حفظ في المفضلة",
                        tint = if (isFavorite) AlertCrimson else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = place.addressAr,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = onStartVoiceNavigation,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = TigrisTurquoise,
                        contentColor = Color(0xFF03201C)
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("start_voice_nav_button")
                ) {
                    Icon(Icons.Default.Directions, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("بدء الملاحة الصوتية", fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onToggleFavorite,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.height(48.dp)
                ) {
                    Text(if (isFavorite) "محفوظ بالمفضلة" else "حفظ بالمفضلة")
                }
            }
        }
    }
}
