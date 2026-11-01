package com.example

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.FuelRecord
import com.example.data.MaintenanceRecord
import com.example.data.ExpenseRecord
import com.example.data.Reminder
import com.example.data.Vehicle
import com.example.ui.formatUnitPriceDotTl
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfReportGenerator {

    /**
     * Generates a sleek, modern, multi-page vector PDF report containing vehicle info,
     * summary statistics, complete maintenance & service history, and fuel expenses.
     */
    fun generateAndShareVehicleReport(
        context: Context,
        vehicle: Vehicle,
        maintenanceRecords: List<MaintenanceRecord>,
        expenseRecords: List<ExpenseRecord> = emptyList(),
        fuelRecords: List<FuelRecord> = emptyList(),
        reminders: List<Reminder> = emptyList()
    ) {
        try {
            val pdfDocument = PdfDocument()
            val pageWidth = 595 // Standard A4 points (72 dpi): 595 x 842
            val pageHeight = 842
            var pageNumber = 1

            // Calculations
            val totalMaintenanceCost = maintenanceRecords.sumOf { it.cost }
            val totalExpenseCost = expenseRecords.sumOf { it.amount }
            val totalFuelCost = fuelRecords.sumOf { it.total }
            val totalFuelLiters = fuelRecords.sumOf { it.liters }
            val totalCombinedExpense = totalMaintenanceCost + totalExpenseCost + totalFuelCost
            val latestKm = maxOf(
                maintenanceRecords.maxOfOrNull { it.km } ?: 0,
                fuelRecords.maxOfOrNull { it.km } ?: 0
            )

            val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale("tr", "TR"))
            val nowFormatted = SimpleDateFormat("dd MMMM yyyy HH:mm", Locale("tr", "TR")).format(Date())

            // Paints setup
            val textPaint = Paint().apply {
                isAntiAlias = true
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            }

            val boldPaint = Paint().apply {
                isAntiAlias = true
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }

            val bgPaint = Paint().apply {
                isAntiAlias = true
                style = Paint.Style.FILL
            }

            val strokePaint = Paint().apply {
                isAntiAlias = true
                style = Paint.Style.STROKE
            }

            // Colors
            val colorPrimary = Color.rgb(74, 120, 181) // #4A78B5 Pastel Denim-Sky
            val colorPrimaryDark = Color.rgb(51, 89, 142) // #33598E
            val colorEmerald = Color.rgb(66, 122, 91) // #427A5B Pastel Sage-Leaf
            val colorAmber = Color.rgb(61, 122, 110) // #3D7A6E Unified Pastel Sage-Eucalyptus Fuel
            val colorDarkText = Color.rgb(15, 23, 42) // #0F172A
            val colorMutedText = Color.rgb(100, 116, 139) // #64748B
            val colorLightBg = Color.rgb(248, 250, 252) // #F8FAFC
            val colorCardBorder = Color.rgb(226, 232, 240) // #E2E8F0
            val colorWhite = Color.WHITE

            // ---------------- PAGE 1: HEADER, SUMMARY, AND START OF MAINTENANCE ----------------
            var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
            var page = pdfDocument.startPage(pageInfo)
            var canvas: Canvas = page.canvas

            // 1. Top Decorative Brand Banner
            bgPaint.color = colorPrimary
            canvas.drawRect(0f, 0f, pageWidth.toFloat(), 110f, bgPaint)

            // Accent bar
            bgPaint.color = colorEmerald
            canvas.drawRect(0f, 106f, pageWidth.toFloat(), 110f, bgPaint)

            // Brand Title
            boldPaint.color = colorWhite
            boldPaint.textSize = 22f
            canvas.drawText("ARAÇ KARNEM & DİJİTAL SERVİS RAPORU", 36f, 48f, boldPaint)

            textPaint.color = Color.rgb(219, 234, 254)
            textPaint.textSize = 11f
            canvas.drawText("Resmi Araç Bakım, Onarım ve Akaryakıt Harcama Dökümü", 36f, 70f, textPaint)
            canvas.drawText("Rapor Tarihi: $nowFormatted", 36f, 88f, textPaint)

            // 2. Vehicle Identification Card (White Box with Plate)
            val vehicleCardTop = 126f
            val vehicleCardBottom = 206f
            bgPaint.color = colorLightBg
            canvas.drawRoundRect(RectF(36f, vehicleCardTop, pageWidth - 36f, vehicleCardBottom), 12f, 12f, bgPaint)
            strokePaint.color = colorCardBorder
            strokePaint.strokeWidth = 1.2f
            canvas.drawRoundRect(RectF(36f, vehicleCardTop, pageWidth - 36f, vehicleCardBottom), 12f, 12f, strokePaint)

            // Turkish Plate Graphic
            val plateLeft = 52f
            val plateTop = vehicleCardTop + 16f
            val plateWidth = 130f
            val plateHeight = 44f
            
            // White plate body
            bgPaint.color = colorWhite
            canvas.drawRoundRect(RectF(plateLeft, plateTop, plateLeft + plateWidth, plateTop + plateHeight), 6f, 6f, bgPaint)
            strokePaint.color = Color.rgb(15, 23, 42)
            strokePaint.strokeWidth = 2f
            canvas.drawRoundRect(RectF(plateLeft, plateTop, plateLeft + plateWidth, plateTop + plateHeight), 6f, 6f, strokePaint)

            // Blue TR band
            bgPaint.color = Color.rgb(29, 78, 216)
            canvas.drawRoundRect(RectF(plateLeft, plateTop, plateLeft + 26f, plateTop + plateHeight), 6f, 6f, bgPaint)
            canvas.drawRect(plateLeft + 20f, plateTop, plateLeft + 26f, plateTop + plateHeight, bgPaint)
            
            boldPaint.color = colorWhite
            boldPaint.textSize = 9f
            canvas.drawText("TR", plateLeft + 6f, plateTop + 34f, boldPaint)

            // Plate Text
            boldPaint.color = Color.BLACK
            boldPaint.textSize = 14f
            boldPaint.typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            canvas.drawText(vehicle.plate.uppercase(Locale("tr", "TR")), plateLeft + 34f, plateTop + 28f, boldPaint)

            // Vehicle Specs text
            boldPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            boldPaint.color = colorDarkText
            boldPaint.textSize = 16f
            canvas.drawText("${vehicle.brand} ${vehicle.model}", 200f, vehicleCardTop + 36f, boldPaint)

            textPaint.color = colorMutedText
            textPaint.textSize = 11f
            canvas.drawText("Model Yılı: ${vehicle.year}   •   Yakıt: ${vehicle.fuelType.uppercase(Locale("tr", "TR"))}   •   Kayıtlı Son KM: ${if (latestKm > 0) String.format(Locale("tr", "TR"), "%,d km", latestKm) else "Belirtilmedi"}", 200f, vehicleCardTop + 58f, textPaint)

            // 3. 4-Box Summary Dashboard Grid
            val summaryTop = 220f
            val boxWidth = (pageWidth - 72f - 24f) / 3f // 3 primary boxes
            val boxHeight = 56f

            // Box 1: Bakım Harcaması
            drawMetricBox(canvas, 36f, summaryTop, boxWidth, boxHeight, "Toplam Bakım", String.format(Locale("tr", "TR"), "%,.0f ₺", totalMaintenanceCost), "${maintenanceRecords.size} İşlem", colorPrimary, bgPaint, strokePaint, boldPaint, textPaint)

            // Box 2: Yakıt Harcaması
            val fuelBoxText = if (totalFuelLiters > 0) String.format(Locale("tr", "TR"), "%,.1f Lt", totalFuelLiters) else "${fuelRecords.size} Dolum"
            drawMetricBox(canvas, 36f + boxWidth + 12f, summaryTop, boxWidth, boxHeight, "Toplam Akaryakıt", String.format(Locale("tr", "TR"), "%,.0f ₺", totalFuelCost), fuelBoxText, colorAmber, bgPaint, strokePaint, boldPaint, textPaint)

            // Box 3: Genel Toplam
            drawMetricBox(canvas, 36f + (boxWidth + 12f) * 2f, summaryTop, boxWidth, boxHeight, "Genel Toplam Masraf", String.format(Locale("tr", "TR"), "%,.0f ₺", totalCombinedExpense), "Servis + Sigorta + Yakıt", colorEmerald, bgPaint, strokePaint, boldPaint, textPaint)

            var currentY = 296f

            // 4. Maintenance History Section Title
            boldPaint.color = colorDarkText
            boldPaint.textSize = 13f
            canvas.drawText("1. PERİYODİK BAKIM & SERVİS GEÇMİŞİ", 36f, currentY, boldPaint)
            
            strokePaint.color = colorPrimary
            strokePaint.strokeWidth = 2f
            canvas.drawLine(36f, currentY + 6f, pageWidth - 36f, currentY + 6f, strokePaint)

            currentY += 24f

            // Maintenance Table Header
            drawTableHeader(canvas, currentY, listOf("Tarih", "Kilometre", "Yapılan Bakım / Değişen Parçalar", "Tutar"), listOf(70f, 75f, 290f, 88f), bgPaint, strokePaint, boldPaint)
            currentY += 22f

            val sortedMaintenance = maintenanceRecords.sortedByDescending { it.date }

            if (sortedMaintenance.isEmpty()) {
                textPaint.color = colorMutedText
                textPaint.textSize = 11f
                canvas.drawText("Bu araca ait henüz bakım kaydı bulunmuyor.", 44f, currentY + 18f, textPaint)
                currentY += 36f
            } else {
                for (record in sortedMaintenance) {
                    // Check page boundary
                    if (currentY > pageHeight - 60f) {
                        drawFooter(canvas, pageNumber, pageWidth, pageHeight, textPaint)
                        pdfDocument.finishPage(page)
                        pageNumber++
                        pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                        page = pdfDocument.startPage(pageInfo)
                        canvas = page.canvas

                        // Header on new page
                        currentY = 40f
                        boldPaint.color = colorPrimaryDark
                        boldPaint.textSize = 12f
                        canvas.drawText("Bakım & Servis Geçmişi (Devam) - ${vehicle.plate}", 36f, currentY, boldPaint)
                        currentY += 16f
                        drawTableHeader(canvas, currentY, listOf("Tarih", "Kilometre", "Yapılan Bakım / Değişen Parçalar", "Tutar"), listOf(70f, 75f, 290f, 88f), bgPaint, strokePaint, boldPaint)
                        currentY += 22f
                    }

                    val rowDate = dateFormat.format(Date(record.date))
                    val rowKm = if (record.km > 0) String.format(Locale("tr", "TR"), "%,d km", record.km) else "-"
                    val rowCost = if (record.cost > 0) String.format(Locale("tr", "TR"), "%,.0f ₺", record.cost) else "-"
                    val rowDesc = if (record.description.isNotBlank()) "${record.type} (${record.description})" else record.type

                    drawTableRow(canvas, currentY, listOf(rowDate, rowKm, rowDesc, rowCost), listOf(70f, 75f, 290f, 88f), bgPaint, strokePaint, textPaint, boldPaint)
                    currentY += 24f
                }
            }

            currentY += 16f

            // Expense History Section
            if (currentY > pageHeight - 140f) {
                drawFooter(canvas, pageNumber, pageWidth, pageHeight, textPaint)
                pdfDocument.finishPage(page)
                pageNumber++
                pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                page = pdfDocument.startPage(pageInfo)
                canvas = page.canvas
                currentY = 40f
            }

            boldPaint.color = colorDarkText
            boldPaint.textSize = 13f
            canvas.drawText("2. SİGORTA & GENEL GİDER GEÇMİŞİ", 36f, currentY, boldPaint)
            
            strokePaint.color = Color.rgb(59, 130, 246) // Blue
            strokePaint.strokeWidth = 2f
            canvas.drawLine(36f, currentY + 6f, pageWidth - 36f, currentY + 6f, strokePaint)

            currentY += 24f

            drawTableHeader(canvas, currentY, listOf("Tarih", "Kategori", "Açıklama", "Tutar"), listOf(70f, 100f, 245f, 108f), bgPaint, strokePaint, boldPaint)
            currentY += 22f

            val sortedExpenses = expenseRecords.sortedByDescending { it.date }

            if (sortedExpenses.isEmpty()) {
                textPaint.color = colorMutedText
                textPaint.textSize = 11f
                canvas.drawText("Bu araca ait genel gider kaydı bulunmuyor.", 44f, currentY + 18f, textPaint)
                currentY += 36f
            } else {
                for (record in sortedExpenses) {
                    if (currentY > pageHeight - 60f) {
                        drawFooter(canvas, pageNumber, pageWidth, pageHeight, textPaint)
                        pdfDocument.finishPage(page)
                        pageNumber++
                        pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                        page = pdfDocument.startPage(pageInfo)
                        canvas = page.canvas
                        currentY = 40f
                        boldPaint.color = colorPrimaryDark
                        boldPaint.textSize = 12f
                        canvas.drawText("Genel Giderler (Devam) - ${vehicle.plate}", 36f, currentY, boldPaint)
                        currentY += 16f
                        drawTableHeader(canvas, currentY, listOf("Tarih", "Kategori", "Açıklama", "Tutar"), listOf(70f, 100f, 245f, 108f), bgPaint, strokePaint, boldPaint)
                        currentY += 22f
                    }

                    val rowDate = dateFormat.format(Date(record.date))
                    val rowCat = record.category
                    val rowAmount = if (record.amount > 0) String.format(Locale("tr", "TR"), "%,.0f ₺", record.amount) else "-"
                    val rowDesc = record.description

                    drawTableRow(canvas, currentY, listOf(rowDate, rowCat, rowDesc, rowAmount), listOf(70f, 100f, 245f, 108f), bgPaint, strokePaint, textPaint, boldPaint)
                    currentY += 24f
                }
            }

            currentY += 16f

            // 5. Fuel Expenses Section
            if (currentY > pageHeight - 140f) {
                drawFooter(canvas, pageNumber, pageWidth, pageHeight, textPaint)
                pdfDocument.finishPage(page)
                pageNumber++
                pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                page = pdfDocument.startPage(pageInfo)
                canvas = page.canvas
                currentY = 40f
            }

            boldPaint.color = colorDarkText
            boldPaint.textSize = 13f
            canvas.drawText("3. AKARYAKIT HARCAMA GEÇMİŞİ", 36f, currentY, boldPaint)
            
            strokePaint.color = colorAmber
            strokePaint.strokeWidth = 2f
            canvas.drawLine(36f, currentY + 6f, pageWidth - 36f, currentY + 6f, strokePaint)

            currentY += 24f

            drawTableHeader(canvas, currentY, listOf("Tarih", "Kilometre", "Litre / Birim Fiyat", "Toplam Tutar"), listOf(70f, 85f, 260f, 108f), bgPaint, strokePaint, boldPaint)
            currentY += 22f

            val sortedFuel = fuelRecords.sortedByDescending { it.date }

            if (sortedFuel.isEmpty()) {
                textPaint.color = colorMutedText
                textPaint.textSize = 11f
                canvas.drawText("Bu araca ait kayıtlı akaryakıt dolum verisi bulunmuyor.", 44f, currentY + 18f, textPaint)
                currentY += 36f
            } else {
                for (fuel in sortedFuel) {
                    if (currentY > pageHeight - 60f) {
                        drawFooter(canvas, pageNumber, pageWidth, pageHeight, textPaint)
                        pdfDocument.finishPage(page)
                        pageNumber++
                        pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                        page = pdfDocument.startPage(pageInfo)
                        canvas = page.canvas

                        currentY = 40f
                        boldPaint.color = colorAmber
                        boldPaint.textSize = 12f
                        canvas.drawText("Akaryakıt Harcama Geçmişi (Devam) - ${vehicle.plate}", 36f, currentY, boldPaint)
                        currentY += 16f
                        drawTableHeader(canvas, currentY, listOf("Tarih", "Kilometre", "Litre / Birim Fiyat", "Toplam Tutar"), listOf(70f, 85f, 260f, 108f), bgPaint, strokePaint, boldPaint)
                        currentY += 22f
                    }

                    val rowDate = dateFormat.format(Date(fuel.date))
                    val rowKm = if (fuel.km > 0) String.format(Locale("tr", "TR"), "%,d km", fuel.km) else "-"
                    val rowDetail = "${String.format(Locale("tr", "TR"), "%.2f", fuel.liters)} Lt @ ${formatUnitPriceDotTl(fuel.price)}"
                    val rowTotal = String.format(Locale("tr", "TR"), "%,.2f ₺", fuel.total)

                    drawTableRow(canvas, currentY, listOf(rowDate, rowKm, rowDetail, rowTotal), listOf(70f, 85f, 260f, 108f), bgPaint, strokePaint, textPaint, boldPaint)
                    currentY += 24f
                }
            }

            // 6. Active Reminders (Optional Section)
            if (reminders.isNotEmpty()) {
                currentY += 16f
                if (currentY > pageHeight - 120f) {
                    drawFooter(canvas, pageNumber, pageWidth, pageHeight, textPaint)
                    pdfDocument.finishPage(page)
                    pageNumber++
                    pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                    page = pdfDocument.startPage(pageInfo)
                    canvas = page.canvas
                    currentY = 40f
                }

                boldPaint.color = colorDarkText
                boldPaint.textSize = 13f
                canvas.drawText("4. PLANLANAN SERVİS VE HATIRLATICILAR", 36f, currentY, boldPaint)
                strokePaint.color = Color.rgb(111, 99, 173) // #6F63AD Pastel Lavender-Iris
                strokePaint.strokeWidth = 2f
                canvas.drawLine(36f, currentY + 6f, pageWidth - 36f, currentY + 6f, strokePaint)
                currentY += 24f

                drawTableHeader(canvas, currentY, listOf("Planlanan Tarih", "İşlem / Hatırlatıcı Konusu", "Durum"), listOf(120f, 300f, 103f), bgPaint, strokePaint, boldPaint)
                currentY += 22f

                for (rem in reminders) {
                    if (currentY > pageHeight - 60f) {
                        drawFooter(canvas, pageNumber, pageWidth, pageHeight, textPaint)
                        pdfDocument.finishPage(page)
                        pageNumber++
                        pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                        page = pdfDocument.startPage(pageInfo)
                        canvas = page.canvas
                        currentY = 40f
                    }
                    val remDate = dateFormat.format(Date(rem.dueDate))
                    val isPast = rem.dueDate < System.currentTimeMillis()
                    val statusText = if (isPast) "Zamanı Geldi / Geçti" else "Beklemede"

                    drawTableRow(canvas, currentY, listOf(remDate, rem.title, statusText), listOf(120f, 300f, 103f), bgPaint, strokePaint, textPaint, boldPaint)
                    currentY += 24f
                }
            }

            // Draw footer on last page
            drawFooter(canvas, pageNumber, pageWidth, pageHeight, textPaint)
            pdfDocument.finishPage(page)

            // Save PDF to cache/reports
            val reportsDir = File(context.cacheDir, "reports")
            if (!reportsDir.exists()) reportsDir.mkdirs()

            val sanitizedPlate = vehicle.plate.replace(" ", "_").replace("/", "_")
            val pdfFile = File(reportsDir, "Arac_Karnesi_${sanitizedPlate}.pdf")
            val outputStream = FileOutputStream(pdfFile)
            pdfDocument.writeTo(outputStream)
            outputStream.flush()
            outputStream.close()
            pdfDocument.close()

            // Open Share / View Intent via FileProvider
            val contentUri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                pdfFile
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, contentUri)
                putExtra(Intent.EXTRA_SUBJECT, "${vehicle.brand} ${vehicle.model} (${vehicle.plate}) - Bakım & Harcama Raporu")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(shareIntent, "PDF Raporunu Paylaş veya Kaydet")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)

            Toast.makeText(context, "PDF Raporu Başarıyla Oluşturuldu!", Toast.LENGTH_SHORT).show()

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "PDF oluşturulurken hata: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }

    private fun drawMetricBox(
        canvas: Canvas,
        x: Float,
        y: Float,
        w: Float,
        h: Float,
        title: String,
        value: String,
        subtitle: String,
        accentColor: Int,
        bgPaint: Paint,
        strokePaint: Paint,
        boldPaint: Paint,
        textPaint: Paint
    ) {
        bgPaint.color = Color.rgb(248, 250, 252)
        canvas.drawRoundRect(RectF(x, y, x + w, y + h), 8f, 8f, bgPaint)

        strokePaint.color = Color.rgb(226, 232, 240)
        strokePaint.strokeWidth = 1f
        canvas.drawRoundRect(RectF(x, y, x + w, y + h), 8f, 8f, strokePaint)

        // Small Left accent line
        bgPaint.color = accentColor
        canvas.drawRoundRect(RectF(x, y, x + 4f, y + h), 4f, 4f, bgPaint)

        textPaint.color = Color.rgb(100, 116, 139)
        textPaint.textSize = 8.5f
        canvas.drawText(title.uppercase(Locale("tr", "TR")), x + 10f, y + 15f, textPaint)

        boldPaint.color = Color.rgb(15, 23, 42)
        boldPaint.textSize = 13f
        canvas.drawText(value, x + 10f, y + 33f, boldPaint)

        textPaint.color = Color.rgb(100, 116, 139)
        textPaint.textSize = 8.5f
        canvas.drawText(subtitle, x + 10f, y + 47f, textPaint)
    }

    private fun drawTableHeader(
        canvas: Canvas,
        y: Float,
        headers: List<String>,
        colWidths: List<Float>,
        bgPaint: Paint,
        strokePaint: Paint,
        boldPaint: Paint
    ) {
        val totalWidth = colWidths.sum()
        val startX = 36f

        bgPaint.color = Color.rgb(241, 245, 249)
        canvas.drawRect(startX, y, startX + totalWidth, y + 20f, bgPaint)

        strokePaint.color = Color.rgb(203, 213, 225)
        strokePaint.strokeWidth = 0.8f
        canvas.drawRect(startX, y, startX + totalWidth, y + 20f, strokePaint)

        boldPaint.color = Color.rgb(51, 65, 85)
        boldPaint.textSize = 9.5f

        var currentX = startX
        for (i in headers.indices) {
            canvas.drawText(headers[i], currentX + 6f, y + 14f, boldPaint)
            currentX += colWidths[i]
        }
    }

    private fun drawTableRow(
        canvas: Canvas,
        y: Float,
        values: List<String>,
        colWidths: List<Float>,
        bgPaint: Paint,
        strokePaint: Paint,
        textPaint: Paint,
        boldPaint: Paint
    ) {
        val totalWidth = colWidths.sum()
        val startX = 36f

        strokePaint.color = Color.rgb(241, 245, 249)
        strokePaint.strokeWidth = 0.8f
        canvas.drawLine(startX, y + 20f, startX + totalWidth, y + 20f, strokePaint)

        textPaint.color = Color.rgb(15, 23, 42)
        textPaint.textSize = 9f

        var currentX = startX
        for (i in values.indices) {
            val text = values[i]
            val maxChars = (colWidths[i] / 5.2f).toInt()
            val truncated = if (text.length > maxChars) text.take(maxChars - 3) + "..." else text

            if (i == values.lastIndex) {
                // Right aligned or bold for amount
                boldPaint.color = Color.rgb(15, 23, 42)
                boldPaint.textSize = 9f
                canvas.drawText(truncated, currentX + 6f, y + 14f, boldPaint)
            } else {
                canvas.drawText(truncated, currentX + 6f, y + 14f, textPaint)
            }
            currentX += colWidths[i]
        }
    }

    private fun drawFooter(
        canvas: Canvas,
        pageNumber: Int,
        pageWidth: Int,
        pageHeight: Int,
        textPaint: Paint
    ) {
        textPaint.color = Color.rgb(148, 163, 184)
        textPaint.textSize = 8.5f
        canvas.drawText("Araç Karnem Akıllı Bakım & Harcama Takip Uygulaması • Sayfa $pageNumber", 36f, pageHeight - 24f, textPaint)
    }
}
