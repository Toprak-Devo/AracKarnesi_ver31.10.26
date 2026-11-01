package com.example.ui

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

private val trLocale: Locale = Locale.forLanguageTag("tr-TR")

/**
 * VisualTransformation that automatically formats numeric/currency inputs with "." thousands separators
 * as the user types (e.g., "4500" -> "4.500", "14800" -> "14.800", "14800,50" -> "14.800,50").
 * Uses explicit monotonic offset lookup tables to guarantee crash-free cursor & selection mapping.
 */
class TurkishThousandsVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val original = text.text
        val n = original.length
        if (n == 0) {
            return TransformedText(AnnotatedString(""), OffsetMapping.Identity)
        }

        val commaIndex = original.indexOf(',')
        val intLen = if (commaIndex >= 0) commaIndex else n

        val sb = StringBuilder(n + (intLen / 3) + 2)
        val origToTrans = IntArray(n + 1)
        // Maximum possible transformed length is n + n (upper bound safe allocation)
        val tempTransToOrig = IntArray(n * 2 + 2)

        for (i in 0 until n) {
            if (i in 1 until intLen && (intLen - i) % 3 == 0) {
                tempTransToOrig[sb.length] = i
                sb.append('.')
            }
            origToTrans[i] = sb.length
            tempTransToOrig[sb.length] = i
            sb.append(original[i])
        }

        val transformedLength = sb.length
        origToTrans[n] = transformedLength
        tempTransToOrig[transformedLength] = n

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                return origToTrans[offset.coerceIn(0, n)]
            }

            override fun transformedToOriginal(offset: Int): Int {
                return tempTransToOrig[offset.coerceIn(0, transformedLength)]
            }
        }

        return TransformedText(AnnotatedString(sb.toString()), offsetMapping)
    }
}

/**
 * Sanitizes currency input (cost, fee, amount) into canonical digits (plus optional ",XX" kuruş)
 * ready for [TurkishThousandsVisualTransformation] to render with "." thousands separators.
 */
fun sanitizeCurrencyDigits(input: String, allowCommaDecimal: Boolean = true, maxIntDigits: Int = 10): String {
    if (input.isEmpty()) return ""
    val withoutThousandDots = input.replace(".", "").replace(" ", "").replace("₺", "")

    if (!allowCommaDecimal) {
        val digits = withoutThousandDots.filter { it.isDigit() }
            .trimStart('0')
            .take(maxIntDigits)
        return digits
    }

    val parts = withoutThousandDots.split(',')
    val rawInt = parts.firstOrNull().orEmpty().filter { it.isDigit() }
    val normalizedInt = when {
        rawInt.isEmpty() && parts.size > 1 -> "0"
        rawInt.length > 1 && rawInt.all { it == '0' } -> "0"
        rawInt.length > 1 -> rawInt.trimStart('0').ifEmpty { "0" }
        else -> rawInt
    }.take(maxIntDigits)

    return if (parts.size > 1) {
        val frac = parts.drop(1).joinToString("").filter { it.isDigit() }.take(2)
        "$normalizedInt,$frac"
    } else {
        normalizedInt
    }
}

/**
 * Parses currency string (with or without "." thousands separators and "," decimals) into Double.
 */
fun parseTurkishCurrency(input: String): Double {
    val cleaned = input.trim()
        .replace("₺", "")
        .replace(" ", "")
        .replace(".", "")
        .replace(",", ".")
    return cleaned.toDoubleOrNull() ?: 0.0
}

/**
 * Sanitizes unit fuel price input so decimal separator is automatically "." (e.g., "46.50").
 */
