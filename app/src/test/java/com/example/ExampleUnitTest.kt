package com.example

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import com.example.data.FuelPriceService
import com.example.data.VehicleEngineCatalog
import com.example.data.normalizeFuelLookupKey
import com.example.ui.*
import com.example.ui.theme.Typography
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun turkishThousandsVisualTransformation_formatsAndMapsOffsetsSafely() {
        val vt = TurkishThousandsVisualTransformation()
        val cases = listOf(
            "" to "",
            "500" to "500",
            "1500" to "1.500",
            "14800" to "14.800",
            "1250000" to "1.250.000",
            "14800,50" to "14.800,50"
        )

        for ((raw, expected) in cases) {
            val res = vt.filter(AnnotatedString(raw))
            assertEquals(expected, res.text.text)

            var prevTrans = 0
            for (i in 0..raw.length) {
                val mapped = res.offsetMapping.originalToTransformed(i)
                assertTrue(mapped in 0..expected.length)
                assertTrue(mapped >= prevTrans)
                prevTrans = mapped
            }

            var prevOrig = 0
            for (j in 0..expected.length) {
                val mappedOrig = res.offsetMapping.transformedToOriginal(j)
                assertTrue(mappedOrig in 0..raw.length)
                assertTrue(mappedOrig >= prevOrig)
                prevOrig = mappedOrig
            }
        }
    }

    @Test
    fun currencyAndUnitPriceSanitizers_formatWithDotCorrectly() {
        assertEquals("14800", sanitizeCurrencyDigits("14.800"))
        assertEquals("14800,50", sanitizeCurrencyDigits("14.800,509"))
        assertEquals(14800.50, parseTurkishCurrency("14.800,50"), 0.001)
        assertEquals("46.52", sanitizeUnitPriceInput("46,529"))
        assertEquals("14.800 ₺", formatCurrencyTl(14800.0))
        assertEquals("46.52 TL/ Lt", formatUnitPriceDotTl(46.52))
        assertEquals("98.00 TL/ Lt", formatUnitPriceDotTl(98.0))
        assertEquals("4.25 TL", formatDecimalTl(4.25))
    }

    @Test
    fun garageTitleFormatter_alwaysEndsWithGarajiWithoutDuplicating() {
        assertEquals("TOPRAK BARAN GARAJI", formatGarageTitle("", "Toprak", "Baran"))
        assertEquals("YILMAZ AİLESİ GARAJI", formatGarageTitle("Yılmaz Ailesi", "Toprak", "Baran"))
        assertEquals("ANKARA MERKEZ GARAJI", formatGarageTitle("Ankara Merkez Garajı", "Toprak", "Baran"))
        assertEquals("Ankara Merkez", extractGaragePrefix("Ankara Merkez Garajı", "Toprak", "Baran"))
    }

    @Test
    fun supportedCities_coversAll81TurkishProvinces() {
        val uniqueProvinceCodes = FuelPriceService.supportedCities.map { it.provinceCode }.toSet()
        assertEquals(81, uniqueProvinceCodes.size)
    }

    @Test
    fun vehicleEngineCatalog_returnsVehicleSpecificEngines() {
        val egeaEngines = VehicleEngineCatalog.getEnginesForVehicle("Fiat", "Egea Sedan")
        assertTrue(egeaEngines.isNotEmpty())
        assertTrue(egeaEngines.any { it.engineName.contains("MultiJet", ignoreCase = true) })

        val toggEngines = VehicleEngineCatalog.getEnginesForVehicle("Togg", "T10X")
        assertTrue(toggEngines.isNotEmpty())
        assertTrue(toggEngines.all { it.fuelType == "Elektrik" })

        val clioEngines = VehicleEngineCatalog.getEnginesForVehicle("Renault", "Clio")
        assertTrue(clioEngines.isNotEmpty())
        assertTrue(clioEngines.any { it.engineName.contains("TCe", ignoreCase = true) })
    }

    @Test
    fun normalizeFuelLookupKey_handlesTurkishAndAsciiUppercaseSafely() {
        assertTrue(normalizeFuelLookupKey("KURŞUNSUZ BENZIN").contains("benzin"))
        assertTrue(normalizeFuelLookupKey("KURŞUNSUZ BENZİN 95").contains("benzin"))
        assertTrue(normalizeFuelLookupKey("MOTORIN ULTRAFORCE").contains("motorin"))
        assertTrue(normalizeFuelLookupKey("MOTORİN").contains("motorin"))
        assertTrue(normalizeFuelLookupKey("DIZEL").contains("dizel"))
        assertTrue(normalizeFuelLookupKey("DİZEL").contains("dizel"))
        assertTrue(normalizeFuelLookupKey("ELEKTRIK").contains("elektrik"))
    }

    @Test
    fun regionalFallbackPrices_returnsValidPositivePricesForAllCities() {
        val service = FuelPriceService(context = null)
        for (city in FuelPriceService.supportedCities) {
            val fallback = service.buildRegionalFallbackPrices(city, "14:00")
            assertEquals(city.displayName, fallback.cityName)
            assertTrue(fallback.gasoline95 > 30.0)
            assertTrue(fallback.diesel > 30.0)
            assertTrue(fallback.lpg > 15.0)
            assertEquals(fallback.diesel, fallback.priceForFuelType("DİZEL"), 0.001)
            assertEquals(fallback.diesel, fallback.priceForFuelType("MOTORIN"), 0.001)
            assertEquals(fallback.gasoline95, fallback.priceForFuelType("BENZIN"), 0.001)
        }
    }

    @Test
    fun typographyStyles_leaveColorUnspecifiedSoButtonWhiteTextWorks() {
        assertEquals(Color.Unspecified, Typography.headlineLarge.color)
        assertEquals(Color.Unspecified, Typography.headlineMedium.color)
        assertEquals(Color.Unspecified, Typography.titleLarge.color)
        assertEquals(Color.Unspecified, Typography.titleMedium.color)
        assertEquals(Color.Unspecified, Typography.bodyLarge.color)
        assertEquals(Color.Unspecified, Typography.bodyMedium.color)
        assertEquals(Color.Unspecified, Typography.labelLarge.color)
    }
}


