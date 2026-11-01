package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.DirectionsCarFilled
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.R
import com.example.data.VehicleBrandItem
import com.example.data.VehicleCatalogData
import com.example.data.VehicleEngineCatalog
import com.example.data.VehicleEngineOption
import com.example.ui.theme.*
import java.util.Locale

internal fun normalizeSearchText(text: String): String {
    val trLocale = Locale.forLanguageTag("tr-TR")
    return text.lowercase(trLocale)
        .replace("ı", "i")
        .replace("i̇", "i")
        .replace("ş", "s")
        .replace("š", "s")
        .replace("ğ", "g")
        .replace("ü", "u")
        .replace("ö", "o")
        .replace("ç", "c")
        .replace("é", "e")
        .replace("ë", "e")
}

@Composable
fun BrandModelSelectorSection(
    catalog: VehicleCatalogData,
    engineOptions: List<VehicleEngineOption>,
    isLoadingEngines: Boolean,
    selectedBrand: String,
    selectedModel: String,
    selectedEngine: String,
    showValidationErrors: Boolean = false,
    onBrandSelected: (String) -> Unit,
    onModelSelected: (String) -> Unit,
    onEngineSelected: (VehicleEngineOption) -> Unit
) {
    var showBrandModal by remember { mutableStateOf(false) }
    var showModelModal by remember { mutableStateOf(false) }
    var showEngineModal by remember { mutableStateOf(false) }

    val brandMissingError = showValidationErrors && selectedBrand.isBlank()
    val modelMissingError = showValidationErrors && selectedModel.isBlank()
    val engineMissingError = showValidationErrors && selectedEngine.isBlank()

    val modelsForSelectedBrand = remember(catalog, selectedBrand) {
        catalog.modelsForBrand(selectedBrand)
    }

    val isBrandChosen = selectedBrand.isNotBlank()
    val isModelChosen = selectedBrand.isNotBlank() && selectedModel.isNotBlank()
    val effectiveEngineOptions = remember(engineOptions, selectedBrand, selectedModel) {
        if (engineOptions.isNotEmpty()) {
            engineOptions
        } else if (isModelChosen) {
            VehicleEngineCatalog.getEnginesForVehicle(selectedBrand, selectedModel)
        } else {
            emptyList()
        }
    }
    val selectedEngineObj = remember(effectiveEngineOptions, selectedEngine) {
        effectiveEngineOptions.firstOrNull { it.engineName.equals(selectedEngine, ignoreCase = true) }
    }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        // 1. Araç Markası Kutusu
        ModernCompactSelectorField(
            label = "Araç Markası *",
            value = selectedBrand,
            placeholder = "Marka seçin",
            leadingIcon = Icons.Default.DirectionsCarFilled,
            buttonTestTag = "select_brand_button",
            isError = brandMissingError,
            errorMessage = if (brandMissingError) "Lütfen araç markası seçiniz." else null,
            onClick = { showBrandModal = true }
        )

        // 2. Araç Modeli Kutusu
        ModernCompactSelectorField(
            label = if (isBrandChosen) "$selectedBrand Modeli *" else "Araç Modeli *",
            value = selectedModel,
            placeholder = if (isBrandChosen) "Model seçin" else "Önce marka seçin",
            leadingIcon = Icons.Default.DirectionsCar,
            buttonTestTag = "select_model_button",
            enabled = isBrandChosen,
            isError = modelMissingError,
            errorMessage = if (modelMissingError) "Lütfen araç modeli seçiniz." else null,
            onClick = { showModelModal = true }
        )

        // 3. Motor Seçenekleri Kutusu
        ModernCompactSelectorField(
            label = if (isModelChosen) "$selectedModel Motor Seçeneği *" else "Motor Seçeneği *",
            value = selectedEngine,
            placeholder = when {
                isLoadingEngines -> "Motor seçenekleri yükleniyor..."
                isModelChosen -> "Motor seçeneği seçin"
                else -> "Önce marka ve model seçin"
            },
            leadingIcon = Icons.Default.Tune,
            buttonTestTag = "select_engine_button",
            enabled = isModelChosen && effectiveEngineOptions.isNotEmpty(),
            isLoading = isModelChosen && isLoadingEngines,
            isError = engineMissingError,
            errorMessage = if (engineMissingError) stringResource(R.string.engine_missing_error) else null,
            badgeText = selectedEngineObj?.fuelType,
            onClick = { showEngineModal = true }
        )
    }

    if (showBrandModal) {
        BrandPickerModalDialog(
            brands = catalog.brands,
            selectedBrand = selectedBrand,
            onBrandPicked = { picked ->
                onBrandSelected(picked)
                showBrandModal = false
            },
            onDismiss = { showBrandModal = false }
        )
    }

    if (showModelModal && selectedBrand.isNotBlank()) {
        ModelPickerModalDialog(
            brandName = selectedBrand,
            models = modelsForSelectedBrand,
            selectedModel = selectedModel,
            onModelPicked = { pickedModel ->
                onModelSelected(pickedModel)
                showModelModal = false
            },
            onDismiss = { showModelModal = false }
        )
    }

    if (showEngineModal && selectedBrand.isNotBlank() && selectedModel.isNotBlank()) {
        EnginePickerModalDialog(
            brandName = selectedBrand,
            modelName = selectedModel,
            engines = effectiveEngineOptions,
            selectedEngine = selectedEngine,
            onEnginePicked = { pickedEngine ->
                onEngineSelected(pickedEngine)
                showEngineModal = false
            },
            onDismiss = { showEngineModal = false }
        )
    }
}