fun sanitizeUnitPriceInput(input: String): String {
    if (input.isEmpty()) return ""
    val normalized = input.replace(",", ".").replace("₺", "").replace(" ", "")
    val sb = StringBuilder()
    var dotSeen = false
    var decimalsAfterDot = 0
    var intDigits = 0

    for (ch in normalized) {
        when {
            ch.isDigit() -> {
                if (dotSeen) {
                    if (decimalsAfterDot < 2) {
                        sb.append(ch)
                        decimalsAfterDot++
                    }
                } else {
                    if (intDigits < 5) {
                        sb.append(ch)
                        intDigits++
                    }
                }
            }
            ch == '.' && !dotSeen -> {
                if (sb.isEmpty()) {
                    sb.append('0')
                    intDigits = 1
                }
                sb.append('.')
                dotSeen = true
            }
        }
    }
    return sb.toString()
}

fun parseUnitPrice(input: String): Double {
    return input.trim().replace(",", ".").toDoubleOrNull() ?: 0.0
}

/**
 * Sanitizes decimal liter quantity input (e.g., "38.5" or "42.25").
 */
fun sanitizeDecimalQuantityInput(input: String): String {
    return sanitizeUnitPriceInput(input)
}

/**
 * Sanitizes integer kilometer input (digits only, no leading zeros) for [TurkishThousandsVisualTransformation].
 */
fun sanitizeIntegerDigits(input: String, maxDigits: Int = 7): String {
    return input.replace(".", "")
        .replace(",", "")
        .filter { it.isDigit() }
        .trimStart('0')
        .take(maxDigits)
}

/**
 * Formats TL amounts explicitly with "." thousands separator and "," decimal separator.
 */
fun formatCurrencyTl(amount: Double, decimals: Int = 0): String {
    val symbols = DecimalFormatSymbols(trLocale).apply {
        groupingSeparator = '.'
        decimalSeparator = ','
    }
    val pattern = if (decimals > 0) {
        "#,##0." + "0".repeat(decimals)
    } else {
        "#,##0"
    }
    return "${DecimalFormat(pattern, symbols).format(amount)} ₺"
}

/**
 * Formats unit pump liter price as '98.00 TL/ Lt' (two decimals with '.' separator + ' TL/ Lt').
 */
fun formatUnitPriceDotTl(price: Double): String {
    val symbols = DecimalFormatSymbols(Locale.US).apply {
        groupingSeparator = '.'
        decimalSeparator = '.'
    }
    return "${DecimalFormat("0.00", symbols).format(price)} TL/ Lt"
}

/**
 * Formats decimal TL cost (e.g. for cost per km: '4.12 TL').
 */
fun formatDecimalTl(amount: Double): String {
    val symbols = DecimalFormatSymbols(Locale.US).apply {
        groupingSeparator = '.'
        decimalSeparator = '.'
    }
    return "${DecimalFormat("0.00", symbols).format(amount)} TL"
}

/**
 * Extracts the editable prefix before "GARAJI" so we never duplicate "... GARAJI GARAJI".
 */
fun extractGaragePrefix(rawGarageName: String?, ownerName: String?, ownerSurname: String?): String {
    val fallback = listOfNotNull(ownerName?.trim(), ownerSurname?.trim())
        .filter { it.isNotEmpty() }
        .joinToString(" ")
        .ifBlank { "TOPRAK BARAN" }

    val candidate = rawGarageName?.trim().orEmpty().ifBlank { fallback }
    val upper = candidate.uppercase(trLocale).trim()
    val stripped = when {
        upper.endsWith(" GARAJI") -> candidate.dropLast(7).trim()
        upper.endsWith(" GARAJ") -> candidate.dropLast(6).trim()
        upper == "GARAJI" || upper == "GARAJ" -> fallback
        else -> candidate
    }
    return stripped.ifBlank { fallback }
}

/**
 * Formats the garage display banner title as "<PREFIX> GARAJI" in Turkish uppercase.
 */
fun formatGarageTitle(rawGarageName: String?, ownerName: String?, ownerSurname: String?): String {
    val prefix = extractGaragePrefix(rawGarageName, ownerName, ownerSurname)
    return "${prefix.uppercase(trLocale)} GARAJI"
}
