package com.example.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import android.content.Context
import androidx.compose.ui.layout.ContentScale
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.PickVisualMediaRequest
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.NotificationHelper
import com.example.PdfReportGenerator
import com.example.data.FuelPriceUiState
import com.example.data.FuelRecord
import com.example.data.MaintenanceRecord
import com.example.data.ExpenseRecord
import com.example.data.Reminder
import com.example.data.Vehicle
import com.example.data.normalizeFuelLookupKey
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

// -----------------------------------------------------------------------------
// 0. SPLASH SCREEN (AÇILIŞ EKRANI)
// -----------------------------------------------------------------------------

@Composable
fun SplashScreen(onTimeout: () -> Unit) {
    var startAnimation by remember { mutableStateOf(false) }

    val logoScale by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0.4f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "logoScale"
    )

    val logoAlpha by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 700, easing = FastOutSlowInEasing),
        label = "logoAlpha"
    )

    val textAlpha by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 800, delayMillis = 350, easing = FastOutSlowInEasing),
        label = "textAlpha"
    )

    val textOffsetY by animateDpAsState(
        targetValue = if (startAnimation) 0.dp else 24.dp,
        animationSpec = tween(durationMillis = 800, delayMillis = 350, easing = FastOutSlowInEasing),
        label = "textOffsetY"
    )

    val sloganAlpha by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 800, delayMillis = 650, easing = FastOutSlowInEasing),
        label = "sloganAlpha"
    )

    val sloganOffsetY by animateDpAsState(
        targetValue = if (startAnimation) 0.dp else 16.dp,
        animationSpec = tween(durationMillis = 800, delayMillis = 650, easing = FastOutSlowInEasing),
        label = "sloganOffsetY"
    )

    // Infinite breathing glow for the background circle
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    LaunchedEffect(Unit) {
        startAnimation = true
        kotlinx.coroutines.delay(2200)
        onTimeout()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        BgLight,
                        PrimarySoft,
                        BgLightAlt
                    )
                )
            )
            .windowInsetsPadding(WindowInsets.safeDrawing),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
        ) {
            // Animated Brand Badge with Glowing Halo
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(140.dp)
                    .scale(logoScale)
                    .alpha(logoAlpha)
            ) {
                // Pulsing Halo Glow
                Box(
                    modifier = Modifier
                        .size(130.dp)
                        .scale(pulseScale)
                        .background(PrimaryVibrant.copy(alpha = pulseAlpha), CircleShape)
                )

                // Main App Icon Container
                Surface(
                    modifier = Modifier
                        .size(96.dp)
                        .shadow(16.dp, RoundedCornerShape(28.dp), spotColor = PrimaryVibrant.copy(alpha = 0.45f)),
                    shape = RoundedCornerShape(28.dp),
                    color = PrimaryVibrant,
                    border = BorderStroke(2.dp, Color.White.copy(alpha = 0.35f))
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        PrimaryVibrant,
                                        PrimaryVibrantDark
                                    )
                                )
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.DirectionsCar,
                            contentDescription = "Araç Karnem",
                            tint = Color.White,
                            modifier = Modifier.size(52.dp)
                        )

                        // Small badge overlay
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .offset(x = (-8).dp, y = (-8).dp)
                                .size(26.dp)
                                .background(AccentEmerald, CircleShape)
                                .border(2.dp, Color.White, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Animated App Name "Araç Karnem"
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .offset(y = textOffsetY)
                    .alpha(textAlpha)
            ) {
                Text(
                    text = "Araç Karnem",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextDark,
                    letterSpacing = 0.5.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Animated Slogan
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .offset(y = sloganOffsetY)
                    .alpha(sloganAlpha)
            ) {
                Text(
                    text = "Akıllı Araç Bakım & Masraf Takip Asistanı",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = TextMedium,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Modern loading dots or slim progress bar
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = PrimarySoft.copy(alpha = 0.7f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(14.dp),
                            strokeWidth = 2.dp,
                            color = PrimaryVibrant
                        )
                        Text(
                            text = "Yükleniyor...",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryVibrant
                        )
                    }
                }
            }
        }

        // Bottom Brand Signature
        Text(
            text = "v1.0.0 • Güvenli & Yerel Veri Kaydı",
            style = MaterialTheme.typography.labelSmall,
            color = TextLight,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
                .alpha(sloganAlpha)
        )
    }
}

// -----------------------------------------------------------------------------
// REUSABLE UI COMPONENTS & BADGES
// -----------------------------------------------------------------------------

enum class BottomBarTab {
    VEHICLES,
    ADD,
    PROFILE
}

@Composable
fun topSafeBarWindowInsets(): WindowInsets =
    WindowInsets.statusBars
        .union(WindowInsets.displayCutout)
        .only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal)

@Composable
fun bottomSafeBarWindowInsets(): WindowInsets =
    WindowInsets.navigationBars
        .union(WindowInsets.displayCutout)
        .only(WindowInsetsSides.Bottom + WindowInsetsSides.Horizontal)

@Composable
fun ModernAppBottomBar(
    currentTab: BottomBarTab,
    onTabSelected: (BottomBarTab) -> Unit,
    modifier: Modifier = Modifier
) {
    // Non-clipping outer Box so elevated center + button is never cut off at the top edge,
    // while SurfaceWhite fills all the way under the system gesture pill / 3-button nav bar.
    Box(
        modifier = modifier.fillMaxWidth()
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 14.dp)
                .align(Alignment.BottomCenter),
            color = SurfaceWhite,
            shadowElevation = 12.dp,
            border = BorderStroke(1.dp, BorderLight)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(bottomSafeBarWindowInsets())
                    .height(66.dp)
                    .padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left Tab: Araçlarım
                val isVehicles = currentTab == BottomBarTab.VEHICLES
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 52.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { onTabSelected(BottomBarTab.VEHICLES) }
                        .padding(vertical = 4.dp)
                        .testTag("nav_vehicles"),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .width(48.dp)
                            .height(30.dp)
                            .background(
                                color = if (isVehicles) PrimarySoft else Color.Transparent,
                                shape = RoundedCornerShape(15.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isVehicles) Icons.Filled.DirectionsCar else Icons.Outlined.DirectionsCar,
                            contentDescription = "Araçlarım",
                            tint = if (isVehicles) PrimaryVibrant else TextMedium,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Araçlarım",
                        fontSize = 11.sp,
                        fontWeight = if (isVehicles) FontWeight.Bold else FontWeight.Medium,
                        color = if (isVehicles) PrimaryVibrant else TextMedium,
                        maxLines = 1
                    )
                }

                // Center Label Slot beneath Elevated + Button
                Column(
                    modifier = Modifier
                        .width(92.dp)
                        .height(66.dp)
                        .clickable(
                            indication = null,
                            interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                        ) { onTabSelected(BottomBarTab.ADD) }
                        .padding(bottom = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom
                ) {
                    Text(
                        text = "Araç Ekle",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryVibrant,
                        maxLines = 1
                    )
                }

                // Right Tab: Profil
                val isProfile = currentTab == BottomBarTab.PROFILE
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 52.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { onTabSelected(BottomBarTab.PROFILE) }
                        .padding(vertical = 4.dp)
                        .testTag("nav_profile"),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .width(48.dp)
                            .height(30.dp)
                            .background(
                                color = if (isProfile) PrimarySoft else Color.Transparent,
                                shape = RoundedCornerShape(15.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isProfile) Icons.Filled.Person else Icons.Outlined.Person,
                            contentDescription = "Profil",
                            tint = if (isProfile) PrimaryVibrant else TextMedium,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Profil",
                        fontSize = 11.sp,
                        fontWeight = if (isProfile) FontWeight.Bold else FontWeight.Medium,
                        color = if (isProfile) PrimaryVibrant else TextMedium,
                        maxLines = 1
                    )
                }
            }
        }

        // Center Elevated Add Vehicle Button (unclipped in outer Box)
        Surface(
            onClick = { onTabSelected(BottomBarTab.ADD) },
            shape = CircleShape,
            color = PrimaryVibrant,
            shadowElevation = 8.dp,
            border = BorderStroke(3.dp, SurfaceWhite),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .size(54.dp)
                .testTag("nav_add_vehicle")
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(
                            listOf(PrimaryVibrant, PrimaryVibrantDark)
                        )
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Araç Ekle",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}

@Composable
fun TurkishPlateBadge(
    plate: String,
    modifier: Modifier = Modifier,
    compact: Boolean = false
) {
    val trLocale = Locale.forLanguageTag("tr-TR")
    val badgeHeight = if (compact) 32.dp else 36.dp
    val stripWidth = if (compact) 23.dp else 27.dp
    val plateFontSize = if (compact) 13.5.sp else 15.5.sp
    val plateLetterSpacing = if (compact) 1.sp else 1.4.sp
    val plateHorizPadding = if (compact) 9.dp else 11.dp

    Surface(
        modifier = modifier
            .border(1.5.dp, PlateText.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
            .shadow(2.dp, RoundedCornerShape(8.dp), spotColor = Color.Black.copy(alpha = 0.15f)),
        shape = RoundedCornerShape(8.dp),
        color = PlateBg
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.height(badgeHeight)
        ) {
            // Blue TR strip
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(stripWidth)
                    .background(PlateBlueTR),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "★",
                        color = Color.White,
                        fontSize = if (compact) 6.sp else 7.sp,
                        lineHeight = if (compact) 6.sp else 7.sp,
                        maxLines = 1,
                        softWrap = false
                    )
                    Text(
                        text = "TR",
                        color = Color.White,
                        fontSize = if (compact) 9.5.sp else 10.5.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.4.sp,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }

            // Plate text
            Text(
                text = plate.uppercase(trLocale),
                color = PlateText,
                fontSize = plateFontSize,
                fontWeight = FontWeight.Black,
                letterSpacing = plateLetterSpacing,
                fontFamily = FontFamily.Monospace,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = plateHorizPadding)
            )
        }
    }
}