@Composable
fun BrandPickerModalDialog(
    brands: List<VehicleBrandItem>,
    selectedBrand: String,
    onBrandPicked: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val filteredBrands = remember(brands, searchQuery) {
        val normQuery = normalizeSearchText(searchQuery.trim())
        if (normQuery.isEmpty()) {
            brands
        } else {
            brands.filter { item ->
                normalizeSearchText(item.brand).contains(normQuery) ||
                    item.models.any { m -> normalizeSearchText(m).contains(normQuery) }
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
                    .shadow(16.dp, RoundedCornerShape(26.dp), spotColor = TextDark.copy(alpha = 0.18f))
                    .testTag("brand_picker_modal"),
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
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "ARAÇ MARKASI SEÇİN",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = PrimaryVibrant,
                                letterSpacing = 0.6.sp
                            )
                            Text(
                                text = "Toplam ${brands.size} Marka Mevcut",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = TextDark
                            )
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(44.dp)
                                .background(BgLightAlt, CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Kapat",
                                tint = TextDark,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    // Search Field
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Marka veya model ara (Örn: Fiat, Renault, Togg)...") },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = null, tint = PrimaryVibrant)
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
                            focusedBorderColor = PrimaryVibrant,
                            unfocusedContainerColor = BgLight,
                            focusedContainerColor = SurfaceWhite
                        )
                    )

                    // Scrollable Brand List
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredBrands, key = { it.brand }) { item ->
                            val isSelected = item.brand.equals(selectedBrand, ignoreCase = true)
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable { onBrandPicked(item.brand) },
                                shape = RoundedCornerShape(14.dp),
                                color = if (isSelected) PrimarySoft else BgLight,
                                border = BorderStroke(
                                    1.dp,
                                    if (isSelected) PrimaryVibrant else BorderLight
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .background(
                                                if (isSelected) PrimaryVibrant else SurfaceWhite,
                                                CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = item.brand.take(2).uppercase(Locale.forLanguageTag("tr-TR")),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Black,
                                            color = if (isSelected) Color.White else PrimaryVibrant
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text(
                                                text = item.brand,
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = TextDark
                                            )
                                            if (item.isPopularInTR) {
                                                Surface(
                                                    shape = RoundedCornerShape(6.dp),
                                                    color = SurfaceWhite
                                                ) {
                                                    Text(
                                                        text = "Popüler",
                                                        fontSize = 9.5.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = PrimaryVibrant,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }
                                        }
                                        Text(
                                            text = item.models.take(4).joinToString(", ") +
                                                if (item.models.size > 4) "..." else "",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = TextMedium,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))

                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isSelected) PrimaryVibrant else SurfaceWhite
                                    ) {
                                        Text(
                                            text = "${item.models.size} model",
                                            fontSize = 10.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) Color.White else TextMedium,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                        }

                        if (searchQuery.trim().isNotEmpty() &&
                            filteredBrands.none { it.brand.equals(searchQuery.trim(), ignoreCase = true) }
                        ) {
                            item {
                                OutlinedButton(
                                    onClick = { onBrandPicked(searchQuery.trim()) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp),
                                    shape = RoundedCornerShape(14.dp),
                                    border = BorderStroke(1.dp, PrimaryVibrant)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, tint = PrimaryVibrant)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "\"${searchQuery.trim()}\" markasını ekle",
                                        fontWeight = FontWeight.Bold,
                                        color = PrimaryVibrant
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

@Composable
fun ModelPickerModalDialog(
    brandName: String,
    models: List<String>,
    selectedModel: String,
    onModelPicked: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val filteredModels = remember(models, searchQuery) {
        val normQuery = normalizeSearchText(searchQuery.trim())
        if (normQuery.isEmpty()) {
            models
        } else {
            models.filter { normalizeSearchText(it).contains(normQuery) }
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
                    .fillMaxHeight(0.82f)
                    .shadow(16.dp, RoundedCornerShape(26.dp), spotColor = TextDark.copy(alpha = 0.18f))
                    .testTag("model_picker_modal"),
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
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${brandName.uppercase(Locale.forLanguageTag("tr-TR"))} MODELLERİ",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = PrimaryVibrant,
                                letterSpacing = 0.6.sp
                            )
                            Text(
                                text = "Model Seçin (${models.size} Model)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = TextDark
                            )
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(44.dp)
                                .background(BgLightAlt, CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Kapat",
                                tint = TextDark,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    // Search Field
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("$brandName modeli ara...") },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = null, tint = PrimaryVibrant)
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
                            focusedBorderColor = PrimaryVibrant,
                            unfocusedContainerColor = BgLight,
                            focusedContainerColor = SurfaceWhite
                        )
                    )

                    // Models List
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredModels, key = { it }) { modelItem ->
                            val isSelected = modelItem.equals(selectedModel, ignoreCase = true)
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable { onModelPicked(modelItem) },
                                shape = RoundedCornerShape(14.dp),
                                color = if (isSelected) PrimarySoft else BgLight,
                                border = BorderStroke(
                                    1.dp,
                                    if (isSelected) PrimaryVibrant else BorderLight
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 13.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.DirectionsCar,
                                            contentDescription = null,
                                            tint = if (isSelected) PrimaryVibrant else TextMedium,
                                            modifier = Modifier.size(19.dp)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(
                                            text = "$brandName $modelItem",
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                            color = TextDark
                                        )
                                    }

                                    if (isSelected) {
                                        Box(
                                            modifier = Modifier
                                                .size(24.dp)
                                                .background(PrimaryVibrant, CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "Seçili",
                                                tint = Color.White,
                                                modifier = Modifier.size(15.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        if (searchQuery.trim().isNotEmpty() &&
                            filteredModels.none { it.equals(searchQuery.trim(), ignoreCase = true) }
                        ) {
                            item {
                                OutlinedButton(
                                    onClick = { onModelPicked(searchQuery.trim()) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp),
                                    shape = RoundedCornerShape(14.dp),
                                    border = BorderStroke(1.dp, PrimaryVibrant)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, tint = PrimaryVibrant)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "\"${searchQuery.trim()}\" modelini özel olarak seç",
                                        fontWeight = FontWeight.Bold,
                                        color = PrimaryVibrant
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
