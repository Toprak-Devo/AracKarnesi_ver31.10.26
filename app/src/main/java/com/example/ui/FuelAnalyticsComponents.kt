package com.example.ui

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.FuelRecord
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class FuelChartMetric(val label: String) {
    LITERS("Tüketim (Lt)"),
    COST_TL("Maliyet (₺)")
}

data class MonthlyFuelPoint(
    val monthKey: String,
    val shortLabel: String,
    val fullLabel: String,
    val totalLiters: Double,
    val totalCostTl: Double,
    val avgPricePerLiter: Double,
    val fillCount: Int
)

data class FuelAnalyticsSummary(
    val monthlyPoints: List<MonthlyFuelPoint>,
    val avgLitersPer100Km: Double?,
    val avgMonthlyLiters: Double,
    val avgLitersPerFill: Double,
    val avgMonthlyCostTl: Double,
    val avgCostPerKmTl: Double?,
    val liveCostPerKmTl: Double?,
    val totalLitersAllTime: Double,
    val totalCostAllTimeTl: Double,
    val totalTrackedKm: Int
)

fun computeFuelAnalytics(
    records: List<FuelRecord>,
    liveUnitPriceTl: Double?
): FuelAnalyticsSummary {
    val trLocale = Locale.forLanguageTag("tr-TR")
    val shortFmt = SimpleDateFormat("MMM", trLocale)
    val fullFmt = SimpleDateFormat("MMMM yyyy", trLocale)

    val latestMillis = records.maxOfOrNull { it.date } ?: System.currentTimeMillis()
    val groupedByMonth = records.groupBy { rec ->
        val cal = Calendar.getInstance(trLocale).apply { timeInMillis = rec.date }
        String.format(Locale.US, "%04d-%02d", cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1)
    }

    val monthlyPoints = (5 downTo 0).map { offset ->
        val cal = Calendar.getInstance(trLocale).apply {
            timeInMillis = latestMillis
            set(Calendar.DAY_OF_MONTH, 15)
            add(Calendar.MONTH, -offset)
        }
        val key = String.format(Locale.US, "%04d-%02d", cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1)
        val monthRecords = groupedByMonth[key].orEmpty()
        val liters = monthRecords.sumOf { it.liters }
        val cost = monthRecords.sumOf { it.total }
        val avgPrice = if (liters > 0.0) cost / liters else 0.0

        MonthlyFuelPoint(
            monthKey = key,
            shortLabel = shortFmt.format(Date(cal.timeInMillis))
                .replaceFirstChar { if (it.isLowerCase()) it.titlecase(trLocale) else it.toString() },
            fullLabel = fullFmt.format(Date(cal.timeInMillis))
                .replaceFirstChar { if (it.isLowerCase()) it.titlecase(trLocale) else it.toString() },
            totalLiters = liters,
            totalCostTl = cost,
            avgPricePerLiter = avgPrice,
            fillCount = monthRecords.size
        )
    }

    if (records.isEmpty()) {
        return FuelAnalyticsSummary(
            monthlyPoints = monthlyPoints,
            avgLitersPer100Km = null,
            avgMonthlyLiters = 0.0,
            avgLitersPerFill = 0.0,
            avgMonthlyCostTl = 0.0,
            avgCostPerKmTl = null,
            liveCostPerKmTl = null,
            totalLitersAllTime = 0.0,
            totalCostAllTimeTl = 0.0,
            totalTrackedKm = 0
        )
    }

    val totalLiters = records.sumOf { it.liters }
    val totalCost = records.sumOf { it.total }
    val activeMonthCount = groupedByMonth.keys.size.coerceAtLeast(1)
    val avgMonthlyLiters = totalLiters / activeMonthCount.toDouble()
    val avgMonthlyCostTl = totalCost / activeMonthCount.toDouble()
    val avgLitersPerFill = totalLiters / records.size.coerceAtLeast(1).toDouble()

    val byVehicle = records.filter { it.km > 0 }.groupBy { it.vehicleId }
    var sumDeltaKm = 0
    var sumConsumedLiters = 0.0
    var sumConsumedCost = 0.0

    byVehicle.values.forEach { vRecords ->
        val sorted = vRecords.sortedBy { it.km }
        if (sorted.size >= 2) {
            val deltaKm = sorted.last().km - sorted.first().km
            if (deltaKm > 0) {
                sumDeltaKm += deltaKm
                sumConsumedLiters += sorted.drop(1).sumOf { it.liters }
                sumConsumedCost += sorted.drop(1).sumOf { it.total }
            }
        }
    }

    val avgLtPer100Km = if (sumDeltaKm > 0) (sumConsumedLiters / sumDeltaKm.toDouble()) * 100.0 else null
    val avgCostPerKm = if (sumDeltaKm > 0) sumConsumedCost / sumDeltaKm.toDouble() else null
    val liveCostPerKm = if (avgLtPer100Km != null && liveUnitPriceTl != null && liveUnitPriceTl > 0.0) {
        (avgLtPer100Km / 100.0) * liveUnitPriceTl
    } else {
        null
    }

    return FuelAnalyticsSummary(
        monthlyPoints = monthlyPoints,
        avgLitersPer100Km = avgLtPer100Km,
        avgMonthlyLiters = avgMonthlyLiters,
        avgLitersPerFill = avgLitersPerFill,
        avgMonthlyCostTl = avgMonthlyCostTl,
        avgCostPerKmTl = avgCostPerKm,
        liveCostPerKmTl = liveCostPerKm,
        totalLitersAllTime = totalLiters,
        totalCostAllTimeTl = totalCost,
        totalTrackedKm = sumDeltaKm
    )
}