@Composable
fun MetricBentoCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    softBg: Color,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = modifier
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .shadow(3.dp, RoundedCornerShape(20.dp), spotColor = TextLight.copy(alpha = 0.12f)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        border = BorderStroke(1.dp, BorderLight)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(softBg, RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                color = TextDark,
                fontWeight = FontWeight.Black
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = TextMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun StatusSquareCard(
    title: String,
    hasData: Boolean,
    activeColor: Color,
    activeBg: Color,
    activeIcon: ImageVector,
    inactiveIcon: ImageVector,
    activeTitle: String,
    activeSubtitle: String,
    inactiveTitle: String,
    inactiveSubtitle: String,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val borderColor = if (hasData) activeColor.copy(alpha = 0.28f) else BorderLight
    val iconBoxBg = if (hasData) activeBg else BgLightAlt
    val iconColor = if (hasData) activeColor else TextLight
    val eyebrowColor = if (hasData) activeColor else TextMedium
    val statusBadgeBg = if (hasData) activeBg else activeColor
    val statusBadgeTextColor = if (hasData) eyebrowColor else Color.White
    val statusDotColor = if (hasData) iconColor else Color.White
    val statusBadgeText = if (hasData) "AKTİF" else "EKLE"

    Card(
        modifier = modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .shadow(if (hasData) 3.dp else 1.dp, RoundedCornerShape(20.dp), spotColor = activeColor.copy(alpha = 0.14f)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        border = BorderStroke(1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(iconBoxBg, RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (hasData) activeIcon else inactiveIcon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelSmall,
                        color = eyebrowColor,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.6.sp
                    )
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = statusBadgeBg
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .background(statusDotColor, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = statusBadgeText,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                color = statusBadgeTextColor
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = if (hasData) activeTitle else inactiveTitle,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextDark,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = if (hasData) activeSubtitle else inactiveSubtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(BgLight, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "Aç",
                    tint = if (hasData) activeColor else TextMedium,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

// -----------------------------------------------------------------------------
// 1. DASHBOARD SCREEN
// -----------------------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onVehicleClick: (Int) -> Unit,
    onAddVehicleClick: () -> Unit,
    onProfileClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onVehicleClickWithTab: (Int, Int) -> Unit = { id, _ -> onVehicleClick(id) },
    onQuickAddMaintenanceClick: (Int) -> Unit = { id -> onVehicleClickWithTab(id, 0) }
) {
    val context = LocalContext.current
    val trLocale = Locale.forLanguageTag("tr-TR")
    val user by viewModel.user.collectAsStateWithLifecycle()
    val vehicles by viewModel.vehicles.collectAsStateWithLifecycle()
    val selectedVehicleId by viewModel.selectedVehicleId.collectAsStateWithLifecycle()
    val allMaintenanceRecords by viewModel.allMaintenanceRecords.collectAsStateWithLifecycle()
    val allFuelRecords by viewModel.allFuelRecords.collectAsStateWithLifecycle()
    val allReminders by viewModel.allReminders.collectAsStateWithLifecycle()
    val fuelPriceState by viewModel.fuelPriceState.collectAsStateWithLifecycle()
    val selectedFuelCity by viewModel.selectedFuelCity.collectAsStateWithLifecycle()
    val isSavingFuel by viewModel.isLoading.collectAsStateWithLifecycle()

    val selectedVehicle: Vehicle? = remember(vehicles, selectedVehicleId) {
        vehicles.firstOrNull { it.id == selectedVehicleId } ?: vehicles.firstOrNull()
    }

    LaunchedEffect(vehicles, selectedVehicleId) {
        if (vehicles.isNotEmpty() && vehicles.none { it.id == selectedVehicleId }) {
            viewModel.selectVehicle(vehicles.first().id)
        }
    }

    val lastKmByVehicleId = remember(vehicles, allMaintenanceRecords, allFuelRecords) {
        vehicles.associate { v ->
            val maxMaint = allMaintenanceRecords.filter { it.vehicleId == v.id }.maxOfOrNull { it.km } ?: 0
            val maxFuel = allFuelRecords.filter { it.vehicleId == v.id }.maxOfOrNull { it.km } ?: 0
            v.id to maxOf(maxMaint, maxFuel)
        }
    }

    val selectedMaintenanceRecords = remember(allMaintenanceRecords, selectedVehicle) {
        selectedVehicle?.let { v -> allMaintenanceRecords.filter { it.vehicleId == v.id } } ?: emptyList()
    }
    val selectedFuelRecords = remember(allFuelRecords, selectedVehicle) {
        selectedVehicle?.let { v -> allFuelRecords.filter { it.vehicleId == v.id } } ?: emptyList()
    }
    val selectedReminders = remember(allReminders, selectedVehicle) {
        selectedVehicle?.let { v -> allReminders.filter { it.vehicleId == v.id } } ?: emptyList()
    }

    var showDashboardFuelModal by remember { mutableStateOf(false) }
    var modalTargetVehicleId by remember { mutableStateOf<Int?>(null) }

    var vehicleToDelete by remember { mutableStateOf<Vehicle?>(null) }
    val now = System.currentTimeMillis()

    // Find urgent reminders for the currently selected vehicle (due in <= 7 days or overdue)
    val urgentReminders = remember(selectedReminders, now) {
        selectedReminders.filter { reminder ->
            val diffDays = (reminder.dueDate - now) / (1000 * 60 * 60 * 24)
            diffDays <= 7
        }
    }

    // Permission check for notifications
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { }
    )

    val sharedPrefs = remember(context) { context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE) }
    var bannerRefreshTick by remember { mutableIntStateOf(0) }
    val activeBannerPrefKey = selectedVehicle?.let { "dashboard_banner_uri_${it.id}" } ?: "dashboard_banner_uri"
    val currentBannerKeyState by rememberUpdatedState(activeBannerPrefKey)
    val customBannerUri = remember(activeBannerPrefKey, bannerRefreshTick) {
        sharedPrefs.getString(activeBannerPrefKey, null)
            ?: sharedPrefs.getString("dashboard_banner_uri", null)
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null) {
                try {
                    context.contentResolver.takePersistableUriPermission(
                        uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                } catch (e: Exception) {
                    // Ignore
                }
                sharedPrefs.edit().putString(currentBannerKeyState, uri.toString()).apply()
                bannerRefreshTick++
            }
        }
    )

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    Scaffold(
        containerColor = BgLight,
        contentWindowInsets = WindowInsets.safeDrawing,
        bottomBar = {
            ModernAppBottomBar(
                currentTab = BottomBarTab.VEHICLES,
                onTabSelected = { tab ->
                    when (tab) {
                        BottomBarTab.VEHICLES -> { /* Zaten Araçlarım / Dashboard ekranında */ }
                        BottomBarTab.ADD -> onAddVehicleClick()
                        BottomBarTab.PROFILE -> onProfileClick()
                    }
                }
            )
        },
        topBar = {
            Surface(
                color = SurfaceWhite,
                border = BorderStroke(0.5.dp, BorderLight),
                shadowElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .windowInsetsPadding(topSafeBarWindowInsets())
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    Brush.linearGradient(listOf(PrimaryVibrant, SecondaryVibrant)),
                                    RoundedCornerShape(12.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.DirectionsCar,
                                contentDescription = "Logo",
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "ARAÇ KARNESİ",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = TextDark,
                                letterSpacing = 0.4.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            val garageTitle = remember(user) {
                                formatGarageTitle(user?.garageName, user?.name, user?.surname)
                            }
                            Text(
                                text = "$garageTitle • ${selectedFuelCity.displayName}",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMedium,
                                fontSize = 11.5.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                NotificationHelper.showInstantNotification(
                                    context,
                                    9999,
                                    "Araç Karnesi Bildirimi",
                                    "Bildirim sistemi aktif! Bakım ve muayene tarihleriniz zamanında bildirilecektir."
                                )
                            },
                            modifier = Modifier
                                .size(44.dp)
                                .background(PrimarySoft, CircleShape)
                        ) {
                            Icon(
                                Icons.Outlined.NotificationsActive,
                                contentDescription = "Test Bildirimi",
                                tint = PrimaryVibrant,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        IconButton(
                            onClick = onProfileClick,
                            modifier = Modifier
                                .size(44.dp)
                                .background(PrimarySoft, CircleShape)
                                .testTag("profile_button")
                        ) {
                            Icon(
                                Icons.Outlined.Person,
                                contentDescription = "Profilim",
                                tint = PrimaryVibrant,
                                modifier = Modifier.size(21.dp)
                            )
                        }
                    }
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.TopCenter
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxHeight()
                    .widthIn(max = 680.dp)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 28.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
            // Multi-Vehicle Selector (Shown automatically when 2 or more vehicles exist)
            if (vehicles.size >= 2 && selectedVehicle != null) {
                item(key = "dashboard_vehicle_selector_item") {
                    DashboardVehicleSelectorCard(
                        vehicles = vehicles,
                        selectedVehicle = selectedVehicle,
                        lastKmByVehicleId = lastKmByVehicleId,
                        onVehicleSelected = { pickedId -> viewModel.selectVehicle(pickedId) }
                    )
                }
            }

            // Dashboard Hero Banner
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(185.dp)
                        .shadow(4.dp, RoundedCornerShape(20.dp), spotColor = PrimaryVibrant.copy(alpha = 0.16f))
                        .clickable {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        }
                        .testTag("dashboard_banner_card"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                    border = BorderStroke(1.dp, BorderLight)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(customBannerUri ?: com.example.R.drawable.fiat_egea)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Araç Fotoğrafı",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.42f)),
                                        startY = 180f
                                    )
                                )
                        )
                        Row(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (selectedVehicle != null) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color.Black.copy(alpha = 0.50f),
                                    modifier = Modifier.weight(1f, fill = false)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.DirectionsCar,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(15.dp)
                                        )
                                        Text(
                                            text = "${selectedVehicle.plate.uppercase(trLocale)} • ${selectedVehicle.brand}",
                                            color = Color.White,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                            } else {
                                Spacer(modifier = Modifier.weight(1f))
                            }

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color.Black.copy(alpha = 0.48f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        Icons.Default.AddAPhoto,
                                        contentDescription = "Fotoğrafı Değiştir",
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "Fotoğraf",
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Quick Add Row (Hızlı Ekle: Yakıt Ekle & Bakım Ekle — White Text & Icons)
            item(key = "dashboard_quick_add_row") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            if (selectedVehicle == null) {
                                Toast.makeText(context, "Önce bir araç ekleyiniz", Toast.LENGTH_SHORT).show()
                                onAddVehicleClick()
                            } else {
                                modalTargetVehicleId = selectedVehicle.id
                                showDashboardFuelModal = true
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .testTag("quick_add_fuel_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = FuelColor,
                            contentColor = Color.White
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalGasStation,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(7.dp))
                        Text(
                            text = "Yakıt Ekle",
                            color = Color.White,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            softWrap = false
                        )
                    }

                    Button(
                        onClick = {
                            if (selectedVehicle == null) {
                                Toast.makeText(context, "Önce bir araç ekleyiniz", Toast.LENGTH_SHORT).show()
                                onAddVehicleClick()
                            } else {
                                onQuickAddMaintenanceClick(selectedVehicle.id)
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .testTag("quick_add_maintenance_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryVibrant,
                            contentColor = Color.White
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Build,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(17.dp)
                        )
                        Spacer(modifier = Modifier.width(7.dp))
                        Text(
                            text = "Bakım Ekle",
                            color = Color.White,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
            }

            // Urgent Maintenance / Reminder Banner (filtered for selected vehicle)
            if (urgentReminders.isNotEmpty()) {
                val hasOverdue = urgentReminders.any { now > it.dueDate }
                val bannerAccent = if (hasOverdue) AccentRose else ReminderColor
                val bannerAccentText = if (hasOverdue) MaintenanceRedDark else ReminderColorDark
                val bannerSoft = if (hasOverdue) AccentRoseSoft else ReminderSoft
                val bannerBorder = if (hasOverdue) MaintenanceRedBorder else ReminderBorder
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(2.dp, RoundedCornerShape(20.dp), spotColor = bannerAccent.copy(alpha = 0.12f))
                            .clickable {
                                selectedVehicle?.let { onVehicleClickWithTab(it.id, 3) }
                            },
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = bannerSoft),
                        border = BorderStroke(1.dp, bannerBorder)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .background(bannerAccent, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (hasOverdue) Icons.Default.NotificationImportant else Icons.Default.NotificationsActive,
                                    contentDescription = null,
                                    tint = Color.White
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (hasOverdue) "GECİKEN HATIRLATICI UYARISI" else "YAKLAŞAN HATIRLATICI PLANI",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = bannerAccentText,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${selectedVehicle?.plate?.let { "$it • " } ?: ""}${urgentReminders.size} adet hatırlatıcı yaklaşmakta veya kontrol bekliyor.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextDark,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }

            // Stats Bento Row (Shows selected vehicle's metrics when a vehicle is active)
            item {
                val hasOverdueReminder = selectedReminders.any { now > it.dueDate }
                val selectedKm = selectedVehicle?.let { lastKmByVehicleId[it.id] ?: 0 } ?: 0
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (selectedVehicle != null) {
                        MetricBentoCard(
                            title = "BAKIM KAYDI",
                            value = "${selectedMaintenanceRecords.size}",
                            subtitle = if (selectedKm > 0) {
                                String.format(trLocale, "%,d km • %s", selectedKm, selectedVehicle.plate)
                            } else {
                                "${selectedVehicle.plate} • Servis"
                            },
                            icon = Icons.Default.BuildCircle,
                            color = PrimaryVibrantDark,
                            softBg = PrimarySoft,
                            modifier = Modifier.weight(1f),
                            onClick = { onVehicleClickWithTab(selectedVehicle.id, 0) }
                        )
                    } else {
                        MetricBentoCard(
                            title = "GARAJ",
                            value = "0",
                            subtitle = "Kayıtlı Araç",
                            icon = Icons.Default.DirectionsCar,
                            color = PrimaryVibrantDark,
                            softBg = PrimarySoft,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    MetricBentoCard(
                        title = "HATIRLATICI",
                        value = "${selectedReminders.size}",
                        subtitle = when {
                            urgentReminders.isNotEmpty() -> "${urgentReminders.size} Yaklaşan"
                            selectedReminders.isNotEmpty() -> "Planlandı"
                            else -> "Kurulu Alarm Yok"
                        },
                        icon = Icons.Default.Alarm,
                        color = if (hasOverdueReminder) MaintenanceRedDark else ReminderColorDark,
                        softBg = if (hasOverdueReminder) AccentRoseSoft else ReminderSoft,
                        modifier = Modifier.weight(1f),
                        onClick = selectedVehicle?.let { v -> { onVehicleClickWithTab(v.id, 3) } }
                    )
                }
            }

            // Live Fuel Pump Prices Card (highlights selected vehicle's fuel type; city selected in Profile only)
            item {
                val activeFuelType = selectedVehicle?.fuelType
                LiveFuelPricesCard(
                    fuelPriceState = fuelPriceState,
                    selectedCity = selectedFuelCity,
                    highlightedFuelType = activeFuelType,
                    onRefresh = { viewModel.refreshFuelPrices() }
                )
            }

            // Monthly Fuel Consumption & Average TL Visualization (filtered for selected vehicle)
            item {
                val activeFuelType = selectedVehicle?.fuelType
                val liveUnitPrice = (fuelPriceState as? FuelPriceUiState.Success)
                    ?.prices
                    ?.priceForFuelType(activeFuelType)
                MonthlyFuelChartSection(
                    fuelRecords = selectedFuelRecords,
                    liveUnitPriceTl = liveUnitPrice,
                    sectionTitle = if (selectedVehicle != null) {
                        "${selectedVehicle.plate.uppercase(trLocale)} • AYLIK YAKIT GRAFİĞİ"
                    } else {
                        "AYLIK YAKIT TÜKETİM GRAFİĞİ"
                    },
                    onAddFuelClick = {
                        if (selectedVehicle == null) {
                            Toast.makeText(context, "Önce bir araç ekleyiniz", Toast.LENGTH_SHORT).show()
                            onAddVehicleClick()
                        } else {
                            modalTargetVehicleId = selectedVehicle.id
                            showDashboardFuelModal = true
                        }
                    }
                )
            }

            // Section Header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (vehicles.size >= 2) "SEÇİLİ ARAÇ ÖZETİ" else "ARAÇLARIM",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextDark,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = if (vehicles.size >= 2 && selectedVehicle != null) {
                            "${selectedVehicle.brand} ${selectedVehicle.model}"
                        } else {
                            "${vehicles.size} Araç"
                        },
                        style = MaterialTheme.typography.labelMedium,
                        color = TextMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // 2 Stacked Menu Cards under ARAÇLARIM (Filtered for selected vehicle)
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatusSquareCard(
                        title = "PERİYODİK BAKIM",
                        hasData = selectedMaintenanceRecords.isNotEmpty(),
                        activeColor = PrimaryVibrantDark,
                        activeBg = PrimarySoft,
                        activeIcon = Icons.Default.BuildCircle,
                        inactiveIcon = Icons.Outlined.Build,
                        activeTitle = "${selectedMaintenanceRecords.size} Bakım Kaydı Mevcut",
                        activeSubtitle = selectedMaintenanceRecords.firstOrNull()?.let {
                            val part = it.type.split(',').firstOrNull()?.trim() ?: it.type
                            "Son işlem: $part • ${String.format(trLocale, "%,d km", it.km)}"
                        } ?: "Bakımlar güncel durumda",
                        inactiveTitle = "Bakım Kaydı Yok",
                        inactiveSubtitle = "Servis ve bakım geçmişini eklemek için dokunun",
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            if (selectedVehicle == null) {
                                onAddVehicleClick()
                            } else {
                                onVehicleClickWithTab(selectedVehicle.id, 0)
                            }
                        }
                    )

                    StatusSquareCard(
                        title = "HATIRLATICI & ALARM",
                        hasData = selectedReminders.isNotEmpty(),
                        activeColor = ReminderColorDark,
                        activeBg = ReminderSoft,
                        activeIcon = Icons.Default.AlarmOn,
                        inactiveIcon = Icons.Outlined.Alarm,
                        activeTitle = "${selectedReminders.size} Aktif Hatırlatıcı",
                        activeSubtitle = selectedReminders.firstOrNull()?.let { "Yaklaşan: ${it.title}" } ?: "Kurulu bakım ve muayene alarmları",
                        inactiveTitle = "Hatırlatıcı Yok",
                        inactiveSubtitle = "Muayene ve sigorta alarmı kurmak için dokunun",
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            if (selectedVehicle == null) {
                                onAddVehicleClick()
                            } else {
                                onVehicleClickWithTab(selectedVehicle.id, 3)
                            }
                        }
                    )
                }
            }

            // Active Selected Vehicle Card or Empty State
            if (selectedVehicle == null) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 20.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                        border = BorderStroke(1.dp, BorderLight)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .background(PrimarySoft, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.DirectionsCarFilled,
                                    contentDescription = null,
                                    tint = PrimaryVibrant,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Henüz Araç Eklenmedi",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = TextDark
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Aracınızı sisteme ekleyerek servis karnesini, triger, v kayışı, periyodik bakımları ve muayene hatırlatıcılarını tek ekranda takip edin.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextMedium,
                                textAlign = TextAlign.Center,
                                lineHeight = 20.sp
                            )
                            Spacer(modifier = Modifier.height(20.dp))
                            Button(
                                onClick = onAddVehicleClick,
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = PrimaryVibrant,
                                    contentColor = Color.White
                                )
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Hemen İlk Aracını Ekle", fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                }
            } else {
                item(key = "selected_vehicle_card_${selectedVehicle.id}") {
                    ModernVehicleCard(
                        vehicle = selectedVehicle,
                        latestKm = lastKmByVehicleId[selectedVehicle.id] ?: 0,
                        onClick = { onVehicleClick(selectedVehicle.id) },
                        onQuickFuelClick = {
                            modalTargetVehicleId = selectedVehicle.id
                            showDashboardFuelModal = true
                        },
                        onQuickMaintenanceClick = {
                            onQuickAddMaintenanceClick(selectedVehicle.id)
                        },
                        onDeleteClick = { vehicleToDelete = selectedVehicle }
                    )
                }
            }
            }
        }
    }

    if (showDashboardFuelModal && vehicles.isNotEmpty()) {
        AddFuelModalDialog(
            vehicles = vehicles,
            initialVehicleId = modalTargetVehicleId,
            lastKmByVehicleId = lastKmByVehicleId,
            fuelPriceState = fuelPriceState,
            isSaving = isSavingFuel,
            onSaveFuel = { targetVehicleId, liters, price, newTotalKm, drivenKm ->
                viewModel.addFuelRecord(
                    vehicleId = targetVehicleId,
                    liters = liters,
                    price = price,
                    km = newTotalKm,
                    date = System.currentTimeMillis(),
                    onSuccess = {
                        showDashboardFuelModal = false
                        Toast.makeText(
                            context,
                            String.format(
                                Locale.forLanguageTag("tr-TR"),
                                "Yakıt kaydedildi! Yeni güncel KM: %,d km (+%,d km)",
                                newTotalKm,
                                drivenKm
                            ),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                )
            },
            onDismiss = { showDashboardFuelModal = false }
        )
    }

    // Delete Confirmation Dialog
    if (vehicleToDelete != null) {
        AlertDialog(
            onDismissRequest = { vehicleToDelete = null },
            icon = { Icon(Icons.Default.DeleteOutline, contentDescription = null, tint = AccentRose) },
            title = { Text("Aracı Sil?", fontWeight = FontWeight.Bold) },
            text = {
                Text("${vehicleToDelete?.brand} ${vehicleToDelete?.model} (${vehicleToDelete?.plate}) silinecek. Bu işlem araca ait tüm bakım ve hatırlatıcı kayıtlarını da silecektir.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        vehicleToDelete?.let { viewModel.deleteVehicle(it.id) }
                        vehicleToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AccentRose,
                        contentColor = Color.White
                    )
                ) {
                    Text("Evet, Sil", fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { vehicleToDelete = null }) {
                    Text("İptal", color = TextDark)
                }
            },
            shape = RoundedCornerShape(20.dp),
            containerColor = SurfaceWhite
        )
    }
}

// -----------------------------------------------------------------------------
// VEHICLE CARD COMPONENT
// -----------------------------------------------------------------------------

@Composable
fun ModernVehicleCard(
    vehicle: Vehicle,
    latestKm: Int = 0,
    onClick: () -> Unit,
    onQuickFuelClick: (() -> Unit)? = null,
    onQuickMaintenanceClick: (() -> Unit)? = null,
    onDeleteClick: () -> Unit
) {
    val trLocale = Locale.forLanguageTag("tr-TR")
    val normalizedFuel = normalizeFuelLookupKey(vehicle.fuelType)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(24.dp), spotColor = TextLight.copy(alpha = 0.12f))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        border = BorderStroke(1.dp, BorderLight)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${vehicle.brand} ${vehicle.model}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextDark,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = if (latestKm > 0) {
                            "${vehicle.year} Model • Son Güncel KM: ${String.format(trLocale, "%,d km", latestKm)}"
                        } else {
                            "${vehicle.year} Model"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                IconButton(
                    onClick = onDeleteClick,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        Icons.Outlined.Delete,
                        contentDescription = "Aracı Sil",
                        tint = TextLight,
                        modifier = Modifier.size(19.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Turkish Plate Badge & Fuel Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TurkishPlateBadge(plate = vehicle.plate)

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = FuelSoft,
                    border = BorderStroke(1.dp, FuelBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (normalizedFuel.contains("elektrik") || normalizedFuel.contains("electric")) {
                                Icons.Default.ElectricCar
                            } else {
                                Icons.Default.LocalGasStation
                            },
                            contentDescription = null,
                            tint = FuelColorDark,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = vehicle.fuelType.uppercase(trLocale),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = FuelColorDark,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Quick Add Buttons Row (Yakıt Ekle & Bakım Ekle — White Text) + Araç Karnesi
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (onQuickFuelClick != null || onQuickMaintenanceClick != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (onQuickFuelClick != null) {
                            Button(
                                onClick = onQuickFuelClick,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("vehicle_card_add_fuel_${vehicle.id}"),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = FuelColor,
                                    contentColor = Color.White
                                ),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp)
                            ) {
                                Icon(
                                    Icons.Default.LocalGasStation,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(17.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Yakıt Ekle",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }

                        if (onQuickMaintenanceClick != null) {
                            Button(
                                onClick = onQuickMaintenanceClick,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("vehicle_card_add_maintenance_${vehicle.id}"),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = PrimaryVibrant,
                                    contentColor = Color.White
                                ),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp)
                            ) {
                                Icon(
                                    Icons.Default.Build,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Bakım Ekle",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }
                    }
                }

                OutlinedButton(
                    onClick = onClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, PrimaryVibrant.copy(alpha = 0.38f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryVibrantDark)
                ) {
                    Icon(
                        Icons.Default.MenuBook,
                        contentDescription = null,
                        tint = PrimaryVibrantDark,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "ARAÇ KARNESİ & DETAYLAR",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryVibrantDark,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------
// 2. VEHICLE DETAIL SCREEN (ARAÇ KARNESİ)
// -----------------------------------------------------------------------------

enum class MaintenanceFilterType(val label: String, val icon: ImageVector) {
    ALL("Tümü", Icons.Default.ListAlt),
    PERIODIC("Periyodik", Icons.Default.Build),
    REPAIR("Onarım", Icons.Default.Handyman),
    CUSTOM("Kullanıcı Tanımlı", Icons.Default.EditNote)
}

fun getMaintenanceCategories(record: MaintenanceRecord): Set<MaintenanceFilterType> {
    val result = mutableSetOf<MaintenanceFilterType>()
    val typeLower = record.type.lowercase(Locale("tr", "TR"))

    // Periodic keywords
    val periodicKeywords = listOf("periyodik", "yağ", "hava filtre", "polen filtre", "mazot", "yakıt", "antifriz", "sıvı", "muayene")
    val isPeriodic = periodicKeywords.any { typeLower.contains(it) }
    if (isPeriodic) {
        result.add(MaintenanceFilterType.PERIODIC)
    }

    // Repair keywords (Onarım / Değişim)
    val repairKeywords = listOf("triger", "v kayış", "kayış", "devirdaim", "fren", "balata", "disk", "buji", "ateşleme", "şanzıman", "debriyaj", "lastik", "akü", "amortisör", "rot", "tamir", "onarım", "pompa")
    val isRepair = repairKeywords.any { typeLower.contains(it) }
    if (isRepair) {
        result.add(MaintenanceFilterType.REPAIR)
    }

    // Standard catalog titles
    val standardTitles = listOf(
        "periyodik bakım", "triger kayışı", "triger seti", "v kayışı", "su devirdaim pompası",
        "mazot pompası / filtresi", "hava filtresi", "polen filtresi", "fren balata & disk",
        "buji & ateşleme sistemi", "şanzıman yağı & debriyaj", "antifriz & sıvı bakımı",
        "yağ & filtre", "araç muayenesi", "lastik değişimi", "akü değişimi", "fren & balata", "diğer"
    )

    val parts = record.type.split(",").map { it.trim().lowercase(Locale("tr", "TR")) }.filter { it.isNotEmpty() }
    val hasCustomPart = parts.any { part -> standardTitles.none { std -> part == std || std.startsWith(part) || part.startsWith(std) } }

    if (hasCustomPart || (!isPeriodic && !isRepair)) {
        result.add(MaintenanceFilterType.CUSTOM)
    }

    return result
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VehicleDetailScreen(
    viewModel: VehicleDetailViewModel,
    initialTab: Int = 0,
    onBack: () -> Unit,
    onAddMaintenance: () -> Unit,
    onAddReminder: () -> Unit,
    onAddExpense: () -> Unit
) {
    val vehicle by viewModel.vehicle.collectAsStateWithLifecycle()
    val maintenanceRecords by viewModel.maintenanceRecords.collectAsStateWithLifecycle()
    val expenseRecords by viewModel.expenseRecords.collectAsStateWithLifecycle()
    val reminders by viewModel.reminders.collectAsStateWithLifecycle()
    val fuelRecords by viewModel.fuelRecords.collectAsStateWithLifecycle()
    val fuelPriceState by viewModel.fuelPriceState.collectAsStateWithLifecycle()
    val isSavingFuel by viewModel.isLoading.collectAsStateWithLifecycle()
    var selectedTab by remember(initialTab) { mutableIntStateOf(initialTab.coerceIn(0, 3)) }
    var maintenanceFilter by remember { mutableStateOf(MaintenanceFilterType.ALL) }
    var showAddFuelModal by remember { mutableStateOf(false) }

    val periodicCount = remember(maintenanceRecords) {
        maintenanceRecords.count { MaintenanceFilterType.PERIODIC in getMaintenanceCategories(it) }
    }
    val repairCount = remember(maintenanceRecords) {
        maintenanceRecords.count { MaintenanceFilterType.REPAIR in getMaintenanceCategories(it) }
    }
    val customCount = remember(maintenanceRecords) {
        maintenanceRecords.count { MaintenanceFilterType.CUSTOM in getMaintenanceCategories(it) }
    }

    val totalMaintenanceCost = remember(maintenanceRecords) {
        maintenanceRecords.sumOf { it.cost }
    }
    val totalExpenseCost = remember(expenseRecords) {
        expenseRecords.sumOf { it.amount }
    }
    val totalFuelCost = remember(fuelRecords) {
        fuelRecords.sumOf { it.total }
    }
    val totalCombinedCost = totalMaintenanceCost + totalExpenseCost
    val grandTotalCost = totalCombinedCost + totalFuelCost

    val latestKm = remember(maintenanceRecords, fuelRecords) {
        maxOf(
            maintenanceRecords.maxOfOrNull { it.km } ?: 0,
            fuelRecords.maxOfOrNull { it.km } ?: 0
        )
    }

    var showExportDialog by remember { mutableStateOf(false) }
    var showVehicleOverviewModal by remember { mutableStateOf(false) }

    // Modal state holders for click-to-view item details
    var selectedMaintenanceForDetail by remember { mutableStateOf<MaintenanceRecord?>(null) }
    var selectedExpenseForDetail by remember { mutableStateOf<ExpenseRecord?>(null) }
    var selectedFuelForDetail by remember { mutableStateOf<FuelRecord?>(null) }
    var selectedReminderForDetail by remember { mutableStateOf<Reminder?>(null) }

    val filteredMaintenanceRecords = remember(maintenanceRecords, maintenanceFilter) {
        when (maintenanceFilter) {
            MaintenanceFilterType.ALL -> maintenanceRecords
            MaintenanceFilterType.PERIODIC -> maintenanceRecords.filter { MaintenanceFilterType.PERIODIC in getMaintenanceCategories(it) }
            MaintenanceFilterType.REPAIR -> maintenanceRecords.filter { MaintenanceFilterType.REPAIR in getMaintenanceCategories(it) }
            MaintenanceFilterType.CUSTOM -> maintenanceRecords.filter { MaintenanceFilterType.CUSTOM in getMaintenanceCategories(it) }
        }
    }

    var recordToDelete by remember { mutableStateOf<MaintenanceRecord?>(null) }
    var expenseToDelete by remember { mutableStateOf<ExpenseRecord?>(null) }
    var fuelToDelete by remember { mutableStateOf<FuelRecord?>(null) }
    var reminderToDelete by remember { mutableStateOf<Reminder?>(null) }
    val context = LocalContext.current

    val liveUnitPrice = (fuelPriceState as? FuelPriceUiState.Success)
        ?.prices
        ?.priceForFuelType(vehicle?.fuelType)

    val drivenKmMap = remember(fuelRecords, maintenanceRecords) {
        val sortedByKm = fuelRecords
            .filter { it.km > 0 }
            .sortedWith(compareBy<FuelRecord> { it.km }.thenBy { it.date })
        buildMap<Int, Int> {
            if (sortedByKm.isNotEmpty()) {
                val firstFuelKm = sortedByKm.first().km
                val prevMaintKm = maintenanceRecords
                    .map { it.km }
                    .filter { it in 1 until firstFuelKm }
                    .maxOrNull()
                if (prevMaintKm != null) {
                    put(sortedByKm.first().id, firstFuelKm - prevMaintKm)
                }
            }
            for (i in 1 until sortedByKm.size) {
                val diff = sortedByKm[i].km - sortedByKm[i - 1].km
                if (diff > 0) {
                    put(sortedByKm[i].id, diff)
                }
            }
        }
    }

    Scaffold(
        containerColor = BgLight,
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            TopAppBar(
                windowInsets = topSafeBarWindowInsets(),
                title = {
                    Column {
                        Text(
                            text = vehicle?.let { "${it.brand} ${it.model}" } ?: "Araç Karnesi",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = vehicle?.plate?.uppercase() ?: "",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMedium,
                            fontFamily = FontFamily.Monospace,
                            maxLines = 1
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri", tint = TextDark)
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showVehicleOverviewModal = true },
                        modifier = Modifier
                            .padding(end = 6.dp)
                            .size(42.dp)
                            .background(PrimarySoft, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Analytics,
                            contentDescription = "Maliyet Özeti",
                            tint = PrimaryVibrant,
                            modifier = Modifier.size(19.dp)
                        )
                    }
                    IconButton(
                        onClick = { showExportDialog = true },
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .size(42.dp)
                            .background(BgLightAlt, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PictureAsPdf,
                            contentDescription = "PDF Rapor Al",
                            tint = TextDark,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SurfaceWhite,
                    titleContentColor = TextDark
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    when (selectedTab) {
                        0 -> onAddMaintenance()
                        1 -> onAddExpense()
                        2 -> showAddFuelModal = true
                        3 -> onAddReminder()
                    }
                },
                containerColor = when (selectedTab) {
                    0 -> PrimaryVibrant
                    1 -> InsuranceBlue
                    2 -> FuelColor
                    3 -> ReminderColor
                    else -> PrimaryVibrant
                },
                contentColor = Color.White,
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("detail_primary_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = when (selectedTab) {
                        0 -> "BAKIM EKLE"
                        1 -> "GİDER EKLE"
                        2 -> "YAKIT EKLE"
                        3 -> "HATIRLATICI KUR"
                        else -> "EKLE"
                    },
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    ) { padding ->
        val trLocale = Locale.forLanguageTag("tr-TR")
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.TopCenter
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxHeight()
                    .widthIn(max = 680.dp)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // 1. Simplified Vehicle Summary Card (Tap to open full Overview Modal)
                vehicle?.let { v ->
                    item(key = "vehicle_hero_card") {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(3.dp, RoundedCornerShape(22.dp), spotColor = PrimaryVibrant.copy(alpha = 0.12f))
                                .clickable { showVehicleOverviewModal = true }
                                .testTag("vehicle_summary_card"),
                            shape = RoundedCornerShape(22.dp),
                            colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                            border = BorderStroke(1.dp, BorderLight)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    TurkishPlateBadge(plate = v.plate)

                                    Spacer(modifier = Modifier.width(8.dp))

                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = FuelSoft,
                                        border = BorderStroke(1.dp, FuelBorder)
                                    ) {
                                        Text(
                                            text = "${v.year} • ${v.fuelType.uppercase(trLocale)}",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = FuelColorDark,
                                            maxLines = 1,
                                            softWrap = false,
                                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp)
                                        )
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = BgLight,
                                    border = BorderStroke(1.dp, BorderLight),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 14.dp, vertical = 10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "TOPLAM KARNE HARCAMASI",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = TextMedium,
                                                letterSpacing = 0.5.sp,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Text(
                                                    text = String.format(trLocale, "%,.0f ₺", grandTotalCost),
                                                    style = MaterialTheme.typography.titleMedium,
                                                    fontWeight = FontWeight.Black,
                                                    color = TotalCostGreenDark,
                                                    maxLines = 1,
                                                    softWrap = false
                                                )
                                                if (latestKm > 0) {
                                                    Surface(
                                                        shape = RoundedCornerShape(6.dp),
                                                        color = SurfaceWhite,
                                                        border = BorderStroke(1.dp, BorderLight)
                                                    ) {
                                                        Text(
                                                            text = String.format(trLocale, "%,d km", latestKm),
                                                            fontSize = 10.5.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = TextDark,
                                                            maxLines = 1,
                                                            softWrap = false,
                                                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                                        )
                                                    }
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.width(8.dp))

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = "Detay & Analiz",
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = PrimaryVibrant,
                                                maxLines = 1,
                                                softWrap = false
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Icon(
                                                imageVector = Icons.Default.ChevronRight,
                                                contentDescription = null,
                                                tint = PrimaryVibrant,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // 2. Responsive Scrollable Tabs Card (never truncates on 320dp–360dp phones)
                item(key = "vehicle_detail_tabs") {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = SurfaceWhite,
                        border = BorderStroke(1.dp, BorderLight),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        ScrollableTabRow(
                            selectedTabIndex = selectedTab,
                            edgePadding = 8.dp,
                            containerColor = Color.Transparent,
                            contentColor = PrimaryVibrant,
                            divider = {},
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Tab(
                                selected = selectedTab == 0,
                                onClick = { selectedTab = 0 },
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Default.Build,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp),
                                            tint = if (selectedTab == 0) MaintenanceRed else TextMedium
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Bakım (${maintenanceRecords.size})",
                                            fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Medium,
                                            color = if (selectedTab == 0) MaintenanceRed else TextMedium,
                                            fontSize = 13.sp,
                                            maxLines = 1
                                        )
                                    }
                                }
                            )
                            Tab(
                                selected = selectedTab == 1,
                                onClick = { selectedTab = 1 },
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Default.Security,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp),
                                            tint = if (selectedTab == 1) InsuranceBlue else TextMedium
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Sigorta (${expenseRecords.size})",
                                            fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Medium,
                                            color = if (selectedTab == 1) InsuranceBlue else TextMedium,
                                            fontSize = 13.sp,
                                            maxLines = 1
                                        )
                                    }
                                }
                            )
                            Tab(
                                selected = selectedTab == 2,
                                onClick = { selectedTab = 2 },
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Default.LocalGasStation,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp),
                                            tint = if (selectedTab == 2) FuelColor else TextMedium
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Yakıt (${fuelRecords.size})",
                                            fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Medium,
                                            color = if (selectedTab == 2) FuelColor else TextMedium,
                                            fontSize = 13.sp,
                                            maxLines = 1
                                        )
                                    }
                                }
                            )
                            Tab(
                                selected = selectedTab == 3,
                                onClick = { selectedTab = 3 },
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Default.Notifications,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp),
                                            tint = if (selectedTab == 3) ReminderColor else TextMedium
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Alarm (${reminders.size})",
                                            fontWeight = if (selectedTab == 3) FontWeight.Bold else FontWeight.Medium,
                                            color = if (selectedTab == 3) ReminderColor else TextMedium,
                                            fontSize = 13.sp,
                                            maxLines = 1
                                        )
                                    }
                                }
                            )
                        }
                    }
                }

                // 3. Active Tab Content
                when (selectedTab) {
                    0 -> {
                        if (maintenanceRecords.isEmpty()) {
                            item(key = "maint_empty") {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(18.dp),
                                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                                    border = BorderStroke(1.dp, BorderLight)
                                ) {
                                    Column(
                                        modifier = Modifier.fillMaxWidth().padding(28.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Box(
                                            modifier = Modifier.size(60.dp).background(PrimarySoft, CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(Icons.Default.BuildCircle, contentDescription = null, tint = PrimaryVibrant, modifier = Modifier.size(30.dp))
                                        }
                                        Spacer(modifier = Modifier.height(14.dp))
                                        Text("Bakım Kaydı Bulunmuyor", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextDark)
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            "Araca yapılan periyodik bakım, triger, v kayışı ve diğer servis işlemlerini ekleyerek karnenizi oluşturun.",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = TextMedium,
                                            textAlign = TextAlign.Center
                                        )
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Button(
                                            onClick = onAddMaintenance,
                                            shape = RoundedCornerShape(14.dp),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = PrimaryVibrant,
                                                contentColor = Color.White
                                            )
                                        ) {
                                            Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Bakım Ekle", fontWeight = FontWeight.Bold, color = Color.White)
                                        }
                                    }
                                }
                            }
                        } else {
                            item(key = "maint_filters") {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val filterOptions = listOf(
                                        Triple(MaintenanceFilterType.ALL, "Tümü", maintenanceRecords.size),
                                        Triple(MaintenanceFilterType.PERIODIC, "Periyodik", periodicCount),
                                        Triple(MaintenanceFilterType.REPAIR, "Onarım", repairCount),
                                        Triple(MaintenanceFilterType.CUSTOM, "Kullanıcı Tanımlı", customCount)
                                    )

                                    filterOptions.forEach { (filterType, label, count) ->
                                        val isSelected = maintenanceFilter == filterType
                                        FilterChip(
                                            selected = isSelected,
                                            onClick = { maintenanceFilter = filterType },
                                            leadingIcon = {
                                                Icon(
                                                    imageVector = filterType.icon,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(16.dp),
                                                    tint = if (isSelected) PrimaryVibrant else TextMedium
                                                )
                                            },
                                            label = {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text(text = label, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium)
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Surface(
                                                        shape = RoundedCornerShape(10.dp),
                                                        color = if (isSelected) PrimaryVibrant else BgLightAlt
                                                    ) {
                                                        Text(
                                                            text = "$count",
                                                            fontSize = 11.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = if (isSelected) Color.White else TextMedium,
                                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                                                        )
                                                    }
                                                }
                                            },
                                            shape = RoundedCornerShape(12.dp),
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = PrimarySoft,
                                                selectedLabelColor = PrimaryVibrant,
                                                containerColor = SurfaceWhite,
                                                labelColor = TextDark
                                            ),
                                            border = FilterChipDefaults.filterChipBorder(
                                                enabled = true,
                                                selected = isSelected,
                                                borderColor = if (isSelected) PrimaryVibrant.copy(alpha = 0.5f) else BorderLight
                                            )
                                        )
                                    }
                                }
                            }

                            if (filteredMaintenanceRecords.isEmpty()) {
                                item(key = "maint_filter_empty") {
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(18.dp),
                                        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                                        border = BorderStroke(1.dp, BorderLight)
                                    ) {
                                        Column(
                                            modifier = Modifier.fillMaxWidth().padding(28.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Box(
                                                modifier = Modifier.size(56.dp).background(BgLightAlt, CircleShape),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(Icons.Default.FilterAltOff, contentDescription = null, tint = TextMedium, modifier = Modifier.size(28.dp))
                                            }
                                            Spacer(modifier = Modifier.height(12.dp))
                                            Text("Bu Filtrede Kayıt Yok", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextDark)
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "'${maintenanceFilter.label}' türünde kayıtlı bakım bulunamadı.",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = TextMedium,
                                                textAlign = TextAlign.Center
                                            )
                                            Spacer(modifier = Modifier.height(12.dp))
                                            OutlinedButton(
                                                onClick = { maintenanceFilter = MaintenanceFilterType.ALL },
                                                shape = RoundedCornerShape(12.dp)
                                            ) {
                                                Text("Tüm Bakımları Göster")
                                            }
                                        }
                                    }
                                }
                            } else {
                                item(key = "maint_tap_hint") {
                                    Text(
                                        text = "BAKIM GEÇMİŞİ • Detay için kayda dokunun",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = TextMedium,
                                        letterSpacing = 0.4.sp
                                    )
                                }
                                items(filteredMaintenanceRecords, key = { "maint_${it.id}" }) { record ->
                                    MaintenanceRecordCard(
                                        record = record,
                                        onClick = { selectedMaintenanceForDetail = record },
                                        onDelete = { recordToDelete = record }
                                    )
                                }
                            }
                        }
                    }

                    1 -> {
                        if (expenseRecords.isEmpty()) {
                            item(key = "exp_empty") {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(18.dp),
                                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                                    border = BorderStroke(1.dp, BorderLight)
                                ) {
                                    Column(
                                        modifier = Modifier.fillMaxWidth().padding(28.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Box(
                                            modifier = Modifier.size(60.dp).background(InsuranceBlueSoft, CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(Icons.Default.Security, contentDescription = null, tint = InsuranceBlue, modifier = Modifier.size(30.dp))
                                        }
                                        Spacer(modifier = Modifier.height(14.dp))
                                        Text("Gider Kaydı Bulunmuyor", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextDark)
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            "Sigorta, kasko, vergi gibi periyodik olmayan genel giderleri buraya ekleyebilirsiniz.",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = TextMedium,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }
                        } else {
                            item(key = "exp_tap_hint") {
                                Text(
                                    text = "SİGORTA & GENEL GİDERLER • Detay için kayda dokunun",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = TextMedium,
                                    letterSpacing = 0.4.sp
                                )
                            }
                            items(expenseRecords, key = { "exp_${it.id}" }) { record ->
                                ExpenseRecordCard(
                                    record = record,
                                    onClick = { selectedExpenseForDetail = record },
                                    onDelete = { expenseToDelete = record }
                                )
                            }
                        }
                    }

                    2 -> {
                        item(key = "fuel_monthly_chart") {
                            MonthlyFuelChartSection(
                                fuelRecords = fuelRecords,
                                liveUnitPriceTl = liveUnitPrice,
                                sectionTitle = "AYLIK YAKIT ÖZETİ",
                                onAddFuelClick = { showAddFuelModal = true }
                            )
                        }

                        if (fuelRecords.isEmpty()) {
                            item(key = "fuel_empty") {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(18.dp),
                                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                                    border = BorderStroke(1.dp, BorderLight)
                                ) {
                                    Column(
                                        modifier = Modifier.fillMaxWidth().padding(28.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Box(
                                            modifier = Modifier.size(56.dp).background(FuelSoft, CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(Icons.Default.LocalGasStation, contentDescription = null, tint = FuelColor, modifier = Modifier.size(28.dp))
                                        }
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Text("Yakıt Kaydı Bulunmuyor", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextDark)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            "Akaryakıt alımlarınızı ekleyerek aylık tüketim grafiğini ve ortalama maliyeti takip edin.",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = TextMedium,
                                            textAlign = TextAlign.Center
                                        )
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Button(
                                            onClick = { showAddFuelModal = true },
                                            shape = RoundedCornerShape(14.dp),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = FuelColor,
                                                contentColor = Color.White
                                            )
                                        ) {
                                            Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Yakıt Ekle", fontWeight = FontWeight.Bold, color = Color.White)
                                        }
                                    }
                                }
                            }
                        } else {
                            item(key = "fuel_history_header") {
                                Text(
                                    text = "DOLUM GEÇMİŞİ (${fuelRecords.size}) • Detay için kayda dokunun",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = TextMedium,
                                    letterSpacing = 0.4.sp,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                            items(fuelRecords, key = { "fuel_${it.id}" }) { record ->
                                FuelRecordCard(
                                    record = record,
                                    drivenKmDelta = drivenKmMap[record.id],
                                    onClick = { selectedFuelForDetail = record },
                                    onDelete = { fuelToDelete = record }
                                )
                            }
                        }
                    }

                    3 -> {
                        if (reminders.isEmpty()) {
                            item(key = "rem_empty") {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(18.dp),
                                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                                    border = BorderStroke(1.dp, BorderLight)
                                ) {
                                    Column(
                                        modifier = Modifier.fillMaxWidth().padding(28.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Box(
                                            modifier = Modifier.size(60.dp).background(ReminderSoft, CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(Icons.Default.AlarmOn, contentDescription = null, tint = ReminderColor, modifier = Modifier.size(30.dp))
                                        }
                                        Spacer(modifier = Modifier.height(14.dp))
                                        Text("Aktif Hatırlatıcı Yok", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextDark)
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            "TÜVTÜRK muayenesi, sigorta, kasko veya yaklaşan periyodik bakım için yerel alarm hatırlatıcısı kurun.",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = TextMedium,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }
                        } else {
                            item(key = "rem_tap_hint") {
                                Text(
                                    text = "HATIRLATICILAR • Detay için kayda dokunun",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = TextMedium,
                                    letterSpacing = 0.4.sp
                                )
                            }
                            items(reminders, key = { "rem_${it.id}" }) { reminder ->
                                ReminderRecordCard(
                                    reminder = reminder,
                                    onClick = { selectedReminderForDetail = reminder },
                                    onDelete = { reminderToDelete = reminder },
                                    onTestAlarm = {
                                        NotificationHelper.showInstantNotification(
                                            context,
                                            reminder.id,
                                            "Hatırlatıcı: ${reminder.title}",
                                            "${vehicle?.plate ?: "Aracınız"} için planlanan işlem zamanı geldi!"
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Delete Expense Dialog
    if (expenseToDelete != null) {
        AlertDialog(
            onDismissRequest = { expenseToDelete = null },
            icon = { Icon(Icons.Default.DeleteOutline, contentDescription = null, tint = AccentRose) },
            title = { Text("Gider Kaydını Sil?", fontWeight = FontWeight.Bold) },
            text = { Text("${expenseToDelete?.category} kaydı kalıcı olarak silinecek.") },
            confirmButton = {
                Button(
                    onClick = {
                        expenseToDelete?.let { viewModel.deleteExpense(it.id) }
                        expenseToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AccentRose,
                        contentColor = Color.White
                    )
                ) {
                    Text("Sil", fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { expenseToDelete = null }) { Text("İptal", color = TextDark) }
            },
            shape = RoundedCornerShape(20.dp),
            containerColor = SurfaceWhite
        )
    }

    // Delete Fuel Dialog
    if (fuelToDelete != null) {
        AlertDialog(
            onDismissRequest = { fuelToDelete = null },
            icon = { Icon(Icons.Default.DeleteOutline, contentDescription = null, tint = AccentRose) },
            title = { Text("Yakıt Kaydını Sil?", fontWeight = FontWeight.Bold) },
            text = { Text("Seçili akaryakıt kaydı kalıcı olarak silinecek.") },
            confirmButton = {
                Button(
                    onClick = {
                        fuelToDelete?.let { viewModel.deleteFuelRecord(it.id) }
                        fuelToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AccentRose,
                        contentColor = Color.White
                    )
                ) {
                    Text("Sil", fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { fuelToDelete = null }) { Text("İptal", color = TextDark) }
            },
            shape = RoundedCornerShape(20.dp),
            containerColor = SurfaceWhite
        )
    }

    // Delete Record Dialog
    if (recordToDelete != null) {
        AlertDialog(
            onDismissRequest = { recordToDelete = null },
            icon = { Icon(Icons.Default.DeleteOutline, contentDescription = null, tint = AccentRose) },
            title = { Text("Bakım Kaydını Sil?", fontWeight = FontWeight.Bold) },
            text = { Text("${recordToDelete?.type} kaydı kalıcı olarak silinecek.") },
            confirmButton = {
                Button(
                    onClick = {
                        recordToDelete?.let { viewModel.deleteMaintenance(it.id) }
                        recordToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AccentRose,
                        contentColor = Color.White
                    )
                ) {
                    Text("Sil", fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { recordToDelete = null }) { Text("İptal", color = TextDark) }
            },
            shape = RoundedCornerShape(20.dp),
            containerColor = SurfaceWhite
        )
    }

    // Delete Reminder Dialog
    if (reminderToDelete != null) {
        AlertDialog(
            onDismissRequest = { reminderToDelete = null },
            icon = { Icon(Icons.Default.DeleteOutline, contentDescription = null, tint = AccentRose) },
            title = { Text("Hatırlatıcıyı Sil?", fontWeight = FontWeight.Bold) },
            text = { Text("${reminderToDelete?.title} hatırlatıcısı ve kurulu alarm silinecektir.") },
            confirmButton = {
                Button(
                    onClick = {
                        reminderToDelete?.let {
                            NotificationHelper.cancelNotification(context, it.id)
                            viewModel.deleteReminder(it.id)
                        }
                        reminderToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AccentRose,
                        contentColor = Color.White
                    )
                ) {
                    Text("Sil", fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { reminderToDelete = null }) { Text("İptal", color = TextDark) }
            },
            shape = RoundedCornerShape(20.dp),
            containerColor = SurfaceWhite
        )
    }

    // Add Fuel Modal Dialog (On-Screen Modal)
    if (showAddFuelModal && vehicle != null) {
        val currentVehicle = vehicle!!
        AddFuelModalDialog(
            vehicles = listOf(currentVehicle),
            initialVehicleId = currentVehicle.id,
            lastKmByVehicleId = mapOf(currentVehicle.id to latestKm),
            fuelPriceState = fuelPriceState,
            isSaving = isSavingFuel,
            onSaveFuel = { _, liters, price, newTotalKm, drivenKm ->
                viewModel.addFuelRecord(
                    liters = liters,
                    price = price,
                    km = newTotalKm,
                    date = System.currentTimeMillis(),
                    onSuccess = {
                        showAddFuelModal = false
                        Toast.makeText(
                            context,
                            String.format(
                                Locale.forLanguageTag("tr-TR"),
                                "Yakıt kaydedildi! Yeni güncel KM: %,d km (+%,d km)",
                                newTotalKm,
                                drivenKm
                            ),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                )
            },
            onDismiss = { showAddFuelModal = false }
        )
    }

    // 1. Export / Share Carnet Dialog (PDF & Text)
    if (showExportDialog && vehicle != null) {
        val v = vehicle!!
        val dateFormatted = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale("tr", "TR")).format(Date())

        val carnetText = buildString {
            appendLine("📋 ARAÇ BAKIM KARNESİ - DİJİTAL SERVİS GEÇMİŞİ")
            appendLine("----------------------------------------")
            appendLine("🚗 Araç: ${v.brand} ${v.model} (${v.year})")
            appendLine("🔢 Plaka: ${v.plate}")
            appendLine("⛽ Yakıt: ${v.fuelType}")
            appendLine("📊 Toplam Bakım: ${maintenanceRecords.size} Adet")
            appendLine("💰 Toplam Masraf: ${String.format(Locale("tr", "TR"), "%,.0f ₺", totalCombinedCost)}")
            appendLine("⏱️ Son Kayıtlı KM: ${if (latestKm > 0) String.format(Locale("tr", "TR"), "%,d km", latestKm) else "-"}")
            appendLine("📅 Rapor Tarihi: $dateFormatted")
            appendLine("----------------------------------------")
            appendLine("📌 YAPILAN İŞLEMLER & BAKIM LİSTESİ:")
            if (maintenanceRecords.isEmpty()) {
                appendLine("Henüz bakım kaydı girilmedi.")
            } else {
                maintenanceRecords.sortedByDescending { it.km }.forEachIndexed { index, rec ->
                    val recDate = SimpleDateFormat("dd.MM.yyyy", Locale("tr", "TR")).format(Date(rec.date))
                    appendLine("${index + 1}. [${recDate}] - ${String.format(Locale("tr", "TR"), "%,d km", rec.km)}")
                    appendLine("   • İşlem: ${rec.type}")
                    if (rec.cost > 0) appendLine("   • Tutar: ${String.format(Locale("tr", "TR"), "%,.0f ₺", rec.cost)}")
                    if (rec.description.isNotBlank()) appendLine("   • Not: ${rec.description}")
                    appendLine()
                }
            }
            if (expenseRecords.isNotEmpty()) {
                appendLine("----------------------------------------")
                appendLine("🛡️ SİGORTA VE GENEL GİDERLER:")
                expenseRecords.sortedByDescending { it.date }.forEachIndexed { index, rec ->
                    val recDate = SimpleDateFormat("dd.MM.yyyy", Locale("tr", "TR")).format(Date(rec.date))
                    appendLine("${index + 1}. [${recDate}] - ${rec.category}")
                    if (rec.amount > 0) appendLine("   • Tutar: ${String.format(Locale("tr", "TR"), "%,.0f ₺", rec.amount)}")
                    if (rec.description.isNotBlank()) appendLine("   • Not: ${rec.description}")
                    appendLine()
                }
            }
            if (fuelRecords.isNotEmpty()) {
                appendLine("----------------------------------------")
                appendLine("⛽ AKARYAKIT HARCAMA ÖZETİ:")
                appendLine("Toplam Akaryakıt: ${String.format(Locale("tr", "TR"), "%,.0f ₺", fuelRecords.sumOf { it.total })} (${String.format(Locale("tr", "TR"), "%,.1f Lt", fuelRecords.sumOf { it.liters })})")
                fuelRecords.take(5).forEachIndexed { i, f ->
                    val fDate = SimpleDateFormat("dd.MM.yyyy", Locale("tr", "TR")).format(Date(f.date))
                    appendLine("${i + 1}. [$fDate] ${String.format(Locale("tr", "TR"), "%.2f Lt", f.liters)} - ${String.format(Locale("tr", "TR"), "%,.2f ₺", f.total)}")
                }
            }
            if (reminders.isNotEmpty()) {
                appendLine("----------------------------------------")
                appendLine("⏰ PLANLANAN SERVİS & HATIRLATICILAR:")
                reminders.forEachIndexed { i, rem ->
                    val remDate = SimpleDateFormat("dd.MM.yyyy", Locale("tr", "TR")).format(Date(rem.dueDate))
                    appendLine("${i + 1}. ${rem.title} (Tarih: $remDate)")
                }
            }
            appendLine("----------------------------------------")
            appendLine("⚡ Araç Karnem Uygulaması ile oluşturuldu.")
        }

        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .background(PrimarySoft, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = PrimaryVibrant, modifier = Modifier.size(22.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text("Araç Karnesi Raporu", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                        Text("${v.brand} ${v.model} (${v.plate})", style = MaterialTheme.typography.bodySmall, color = TextMedium)
                    }
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // PDF Feature Highlight Banner
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = PrimarySoft,
                        border = BorderStroke(1.dp, PrimaryVibrant.copy(alpha = 0.28f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Description,
                                contentDescription = null,
                                tint = PrimaryVibrant,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Resmi Formatlı PDF Raporu",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = PrimaryVibrant
                                )
                                Text(
                                    text = "Bakım kayıtları, akaryakıt masrafları ve plaka bilgisi içeren şık A4 PDF oluşturulur.",
                                    fontSize = 11.sp,
                                    color = TextDark,
                                    lineHeight = 15.sp
                                )
                            }
                        }
                    }

                    Text(
                        text = "RAPOR ÖNİZLEMESİ",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextMedium
                    )

                    // Preview Box
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = BgLightAlt,
                        border = BorderStroke(1.dp, BorderLight),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 140.dp)
                    ) {
                        LazyColumn(modifier = Modifier.padding(12.dp)) {
                            item {
                                Text(
                                    text = carnetText,
                                    fontSize = 10.5.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = TextDark
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Primary: Generate & Share Clean PDF
                    Button(
                        onClick = {
                            PdfReportGenerator.generateAndShareVehicleReport(
                                context = context,
                                vehicle = v,
                                maintenanceRecords = maintenanceRecords,
                                expenseRecords = expenseRecords,
                                fuelRecords = fuelRecords,
                                reminders = reminders
                            )
                            showExportDialog = false
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryVibrant,
                            contentColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("PDF Raporu Oluştur & Paylaş", fontWeight = FontWeight.Bold, color = Color.White)
                    }

                    // Secondary: Text / WhatsApp Share
                    OutlinedButton(
                        onClick = {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, "${v.brand} ${v.model} (${v.plate}) - Araç Karnesi")
                                putExtra(Intent.EXTRA_TEXT, carnetText)
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Araç Karnesini Paylaş"))
                            showExportDialog = false
                        },
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, BorderLight),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp), tint = TextDark)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Metin Olarak Paylaş (WhatsApp)", color = TextDark, fontWeight = FontWeight.Medium, fontSize = 13.sp)
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showExportDialog = false },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Kapat", color = TextMedium)
                }
            },
            shape = RoundedCornerShape(20.dp),
            containerColor = SurfaceWhite
        )
    }

    // Click-to-open Detail Modals
    selectedMaintenanceForDetail?.let { selectedRecord ->
        MaintenanceDetailModalDialog(
            record = selectedRecord,
            onDeleteRequest = { recordToDelete = selectedRecord },
            onDismiss = { selectedMaintenanceForDetail = null }
        )
    }

    selectedExpenseForDetail?.let { selectedExpense ->
        ExpenseDetailModalDialog(
            record = selectedExpense,
            onDeleteRequest = { expenseToDelete = selectedExpense },
            onDismiss = { selectedExpenseForDetail = null }
        )
    }

    selectedFuelForDetail?.let { selectedFuel ->
        FuelDetailModalDialog(
            record = selectedFuel,
            drivenKmDelta = drivenKmMap[selectedFuel.id],
            onDeleteRequest = { fuelToDelete = selectedFuel },
            onDismiss = { selectedFuelForDetail = null }
        )
    }

    selectedReminderForDetail?.let { selectedReminder ->
        ReminderDetailModalDialog(
            reminder = selectedReminder,
            onTestAlarm = {
                NotificationHelper.showInstantNotification(
                    context,
                    selectedReminder.id,
                    "Hatırlatıcı: ${selectedReminder.title}",
                    "${vehicle?.plate ?: "Aracınız"} için planlanan işlem zamanı geldi!"
                )
            },
            onDeleteRequest = { reminderToDelete = selectedReminder },
            onDismiss = { selectedReminderForDetail = null }
        )
    }

    if (showVehicleOverviewModal && vehicle != null) {
        VehicleOverviewModalDialog(
            vehicle = vehicle!!,
            latestKm = latestKm,
            maintenanceRecords = maintenanceRecords,
            expenseRecords = expenseRecords,
            fuelRecords = fuelRecords,
            onOpenExportReport = { showExportDialog = true },
            onDismiss = { showVehicleOverviewModal = false }
        )
    }
}

// -----------------------------------------------------------------------------
// SIMPLIFIED MAINTENANCE RECORD CARD (Tap to view full details in modal)
// -----------------------------------------------------------------------------

@Composable
fun MaintenanceRecordCard(
    record: MaintenanceRecord,
    onClick: () -> Unit = {},
    onDelete: () -> Unit = {}
) {
    val trLocale = Locale.forLanguageTag("tr-TR")
    val (icon, tintColor, softBg) = when {
        record.type.contains("Triger", ignoreCase = true) -> Triple(Icons.Default.Settings, MaintenanceRed, MaintenanceRedSoft)
        record.type.contains("V Kayışı", ignoreCase = true) -> Triple(Icons.Default.Bolt, SecondaryVibrant, SecondarySoft)
        record.type.contains("Devirdaim", ignoreCase = true) -> Triple(Icons.Default.WaterDrop, SecondaryVibrant, SecondarySoft)
        record.type.contains("Mazot", ignoreCase = true) || record.type.contains("Yakıt", ignoreCase = true) -> Triple(Icons.Default.LocalGasStation, FuelColorDark, FuelSoft)
        record.type.contains("Periyodik", ignoreCase = true) -> Triple(Icons.Default.BuildCircle, PrimaryVibrantDark, PrimarySoft)
        record.type.contains("Muayene", ignoreCase = true) -> Triple(Icons.Default.FactCheck, TotalCostGreenDark, AccentEmeraldSoft)
        else -> Triple(Icons.Default.Handyman, PrimaryVibrantDark, PrimarySoft)
    }

    val partsList = remember(record.type) {
        record.type.split(",").map { it.trim() }.filter { it.isNotEmpty() }
    }
    val extraCount = (partsList.size - 1).coerceAtLeast(0)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(18.dp), spotColor = TextLight.copy(alpha = 0.1f))
            .clickable(onClick = onClick)
            .testTag("maintenance_card_${record.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        border = BorderStroke(1.dp, BorderLight)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(softBg, RoundedCornerShape(13.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = tintColor, modifier = Modifier.size(22.dp))
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = partsList.firstOrNull() ?: record.type,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextDark,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (extraCount > 0) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = PrimarySoft
                        ) {
                            Text(
                                text = "+$extraCount işlem",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryVibrant,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = "${SimpleDateFormat("dd MMM yyyy", trLocale).format(Date(record.date))} • ${String.format(trLocale, "%,d km", record.km)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            if (record.cost > 0) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = TotalCostGreenSoft,
                    border = BorderStroke(1.dp, TotalCostGreenBorder)
                ) {
                    Text(
                        text = String.format(trLocale, "%,.0f ₺", record.cost),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Black,
                        color = TotalCostGreenDark,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Detayları Gör",
                tint = TextLight,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

// -----------------------------------------------------------------------------
// SIMPLIFIED EXPENSE RECORD CARD (Tap to view full details in modal)
// -----------------------------------------------------------------------------

@Composable
fun ExpenseRecordCard(
    record: ExpenseRecord,
    onClick: () -> Unit = {},
    onDelete: () -> Unit = {}
) {
    val trLocale = Locale.forLanguageTag("tr-TR")
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(18.dp), spotColor = TextLight.copy(alpha = 0.1f))
            .clickable(onClick = onClick)
            .testTag("expense_card_${record.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        border = BorderStroke(1.dp, BorderLight)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(InsuranceBlueSoft, RoundedCornerShape(13.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Shield, contentDescription = null, tint = InsuranceBlue, modifier = Modifier.size(22.dp))
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = record.category,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextDark,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = SimpleDateFormat("dd MMM yyyy", trLocale).format(Date(record.date)),
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMedium,
                    maxLines = 1
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = InsuranceBlueSoft
            ) {
                Text(
                    text = String.format(trLocale, "%,.0f ₺", record.amount),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Black,
                    color = InsuranceBlue,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Detayları Gör",
                tint = TextLight,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

// -----------------------------------------------------------------------------
// SIMPLIFIED REMINDER RECORD CARD (Tap to view full details in modal)
// -----------------------------------------------------------------------------

@Composable
fun ReminderRecordCard(
    reminder: Reminder,
    onClick: () -> Unit = {},
    onDelete: () -> Unit = {},
    onTestAlarm: () -> Unit = {}
) {
    val trLocale = Locale.forLanguageTag("tr-TR")
    val now = System.currentTimeMillis()
    val isPastDue = now > reminder.dueDate
    val daysUntil = ((reminder.dueDate - now) / (1000 * 60 * 60 * 24)).toInt()

    val (statusText, statusColor, statusBg) = when {
        isPastDue -> Triple("Gecikti", AccentRose, AccentRoseSoft)
        daysUntil == 0 -> Triple("Bugün", AccentRose, AccentRoseSoft)
        daysUntil <= 7 -> Triple("$daysUntil gün", ReminderColor, ReminderSoft)
        else -> Triple("$daysUntil gün", ReminderColor, ReminderSoft)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(18.dp), spotColor = TextLight.copy(alpha = 0.1f))
            .clickable(onClick = onClick)
            .testTag("reminder_card_${reminder.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        border = BorderStroke(
            1.dp,
            when {
                isPastDue || daysUntil == 0 -> AccentRose.copy(alpha = 0.35f)
                daysUntil <= 7 -> ReminderBorder
                else -> BorderLight
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(statusBg, RoundedCornerShape(13.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isPastDue) Icons.Default.NotificationImportant else Icons.Default.NotificationsActive,
                    contentDescription = null,
                    tint = statusColor,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = reminder.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextDark,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = SimpleDateFormat("dd MMMM yyyy", trLocale).format(Date(reminder.dueDate)),
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMedium,
                    maxLines = 1
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = statusBg
            ) {
                Text(
                    text = statusText,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = statusColor,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Detayları Gör",
                tint = TextLight,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

// -----------------------------------------------------------------------------
// 3. ADD VEHICLE SCREEN
// -----------------------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddVehicleScreen(viewModel: DashboardViewModel, onBack: () -> Unit) {
    val context = LocalContext.current
    var brand by remember { mutableStateOf("") }
    var model by remember { mutableStateOf("") }
    var selectedEngine by remember { mutableStateOf("") }
    var year by remember { mutableStateOf("2022") }
    var plate by remember { mutableStateOf("") }
    var fuelType by remember { mutableStateOf("Benzin") }

    var attemptedSubmit by remember { mutableStateOf(false) }
    var plateTouched by remember { mutableStateOf(false) }
    var yearTouched by remember { mutableStateOf(false) }
    var showFuelTypeModal by remember { mutableStateOf(false) }
    var showYearMenu by remember { mutableStateOf(false) }

    val fuelTypes = remember { listOf("Benzin", "Dizel", "Hibrit", "Elektrik", "LPG") }
    val selectableYears = remember { (2026 downTo 1990).map { it.toString() } }

    val vehicleCatalog by viewModel.vehicleCatalog.collectAsStateWithLifecycle()
    val engineOptions by viewModel.vehicleEngineOptions.collectAsStateWithLifecycle()
    val isLoadingEngines by viewModel.isLoadingEngines.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()

    val plateError = plate.trim().isEmpty() && (attemptedSubmit || plateTouched)
    val parsedYear = year.trim().toIntOrNull()
    val yearError = (year.trim().length != 4 || parsedYear == null || parsedYear !in 1950..2035) &&
        (attemptedSubmit || yearTouched)

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = BgLight,
            contentWindowInsets = WindowInsets.safeDrawing,
            topBar = {
                TopAppBar(
                    windowInsets = topSafeBarWindowInsets(),
                    title = { Text("Yeni Araç Ekle", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = SurfaceWhite,
                        titleContentColor = TextDark
                    )
                )
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .imePadding(),
                contentAlignment = Alignment.TopCenter
            ) {
                Column(
                    modifier = Modifier
                        .widthIn(max = 620.dp)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Kompakt Plaka & Seçili Araç Önizleme Şeridi
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                        border = BorderStroke(1.dp, BorderLight)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TurkishPlateBadge(
                                plate = if (plate.isNotBlank()) plate else "34 TR 001",
                                compact = true
                            )

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (brand.isNotBlank()) {
                                        buildString {
                                            append(brand)
                                            if (model.isNotBlank()) append(" $model")
                                            if (selectedEngine.isNotBlank()) append(" ${selectedEngine.trim()}")
                                        }
                                    } else {
                                        "Yeni Araç Bilgileri"
                                    },
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextDark,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${year.ifBlank { "—" }} Model • $fuelType",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = TextMedium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    // Araç Plakası Kutusu
                    ModernCompactTextField(
                        value = plate,
                        onValueChange = {
                            plate = it.uppercase(Locale.forLanguageTag("tr-TR"))
                            plateTouched = true
                        },
                        label = "Araç Plakası *",
                        placeholder = "Örn: 34 ABC 123",
                        leadingIcon = Icons.Default.Badge,
                        inputTestTag = "add_vehicle_plate_input",
                        isError = plateError,
                        errorMessage = if (plateError) "Araç plakası boş bırakılamaz." else null
                    )

                    // Seçilebilir Marka, Model & Motor Seçenekleri Kutuları
                    BrandModelSelectorSection(
                        catalog = vehicleCatalog,
                        engineOptions = engineOptions,
                        isLoadingEngines = isLoadingEngines,
                        selectedBrand = brand,
                        selectedModel = model,
                        selectedEngine = selectedEngine,
                        showValidationErrors = attemptedSubmit,
                        onBrandSelected = { pickedBrand ->
                            if (!pickedBrand.equals(brand, ignoreCase = true)) {
                                brand = pickedBrand
                                val validModels = vehicleCatalog.modelsForBrand(pickedBrand)
                                if (validModels.none { it.equals(model, ignoreCase = true) }) {
                                    model = ""
                                    selectedEngine = ""
                                    viewModel.loadEnginesForVehicle("", "", year)
                                } else {
                                    selectedEngine = ""
                                    viewModel.loadEnginesForVehicle(pickedBrand, model, year)
                                }
                            }
                            viewModel.onBrandSelected(pickedBrand)
                        },
                        onModelSelected = { pickedModel ->
                            if (!pickedModel.equals(model, ignoreCase = true)) {
                                model = pickedModel
                                selectedEngine = ""
                            }
                            viewModel.loadEnginesForVehicle(brand, pickedModel, year)
                        },
                        onEngineSelected = { pickedEngine ->
                            selectedEngine = pickedEngine.engineName
                            if (pickedEngine.fuelType in fuelTypes) {
                                fuelType = pickedEngine.fuelType
                            }
                        }
                    )

                    // Model Yılı & Yakıt Türü (Yan Yana Kompakt Kutular)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            ModernCompactTextField(
                                value = year,
                                onValueChange = {
                                    val sanitized = sanitizeIntegerDigits(it, maxDigits = 4)
                                    year = sanitized
                                    yearTouched = true
                                    if (sanitized.length == 4 && brand.isNotBlank() && model.isNotBlank()) {
                                        viewModel.loadEnginesForVehicle(brand, model, sanitized)
                                    }
                                },
                                label = "Model Yılı *",
                                placeholder = "2022",
                                leadingIcon = Icons.Default.CalendarToday,
                                inputTestTag = "add_vehicle_year_input",
                                isError = yearError,
                                errorMessage = if (yearError) "4 haneli geçerli yıl giriniz." else null,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                trailingContent = {
                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .clickable { showYearMenu = true },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.KeyboardArrowDown,
                                            contentDescription = "Model Yılı Seç",
                                            tint = PrimaryVibrant,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            )

                            DropdownMenu(
                                expanded = showYearMenu,
                                onDismissRequest = { showYearMenu = false },
                                modifier = Modifier
                                    .heightIn(max = 260.dp)
                                    .background(SurfaceWhite)
                            ) {
                                selectableYears.forEach { y ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = y,
                                                fontWeight = if (y == year) FontWeight.Bold else FontWeight.Medium,
                                                color = if (y == year) PrimaryVibrant else TextDark
                                            )
                                        },
                                        onClick = {
                                            year = y
                                            yearTouched = true
                                            showYearMenu = false
                                            if (brand.isNotBlank() && model.isNotBlank()) {
                                                viewModel.loadEnginesForVehicle(brand, model, y)
                                            }
                                        }
                                    )
                                }
                            }
                        }

                        ModernCompactSelectorField(
                            label = "Yakıt Türü *",
                            value = fuelType,
                            placeholder = "Yakıt seçin",
                            leadingIcon = Icons.Default.LocalGasStation,
                            buttonTestTag = "select_fuel_type_button",
                            accentColor = FuelColorDark,
                            accentSoftColor = FuelSoft,
                            onClick = { showFuelTypeModal = true },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Button(
                        onClick = {
                            attemptedSubmit = true
                            val validYear = year.trim().toIntOrNull()
                            if (plate.trim().isEmpty() ||
                                brand.trim().isEmpty() ||
                                model.trim().isEmpty() ||
                                selectedEngine.trim().isEmpty() ||
                                year.trim().length != 4 ||
                                validYear == null || validYear !in 1950..2035
                            ) {
                                Toast.makeText(
                                    context,
                                    "Lütfen tüm araç bilgilerini ve motor seçeneğini seçiniz, boş alan bırakılamaz.",
                                    Toast.LENGTH_SHORT
                                ).show()
                                return@Button
                            }
                            val finalModel = "${model.trim()} ${selectedEngine.trim()}"
                            viewModel.addVehicle(
                                brand = brand.trim(),
                                model = finalModel,
                                year = year.trim(),
                                plate = plate.trim().uppercase(Locale.forLanguageTag("tr-TR")),
                                fuelType = fuelType,
                                onSuccess = onBack
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("save_vehicle_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryVibrant,
                            contentColor = Color.White
                        ),
                        enabled = !isLoading
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, tint = Color.White, modifier = Modifier.size(19.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("ARACI SİSTEME KAYDET", fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp, color = Color.White)
                    }
                }
            }
        }
        LoadingOverlay(isLoading)
    }

    if (showFuelTypeModal) {
        FuelTypePickerModalDialog(
            fuelTypes = fuelTypes,
            selectedFuelType = fuelType,
            onFuelTypePicked = { picked ->
                fuelType = picked
                showFuelTypeModal = false
            },
            onDismiss = { showFuelTypeModal = false }
        )
    }
}

// -----------------------------------------------------------------------------
// 4. ADD MAINTENANCE SCREEN
// -----------------------------------------------------------------------------

data class MaintenanceRoutineOption(
    val key: String,
    val title: String,
    val subtitle: String,
    val icon: ImageVector
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMaintenanceScreen(viewModel: VehicleDetailViewModel, onBack: () -> Unit) {
    val routineOptions = remember {
        listOf(
            MaintenanceRoutineOption("periyodik", "Periyodik Bakım", "Motor Yağı, Yağ Filtresi & Genel Kontrol", Icons.Default.Build),
            MaintenanceRoutineOption("triger_kayis", "Triger Kayışı", "Eksantrik zamanlama kayışı değişimi", Icons.Default.Settings),
            MaintenanceRoutineOption("triger_set", "Triger Seti", "Triger kayışı, gergi bilyası & rulman takımı", Icons.Default.SettingsSuggest),
            MaintenanceRoutineOption("v_kayis", "V Kayışı", "Alternatör, şarj & klima kayışı", Icons.Default.Bolt),
            MaintenanceRoutineOption("su_devirdaim", "Su Devirdaim Pompası", "Soğutma suyu devirdaim pompası değişimi", Icons.Default.WaterDrop),
            MaintenanceRoutineOption("mazot_filtre", "Mazot Pompası / Filtresi", "Yakıt filtresi & mazot pompası bakımı", Icons.Default.LocalGasStation),
            MaintenanceRoutineOption("hava_filtre", "Hava Filtresi", "Motor hava emiş filtresi değişimi", Icons.Default.Air),
            MaintenanceRoutineOption("polen_filtre", "Polen Filtresi", "Klima ve kabin hava filtresi", Icons.Default.AcUnit),
            MaintenanceRoutineOption("fren_balata", "Fren Balata & Disk", "Ön / Arka balatalar ve fren diskleri", Icons.Default.Dangerous),
            MaintenanceRoutineOption("buji", "Buji & Ateşleme Sistemi", "Bujiler, buji kablosu & ateşleme bobini", Icons.Default.ElectricBolt),
            MaintenanceRoutineOption("sanziman", "Şanzıman Yağı & Debriyaj", "Baskı balata ve şanzıman yağı değişimi", Icons.Default.Tune),
            MaintenanceRoutineOption("antifriz", "Antifriz & Sıvı Bakımı", "Fren hidroliği & antifriz yenileme", Icons.Default.Opacity)
        )
    }

    val context = LocalContext.current
    val selectedParts = remember { mutableStateListOf<String>("Periyodik Bakım") }
    var includeCustomPart by remember { mutableStateOf(false) }
    var customPart by remember { mutableStateOf("") }
    var km by remember { mutableStateOf("") }
    var costText by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    var attemptedSubmit by remember { mutableStateOf(false) }
    var customPartTouched by remember { mutableStateOf(false) }
    var kmTouched by remember { mutableStateOf(false) }
    var costTouched by remember { mutableStateOf(false) }
    var descriptionTouched by remember { mutableStateOf(false) }

    val parsedKm = km.replace(".", "").toIntOrNull() ?: 0
    val parsedCost = parseTurkishCurrency(costText)

    val customPartError = includeCustomPart && customPart.trim().isEmpty() && (attemptedSubmit || customPartTouched)
    val kmError = (km.trim().isEmpty() || parsedKm <= 0) && (attemptedSubmit || kmTouched)
    val costError = (costText.trim().isEmpty() || parsedCost <= 0.0) && (attemptedSubmit || costTouched)
    val descriptionError = description.trim().isEmpty() && (attemptedSubmit || descriptionTouched)

    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = System.currentTimeMillis())
    var showDatePicker by remember { mutableStateOf(false) }
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()

    val selectedDateText = if (datePickerState.selectedDateMillis != null) {
        SimpleDateFormat("dd MMMM yyyy", Locale("tr", "TR")).format(Date(datePickerState.selectedDateMillis!!))
    } else {
        "Tarih Seç"
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = BgLight,
            contentWindowInsets = WindowInsets.safeDrawing,
            topBar = {
                TopAppBar(
                    windowInsets = topSafeBarWindowInsets(),
                    title = {
                        Column {
                            Text("Araç Bakım Kaydı Ekle", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text("Rutin parçaları seçin veya el ile ekleyin", style = MaterialTheme.typography.bodySmall, color = TextMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = SurfaceWhite,
                        titleContentColor = TextDark
                    )
                )
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .imePadding(),
                contentAlignment = Alignment.TopCenter
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxHeight()
                        .widthIn(max = 620.dp)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 36.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                // Section Header: Rutin Parçalar Checklist
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                        border = BorderStroke(1.dp, BorderLight)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "RUTİN BAKIM & PARÇA LİSTESİ",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = PrimaryVibrant,
                                        letterSpacing = 0.5.sp
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Yapılan bakım ve değişen parçaları seçin",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextMedium
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (selectedParts.isNotEmpty()) PrimarySoft else BgLightAlt
                                ) {
                                    Text(
                                        text = "${selectedParts.size} Seçili",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (selectedParts.isNotEmpty()) PrimaryVibrant else TextMedium,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Checklist Items
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                routineOptions.forEach { option ->
                                    val isSelected = selectedParts.contains(option.title)
                                    Surface(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                if (isSelected) {
                                                    selectedParts.remove(option.title)
                                                } else {
                                                    selectedParts.add(option.title)
                                                }
                                            },
                                        shape = RoundedCornerShape(14.dp),
                                        color = if (isSelected) PrimarySoft.copy(alpha = 0.6f) else BgLightAlt,
                                        border = BorderStroke(
                                            1.dp,
                                            if (isSelected) PrimaryVibrant.copy(alpha = 0.5f) else BorderLight
                                        )
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 12.dp, vertical = 10.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Checkbox(
                                                checked = isSelected,
                                                onCheckedChange = { checked ->
                                                    if (checked) {
                                                        if (!selectedParts.contains(option.title)) selectedParts.add(option.title)
                                                    } else {
                                                        selectedParts.remove(option.title)
                                                    }
                                                },
                                                colors = CheckboxDefaults.colors(
                                                    checkedColor = PrimaryVibrant,
                                                    uncheckedColor = TextLight
                                                )
                                            )

                                            Spacer(modifier = Modifier.width(6.dp))

                                            Box(
                                                modifier = Modifier
                                                    .size(34.dp)
                                                    .background(
                                                        if (isSelected) SurfaceWhite else SurfaceWhite.copy(alpha = 0.7f),
                                                        RoundedCornerShape(8.dp)
                                                    ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    option.icon,
                                                    contentDescription = null,
                                                    tint = if (isSelected) PrimaryVibrant else TextMedium,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }

                                            Spacer(modifier = Modifier.width(10.dp))

                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = option.title,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                    color = if (isSelected) TextDark else TextDark.copy(alpha = 0.85f)
                                                )
                                                Text(
                                                    text = option.subtitle,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = TextMedium,
                                                    fontSize = 11.sp,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Section: Farklı / Özel Durum Girişi (Toggleable so input is never left empty when opened)
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                        border = BorderStroke(1.dp, if (customPartError) AccentRose else BorderLight)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "ÖZEL PARÇA VEYA İŞLEM EKLE",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = TextDark,
                                        letterSpacing = 0.5.sp
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Listede olmayan ek bir onarım varsa aktif edin",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextMedium
                                    )
                                }

                                Switch(
                                    checked = includeCustomPart,
                                    onCheckedChange = { checked ->
                                        includeCustomPart = checked
                                        if (!checked) {
                                            customPart = ""
                                            customPartTouched = false
                                        }
                                    },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = PrimaryVibrant
                                    )
                                )
                            }

                            if (includeCustomPart) {
                                Spacer(modifier = Modifier.height(12.dp))
                                OutlinedTextField(
                                    value = customPart,
                                    onValueChange = {
                                        customPart = it
                                        customPartTouched = true
                                    },
                                    label = { Text("Özel Parça / İşlem Adı *") },
                                    placeholder = { Text("Örn: Rot başı, amortisör takozu, şarj dinamosu...") },
                                    isError = customPartError,
                                    supportingText = if (customPartError) {
                                        { Text("Özel parça alanı boş bırakılamaz.") }
                                    } else null,
                                    leadingIcon = {
                                        Icon(Icons.Default.Edit, contentDescription = null, tint = TextMedium, modifier = Modifier.size(20.dp))
                                    },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(14.dp)
                                )
                            }
                        }
                    }
                }

                // Section: Bakım Girdileri (KM, Maliyet, Tarih, Açıklama)
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                        border = BorderStroke(1.dp, BorderLight)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = "BAKIM BİLGİLERİ VE MALİYET",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextDark,
                                letterSpacing = 0.5.sp
                            )

                            // Kilometre Input & Quick Increment Chips
                            Column {
                                OutlinedTextField(
                                    value = km,
                                    onValueChange = {
                                        km = sanitizeIntegerDigits(it)
                                        kmTouched = true
                                    },
                                    label = { Text("Bakım Yapıldığı Kilometre *") },
                                    placeholder = { Text("Örn: 95.000") },
                                    visualTransformation = TurkishThousandsVisualTransformation(),
                                    isError = kmError,
                                    supportingText = if (kmError) {
                                        { Text("Bakım kilometresi boş bırakılamaz.") }
                                    } else null,
                                    leadingIcon = {
                                        Icon(Icons.Default.Speed, contentDescription = null, tint = PrimaryVibrant, modifier = Modifier.size(20.dp))
                                    },
                                    trailingIcon = {
                                        Text("KM", fontWeight = FontWeight.Bold, color = TextMedium, modifier = Modifier.padding(end = 12.dp))
                                    },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("maintenance_km_input"),
                                    shape = RoundedCornerShape(14.dp)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.horizontalScroll(rememberScrollState())
                                ) {
                                    listOf("50.000", "80.000", "100.000", "120.000", "150.000", "200.000").forEach { presetKm ->
                                        AssistChip(
                                            onClick = {
                                                km = sanitizeIntegerDigits(presetKm)
                                                kmTouched = true
                                            },
                                            label = { Text("$presetKm km", fontSize = 11.sp) }
                                        )
                                    }
                                }
                            }

                            // Bakımın Maliyeti (₺) — Otomatik "." binlik ayırıcı ile
                            Column {
                                OutlinedTextField(
                                    value = costText,
                                    onValueChange = {
                                        costText = sanitizeCurrencyDigits(it, allowCommaDecimal = true)
                                        costTouched = true
                                    },
                                    label = { Text("Bakımın Maliyeti / Tutar (₺) *") },
                                    placeholder = { Text("Örn: 4.500") },
                                    visualTransformation = TurkishThousandsVisualTransformation(),
                                    isError = costError,
                                    supportingText = if (costError) {
                                        { Text("Maliyet / tutar boş bırakılamaz.") }
                                    } else {
                                        { Text("Para birimi otomatik olarak '.' ayırıcılı biçimlendirilir.", fontSize = 11.sp) }
                                    },
                                    leadingIcon = {
                                        Icon(Icons.Default.Payments, contentDescription = null, tint = TotalCostGreenDark, modifier = Modifier.size(20.dp))
                                    },
                                    trailingIcon = {
                                        Text("₺", fontWeight = FontWeight.Black, color = TotalCostGreenDark, fontSize = 16.sp, modifier = Modifier.padding(end = 12.dp))
                                    },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    singleLine = true,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("maintenance_cost_input"),
                                    shape = RoundedCornerShape(14.dp)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.horizontalScroll(rememberScrollState())
                                ) {
                                    listOf("1.500", "3.000", "5.000", "8.000", "12.000", "18.000").forEach { presetCost ->
                                        AssistChip(
                                            onClick = {
                                                costText = sanitizeCurrencyDigits(presetCost, allowCommaDecimal = true)
                                                costTouched = true
                                            },
                                            label = { Text("$presetCost ₺", fontSize = 11.sp) }
                                        )
                                    }
                                }
                            }

                            // Date Picker Card
                            OutlinedCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showDatePicker = true },
                                shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(1.dp, BorderLight)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .background(PrimarySoft, RoundedCornerShape(10.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.CalendarToday, contentDescription = null, tint = PrimaryVibrant, modifier = Modifier.size(20.dp))
                                    }
                                    Spacer(modifier = Modifier.width(14.dp))
                                    Column {
                                        Text("Bakım Tarihi *", style = MaterialTheme.typography.labelSmall, color = TextMedium)
                                        Text(selectedDateText, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextDark)
                                    }
                                }
                            }

                            // Mandatory Description & Service Notes + Quick Note Chips
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                OutlinedTextField(
                                    value = description,
                                    onValueChange = {
                                        description = it
                                        descriptionTouched = true
                                    },
                                    label = { Text("Servis & Bakım Açıklaması *") },
                                    placeholder = { Text("Örn: Yetkili serviste yapıldı, 5W-30 yağ ve filtre seti yenilendi.") },
                                    isError = descriptionError,
                                    supportingText = if (descriptionError) {
                                        { Text("Servis ve bakım açıklaması boş bırakılamaz.") }
                                    } else null,
                                    minLines = 2,
                                    maxLines = 4,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("maintenance_description_input"),
                                    shape = RoundedCornerShape(14.dp)
                                )

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.horizontalScroll(rememberScrollState())
                                ) {
                                    listOf(
                                        "Periyodik yağ ve filtre bakımı yapıldı.",
                                        "Yetkili servis kontrolü tamamlandı.",
                                        "Özel serviste parça değişimi yapıldı."
                                    ).forEach { quickDesc ->
                                        AssistChip(
                                            onClick = {
                                                description = quickDesc
                                                descriptionTouched = true
                                            },
                                            label = { Text(quickDesc, fontSize = 11.sp) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Submit Button
                item {
                    Button(
                        onClick = {
                            attemptedSubmit = true
                            val allParts = mutableListOf<String>()
                            allParts.addAll(selectedParts)
                            if (includeCustomPart && customPart.trim().isNotEmpty()) {
                                allParts.add(customPart.trim())
                            }

                            if (allParts.isEmpty()) {
                                Toast.makeText(context, "Lütfen en az bir bakım parçası seçiniz.", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            if (includeCustomPart && customPart.trim().isEmpty()) {
                                Toast.makeText(context, "Özel parça alanı boş bırakılamaz.", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            if (km.trim().isEmpty() || parsedKm <= 0 ||
                                costText.trim().isEmpty() || parsedCost <= 0.0 ||
                                description.trim().isEmpty() ||
                                datePickerState.selectedDateMillis == null
                            ) {
                                Toast.makeText(
                                    context,
                                    "Lütfen kilometre, maliyet ve servis açıklaması alanlarını eksiksiz doldurunuz.",
                                    Toast.LENGTH_SHORT
                                ).show()
                                return@Button
                            }

                            val finalType = allParts.joinToString(", ")
                            val date = datePickerState.selectedDateMillis ?: System.currentTimeMillis()
                            viewModel.addMaintenance(finalType, description.trim(), parsedKm, parsedCost, date, onBack)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .testTag("save_maintenance_button"),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryVibrant,
                            contentColor = Color.White
                        ),
                        enabled = !isLoading
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("BAKIM KAYDINI KARNEYE EKLE", fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp, color = Color.White)
                    }
                }
            }
            }
        }

        if (showDatePicker) {
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = { TextButton(onClick = { showDatePicker = false }) { Text("Tamam") } },
                dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("İptal") } }
            ) {
                DatePicker(state = datePickerState)
            }
        }

        LoadingOverlay(isLoading)
    }
}

// -----------------------------------------------------------------------------
// 5. ADD REMINDER SCREEN
// -----------------------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddReminderScreen(viewModel: VehicleDetailViewModel, onBack: () -> Unit) {
    var title by remember { mutableStateOf("Periyodik Bakım") }
    var attemptedSubmit by remember { mutableStateOf(false) }
    var titleTouched by remember { mutableStateOf(false) }
    val titleError = title.trim().isEmpty() && (attemptedSubmit || titleTouched)
    val reminderPresets = listOf(
        "Periyodik Bakım",
        "TÜVTÜRK Araç Muayenesi",
        "Trafik Sigortası Yenileme",
        "Kasko Yenileme",
        "Triger Seti Kontrolü",
        "V Kayışı Kontrolü",
        "Egzoz Gazı Emisyon",
        "Akü Kontrolü",
        "Kış Lastiği Değişimi"
    )

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = System.currentTimeMillis() + (30L * 24 * 60 * 60 * 1000)
    )
    var showDatePicker by remember { mutableStateOf(false) }

    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { }
    )

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    val selectedDateText = if (datePickerState.selectedDateMillis != null) {
        SimpleDateFormat("dd MMMM yyyy", Locale("tr", "TR")).format(Date(datePickerState.selectedDateMillis!!))
    } else {
        "Tarih Seç"
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = BgLight,
            contentWindowInsets = WindowInsets.safeDrawing,
            topBar = {
                TopAppBar(
                    windowInsets = topSafeBarWindowInsets(),
                    title = { Text("Hatırlatıcı Kur", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = SurfaceWhite,
                        titleContentColor = TextDark
                    )
                )
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .imePadding(),
                contentAlignment = Alignment.TopCenter
            ) {
                Column(
                    modifier = Modifier
                        .widthIn(max = 620.dp)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                // Reminder Presets
                Column {
                    Text(
                        text = "Hatırlatıcı Konusu",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        reminderPresets.forEach { preset ->
                            val isPresetSelected = title == preset
                            FilterChip(
                                selected = isPresetSelected,
                                onClick = { title = preset },
                                label = { Text(preset, fontWeight = FontWeight.SemiBold) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = ReminderSoft,
                                    selectedLabelColor = ReminderColor,
                                    containerColor = SurfaceWhite,
                                    labelColor = TextDark
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = isPresetSelected,
                                    borderColor = if (isPresetSelected) ReminderBorder else BorderLight
                                )
                            )
                        }
                    }
                }

                // Custom Title input
                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        titleTouched = true
                    },
                    label = { Text("Hatırlatıcı Başlığı *") },
                    isError = titleError,
                    supportingText = if (titleError) {
                        { Text("Hatırlatıcı başlığı boş bırakılamaz.") }
                    } else null,
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("reminder_title_input"),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ReminderColor,
                        focusedLabelColor = ReminderColor,
                        unfocusedBorderColor = BorderLight,
                        focusedContainerColor = SurfaceWhite,
                        unfocusedContainerColor = SurfaceWhite
                    )
                )

                // Quick Date Interval Buttons
                Column {
                    Text(
                        text = "Hızlı Süre Seçimi",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.horizontalScroll(rememberScrollState())
                    ) {
                        val now = System.currentTimeMillis()
                        AssistChip(
                            onClick = { datePickerState.selectedDateMillis = now + (30L * 24 * 60 * 60 * 1000) },
                            label = { Text("1 Ay Sonra") }
                        )
                        AssistChip(
                            onClick = { datePickerState.selectedDateMillis = now + (90L * 24 * 60 * 60 * 1000) },
                            label = { Text("3 Ay Sonra") }
                        )
                        AssistChip(
                            onClick = { datePickerState.selectedDateMillis = now + (180L * 24 * 60 * 60 * 1000) },
                            label = { Text("6 Ay Sonra") }
                        )
                        AssistChip(
                            onClick = { datePickerState.selectedDateMillis = now + (365L * 24 * 60 * 60 * 1000) },
                            label = { Text("1 Yıl (Muayene/Sigorta)") }
                        )
                    }
                }

                // Date Picker Card
                OutlinedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showDatePicker = true },
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, ReminderBorder),
                    colors = CardDefaults.outlinedCardColors(containerColor = SurfaceWhite)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(ReminderSoft, RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Alarm, contentDescription = null, tint = ReminderColor, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text("Hatırlatılacak Tarih", style = MaterialTheme.typography.labelSmall, color = TextMedium)
                            Text(selectedDateText, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextDark)
                        }
                    }
                }

                // Information alert
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = ReminderSoft),
                    border = BorderStroke(1.dp, ReminderBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = ReminderColor, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Belirlediğiniz tarih geldiğinde cihazınızda yerel bir bildirim uyarısı gösterilecektir.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextDark,
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        attemptedSubmit = true
                        if (title.trim().isEmpty() || datePickerState.selectedDateMillis == null) {
                            Toast.makeText(
                                context,
                                "Hatırlatıcı başlığı ve tarihi boş bırakılamaz.",
                                Toast.LENGTH_SHORT
                            ).show()
                            return@Button
                        }
                        val cleanTitle = title.trim()
                        val date = datePickerState.selectedDateMillis ?: (System.currentTimeMillis() + 86400000)
                        viewModel.addReminder(cleanTitle, date) { reminderId ->
                            val triggerTime = date
                            if (triggerTime > System.currentTimeMillis()) {
                                NotificationHelper.scheduleNotification(
                                    context,
                                    reminderId.toInt(),
                                    "Araç Bakım & Muayene Uyarısı",
                                    "$cleanTitle için belirlenen tarih geldi! Aracınızın durumunu kontrol ediniz.",
                                    triggerTime
                                )
                            }
                            onBack()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("save_reminder_button"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ReminderColor,
                        contentColor = Color.White
                    ),
                    enabled = !isLoading
                ) {
                    Icon(Icons.Default.AlarmAdd, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("HATIRLATICIYI VE ALARMI KAYDET", fontWeight = FontWeight.Bold, color = Color.White)
                }
                }
            }
        }

        if (showDatePicker) {
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = { TextButton(onClick = { showDatePicker = false }) { Text("Tamam") } },
                dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("İptal") } }
            ) {
                DatePicker(state = datePickerState)
            }
        }

        LoadingOverlay(isLoading)
    }
}

// -----------------------------------------------------------------------------
// LOADING OVERLAY
// -----------------------------------------------------------------------------

@Composable
fun LoadingOverlay(isLoading: Boolean) {
    AnimatedVisibility(
        visible = isLoading,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.35f))
                .clickable(enabled = false) { },
            contentAlignment = Alignment.Center
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = SurfaceWhite,
                shadowElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(color = PrimaryVibrant, strokeWidth = 3.dp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Kaydediliyor...",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------
// 6. PROFILE SCREEN (PROFİL SAYFASI)
// -----------------------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    onBack: () -> Unit,
    onVehicleClick: (Int) -> Unit,
    onAddVehicleClick: () -> Unit
) {
    val user by viewModel.user.collectAsStateWithLifecycle()
    val vehicles by viewModel.vehicles.collectAsStateWithLifecycle()
    val maintenanceRecords by viewModel.maintenanceRecords.collectAsStateWithLifecycle()
    val expenseRecords by viewModel.expenseRecords.collectAsStateWithLifecycle()
    val reminders by viewModel.reminders.collectAsStateWithLifecycle()
    val selectedCity by viewModel.selectedFuelCity.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()

    var showGarageEditDialog by remember { mutableStateOf(false) }
    var showCityModal by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }

    val name = user?.name.orEmpty().ifEmpty { "Toprak" }
    val surname = user?.surname.orEmpty().ifEmpty { "Baran" }
    val email = user?.email.orEmpty().ifEmpty { "toprakmuco321@gmail.com" }
    val phone = user?.phone.orEmpty().ifEmpty { "+90 532 486 74 12" }

    val garagePrefix = remember(user, name, surname) {
        extractGaragePrefix(user?.garageName, name, surname)
    }
    val garageTitle = remember(user, name, surname) {
        formatGarageTitle(user?.garageName, name, surname)
    }

    val initials = remember(name, surname) {
        val first = name.firstOrNull()?.uppercase() ?: "T"
        val last = surname.firstOrNull()?.uppercase() ?: "B"
        "$first$last"
    }

    val userVehicleIds = remember(vehicles) { vehicles.map { it.id }.toSet() }
    val userMaintenanceRecords = remember(maintenanceRecords, userVehicleIds) {
        maintenanceRecords.filter { it.vehicleId in userVehicleIds }
    }
    val userExpenseRecords = remember(expenseRecords, userVehicleIds) {
        expenseRecords.filter { it.vehicleId in userVehicleIds }
    }
    val userReminders = remember(reminders, userVehicleIds) {
        reminders.filter { it.vehicleId in userVehicleIds }
    }

    val totalMaintenanceAndExpenseCost = remember(userMaintenanceRecords, userExpenseRecords) {
        userMaintenanceRecords.sumOf { it.cost } + userExpenseRecords.sumOf { it.amount }
    }

    Scaffold(
        containerColor = BgLight,
        contentWindowInsets = WindowInsets.safeDrawing,
        bottomBar = {
            ModernAppBottomBar(
                currentTab = BottomBarTab.PROFILE,
                onTabSelected = { tab ->
                    when (tab) {
                        BottomBarTab.VEHICLES -> onBack()
                        BottomBarTab.ADD -> onAddVehicleClick()
                        BottomBarTab.PROFILE -> { /* Zaten profil ekranındayız */ }
                    }
                }
            )
        },
        topBar = {
            TopAppBar(
                windowInsets = topSafeBarWindowInsets(),
                title = {
                    Column {
                        Text(
                            text = garageTitle,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = TextDark,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${selectedCity.displayName} • Kullanıcı ve garaj yönetimi",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Geri",
                            tint = TextDark
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showEditDialog = true },
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .size(42.dp)
                            .background(PrimarySoft, CircleShape)
                            .testTag("profile_top_edit_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Profili Düzenle",
                            tint = PrimaryVibrant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceWhite)
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.TopCenter
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxHeight()
                    .widthIn(max = 680.dp)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 28.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
            // 0. En Üstte Kullanıcı Tarafından Güncellenebilir "... GARAJI" Başlık Kartı
            item {
                ProfileGarageHeaderBanner(
                    garageTitle = garageTitle,
                    vehicleCount = vehicles.size,
                    cityName = selectedCity.displayName,
                    onEditGarageClick = { showGarageEditDialog = true }
                )
            }

            // 1. User Profile Hero Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(4.dp, RoundedCornerShape(24.dp), spotColor = PrimaryVibrant.copy(alpha = 0.15f)),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                    border = BorderStroke(1.dp, BorderLight)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Avatar Badge
                        Box(
                            modifier = Modifier
                                .size(84.dp)
                                .background(
                                    brush = Brush.linearGradient(
                                        colors = listOf(PrimaryVibrant, SecondaryVibrant)
                                    ),
                                    shape = CircleShape
                                )
                                .border(3.dp, PrimarySoft, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = initials,
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )

                            // Online / Verified status badge
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .size(22.dp)
                                    .background(AccentEmerald, CircleShape)
                                    .border(2.dp, SurfaceWhite, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Full Name
                        Text(
                            text = "$name $surname",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            color = TextDark,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Contact Details Row
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(BgLightAlt, RoundedCornerShape(16.dp))
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Email
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .background(PrimarySoft, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Email,
                                        contentDescription = null,
                                        tint = PrimaryVibrant,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "E-POSTA",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextMedium,
                                        letterSpacing = 0.5.sp
                                    )
                                    Text(
                                        text = email,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium,
                                        color = TextDark,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            HorizontalDivider(color = BorderLight, thickness = 0.5.dp)

                            // Phone
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .background(AccentEmeraldSoft, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Phone,
                                        contentDescription = null,
                                        tint = TotalCostGreenDark,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "TELEFON",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextMedium,
                                        letterSpacing = 0.5.sp
                                    )
                                    Text(
                                        text = phone,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium,
                                        color = TextDark,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Edit Profile Button
                        OutlinedButton(
                            onClick = { showEditDialog = true },
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, PrimaryVibrant.copy(alpha = 0.5f)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryVibrant),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Profil Bilgilerini Düzenle", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // 1B. Şehir Tercihi Seçim Kartı
            item {
                ProfileCityFuelCard(
                    selectedCity = selectedCity,
                    onOpenAllCitiesModal = { showCityModal = true }
                )
            }

            // 2. Bento Summary Grid (4 Metrics)
            item {
                Text(
                    text = "GENEL BAKIŞ & İSTATİSTİKLER",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextMedium,
                    letterSpacing = 1.sp
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricBentoCard(
                        title = "GARAJ",
                        value = "${vehicles.size}",
                        subtitle = "Kayıtlı Araç",
                        icon = Icons.Default.DirectionsCar,
                        color = PrimaryVibrant,
                        softBg = PrimarySoft,
                        modifier = Modifier.weight(1f)
                    )

                    MetricBentoCard(
                        title = "BAKIM",
                        value = "${userMaintenanceRecords.size}",
                        subtitle = "Kayıt Girildi",
                        icon = Icons.Default.Build,
                        color = SecondaryVibrant,
                        softBg = SecondarySoft,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricBentoCard(
                        title = "TOPLAM HARCAMA",
                        value = if (totalMaintenanceAndExpenseCost > 0) formatCurrencyTl(totalMaintenanceAndExpenseCost) else "0 ₺",
                        subtitle = "Bakım & Giderler",
                        icon = Icons.Default.Payments,
                        color = TotalCostGreenDark,
                        softBg = AccentEmeraldSoft,
                        modifier = Modifier.weight(1f)
                    )

                    MetricBentoCard(
                        title = "HATIRLATICI",
                        value = "${userReminders.size}",
                        subtitle = "Aktif Plan",
                        icon = Icons.Default.Alarm,
                        color = ReminderColor,
                        softBg = ReminderSoft,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // 3. User's Vehicles List Section
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "KAYITLI ARAÇLARIM (${vehicles.size})",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextMedium,
                        letterSpacing = 1.sp
                    )

                    TextButton(onClick = onAddVehicleClick) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp), tint = PrimaryVibrant)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Yeni Araç Ekle", color = PrimaryVibrant, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }

            if (vehicles.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                        border = BorderStroke(1.dp, BorderLight)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .background(PrimarySoft, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.DirectionsCar, contentDescription = null, tint = PrimaryVibrant, modifier = Modifier.size(28.dp))
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("Garajınızda Henüz Araç Yok", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextDark)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Aracınızı ekleyerek servis ve bakım geçmişini hemen tutmaya başlayın.", style = MaterialTheme.typography.bodyMedium, color = TextMedium, textAlign = TextAlign.Center)
                            Spacer(modifier = Modifier.height(14.dp))
                            Button(
                                onClick = onAddVehicleClick,
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = PrimaryVibrant,
                                    contentColor = Color.White
                                )
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Araç Ekle", fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                }
            } else {
                items(vehicles, key = { it.id }) { vehicle ->
                    val vehicleMaintenanceCount = userMaintenanceRecords.count { it.vehicleId == vehicle.id }
                    val vehicleReminderCount = userReminders.count { it.vehicleId == vehicle.id }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onVehicleClick(vehicle.id) }
                            .shadow(2.dp, RoundedCornerShape(20.dp), spotColor = PrimaryVibrant.copy(alpha = 0.08f)),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                        border = BorderStroke(1.dp, BorderLight)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Vehicle Icon
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(
                                        Brush.linearGradient(listOf(PrimaryVibrant, SecondaryVibrant)),
                                        RoundedCornerShape(14.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.DirectionsCar,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(26.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            // Vehicle Info (Clean stacked layout preventing badge overflow on narrow phones)
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "${vehicle.brand} ${vehicle.model}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TextDark,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                                TurkishPlateBadge(
                                    plate = vehicle.plate,
                                    compact = true
                                )

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = BgLightAlt
                                    ) {
                                        Text(
                                            text = vehicle.year,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = TextMedium,
                                            maxLines = 1,
                                            softWrap = false,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = FuelSoft,
                                        border = BorderStroke(1.dp, FuelBorder)
                                    ) {
                                        Text(
                                            text = vehicle.fuelType,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = FuelColorDark,
                                            maxLines = 1,
                                            softWrap = false,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }

                                    Text(
                                        text = "• $vehicleMaintenanceCount Bakım • $vehicleReminderCount Alarm",
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = TextMedium,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(6.dp))

                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = "Detay",
                                tint = TextMedium,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }

            // 4. System & Security Information Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = BgLightAlt),
                    border = BorderStroke(1.dp, BorderLight)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Security, contentDescription = null, tint = AccentEmerald, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Veri Güvenliği & Saklama", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextDark)
                        }
                        Text(
                            text = "Tüm araç, bakım, fatura ve hatırlatıcı verileriniz cihazınızdaki SQLite (Room) veritabanında %100 yerel ve güvenli olarak saklanmaktadır.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMedium,
                            lineHeight = 18.sp
                        )

                        HorizontalDivider(color = BorderLight, thickness = 0.5.dp)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Uygulama Sürümü", fontSize = 12.sp, color = TextMedium)
                            Text("v1.0.0 (Güncel)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PrimaryVibrant)
                        }
                    }
                }
            }
            }
        }
    }

    if (showGarageEditDialog) {
        GarageNameEditDialog(
            initialGaragePrefix = garagePrefix,
            onSave = { updatedGarage ->
                viewModel.updateGarageName(updatedGarage) {
                    showGarageEditDialog = false
                }
            },
            onDismiss = { showGarageEditDialog = false }
        )
    }

    if (showCityModal) {
        CityPickerModalDialog(
            selectedCity = selectedCity,
            onCityPicked = { pickedCity ->
                viewModel.selectCity(pickedCity)
                showCityModal = false
            },
            onDismiss = { showCityModal = false }
        )
    }

    if (showEditDialog) {
        ProfileEditModalDialog(
            initialGaragePrefix = garagePrefix,
            initialName = name,
            initialSurname = surname,
            initialPhone = phone,
            initialEmail = email,
            initialCity = selectedCity,
            onSave = { updatedGarage, updatedName, updatedSurname, updatedPhone, updatedEmail, updatedCity ->
                viewModel.updateProfile(
                    garageName = updatedGarage,
                    name = updatedName,
                    surname = updatedSurname,
                    phone = updatedPhone,
                    email = updatedEmail,
                    city = updatedCity,
                    onSuccess = { showEditDialog = false }
                )
            },
            onDismiss = { showEditDialog = false }
        )
    }

    LoadingOverlay(isLoading)
}

// -----------------------------------------------------------------------------
// ADD EXPENSE SCREEN
// -----------------------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseScreen(
    viewModel: VehicleDetailViewModel,
    onBack: () -> Unit
) {
    var category by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }

    var attemptedSubmit by remember { mutableStateOf(false) }
    var categoryTouched by remember { mutableStateOf(false) }
    var descriptionTouched by remember { mutableStateOf(false) }
    var amountTouched by remember { mutableStateOf(false) }

    val parsedAmount = parseTurkishCurrency(amountText)
    val categoryError = category.trim().isEmpty() && (attemptedSubmit || categoryTouched)
    val descriptionError = description.trim().isEmpty() && (attemptedSubmit || descriptionTouched)
    val amountError = (amountText.trim().isEmpty() || parsedAmount <= 0.0) && (attemptedSubmit || amountTouched)

    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val categories = listOf("Sigorta", "Kasko", "Vergi (MTV)", "Otopark", "Yıkama", "Ceza", "Aksesuar", "Diğer")
    var expanded by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = BgLight,
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            TopAppBar(
                windowInsets = topSafeBarWindowInsets(),
                title = { Text("Gider Ekle", fontWeight = FontWeight.Bold, color = TextDark) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceWhite)
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding(),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 620.dp)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
            Text(
                text = "Aracınızın sigorta, kasko, vergi ve diğer giderlerini eksiksiz girin.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextMedium
            )

            // Quick Category Chips + Dropdown
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    categories.forEach { catOption ->
                        val isCatSelected = category == catOption
                        FilterChip(
                            selected = isCatSelected,
                            onClick = {
                                category = catOption
                                categoryTouched = true
                            },
                            label = {
                                Text(
                                    catOption,
                                    fontSize = 12.sp,
                                    fontWeight = if (isCatSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = InsuranceBlueSoft,
                                selectedLabelColor = InsuranceBlue
                            )
                        )
                    }
                }

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Gider Kategorisi *") },
                        isError = categoryError,
                        supportingText = if (categoryError) {
                            { Text("Gider kategorisi boş bırakılamaz.") }
                        } else null,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                            .testTag("expense_category_input"),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = InsuranceBlue,
                            focusedLabelColor = InsuranceBlue,
                            focusedContainerColor = SurfaceWhite,
                            unfocusedContainerColor = SurfaceWhite
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        categories.forEach { selectionOption ->
                            DropdownMenuItem(
                                text = { Text(selectionOption) },
                                onClick = {
                                    category = selectionOption
                                    categoryTouched = true
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                OutlinedTextField(
                    value = description,
                    onValueChange = {
                        description = it
                        descriptionTouched = true
                    },
                    label = { Text("Açıklama / Detay *") },
                    placeholder = { Text("Örn: Yıllık Trafik Sigortası poliçesi") },
                    isError = descriptionError,
                    supportingText = if (descriptionError) {
                        { Text("Açıklama alanı boş bırakılamaz.") }
                    } else null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("expense_description_input"),
                    shape = RoundedCornerShape(12.dp),
                    maxLines = 2,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = InsuranceBlue,
                        focusedLabelColor = InsuranceBlue,
                        focusedContainerColor = SurfaceWhite,
                        unfocusedContainerColor = SurfaceWhite
                    )
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(
                        "Yıllık poliçe yenilemesi",
                        "Dönem vergisi (MTV) ödemesi",
                        "Araç temizlik ve bakım hizmeti"
                    ).forEach { descPreset ->
                        AssistChip(
                            onClick = {
                                description = descPreset
                                descriptionTouched = true
                            },
                            label = { Text(descPreset, fontSize = 11.sp) }
                        )
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                OutlinedTextField(
                    value = amountText,
                    onValueChange = {
                        amountText = sanitizeCurrencyDigits(it, allowCommaDecimal = true)
                        amountTouched = true
                    },
                    label = { Text("Ücret / Tutar (₺) *") },
                    placeholder = { Text("Örn: 6.450") },
                    visualTransformation = TurkishThousandsVisualTransformation(),
                    isError = amountError,
                    supportingText = if (amountError) {
                        { Text("Ücret / tutar alanı boş bırakılamaz.") }
                    } else {
                        { Text("Para birimi otomatik olarak '.' ayırıcılı biçimlendirilir.", fontSize = 11.sp) }
                    },
                    trailingIcon = {
                        Text(
                            text = "₺",
                            fontWeight = FontWeight.Black,
                            color = InsuranceBlue,
                            fontSize = 16.sp,
                            modifier = Modifier.padding(end = 12.dp)
                        )
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("expense_amount_input"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = InsuranceBlue,
                        focusedLabelColor = InsuranceBlue,
                        focusedContainerColor = SurfaceWhite,
                        unfocusedContainerColor = SurfaceWhite
                    )
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("1.500", "3.500", "6.500", "10.000", "15.000").forEach { presetAmt ->
                        AssistChip(
                            onClick = {
                                amountText = sanitizeCurrencyDigits(presetAmt, allowCommaDecimal = true)
                                amountTouched = true
                            },
                            label = { Text("$presetAmt ₺", fontSize = 11.sp) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    attemptedSubmit = true
                    if (category.trim().isEmpty() ||
                        description.trim().isEmpty() ||
                        amountText.trim().isEmpty() ||
                        parsedAmount <= 0.0
                    ) {
                        Toast.makeText(
                            context,
                            "Lütfen kategori, açıklama ve tutar alanlarını eksiksiz doldurunuz.",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@Button
                    }
                    viewModel.addExpense(
                        category = category.trim(),
                        description = description.trim(),
                        amount = parsedAmount,
                        date = System.currentTimeMillis(),
                        onSuccess = onBack
                    )
                },
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("save_expense_button"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = InsuranceBlue,
                    contentColor = Color.White
                )
            ) {
                Text("KAYDET", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
            }
            }
        }
    }

    LoadingOverlay(isLoading)
}

// -----------------------------------------------------------------------------
// FUEL RECORD CARD
// -----------------------------------------------------------------------------

@Composable
fun FuelRecordCard(
    record: FuelRecord,
    drivenKmDelta: Int? = null,
    onClick: () -> Unit = {},
    onDelete: () -> Unit = {}
) {
    val trLocale = Locale.forLanguageTag("tr-TR")
    val kmSubtitle = buildString {
        append(SimpleDateFormat("dd MMM yyyy", trLocale).format(Date(record.date)))
        if (record.km > 0) {
            append(" • ")
            append(String.format(trLocale, "%,d km", record.km))
        }
        if (drivenKmDelta != null && drivenKmDelta > 0) {
            append(String.format(trLocale, " (+%,d km)", drivenKmDelta))
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(18.dp), spotColor = TextLight.copy(alpha = 0.1f))
            .clickable(onClick = onClick)
            .testTag("fuel_card_${record.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        border = BorderStroke(1.dp, BorderLight)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(FuelSoft, RoundedCornerShape(13.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.LocalGasStation, contentDescription = null, tint = FuelColorDark, modifier = Modifier.size(22.dp))
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = String.format(trLocale, "%.2f Lt Dolum", record.liters),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextDark,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = kmSubtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = FuelSoft,
                border = BorderStroke(1.dp, FuelBorder)
            ) {
                Text(
                    text = String.format(trLocale, "%,.0f ₺", record.total),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Black,
                    color = FuelColorDark,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Detayları Gör",
                tint = TextLight,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}