package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Vehicle
import com.example.ui.theme.*
import java.util.Locale

@Composable
fun DashboardVehicleSelectorCard(
    vehicles: List<Vehicle>,
    selectedVehicle: Vehicle,
    lastKmByVehicleId: Map<Int, Int>,
    onVehicleSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    if (vehicles.size < 2) return

    val trLocale = Locale.forLanguageTag("tr-TR")
    var dropdownExpanded by remember { mutableStateOf(false) }
    val selectedIndex = vehicles.indexOfFirst { it.id == selectedVehicle.id }.coerceAtLeast(0)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(3.dp, RoundedCornerShape(20.dp), spotColor = PrimaryVibrant.copy(alpha = 0.10f))
            .testTag("dashboard_vehicle_selector"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        border = BorderStroke(1.dp, BorderLight)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header + Optional Quick Dropdown Trigger
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .background(PrimarySoft, RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.SwapHoriz,
                            contentDescription = null,
                            tint = PrimaryVibrantDark,
                            modifier = Modifier.size(19.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "AKTİF ARAÇ SEÇİCİ",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = PrimaryVibrantDark,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "Seçili aracın bakım, yakıt ve alarm bilgileri gösteriliyor",
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 11.sp,
                            color = TextMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Box {
                    Surface(
                        modifier = Modifier
                            .heightIn(min = 36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { dropdownExpanded = true }
                            .testTag("dashboard_vehicle_dropdown_button"),
                        shape = RoundedCornerShape(10.dp),
                        color = PrimarySoft,
                        border = BorderStroke(1.dp, PrimaryVibrant.copy(alpha = 0.25f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "${selectedIndex + 1}/${vehicles.size} Araç",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryVibrantDark
                            )
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = "Araç Listesi",
                                tint = PrimaryVibrantDark,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = dropdownExpanded,
                        onDismissRequest = { dropdownExpanded = false },
                        containerColor = SurfaceWhite
                    ) {
                        vehicles.forEach { v ->
                            val isSel = v.id == selectedVehicle.id
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(
                                            text = v.plate.uppercase(trLocale),
                                            fontWeight = FontWeight.Black,
                                            fontSize = 13.sp,
                                            color = if (isSel) PrimaryVibrantDark else TextDark,
                                            fontFamily = FontFamily.Monospace
                                        )
                                        Text(
                                            text = "${v.brand} ${v.model} • ${v.fuelType}",
                                            fontSize = 11.5.sp,
                                            color = TextMedium
                                        )
                                    }
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = if (isSel) Icons.Default.Check else Icons.Default.DirectionsCar,
                                        contentDescription = null,
                                        tint = if (isSel) PrimaryVibrantDark else TextMedium,
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                onClick = {
                                    onVehicleSelected(v.id)
                                    dropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            // Interactive Vehicle Option Pills (Equal 2-column grid for 2 vehicles, horizontal scroll for 3+)
            if (vehicles.size == 2) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    vehicles.forEach { v ->
                        VehicleSelectorOptionTile(
                            vehicle = v,
                            latestKm = lastKmByVehicleId[v.id] ?: 0,
                            isSelected = v.id == selectedVehicle.id,
                            onClick = { onVehicleSelected(v.id) },
                            trLocale = trLocale,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    vehicles.forEach { v ->
                        VehicleSelectorOptionTile(
                            vehicle = v,
                            latestKm = lastKmByVehicleId[v.id] ?: 0,
                            isSelected = v.id == selectedVehicle.id,
                            onClick = { onVehicleSelected(v.id) },
                            trLocale = trLocale,
                            modifier = Modifier.widthIn(min = 168.dp, max = 220.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun VehicleSelectorOptionTile(
    vehicle: Vehicle,
    latestKm: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    trLocale: Locale,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .heightIn(min = 62.dp)
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .testTag("select_vehicle_${vehicle.id}"),
        shape = RoundedCornerShape(14.dp),
        color = if (isSelected) PrimarySoft else BgLight,
        border = BorderStroke(
            width = if (isSelected) 1.5.dp else 1.dp,
            color = if (isSelected) PrimaryVibrant else BorderLight
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 11.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(5.dp),
                        color = if (isSelected) PrimaryVibrant else PlateBlueTR.copy(alpha = 0.14f)
                    ) {
                        Text(
                            text = "TR",
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Black,
                            color = if (isSelected) Color.White else PlateBlueTR,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }
                    Text(
                        text = vehicle.plate.uppercase(trLocale),
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        color = if (isSelected) PrimaryVibrantDark else TextDark,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = "${vehicle.brand} ${vehicle.model}",
                    fontSize = 11.5.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                    color = TextDark,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = if (latestKm > 0) {
                        "${vehicle.fuelType} • ${String.format(trLocale, "%,d km", latestKm)}"
                    } else {
                        "${vehicle.year} • ${vehicle.fuelType}"
                    },
                    fontSize = 10.sp,
                    color = TextMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (isSelected) {
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .background(PrimaryVibrant, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Seçili Araç",
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}
