package com.example.ui

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.example.data.FuelCityOption
import com.example.data.FuelPriceService
import com.example.ui.theme.*
import java.util.Locale

private fun normalizeTurkishSearch(text: String): String {
    val trLocale = Locale.forLanguageTag("tr-TR")
    return text.lowercase(trLocale)
        .replace("ı", "i")
        .replace("i̇", "i")
        .replace("ş", "s")
        .replace("ğ", "g")
        .replace("ü", "u")
        .replace("ö", "o")
        .replace("ç", "c")
}

/**
 * Prominent "... GARAJI" editable banner displayed at the very top of ProfileScreen.
 */
@Composable
fun ProfileGarageHeaderBanner(
    garageTitle: String,
    vehicleCount: Int,
    cityName: String,
    onEditGarageClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(24.dp), spotColor = PrimaryVibrant.copy(alpha = 0.16f))
            .clickable(onClick = onEditGarageClick)
            .testTag("profile_garage_header_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        border = BorderStroke(1.2.dp, PrimaryVibrant.copy(alpha = 0.32f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(PrimarySoft, SurfaceWhite, FuelSoft.copy(alpha = 0.55f))
                    )
                )
                .padding(horizontal = 18.dp, vertical = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .background(
                                Brush.linearGradient(listOf(PrimaryVibrant, PrimaryVibrantDark)),
                                RoundedCornerShape(15.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.DirectionsCarFilled,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = SurfaceWhite,
                                border = BorderStroke(1.dp, PrimaryVibrant.copy(alpha = 0.25f))
                            ) {
                                Text(
                                    text = "DİJİTAL GARAJ",
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = PrimaryVibrantDark,
                                    letterSpacing = 0.6.sp,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            Text(
                                text = "• $cityName",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Spacer(modifier = Modifier.height(5.dp))

                        Text(
                            text = garageTitle,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            color = TextDark,
                            letterSpacing = 0.4.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.testTag("profile_garage_title_text")
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        Text(
                            text = "$vehicleCount Kayıtlı Araç • Başlığı güncellemek için dokunun",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMedium,
                            fontSize = 11.5.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Surface(
                    onClick = onEditGarageClick,
                    shape = RoundedCornerShape(12.dp),
                    color = SurfaceWhite,
                    border = BorderStroke(1.dp, PrimaryVibrant.copy(alpha = 0.45f)),
                    modifier = Modifier
                        .heightIn(min = 48.dp)
                        .testTag("edit_garage_name_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Garaj Adını Güncelle",
                            tint = PrimaryVibrantDark,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Düzenle",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryVibrantDark
                        )
                    }
                }
            }
        }
    }
}

/**
 * Profile city preference selector card (updates saved city without re-fetching fuel prices in Profile).
 */
@Composable
fun ProfileCityFuelCard(
    selectedCity: FuelCityOption,
    onOpenAllCitiesModal: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(3.dp, RoundedCornerShape(22.dp), spotColor = FuelColor.copy(alpha = 0.12f))
            .testTag("profile_city_fuel_card"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        border = BorderStroke(1.dp, BorderLight)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(FuelSoft, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = FuelColorDark,
                        modifier = Modifier.size(21.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "ŞEHİR TERCİHİ",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Black,
                        color = TextDark,
                        letterSpacing = 0.4.sp
                    )
                    Text(
                        text = "Ana sayfadaki akaryakıt fiyatları seçili şehre göre gösterilir",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMedium,
                        fontSize = 11.5.sp
                    )
                }
            }

            // Active City Selector Row (opens full 81-city picker modal)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 52.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .clickable(onClick = onOpenAllCitiesModal)
                    .testTag("profile_select_city_button"),
                shape = RoundedCornerShape(14.dp),
                color = FuelSoft,
                border = BorderStroke(1.2.dp, FuelBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = FuelColor,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = selectedCity.provinceCode,
                                    color = Color.White,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 12.5.sp
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "SEÇİLİ ŞEHİR",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = FuelColorDark,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = selectedCity.displayName,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Black,
                                color = TextDark
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Şehir Seç",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = FuelColorDark
                        )
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "Şehir Seç",
                            tint = FuelColorDark,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Dedicated modal dialog to update the "... GARAJI" banner title with strict non-empty validation.
 */
@Composable
fun GarageNameEditDialog(
    initialGaragePrefix: String,
    onSave: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val trLocale = Locale.forLanguageTag("tr-TR")
    var garageInput by remember(initialGaragePrefix) { mutableStateOf(initialGaragePrefix) }
    var attemptedSubmit by remember { mutableStateOf(false) }
    var touched by remember { mutableStateOf(false) }

    val cleanedPrefix = extractGaragePrefix(
        rawGarageName = garageInput,
        ownerName = null,
        ownerSurname = null
    )
    val isInvalid = garageInput.trim().isEmpty()
    val showError = isInvalid && (attemptedSubmit || touched)

    val previewText = if (garageInput.trim().isNotEmpty()) {
        "${cleanedPrefix.uppercase(trLocale)} GARAJI"
    } else {
        "... GARAJI"
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .background(PrimarySoft, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.DirectionsCarFilled,
                        contentDescription = null,
                        tint = PrimaryVibrantDark,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text("Garaj Başlığını Güncelle", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = PrimarySoft,
                    border = BorderStroke(1.dp, PrimaryVibrant.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "ÜST BAŞLIK ÖNİZLEME",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = PrimaryVibrantDark
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = previewText,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = TextDark
                        )
                    }
                }

                OutlinedTextField(
                    value = garageInput,
                    onValueChange = {
                        garageInput = it
                        touched = true
                    },
                    label = { Text("Garaj Adı *") },
                    placeholder = { Text("Örn: TOPRAK BARAN") },
                    suffix = {
                        Text(
                            text = "GARAJI",
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp,
                            color = PrimaryVibrantDark
                        )
                    },
                    isError = showError,
                    supportingText = {
                        if (showError) {
                            Text("Garaj adı boş bırakılamaz.")
                        } else {
                            Text("Sonuna otomatik olarak 'GARAJI' ifadesi eklenir.")
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("garage_name_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    attemptedSubmit = true
                    if (garageInput.trim().isEmpty()) {
                        Toast.makeText(context, "Garaj adı boş bırakılamaz", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    onSave(cleanedPrefix.trim())
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryVibrant,
                    contentColor = Color.White
                ),
                modifier = Modifier.testTag("save_garage_name_button")
            ) {
                Text("Güncelle", fontWeight = FontWeight.Bold, color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("İptal", color = TextDark)
            }
        },
        shape = RoundedCornerShape(22.dp),
        containerColor = SurfaceWhite
    )
}

/**
 * Full 81-province searchable Turkish City Picker modal.
 */
@Composable
fun CityPickerModalDialog(
    selectedCity: FuelCityOption,
    onCityPicked: (FuelCityOption) -> Unit,
    onDismiss: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val filteredCities = remember(searchQuery) {
        val q = normalizeTurkishSearch(searchQuery.trim())
        if (q.isEmpty()) {
            FuelPriceService.supportedCities
        } else {
            FuelPriceService.supportedCities.filter { city ->
                normalizeTurkishSearch(city.displayName).contains(q) ||
                    city.provinceCode.contains(q)
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .imePadding()
                .padding(horizontal = 16.dp, vertical = 18.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 520.dp)
                    .fillMaxHeight(0.84f)
                    .shadow(16.dp, RoundedCornerShape(26.dp), spotColor = TextDark.copy(alpha = 0.2f))
                    .testTag("city_picker_modal"),
                shape = RoundedCornerShape(26.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                border = BorderStroke(1.dp, BorderLight)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "AKARYAKIT ŞEHRİ SEÇİN",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = FuelColorDark,
                                letterSpacing = 0.6.sp
                            )
                            Text(
                                text = "Türkiye Geneli 81 İl",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = TextDark
                            )
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(48.dp)
                                .background(BgLightAlt, CircleShape)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Kapat", tint = TextDark)
                        }
                    }

                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Şehir veya plaka kodu ara (Örn: Ankara, 06, İzmir)...") },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = null, tint = FuelColorDark)
                        },
                        trailingIcon = if (searchQuery.isNotEmpty()) {
                            {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Close, contentDescription = "Temizle", modifier = Modifier.size(18.dp))
                                }
                            }
                        } else null,
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = FuelColor,
                            unfocusedContainerColor = BgLight,
                            focusedContainerColor = SurfaceWhite
                        )
                    )

                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredCities, key = { it.id }) { city ->
                            val isSelected = city.id == selectedCity.id
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable { onCityPicked(city) }
                                    .testTag("city_modal_option_${city.id}"),
                                shape = RoundedCornerShape(14.dp),
                                color = if (isSelected) FuelSoft else BgLight,
                                border = BorderStroke(
                                    width = if (isSelected) 1.4.dp else 1.dp,
                                    color = if (isSelected) FuelBorder else BorderLight
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .background(
                                                    if (isSelected) FuelColor else SurfaceWhite,
                                                    RoundedCornerShape(10.dp)
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = city.provinceCode,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Black,
                                                color = if (isSelected) Color.White else FuelColorDark
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            text = city.displayName,
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                            color = TextDark
                                        )
                                    }

                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = "Seçili",
                                            tint = FuelColorDark,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Full Profile Edit Modal Dialog with strict non-empty validation on every single field.
 */
@Composable
fun ProfileEditModalDialog(
    initialGaragePrefix: String,
    initialName: String,
    initialSurname: String,
    initialPhone: String,
    initialEmail: String,
    initialCity: FuelCityOption,
    onSave: (garageName: String, name: String, surname: String, phone: String, email: String, city: FuelCityOption) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var editGaragePrefix by remember(initialGaragePrefix) { mutableStateOf(initialGaragePrefix) }
    var editName by remember(initialName) { mutableStateOf(initialName) }
    var editSurname by remember(initialSurname) { mutableStateOf(initialSurname) }
    var editPhone by remember(initialPhone) { mutableStateOf(initialPhone) }
    var editEmail by remember(initialEmail) { mutableStateOf(initialEmail) }
    var editCity by remember(initialCity) { mutableStateOf(initialCity) }

    var showCityPicker by remember { mutableStateOf(false) }
    var attemptedSubmit by remember { mutableStateOf(false) }

    var garageTouched by remember { mutableStateOf(false) }
    var nameTouched by remember { mutableStateOf(false) }
    var surnameTouched by remember { mutableStateOf(false) }
    var phoneTouched by remember { mutableStateOf(false) }
    var emailTouched by remember { mutableStateOf(false) }

    val garageError = editGaragePrefix.trim().isEmpty() && (attemptedSubmit || garageTouched)
    val nameError = editName.trim().isEmpty() && (attemptedSubmit || nameTouched)
    val surnameError = editSurname.trim().isEmpty() && (attemptedSubmit || surnameTouched)
    val phoneError = editPhone.trim().isEmpty() && (attemptedSubmit || phoneTouched)
    val emailError = editEmail.trim().isEmpty() && (attemptedSubmit || emailTouched)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .imePadding()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 500.dp)
                    .shadow(16.dp, RoundedCornerShape(24.dp), spotColor = TextDark.copy(alpha = 0.22f))
                    .testTag("profile_edit_modal"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                border = BorderStroke(1.dp, BorderLight)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .background(PrimarySoft, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Person, contentDescription = null, tint = PrimaryVibrant, modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Profil & Garaj Düzenle", fontWeight = FontWeight.Black, style = MaterialTheme.typography.titleMedium)
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(44.dp)
                                .background(BgLightAlt, CircleShape)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Kapat", tint = TextDark, modifier = Modifier.size(20.dp))
                        }
                    }

                    OutlinedTextField(
                        value = editGaragePrefix,
                        onValueChange = {
                            editGaragePrefix = it
                            garageTouched = true
                        },
                        label = { Text("Garaj Adı *") },
                        suffix = { Text("GARAJI", fontWeight = FontWeight.Black, fontSize = 11.sp, color = PrimaryVibrantDark) },
                        isError = garageError,
                        supportingText = if (garageError) { { Text("Garaj adı boş bırakılamaz") } } else null,
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = editName,
                            onValueChange = {
                                editName = it
                                nameTouched = true
                            },
                            label = { Text("Adınız *") },
                            isError = nameError,
                            supportingText = if (nameError) { { Text("Boş bırakılamaz") } } else null,
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        )

                        OutlinedTextField(
                            value = editSurname,
                            onValueChange = {
                                editSurname = it
                                surnameTouched = true
                            },
                            label = { Text("Soyadınız *") },
                            isError = surnameError,
                            supportingText = if (surnameError) { { Text("Boş bırakılamaz") } } else null,
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    OutlinedTextField(
                        value = editPhone,
                        onValueChange = {
                            editPhone = it
                            phoneTouched = true
                        },
                        label = { Text("Telefon Numarası *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        isError = phoneError,
                        supportingText = if (phoneError) { { Text("Telefon numarası boş bırakılamaz") } } else null,
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = editEmail,
                        onValueChange = {
                            editEmail = it
                            emailTouched = true
                        },
                        label = { Text("E-posta Adresi *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        isError = emailError,
                        supportingText = if (emailError) { { Text("E-posta adresi boş bırakılamaz") } } else null,
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // City Selector Row inside Profile Edit Modal
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { showCityPicker = true },
                        shape = RoundedCornerShape(12.dp),
                        color = FuelSoft,
                        border = BorderStroke(1.dp, FuelBorder)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.LocationOn, contentDescription = null, tint = FuelColorDark, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text("AKARYAKIT ŞEHRİ *", fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = FuelColorDark)
                                    Text("${editCity.provinceCode} - ${editCity.displayName}", fontWeight = FontWeight.Bold, color = TextDark)
                                }
                            }
                            Text("Değiştir", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = FuelColorDark)
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, BorderLight)
                        ) {
                            Text("İptal", color = TextDark, fontWeight = FontWeight.SemiBold)
                        }

                        Button(
                            onClick = {
                                attemptedSubmit = true
                                if (editGaragePrefix.trim().isEmpty() ||
                                    editName.trim().isEmpty() ||
                                    editSurname.trim().isEmpty() ||
                                    editPhone.trim().isEmpty() ||
                                    editEmail.trim().isEmpty()
                                ) {
                                    Toast.makeText(
                                        context,
                                        "Lütfen tüm alanları doldurunuz, boş alan bırakılamaz.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    return@Button
                                }
                                val cleanedGarage = extractGaragePrefix(editGaragePrefix, editName, editSurname)
                                onSave(
                                    cleanedGarage.trim(),
                                    editName.trim(),
                                    editSurname.trim(),
                                    editPhone.trim(),
                                    editEmail.trim(),
                                    editCity
                                )
                            },
                            modifier = Modifier
                                .weight(1.3f)
                                .height(50.dp)
                                .testTag("save_profile_edit_button"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PrimaryVibrant,
                                contentColor = Color.White
                            )
                        ) {
                            Text("Kaydet", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }
    }

    if (showCityPicker) {
        CityPickerModalDialog(
            selectedCity = editCity,
            onCityPicked = { picked ->
                editCity = picked
                showCityPicker = false
            },
            onDismiss = { showCityPicker = false }
        )
    }
}
