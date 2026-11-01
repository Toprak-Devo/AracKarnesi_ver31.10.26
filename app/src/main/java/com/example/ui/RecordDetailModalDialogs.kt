package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.ExpenseRecord
import com.example.data.FuelRecord
import com.example.data.MaintenanceRecord
import com.example.data.Reminder
import com.example.data.Vehicle
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs

@Composable
private fun BaseDetailModalContainer(
    testTag: String,
    onDismiss: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
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
                .padding(horizontal = 16.dp, vertical = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 500.dp)
                    .shadow(18.dp, RoundedCornerShape(26.dp), spotColor = TextDark.copy(alpha = 0.24f))
                    .testTag(testTag),
                shape = RoundedCornerShape(26.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                border = BorderStroke(1.dp, BorderLight)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    content = content
                )
            }
        }
    }
}

@Composable
private fun ModalHeaderRow(
    icon: ImageVector,
    accentColor: Color,
    softBg: Color,
    eyebrow: String,
    title: String,
    onDismiss: () -> Unit
) {
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
                    .size(46.dp)
                    .background(softBg, RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = eyebrow.uppercase(Locale.forLanguageTag("tr-TR")),
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = accentColor,
                    letterSpacing = 0.6.sp
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = TextDark,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
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
}

@Composable
private fun ModalMetricTile(
    label: String,
    value: String,
    valueColor: Color = TextDark,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = BgLight,
        border = BorderStroke(1.dp, BorderLight)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = TextMedium,
                letterSpacing = 0.4.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = value,
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Black,
                color = valueColor,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// -----------------------------------------------------------------------------
// 1. BAKIM DETAY MODALI
// -----------------------------------------------------------------------------
@Composable
fun MaintenanceDetailModalDialog(
    record: MaintenanceRecord,
    onDeleteRequest: () -> Unit,
    onDismiss: () -> Unit
) {
    val trLocale = Locale.forLanguageTag("tr-TR")
    val partsList = remember(record.type) {
        record.type.split(",").map { it.trim() }.filter { it.isNotEmpty() }
    }
    val categories = remember(record) { getMaintenanceCategories(record) }
    val isRepair = MaintenanceFilterType.REPAIR in categories
    val accentColor = if (isRepair) MaintenanceRed else PrimaryVibrant
    val softBg = if (isRepair) MaintenanceRedSoft else PrimarySoft

    BaseDetailModalContainer(
        testTag = "maintenance_detail_modal",
        onDismiss = onDismiss
    ) {
        ModalHeaderRow(
            icon = if (isRepair) Icons.Default.Handyman else Icons.Default.BuildCircle,
            accentColor = accentColor,
            softBg = softBg,
            eyebrow = if (isRepair) "Bakım & Onarım Detayı" else "Periyodik Bakım Detayı",
            title = partsList.firstOrNull() ?: record.type,
            onDismiss = onDismiss
        )

        // Key Metrics Strip
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ModalMetricTile(
                label = "TARİH",
                value = SimpleDateFormat("dd MMM yyyy", trLocale).format(Date(record.date)),
                modifier = Modifier.weight(1f)
            )
            ModalMetricTile(
                label = "KİLOMETRE",
                value = String.format(trLocale, "%,d km", record.km),
                valueColor = PrimaryVibrant,
                modifier = Modifier.weight(1f)
            )
            ModalMetricTile(
                label = "MALİYET",
                value = if (record.cost > 0) String.format(trLocale, "%,.0f ₺", record.cost) else "Belirtilmedi",
                valueColor = TotalCostGreenDark,
                modifier = Modifier.weight(1f)
            )
        }

        // Operations / Replaced Parts List
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = BgLight,
            border = BorderStroke(1.dp, BorderLight),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "YAPILAN İŞLEMLER & DEĞİŞEN PARÇALAR (${partsList.size})",
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextMedium,
                    letterSpacing = 0.5.sp
                )
                partsList.forEach { part ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .size(22.dp)
                                .background(PrimarySoft, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = PrimaryVibrant,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = part,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = TextDark
                        )
                    }
                }
            }
        }

        // Service & Mechanic Note
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = SurfaceWhite,
            border = BorderStroke(1.dp, BorderLight),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Description,
                        contentDescription = null,
                        tint = SecondaryVibrant,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "SERVİS & USTA NOTU",
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = SecondaryVibrant,
                        letterSpacing = 0.5.sp
                    )
                }
                Text(
                    text = record.description.ifBlank { "Bu bakım kaydı için ek açıklama girilmemiş." },
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (record.description.isNotBlank()) TextDark else TextMedium,
                    lineHeight = 20.sp
                )
            }
        }

        // Footer Actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(
                onClick = {
                    onDismiss()
                    onDeleteRequest()
                },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, AccentRose.copy(alpha = 0.45f)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentRose)
            ) {
                Icon(Icons.Outlined.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Kaydı Sil", fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = onDismiss,
                modifier = Modifier
                    .weight(1.2f)
                    .height(48.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryVibrant,
                    contentColor = Color.White
                )
            ) {
                Text("Kapat", fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}

// -----------------------------------------------------------------------------
// 2. GİDER / SİGORTA DETAY MODALI
// -----------------------------------------------------------------------------
@Composable
fun ExpenseDetailModalDialog(
    record: ExpenseRecord,
    onDeleteRequest: () -> Unit,
    onDismiss: () -> Unit
) {
    val trLocale = Locale.forLanguageTag("tr-TR")

    BaseDetailModalContainer(
        testTag = "expense_detail_modal",
        onDismiss = onDismiss
    ) {
        ModalHeaderRow(
            icon = Icons.Default.Shield,
            accentColor = InsuranceBlue,
            softBg = InsuranceBlueSoft,
            eyebrow = "Sigorta & Gider Detayı",
            title = record.category,
            onDismiss = onDismiss
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ModalMetricTile(
                label = "İŞLEM TARİHİ",
                value = SimpleDateFormat("dd MMMM yyyy", trLocale).format(Date(record.date)),
                modifier = Modifier.weight(1f)
            )
            ModalMetricTile(
                label = "ÖDENEN TUTAR",
                value = String.format(trLocale, "%,.0f ₺", record.amount),
                valueColor = InsuranceBlue,
                modifier = Modifier.weight(1f)
            )
        }

        Surface(
            shape = RoundedCornerShape(16.dp),
            color = BgLight,
            border = BorderStroke(1.dp, BorderLight),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "POLİÇE & GİDER AÇIKLAMASI",
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextMedium,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = record.description.ifBlank { "Ek açıklama belirtilmedi." },
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextDark,
                    lineHeight = 20.sp
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(
                onClick = {
                    onDismiss()
                    onDeleteRequest()
                },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, AccentRose.copy(alpha = 0.45f)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentRose)
            ) {
                Icon(Icons.Outlined.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Kaydı Sil", fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = onDismiss,
                modifier = Modifier
                    .weight(1.2f)
                    .height(48.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryVibrant,
                    contentColor = Color.White
                )
            ) {
                Text("Kapat", fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}

// -----------------------------------------------------------------------------
// 3. YAKIT DOLUM DETAY MODALI
// -----------------------------------------------------------------------------
@Composable
fun FuelDetailModalDialog(
    record: FuelRecord,
    drivenKmDelta: Int?,
    onDeleteRequest: () -> Unit,
    onDismiss: () -> Unit
) {
    val trLocale = Locale.forLanguageTag("tr-TR")
    val tripConsumption = if (drivenKmDelta != null && drivenKmDelta > 0 && record.liters > 0.0) {
        (record.liters / drivenKmDelta.toDouble()) * 100.0
    } else {
        null
    }

    BaseDetailModalContainer(
        testTag = "fuel_detail_modal",
        onDismiss = onDismiss
    ) {
        ModalHeaderRow(
            icon = Icons.Default.LocalGasStation,
            accentColor = FuelColorDark,
            softBg = FuelSoft,
            eyebrow = "Akaryakıt Dolum Detayı",
            title = SimpleDateFormat("dd MMMM yyyy", trLocale).format(Date(record.date)),
            onDismiss = onDismiss
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ModalMetricTile(
                label = "ALINAN YAKIT",
                value = String.format(trLocale, "%.2f Lt", record.liters),
                modifier = Modifier.weight(1f)
            )
            ModalMetricTile(
                label = "BİRİM FİYAT",
                value = formatUnitPriceDotTl(record.price),
                modifier = Modifier.weight(1f)
            )
            ModalMetricTile(
                label = "TOPLAM TUTAR",
                value = String.format(trLocale, "%,.0f ₺", record.total),
                valueColor = FuelColorDark,
                modifier = Modifier.weight(1f)
            )
        }

        Surface(
            shape = RoundedCornerShape(16.dp),
            color = FuelSoft,
            border = BorderStroke(1.dp, FuelBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Kayıtlı Güncel Kilometre", style = MaterialTheme.typography.bodyMedium, color = TextDark)
                    Text(
                        text = if (record.km > 0) String.format(trLocale, "%,d km", record.km) else "-",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Black,
                        color = TextDark
                    )
                }

                if (drivenKmDelta != null && drivenKmDelta > 0) {
                    HorizontalDivider(color = FuelBorder, thickness = 0.8.dp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Önceki Kayıttan Yapılan Yol", style = MaterialTheme.typography.bodyMedium, color = TextDark)
                        Text(
                            text = String.format(trLocale, "+%,d km", drivenKmDelta),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Black,
                            color = FuelColorDark
                        )
                    }
                }

                if (tripConsumption != null) {
                    HorizontalDivider(color = FuelBorder, thickness = 0.8.dp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Bu Dolum Tüketim Ortalaması", style = MaterialTheme.typography.bodyMedium, color = TextDark)
                        Text(
                            text = String.format(trLocale, "%.2f Lt / 100 km", tripConsumption),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Black,
                            color = FuelColorDark
                        )
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(
                onClick = {
                    onDismiss()
                    onDeleteRequest()
                },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, AccentRose.copy(alpha = 0.45f)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentRose)
            ) {
                Icon(Icons.Outlined.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Kaydı Sil", fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = onDismiss,
                modifier = Modifier
                    .weight(1.2f)
                    .height(48.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = FuelColor,
                    contentColor = Color.White
                )
            ) {
                Text("Kapat", fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}

// -----------------------------------------------------------------------------
// 4. HATIRLATICI DETAY MODALI
// -----------------------------------------------------------------------------
@Composable
fun ReminderDetailModalDialog(
    reminder: Reminder,
    onTestAlarm: () -> Unit,
    onDeleteRequest: () -> Unit,
    onDismiss: () -> Unit
) {
    val trLocale = Locale.forLanguageTag("tr-TR")
    val now = System.currentTimeMillis()
    val isPastDue = now > reminder.dueDate
    val daysUntil = ((reminder.dueDate - now) / (1000 * 60 * 60 * 24)).toInt()

    val (statusLabel, statusColor, statusBg) = when {
        isPastDue -> Triple("Tarihi Geçti (${abs(daysUntil)} gün önce)", AccentRose, AccentRoseSoft)
        daysUntil == 0 -> Triple("Bugün Son Gün!", AccentRose, AccentRoseSoft)
        daysUntil <= 7 -> Triple("Yaklaşıyor ($daysUntil gün kaldı)", ReminderColor, ReminderSoft)
        else -> Triple("Planlandı ($daysUntil gün kaldı)", ReminderColor, ReminderSoft)
    }

    BaseDetailModalContainer(
        testTag = "reminder_detail_modal",
        onDismiss = onDismiss
    ) {
        ModalHeaderRow(
            icon = if (isPastDue) Icons.Default.NotificationImportant else Icons.Default.NotificationsActive,
            accentColor = statusColor,
            softBg = statusBg,
            eyebrow = "Hatırlatıcı & Alarm Detayı",
            title = reminder.title,
            onDismiss = onDismiss
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ModalMetricTile(
                label = "HEDEF TARİH",
                value = SimpleDateFormat("dd MMMM yyyy", trLocale).format(Date(reminder.dueDate)),
                modifier = Modifier.weight(1f)
            )
            ModalMetricTile(
                label = "DURUM",
                value = statusLabel,
                valueColor = statusColor,
                modifier = Modifier.weight(1f)
            )
        }

        OutlinedButton(
            onClick = onTestAlarm,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, ReminderColor.copy(alpha = 0.45f)),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = ReminderColor)
        ) {
            Icon(Icons.Default.NotificationsActive, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Anlık Bildirim Testi Gönder", fontWeight = FontWeight.Bold)
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(
                onClick = {
                    onDismiss()
                    onDeleteRequest()
                },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, AccentRose.copy(alpha = 0.45f)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentRose)
            ) {
                Icon(Icons.Outlined.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Sil", fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = onDismiss,
                modifier = Modifier
                    .weight(1.2f)
                    .height(48.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ReminderColor,
                    contentColor = Color.White
                )
            ) {
                Text("Kapat", fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}

// -----------------------------------------------------------------------------
// 5. ARAÇ GENEL MALİYET & KARNE ÖZETİ MODALI
// -----------------------------------------------------------------------------
@Composable
fun VehicleOverviewModalDialog(
    vehicle: Vehicle,
    latestKm: Int,
    maintenanceRecords: List<MaintenanceRecord>,
    expenseRecords: List<ExpenseRecord>,
    fuelRecords: List<FuelRecord>,
    onOpenExportReport: () -> Unit,
    onDismiss: () -> Unit
) {
    val trLocale = Locale.forLanguageTag("tr-TR")
    val totalMaint = remember(maintenanceRecords) { maintenanceRecords.sumOf { it.cost } }
    val totalExpense = remember(expenseRecords) { expenseRecords.sumOf { it.amount } }
    val totalFuel = remember(fuelRecords) { fuelRecords.sumOf { it.total } }
    val grandTotal = totalMaint + totalExpense + totalFuel

    val breakdownItems = remember(totalMaint, totalExpense, totalFuel) {
        listOf(
            Triple("Bakım & Onarım (${maintenanceRecords.size} kayıt)", totalMaint, MaintenanceRed),
            Triple("Sigorta & Genel Gider (${expenseRecords.size} kayıt)", totalExpense, InsuranceBlue),
            Triple("Akaryakıt Harcaması (${fuelRecords.size} dolum)", totalFuel, FuelColor)
        )
    }

    BaseDetailModalContainer(
        testTag = "vehicle_overview_modal",
        onDismiss = onDismiss
    ) {
        ModalHeaderRow(
            icon = Icons.Default.Analytics,
            accentColor = PrimaryVibrant,
            softBg = PrimarySoft,
            eyebrow = "${vehicle.plate} • Karne Özeti",
            title = "${vehicle.brand} ${vehicle.model} (${vehicle.year})",
            onDismiss = onDismiss
        )

        // Bright, Airy Grand Total Banner (Lightened instead of dark slate)
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = PrimarySoft,
            border = BorderStroke(1.dp, PrimaryVibrant.copy(alpha = 0.28f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            listOf(PrimarySoft, SecondarySoft)
                        )
                    )
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "TOPLAM KÜMÜLATİF HARCAMA",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = PrimaryVibrantDark,
                        letterSpacing = 0.6.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = String.format(trLocale, "%,.0f ₺", grandTotal),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Black,
                        color = TextDark
                    )
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = SurfaceWhite,
                    border = BorderStroke(1.dp, PrimaryVibrant.copy(alpha = 0.25f))
                ) {
                    Text(
                        text = if (latestKm > 0) String.format(trLocale, "%,d km", latestKm) else vehicle.fuelType,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryVibrant,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        }

        // Category Breakdown Progress Rows
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            breakdownItems.forEach { (label, amount, barColor) ->
                val ratio = if (grandTotal > 0.0) (amount / grandTotal).toFloat().coerceIn(0f, 1f) else 0f
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = BgLight,
                    border = BorderStroke(1.dp, BorderLight),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = TextDark,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = String.format(trLocale, "%,.0f ₺", amount),
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Black,
                                color = barColor
                            )
                        }
                        LinearProgressIndicator(
                            progress = { ratio },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp),
                            color = barColor,
                            trackColor = SurfaceWhite
                        )
                    }
                }
            }
        }

        // Actions: Export PDF Report or Close
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(
                onClick = {
                    onDismiss()
                    onOpenExportReport()
                },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, PrimaryVibrant.copy(alpha = 0.45f)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryVibrant)
            ) {
                Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("PDF Rapor", fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = onDismiss,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryVibrant,
                    contentColor = Color.White
                )
            ) {
                Text("Tamam", fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}
