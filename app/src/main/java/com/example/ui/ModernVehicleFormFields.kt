package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.*

/**
 * Compact, high-contrast modern text input box matching ModernCompactSelectorField geometry.
 */
@Composable
fun ModernCompactTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    leadingIcon: ImageVector,
    inputTestTag: String,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    errorMessage: String? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    accentColor: Color = PrimaryVibrant,
    accentSoftColor: Color = PrimarySoft,
    trailingContent: (@Composable () -> Unit)? = null
) {
    var isFocused by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val hasValue = value.isNotBlank()

    val borderColor = when {
        isError -> AccentRose
        isFocused -> accentColor
        hasValue -> accentColor.copy(alpha = 0.45f)
        else -> BorderLight
    }
    val borderWidth = if (isError || isFocused) 1.5.dp else 1.dp

    Column(modifier = modifier) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 54.dp)
                .clip(RoundedCornerShape(14.dp))
                .clickable { focusRequester.requestFocus() },
            shape = RoundedCornerShape(14.dp),
            color = SurfaceWhite,
            border = BorderStroke(borderWidth, borderColor)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = 12.dp,
                        end = if (trailingContent != null) 4.dp else 12.dp,
                        top = if (trailingContent != null) 3.dp else 7.dp,
                        bottom = if (trailingContent != null) 3.dp else 7.dp
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .background(
                            color = when {
                                isError -> AccentRoseSoft
                                isFocused || hasValue -> accentSoftColor
                                else -> BgLightAlt
                            },
                            shape = RoundedCornerShape(10.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = leadingIcon,
                        contentDescription = null,
                        tint = when {
                            isError -> AccentRose
                            isFocused || hasValue -> accentColor
                            else -> TextMedium
                        },
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(1.dp)
                ) {
                    Text(
                        text = label,
                        fontSize = 11.sp,
                        lineHeight = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = when {
                            isError -> AccentRose
                            isFocused -> accentColor
                            else -> TextMedium
                        },
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    BasicTextField(
                        value = value,
                        onValueChange = onValueChange,
                        singleLine = true,
                        keyboardOptions = keyboardOptions,
                        cursorBrush = SolidColor(accentColor),
                        textStyle = TextStyle(
                            fontSize = 15.sp,
                            lineHeight = 19.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextDark
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester)
                            .onFocusChanged { isFocused = it.isFocused }
                            .testTag(inputTestTag),
                        decorationBox = { innerTextField ->
                            Box(contentAlignment = Alignment.CenterStart) {
                                if (value.isEmpty()) {
                                    Text(
                                        text = placeholder,
                                        fontSize = 14.sp,
                                        lineHeight = 18.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = TextLight,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                innerTextField()
                            }
                        }
                    )
                }

                if (trailingContent != null) {
                    Spacer(modifier = Modifier.width(4.dp))
                    trailingContent()
                }
            }
        }

        if (isError && !errorMessage.isNullOrBlank()) {
            Text(
                text = errorMessage,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Medium,
                color = AccentRose,
                modifier = Modifier.padding(start = 6.dp, top = 3.dp)
            )
        }
    }
}

/**
 * Compact, high-contrast modern selector box for modal/dropdown selection without chip clutter.
 */
@Composable
fun ModernCompactSelectorField(
    label: String,
    value: String,
    placeholder: String,
    leadingIcon: ImageVector,
    buttonTestTag: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    isError: Boolean = false,
    errorMessage: String? = null,
    accentColor: Color = PrimaryVibrant,
    accentSoftColor: Color = PrimarySoft,
    badgeText: String? = null
) {
    val hasValue = value.isNotBlank()
    val borderColor = when {
        isError -> AccentRose
        hasValue -> accentColor.copy(alpha = 0.55f)
        enabled -> BorderLight
        else -> BorderLight.copy(alpha = 0.65f)
    }
    val borderWidth = if (isError || hasValue) 1.4.dp else 1.dp

    Column(modifier = modifier) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 54.dp)
                .clip(RoundedCornerShape(14.dp))
                .clickable(enabled = enabled, onClick = onClick)
                .testTag(buttonTestTag),
            shape = RoundedCornerShape(14.dp),
            color = if (enabled) SurfaceWhite else BgLightAlt.copy(alpha = 0.75f),
            border = BorderStroke(borderWidth, borderColor)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .background(
                            color = when {
                                isError -> AccentRoseSoft
                                hasValue -> accentSoftColor
                                enabled -> BgLightAlt
                                else -> SurfaceWhite.copy(alpha = 0.7f)
                            },
                            shape = RoundedCornerShape(10.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = leadingIcon,
                        contentDescription = null,
                        tint = when {
                            isError -> AccentRose
                            hasValue -> accentColor
                            enabled -> TextMedium
                            else -> TextLight
                        },
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(1.dp)
                ) {
                    Text(
                        text = label,
                        fontSize = 11.sp,
                        lineHeight = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = when {
                            isError -> AccentRose
                            hasValue -> accentColor
                            enabled -> TextMedium
                            else -> TextLight
                        },
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = value.ifBlank { placeholder },
                        fontSize = if (hasValue) 15.sp else 14.sp,
                        lineHeight = 19.sp,
                        fontWeight = if (hasValue) FontWeight.Bold else FontWeight.Medium,
                        color = when {
                            isError && !hasValue -> AccentRose
                            hasValue -> TextDark
                            enabled -> TextMedium
                            else -> TextLight
                        },
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (!badgeText.isNullOrBlank()) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = FuelSoft,
                        border = BorderStroke(1.dp, FuelBorder)
                    ) {
                        Text(
                            text = badgeText,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = FuelColorDark,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = accentColor
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = label,
                        tint = if (enabled) accentColor else TextLight,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        if (isError && !errorMessage.isNullOrBlank()) {
            Text(
                text = errorMessage,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Medium,
                color = AccentRose,
                modifier = Modifier.padding(start = 6.dp, top = 3.dp)
            )
        }
    }
}

/**
 * Clean modal dialog for choosing vehicle fuel type directly from the Yakıt Türü box.
 */
@Composable
fun FuelTypePickerModalDialog(
    fuelTypes: List<String>,
    selectedFuelType: String,
    onFuelTypePicked: (String) -> Unit,
    onDismiss: () -> Unit
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
                .padding(horizontal = 20.dp, vertical = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 420.dp)
                    .shadow(16.dp, RoundedCornerShape(24.dp), spotColor = TextDark.copy(alpha = 0.18f))
                    .testTag("fuel_type_picker_modal"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                border = BorderStroke(1.dp, BorderLight)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "YAKIT TÜRÜ",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = FuelColorDark,
                                letterSpacing = 0.6.sp
                            )
                            Text(
                                text = "Yakıt Türü Seçin",
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
                                modifier = Modifier.size(19.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    fuelTypes.forEach { item ->
                        val isSelected = item == selectedFuelType
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 48.dp)
                                .clip(RoundedCornerShape(13.dp))
                                .clickable { onFuelTypePicked(item) }
                                .testTag("fuel_type_option_$item"),
                            shape = RoundedCornerShape(13.dp),
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
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.LocalGasStation,
                                        contentDescription = null,
                                        tint = if (isSelected) FuelColorDark else TextMedium,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = item,
                                        fontSize = 14.5.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                        color = TextDark
                                    )
                                }

                                if (isSelected) {
                                    Box(
                                        modifier = Modifier
                                            .size(22.dp)
                                            .background(FuelColorDark, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Seçili",
                                            tint = Color.White,
                                            modifier = Modifier.size(14.dp)
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
