package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.FuelCityOption
import com.example.data.FuelPriceUiState
import com.example.data.normalizeFuelLookupKey
import com.example.ui.theme.*

/**
 * Compact Live Fuel Prices Card.
 * City selection is intentionally excluded here and managed exclusively inside the Profile screen.
 * Prices are formatted as '98.00 TL/ Lt' in a slim, low-height single-row layout.
 */
@Composable
fun LiveFuelPricesCard(
    fuelPriceState: FuelPriceUiState,
    selectedCity: FuelCityOption,
    highlightedFuelType: String? = null,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    val normalizedHighlight = normalizeFuelLookupKey(highlightedFuelType)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(16.dp), spotColor = PrimaryVibrant.copy(alpha = 0.08f))
            .testTag("live_fuel_prices_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        border = BorderStroke(1.dp, BorderLight)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 12.dp, end = 6.dp, top = 4.dp, bottom = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Slim Header Row (No city chips underneath)
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
                            .size(24.dp)
                            .background(FuelSoft, RoundedCornerShape(7.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalGasStation,
                            contentDescription = null,
                            tint = FuelColorDark,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(
                                when (fuelPriceState) {
                                    is FuelPriceUiState.Success -> FuelColor
                                    is FuelPriceUiState.Loading -> FuelColor
                                    is FuelPriceUiState.Error -> AccentRose
                                },
                                CircleShape
                            )
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = stringResource(R.string.live_fuel_prices_title),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextDark,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = " • ${selectedCity.displayName}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                IconButton(
                    onClick = onRefresh,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("refresh_fuel_prices_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = stringResource(R.string.refresh_fuel_prices_desc),
                        tint = FuelColorDark,
                        modifier = Modifier.size(17.dp)
                    )
                }
            }

            // Compact Single-Row Prices Content
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 6.dp)
            ) {
                when (fuelPriceState) {
                    is FuelPriceUiState.Loading -> {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(15.dp),
                                strokeWidth = 2.dp,
                                color = FuelColor
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Pompa fiyatları güncelleniyor...",
                                fontSize = 11.5.sp,
                                color = TextMedium
                            )
                        }
                    }

                    is FuelPriceUiState.Error -> {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = AccentRoseSoft,
                            border = BorderStroke(1.dp, AccentRose.copy(alpha = 0.28f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.WifiOff,
                                    contentDescription = null,
                                    tint = AccentRose,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = fuelPriceState.message,
                                    fontSize = 11.sp,
                                    color = TextDark,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                                TextButton(
                                    onClick = onRefresh,
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                                    modifier = Modifier
                                        .heightIn(min = 36.dp)
                                        .testTag("retry_fuel_prices_button")
                                ) {
                                    Text("Yenile", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AccentRose)
                                }
                            }
                        }
                    }

                    is FuelPriceUiState.Success -> {
                        val prices = fuelPriceState.prices
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            CompactFuelPumpPill(
                                label = stringResource(R.string.fuel_gasoline_short),
                                price = prices.gasoline95,
                                isHighlighted = normalizedHighlight.contains("benzin"),
                                modifier = Modifier.weight(1f)
                            )
                            CompactFuelPumpPill(
                                label = stringResource(R.string.fuel_diesel_short),
                                price = prices.diesel,
                                isHighlighted = normalizedHighlight.contains("dizel") ||
                                    normalizedHighlight.contains("motorin"),
                                modifier = Modifier.weight(1f)
                            )
                            CompactFuelPumpPill(
                                label = stringResource(R.string.fuel_lpg_short),
                                price = prices.lpg,
                                isHighlighted = normalizedHighlight.contains("lpg") ||
                                    normalizedHighlight.contains("otogaz"),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CompactFuelPumpPill(
    label: String,
    price: Double,
    isHighlighted: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = if (isHighlighted) FuelSoft else BgLightAlt,
        border = BorderStroke(
            width = if (isHighlighted) 1.2.dp else 1.dp,
            color = if (isHighlighted) FuelBorder else BorderLight
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 5.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                fontSize = 10.sp,
                lineHeight = 12.sp,
                fontWeight = if (isHighlighted) FontWeight.ExtraBold else FontWeight.SemiBold,
                color = if (isHighlighted) FuelColorDark else TextMedium,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = formatUnitPriceDotTl(price),
                fontSize = 11.sp,
                lineHeight = 14.sp,
                fontWeight = FontWeight.Black,
                color = TextDark,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
