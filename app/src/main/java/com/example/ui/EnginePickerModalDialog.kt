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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocalGasStation
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
import com.example.data.VehicleEngineOption
import com.example.ui.theme.*
import java.util.Locale

/**
 * Strictly selectable Engine Options modal dialog for the chosen Brand & Model.
 * Does NOT allow free-text package/trim entry; only vehicle-matched engine options are selectable.
 */
@Composable
fun EnginePickerModalDialog(
    brandName: String,
    modelName: String,
    engines: List<VehicleEngineOption>,
    selectedEngine: String,
    onEnginePicked: (VehicleEngineOption) -> Unit,
    onDismiss: () -> Unit
) {
    val trLocale = Locale.forLanguageTag("tr-TR")
    var searchQuery by remember { mutableStateOf("") }

    val filteredEngines = remember(engines, searchQuery) {
        val normQuery = normalizeSearchText(searchQuery.trim())
        if (normQuery.isEmpty()) {
            engines
        } else {
            engines.filter { item ->
                normalizeSearchText(item.engineName).contains(normQuery) ||
                    normalizeSearchText(item.fuelType).contains(normQuery) ||
                    normalizeSearchText(item.specSummary).contains(normQuery)
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
                    .fillMaxHeight(0.80f)
                    .shadow(16.dp, RoundedCornerShape(26.dp), spotColor = TextDark.copy(alpha = 0.18f))
                    .testTag("engine_picker_modal"),
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
                                text = "${brandName.uppercase(trLocale)} ${modelName.uppercase(trLocale)} • ${stringResource(R.string.engine_modal_header_eyebrow)}",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = PrimaryVibrant,
                                letterSpacing = 0.5.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "Motor Seçeneği Seçin (${engines.size})",
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
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Kapat",
                                tint = TextDark,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    // Search Field (filters existing vehicle engine options only)
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("$modelName motor seçeneği ara...") },
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
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("engine_search_input"),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryVibrant,
                            unfocusedContainerColor = BgLight,
                            focusedContainerColor = SurfaceWhite
                        )
                    )

                    // Selectable Engine Options List
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredEngines, key = { "${it.engineName}_${it.fuelType}" }) { option ->
                            val isSelected = option.engineName.equals(selectedEngine, ignoreCase = true)
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable { onEnginePicked(option) }
                                    .testTag("engine_option_item"),
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
                                        .padding(horizontal = 14.dp, vertical = 12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(38.dp)
                                                .background(
                                                    if (isSelected) SurfaceWhite else SurfaceWhite.copy(alpha = 0.85f),
                                                    RoundedCornerShape(10.dp)
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Tune,
                                                contentDescription = null,
                                                tint = if (isSelected) PrimaryVibrant else TextMedium,
                                                modifier = Modifier.size(19.dp)
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(12.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = option.engineName,
                                                style = MaterialTheme.typography.bodyLarge,
                                                fontWeight = FontWeight.Bold,
                                                color = TextDark,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            if (option.specSummary.isNotBlank()) {
                                                Text(
                                                    text = option.specSummary,
                                                    fontSize = 11.5.sp,
                                                    color = TextMedium,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))

                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = FuelSoft,
                                        border = BorderStroke(1.dp, FuelBorder)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.LocalGasStation,
                                                contentDescription = null,
                                                tint = FuelColorDark,
                                                modifier = Modifier.size(12.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = option.fuelType,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = FuelColorDark
                                            )
                                        }
                                    }

                                    if (isSelected) {
                                        Spacer(modifier = Modifier.width(8.dp))
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

                        if (filteredEngines.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 28.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Aramanızla eşleşen motor seçeneği bulunamadı.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TextMedium
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
