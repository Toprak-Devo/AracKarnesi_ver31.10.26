package com.example.ui

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddRoad
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.FuelPriceUiState
import com.example.data.Vehicle
import com.example.ui.theme.*
import java.util.Locale

@Composable
fun AddFuelModalDialog(
    vehicles: List<Vehicle>,
    initialVehicleId: Int?,
    lastKmByVehicleId: Map<Int, Int>,
    fuelPriceState: FuelPriceUiState,
    isSaving: Boolean,
    onSaveFuel: (vehicleId: Int, liters: Double, price: Double, newTotalKm: Int, drivenKm: Int) -> Unit,
    onDismiss: () -> Unit
) {
    if (vehicles.isEmpty()) {
        onDismiss()
        return
    }

    val context = LocalContext.current
    val trLocale = Locale.forLanguageTag("tr-TR")

    var selectedVehicleId by remember(vehicles, initialVehicleId) {
        val validInitial = vehicles.firstOrNull { it.id == initialVehicleId }?.id ?: vehicles.first().id
        mutableIntStateOf(validInitial)
    }

    val selectedVehicle = remember(vehicles, selectedVehicleId) {
        vehicles.firstOrNull { it.id == selectedVehicleId } ?: vehicles.first()
    }

    val lastCurrentKm = lastKmByVehicleId[selectedVehicle.id] ?: 0

    var drivenKmText by remember { mutableStateOf("") }
    var litersText by remember { mutableStateOf("") }
    var priceText by remember { mutableStateOf("") }

    var attemptedSubmit by remember { mutableStateOf(false) }
    var drivenKmTouched by remember { mutableStateOf(false) }
    var litersTouched by remember { mutableStateOf(false) }
    var priceTouched by remember { mutableStateOf(false) }

    // Auto-fill live pump price when vehicle changes or live prices arrive
    LaunchedEffect(fuelPriceState, selectedVehicle.id, selectedVehicle.fuelType) {
        val live = (fuelPriceState as? FuelPriceUiState.Success)?.prices
        if (live != null) {
            val recommended = live.priceForFuelType(selectedVehicle.fuelType)
            priceText = String.format(Locale.US, "%.2f", recommended)
        }
    }

    val parsedDrivenKm = drivenKmText.trim().replace(".", "").replace(",", "").toIntOrNull() ?: 0
    val calculatedNewTotalKm = lastCurrentKm + parsedDrivenKm.coerceAtLeast(0)
    val parsedLiters = parseUnitPrice(litersText)
    val parsedPrice = parseUnitPrice(priceText)
    val calculatedTotalCost = parsedLiters * parsedPrice
    val calculatedTripAvgLt100 = if (parsedDrivenKm > 0 && parsedLiters > 0.0) {
        (parsedLiters / parsedDrivenKm.toDouble()) * 100.0
    } else {
        null
    }

    val drivenKmError = (drivenKmText.trim().isEmpty() || parsedDrivenKm <= 0) && (attemptedSubmit || drivenKmTouched)
    val litersError = (litersText.trim().isEmpty() || parsedLiters <= 0.0) && (attemptedSubmit || litersTouched)
    val priceError = (priceText.trim().isEmpty() || parsedPrice <= 0.0) && (attemptedSubmit || priceTouched)

    Dialog(
        onDismissRequest = { if (!isSaving) onDismiss() },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false,
            dismissOnBackPress = !isSaving,
            dismissOnClickOutside = !isSaving
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .imePadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 500.dp)
                    .shadow(16.dp, RoundedCornerShape(26.dp), spotColor = TextDark.copy(alpha = 0.25f))
                    .testTag("add_fuel_modal_dialog"),
                shape = RoundedCornerShape(26.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                border = BorderStroke(1.dp, BorderLight)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                // 1. Modal Header
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
                                .size(44.dp)
                                .background(
                                    Brush.linearGradient(listOf(FuelColor, FuelColorDark)),
                                    RoundedCornerShape(14.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocalGasStation,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Yakıt Kaydı Ekle",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = TextDark
                            )
                            Text(
                                text = "${selectedVehicle.brand} ${selectedVehicle.model} • ${selectedVehicle.plate}",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    IconButton(
                        onClick = { if (!isSaving) onDismiss() },
                        modifier = Modifier
                            .size(48.dp)
                            .background(BgLightAlt, CircleShape)
                            .testTag("close_fuel_modal_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Kapat",
                            tint = TextDark,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // 2. Vehicle Selector (shown if user has multiple vehicles)
                if (vehicles.size > 1) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        vehicles.forEach { v ->
                            val isSelected = v.id == selectedVehicle.id
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedVehicleId = v.id },
                                label = {
                                    Text(
                                        text = "${v.plate} (${v.brand})",
                                        fontSize = 11.5.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = FuelSoft,
                                    selectedLabelColor = FuelColorDark
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = isSelected,
                                    borderColor = if (isSelected) FuelBorder else BorderLight
                                )
                            )
                        }
                    }
                }

                // 3. Son Güncel Kilometre + Yapılan KM -> Yeni Güncel KM Canlı Önizleme Kartı
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (parsedDrivenKm > 0) FuelSoft else BgLightAlt,
                    border = BorderStroke(
                        width = 1.2.dp,
                        color = if (parsedDrivenKm > 0) FuelBorder else BorderLight
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("odometer_preview_banner")
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Speed,
                                    contentDescription = null,
                                    tint = FuelColorDark,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "SON GÜNCEL KİLOMETRE",
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = TextMedium,
                                    letterSpacing = 0.3.sp
                                )
                            }
                            Text(
                                text = String.format(trLocale, "%,d km", lastCurrentKm),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Black,
                                color = TextDark
                            )
                        }

                        HorizontalDivider(color = BorderLight, thickness = 0.8.dp)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "YENİ GÜNCEL KİLOMETRE",
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (parsedDrivenKm > 0) FuelColorDark else TextMedium,
                                    letterSpacing = 0.3.sp
                                )
                                Text(
                                    text = if (parsedDrivenKm > 0) {
                                        String.format(trLocale, "%,d + %,d km eklendi", lastCurrentKm, parsedDrivenKm)
                                    } else {
                                        "Yapılan km girildiğinde otomatik eklenir"
                                    },
                                    fontSize = 11.sp,
                                    color = TextDark.copy(alpha = 0.78f)
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = SurfaceWhite,
                                border = BorderStroke(
                                    1.dp,
                                    if (parsedDrivenKm > 0) FuelBorder else BorderLight
                                )
                            ) {
                                Text(
                                    text = String.format(trLocale, "%,d km", calculatedNewTotalKm),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Black,
                                    color = if (parsedDrivenKm > 0) FuelColorDark else TextDark,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }
                }

                // 4. Yapılan Kilometre Input (replaces Güncel Kilometre)
                OutlinedTextField(
                    value = drivenKmText,
                    onValueChange = { input ->
                        drivenKmText = sanitizeIntegerDigits(input)
                        drivenKmTouched = true
                    },
                    label = { Text("Yapılan Kilometre (km) *") },
                    placeholder = { Text("Örn: 420") },
                    visualTransformation = TurkishThousandsVisualTransformation(),
                    isError = drivenKmError,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.AddRoad,
                            contentDescription = null,
                            tint = if (drivenKmError) AccentRose else FuelColor
                        )
                    },
                    suffix = {
                        Text(
                            text = "+ km",
                            fontWeight = FontWeight.Bold,
                            color = FuelColor,
                            fontSize = 13.sp
                        )
                    },
                    supportingText = {
                        if (drivenKmError) {
                            Text(
                                text = "Yapılan kilometre boş bırakılamaz.",
                                fontSize = 11.sp
                            )
                        } else {
                            Text(
                                text = String.format(
                                    trLocale,
                                    "Son güncel kilometrenin (%,d km) üzerine eklenecektir.",
                                    lastCurrentKm
                                ),
                                fontSize = 11.sp
                            )
                        }
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("driven_km_input"),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = FuelColor,
                        focusedLabelColor = FuelColor
                    )
                )

                // 5. Live Pump Price Quick Chips
                val livePrices = (fuelPriceState as? FuelPriceUiState.Success)?.prices
                if (livePrices != null) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "Güncel Pompa Fiyatı (${livePrices.cityName})",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = TextMedium
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            AssistChip(
                                onClick = {
                                    priceText = String.format(Locale.US, "%.2f", livePrices.diesel)
                                    priceTouched = true
                                },
                                label = {
                                    Text(
                                        "Motorin: ${formatUnitPriceDotTl(livePrices.diesel)}",
                                        fontSize = 11.5.sp
                                    )
                                }
                            )
                            AssistChip(
                                onClick = {
                                    priceText = String.format(Locale.US, "%.2f", livePrices.gasoline95)
                                    priceTouched = true
                                },
                                label = {
                                    Text(
                                        "Benzin: ${formatUnitPriceDotTl(livePrices.gasoline95)}",
                                        fontSize = 11.5.sp
                                    )
                                }
                            )
                            AssistChip(
                                onClick = {
                                    priceText = String.format(Locale.US, "%.2f", livePrices.lpg)
                                    priceTouched = true
                                },
                                label = {
                                    Text(
                                        "LPG: ${formatUnitPriceDotTl(livePrices.lpg)}",
                                        fontSize = 11.5.sp
                                    )
                                }
                            )
                        }
                    }
                }

                // 6. Litre & Litre Fiyatı Side-by-Side Inputs
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = litersText,
                        onValueChange = {
                            litersText = sanitizeDecimalQuantityInput(it)
                            litersTouched = true
                        },
                        label = { Text("Alınan (Litre) *") },
                        placeholder = { Text("Örn: 38.5") },
                        isError = litersError,
                        supportingText = if (litersError) {
                            { Text("Boş bırakılamaz", fontSize = 10.5.sp) }
                        } else null,
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("fuel_liters_input"),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = FuelColor,
                            focusedLabelColor = FuelColor
                        )
                    )

                    OutlinedTextField(
                        value = priceText,
                        onValueChange = {
                            priceText = sanitizeUnitPriceInput(it)
                            priceTouched = true
                        },
                        label = { Text("Litre Fiyatı *") },
                        placeholder = { Text("Örn: 46.50") },
                        suffix = {
                            Text(
                                text = "TL/ Lt",
                                fontWeight = FontWeight.Black,
                                color = FuelColorDark,
                                fontSize = 11.sp
                            )
                        },
                        isError = priceError,
                        supportingText = if (priceError) {
                            { Text("Boş bırakılamaz", fontSize = 10.5.sp) }
                        } else null,
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("fuel_price_input"),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = FuelColor,
                            focusedLabelColor = FuelColor
                        )
                    )
                }

                // 7. Live Total Cost & Trip Consumption Preview Strip
                if (calculatedTotalCost > 0.0 || calculatedTripAvgLt100 != null) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = FuelSoft,
                        border = BorderStroke(1.dp, FuelBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Payments,
                                    contentDescription = null,
                                    tint = FuelColorDark,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "TOPLAM TUTAR",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = FuelColorDark
                                    )
                                    Text(
                                        text = formatCurrencyTl(calculatedTotalCost, decimals = 2),
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Black,
                                        color = TextDark
                                    )
                                }
                            }

                            if (calculatedTripAvgLt100 != null) {
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "BU DOLUM TÜKETİMİ",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = FuelColorDark
                                    )
                                    Text(
                                        text = String.format(trLocale, "%.2f Lt / 100 km", calculatedTripAvgLt100),
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Black,
                                        color = FuelColorDark
                                    )
                                }
                            }
                        }
                    }
                }

                // 8. Action Buttons (İptal & KAYDET)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = { if (!isSaving) onDismiss() },
                        enabled = !isSaving,
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .testTag("cancel_fuel_modal_button"),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, BorderLight)
                    ) {
                        Text("İptal", color = TextDark, fontWeight = FontWeight.SemiBold)
                    }

                    Button(
                        onClick = {
                            attemptedSubmit = true
                            if (drivenKmText.trim().isEmpty() ||
                                litersText.trim().isEmpty() ||
                                priceText.trim().isEmpty() ||
                                parsedDrivenKm <= 0 ||
                                parsedLiters <= 0.0 ||
                                parsedPrice <= 0.0
                            ) {
                                Toast.makeText(
                                    context,
                                    "Lütfen tüm yakıt bilgilerini doldurunuz, boş alan bırakılamaz.",
                                    Toast.LENGTH_SHORT
                                ).show()
                                return@Button
                            }
                            val newTotalKm = lastCurrentKm + parsedDrivenKm
                            onSaveFuel(selectedVehicle.id, parsedLiters, parsedPrice, newTotalKm, parsedDrivenKm)
                        },
                        enabled = !isSaving,
                        modifier = Modifier
                            .weight(1.4f)
                            .height(52.dp)
                            .testTag("save_fuel_modal_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = FuelColor,
                            contentColor = Color.White
                        )
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.5.dp,
                                color = Color.White
                            )
                        } else {
                            Text("KAYDET", fontWeight = FontWeight.Black, fontSize = 15.sp, color = Color.White)
                        }
                    }
                    }
                }
            }
        }
    }
}