@Composable
private fun FuelAverageHighlightsRow(
    summary: FuelAnalyticsSummary,
    recordCount: Int,
    trLocale: Locale
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // ORTALAMA TÜKETİM CARD
        Card(
            modifier = Modifier
                .weight(1f)
                .shadow(2.dp, RoundedCornerShape(20.dp), spotColor = FuelColor.copy(alpha = 0.08f))
                .testTag("avg_fuel_consumption_card"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
            border = BorderStroke(1.dp, FuelBorder)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .background(FuelSoft, RoundedCornerShape(9.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Speed,
                            contentDescription = null,
                            tint = FuelColorDark,
                            modifier = Modifier.size(17.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "ORTALAMA TÜKETİM",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = FuelColorDark,
                        letterSpacing = 0.4.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                val mainConsumptionText = if (summary.avgLitersPer100Km != null) {
                    String.format(trLocale, "%.2f Lt", summary.avgLitersPer100Km)
                } else {
                    String.format(trLocale, "%.1f Lt", summary.avgMonthlyLiters)
                }
                val mainUnitLabel = if (summary.avgLitersPer100Km != null) "/ 100 km'de net" else "/ Aylık ortalama"

                Text(
                    text = mainConsumptionText,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Black,
                    color = TextDark,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = mainUnitLabel,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = FuelColorDark,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = BorderLight, thickness = 0.8.dp)
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = String.format(trLocale, "Aylık Ort: %.1f Lt / Ay", summary.avgMonthlyLiters),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = String.format(trLocale, "Dolum Ort: %.1f Lt", summary.avgLitersPerFill),
                    fontSize = 10.5.sp,
                    color = TextMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // ORTALAMA TL CARD
        Card(
            modifier = Modifier
                .weight(1f)
                .shadow(2.dp, RoundedCornerShape(20.dp), spotColor = FuelColor.copy(alpha = 0.08f))
                .testTag("avg_fuel_tl_card"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
            border = BorderStroke(1.dp, FuelBorder)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .background(FuelSoft, RoundedCornerShape(9.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Payments,
                            contentDescription = null,
                            tint = FuelColorDark,
                            modifier = Modifier.size(17.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "ORTALAMA TL",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = FuelColorDark,
                        letterSpacing = 0.4.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = String.format(trLocale, "%,.0f ₺", summary.avgMonthlyCostTl),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Black,
                    color = TextDark,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "/ Aylık net yakıt tutarı",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = FuelColorDark,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = BorderLight, thickness = 0.8.dp)
                Spacer(modifier = Modifier.height(6.dp))
                val perKmText = when {
                    summary.avgCostPerKmTl != null -> "KM Başına: ${formatDecimalTl(summary.avgCostPerKmTl)}/km"
                    recordCount > 0 -> "Dolum Ort: ${formatCurrencyTl(summary.totalCostAllTimeTl / recordCount)}"
                    else -> "KM Başına: 0.00 TL/km"
                }
                Text(
                    text = perKmText,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                val liveEstText = if (summary.liveCostPerKmTl != null) {
                    "Güncel Pompa: ${formatDecimalTl(summary.liveCostPerKmTl)}/km"
                } else {
                    "Toplam: ${formatCurrencyTl(summary.totalCostAllTimeTl)}"
                }
                Text(
                    text = liveEstText,
                    fontSize = 10.5.sp,
                    color = FuelColorDark,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun MonthlyFuelChartSection(
    fuelRecords: List<FuelRecord>,
    liveUnitPriceTl: Double?,
    sectionTitle: String = "AYLIK YAKIT TÜKETİM & MALİYET ANALİZİ",
    onAddFuelClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val trLocale = Locale.forLanguageTag("tr-TR")
    val summary = remember(fuelRecords, liveUnitPriceTl) {
        computeFuelAnalytics(fuelRecords, liveUnitPriceTl)
    }

    var selectedMetric by remember { mutableStateOf(FuelChartMetric.LITERS) }
    var selectedMonthIndex by remember(summary.monthlyPoints) {
        val lastNonZero = summary.monthlyPoints.indexOfLast { it.totalLiters > 0.0 }
        mutableIntStateOf(if (lastNonZero >= 0) lastNonZero else summary.monthlyPoints.lastIndex)
    }

    val animProgress by animateFloatAsState(
        targetValue = if (fuelRecords.isNotEmpty()) 1f else 0.15f,
        animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing),
        label = "fuelChartAnim"
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        FuelAverageHighlightsRow(
            summary = summary,
            recordCount = fuelRecords.size,
            trLocale = trLocale
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(4.dp, RoundedCornerShape(22.dp), spotColor = PrimaryVibrant.copy(alpha = 0.12f))
                .testTag("monthly_fuel_chart_card"),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
            border = BorderStroke(1.dp, BorderLight)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = sectionTitle,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Black,
                            color = TextDark,
                            letterSpacing = 0.4.sp
                        )
                        Text(
                            text = "Son 6 aylık yakıt eğilimi • Detay için aya dokunun",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMedium,
                            fontSize = 11.sp
                        )
                    }

                    if (onAddFuelClick != null) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = onAddFuelClick,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = FuelColor,
                                contentColor = Color.White
                            ),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                            modifier = Modifier
                                .heightIn(min = 48.dp)
                                .testTag("chart_add_fuel_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Yakıt Ekle",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Metric Switcher (Litre vs TL) - 48dp accessible touch target
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(BgLightAlt, RoundedCornerShape(12.dp))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FuelChartMetric.entries.forEach { metric ->
                        val isSelected = selectedMetric == metric
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 48.dp)
                                .clip(RoundedCornerShape(9.dp))
                                .background(if (isSelected) SurfaceWhite else Color.Transparent)
                                .then(
                                    if (isSelected) Modifier.border(1.dp, FuelBorder, RoundedCornerShape(9.dp))
                                    else Modifier
                                )
                                .clickable { selectedMetric = metric }
                                .padding(vertical = 8.dp)
                                .testTag("chart_metric_${metric.name.lowercase()}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = metric.label,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) FuelColorDark else TextMedium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                val safeIndex = selectedMonthIndex.coerceIn(0, summary.monthlyPoints.lastIndex)
                val activePoint = summary.monthlyPoints[safeIndex]
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = FuelSoft,
                    border = BorderStroke(
                        1.dp,
                        FuelBorder
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 8.dp)
                        ) {
                            Text(
                                text = activePoint.fullLabel.uppercase(trLocale),
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = FuelColorDark,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = if (activePoint.fillCount > 0) {
                                    "${activePoint.fillCount} Dolum • Ort. ${formatUnitPriceDotTl(activePoint.avgPricePerLiter)}"
                                } else {
                                    "Bu ayda yakıt kaydı bulunmuyor"
                                },
                                fontSize = 11.sp,
                                color = TextDark.copy(alpha = 0.78f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = String.format(trLocale, "%.1f Lt", activePoint.totalLiters),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Black,
                                color = TextDark,
                                maxLines = 1,
                                softWrap = false
                            )
                            Text(
                                text = String.format(trLocale, "%,.0f ₺", activePoint.totalCostTl),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = FuelColorDark,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                val values = summary.monthlyPoints.map {
                    if (selectedMetric == FuelChartMetric.LITERS) it.totalLiters else it.totalCostTl
                }
                val avgRefValue = if (selectedMetric == FuelChartMetric.LITERS) {
                    summary.avgMonthlyLiters
                } else {
                    summary.avgMonthlyCostTl
                }
                val rawMax = maxOf(values.maxOrNull() ?: 0.0, avgRefValue, 10.0)
                val yMax = rawMax * 1.22

                val primaryChartColor = FuelColor
                val secondaryChartColor = FuelBorder

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(190.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column(
                        modifier = Modifier
                            .width(52.dp)
                            .fillMaxHeight(),
                        verticalArrangement = Arrangement.SpaceBetween,
                        horizontalAlignment = Alignment.End
                    ) {
                        listOf(1.0, 0.75, 0.50, 0.25, 0.0).forEach { ratio ->
                            val tickVal = yMax * ratio
                            val tickText = if (selectedMetric == FuelChartMetric.LITERS) {
                                String.format(trLocale, "%.0f Lt", tickVal)
                            } else {
                                String.format(trLocale, "%,.0f₺", tickVal)
                            }
                            Text(
                                text = tickText,
                                fontSize = 9.5.sp,
                                color = TextLight,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    ) {
                        Canvas(
                            modifier = Modifier
                                .fillMaxSize()
                                .pointerInput(summary.monthlyPoints, selectedMetric) {
                                    detectTapGestures { tapOffset ->
                                        val count = summary.monthlyPoints.size
                                        if (count > 0 && size.width > 0) {
                                            val slotWidth = size.width.toFloat() / count
                                            val idx = (tapOffset.x / slotWidth).toInt().coerceIn(0, count - 1)
                                            selectedMonthIndex = idx
                                        }
                                    }
                                }
                        ) {
                            val canvasWidth = size.width
                            val canvasHeight = size.height
                            val count = values.size.coerceAtLeast(1)
                            val slotWidth = canvasWidth / count

                            val gridDash = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)
                            listOf(0f, 0.25f, 0.5f, 0.75f, 1f).forEach { frac ->
                                val y = canvasHeight * frac
                                drawLine(
                                    color = BorderLight,
                                    start = Offset(0f, y),
                                    end = Offset(canvasWidth, y),
                                    strokeWidth = 1.dp.toPx(),
                                    pathEffect = if (frac < 1f) gridDash else null
                                )
                            }

                            val barWidth = (slotWidth * 0.48f).coerceAtMost(34.dp.toPx())
                            val nodeCenters = mutableListOf<Offset>()

                            values.forEachIndexed { idx, v ->
                                val normalized = ((v / yMax).toFloat().coerceIn(0f, 1f)) * animProgress
                                val barHeight = (canvasHeight * normalized).coerceAtLeast(if (v > 0.0) 6.dp.toPx() else 2.dp.toPx())
                                val centerX = slotWidth * idx + slotWidth / 2f
                                val topY = canvasHeight - barHeight
                                nodeCenters.add(Offset(centerX, topY))

                                val isSelectedBar = idx == safeIndex
                                val barBrush = Brush.verticalGradient(
                                    colors = if (isSelectedBar) {
                                        listOf(
                                            primaryChartColor.copy(alpha = 0.82f),
                                            primaryChartColor.copy(alpha = 0.48f)
                                        )
                                    } else {
                                        listOf(
                                            primaryChartColor.copy(alpha = 0.36f),
                                            secondaryChartColor.copy(alpha = 0.55f)
                                        )
                                    },
                                    startY = topY,
                                    endY = canvasHeight
                                )

                                drawRoundRect(
                                    brush = barBrush,
                                    topLeft = Offset(centerX - barWidth / 2f, topY),
                                    size = Size(barWidth, barHeight),
                                    cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
                                )

                                if (isSelectedBar) {
                                    drawRoundRect(
                                        color = FuelColorDark,
                                        topLeft = Offset(centerX - barWidth / 2f, topY),
                                        size = Size(barWidth, barHeight),
                                        cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx()),
                                        style = Stroke(width = 1.6.dp.toPx())
                                    )
                                }
                            }

                            if (nodeCenters.size >= 2 && values.any { it > 0.0 }) {
                                val curvePath = Path().apply {
                                    moveTo(nodeCenters.first().x, nodeCenters.first().y)
                                    for (i in 0 until nodeCenters.lastIndex) {
                                        val p0 = nodeCenters[i]
                                        val p1 = nodeCenters[i + 1]
                                        val controlX = (p0.x + p1.x) / 2f
                                        cubicTo(controlX, p0.y, controlX, p1.y, p1.x, p1.y)
                                    }
                                }

                                drawPath(
                                    path = curvePath,
                                    color = primaryChartColor,
                                    style = Stroke(width = 2.4.dp.toPx(), cap = StrokeCap.Round)
                                )

                                nodeCenters.forEachIndexed { idx, pt ->
                                    val isSel = idx == safeIndex
                                    drawCircle(
                                        color = SurfaceWhite,
                                        radius = if (isSel) 5.5.dp.toPx() else 4.dp.toPx(),
                                        center = pt
                                    )
                                    drawCircle(
                                        color = if (isSel) FuelColorDark else primaryChartColor,
                                        radius = if (isSel) 4.dp.toPx() else 2.6.dp.toPx(),
                                        center = pt
                                    )
                                }
                            }

                            if (avgRefValue > 0.0) {
                                val avgNorm = ((avgRefValue / yMax).toFloat().coerceIn(0f, 1f)) * animProgress
                                val avgY = canvasHeight - (canvasHeight * avgNorm)
                                drawLine(
                                    color = FuelColor,
                                    start = Offset(0f, avgY),
                                    end = Offset(canvasWidth, avgY),
                                    strokeWidth = 2.dp.toPx(),
                                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 9f), 0f)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // X-Axis Interactive Month Pills (48dp height for touch accessibility)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 60.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    summary.monthlyPoints.forEachIndexed { idx, point ->
                        val isSelected = idx == safeIndex
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 48.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isSelected) FuelSoft else Color.Transparent
                                )
                                .then(
                                    if (isSelected) Modifier.border(1.dp, FuelBorder, RoundedCornerShape(8.dp))
                                    else Modifier
                                )
                                .clickable { selectedMonthIndex = idx }
                                .testTag("fuel_month_pill_${point.monthKey}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = point.shortLabel,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                                color = if (isSelected) FuelColorDark else TextMedium,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
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
                                .width(14.dp)
                                .height(3.dp)
                                .background(FuelColor, RoundedCornerShape(2.dp))
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        val avgLegendText = if (selectedMetric == FuelChartMetric.LITERS) {
                            String.format(trLocale, "Aylık Ortalama Çizgisi: %.1f Lt", summary.avgMonthlyLiters)
                        } else {
                            String.format(trLocale, "Aylık Ortalama Çizgisi: %,.0f ₺", summary.avgMonthlyCostTl)
                        }
                        Text(
                            text = avgLegendText,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    if (summary.totalTrackedKm > 0) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = String.format(trLocale, "Takip: %,d km", summary.totalTrackedKm),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = FuelColorDark,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
            }
        }
    }
}
