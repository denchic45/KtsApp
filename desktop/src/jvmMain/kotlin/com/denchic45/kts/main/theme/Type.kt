package com.denchic45.kts.main.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.platform.Font
import androidx.compose.ui.unit.ExperimentalUnitApi
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.sp

val defaultTypography = Typography()

@OptIn(ExperimentalUnitApi::class)
val Typography = Typography(
    headlineMedium = defaultTypography.headlineMedium.copy(
        fontFamily = FontFamily(
            Font(resource = "fonts/Gilroy-Medium.ttf")
        ),
    ),
    titleLarge = defaultTypography.titleLarge.copy(
        fontFamily = FontFamily(
            Font(resource = "fonts/Gilroy-Semibold.ttf")
        ),
        fontSize = TextUnit(20F, TextUnitType.Sp)

//        fontSize = TextUnit(22F, TextUnitType.Sp),
//        lineHeight = TypeScale.TitleLargeLineHeight,
//        letterSpacing = TypeScale.TitleLargeTracking,
    ),
    bodyLarge = defaultTypography.bodyLarge.copy(
        fontFamily = FontFamily(
            Font(resource = "fonts/Gilroy-Medium.ttf")
        ),
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp
    )
    /* Other default text styles to override
    button = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.W500,
        fontSize = 14.sp
    ),
    caption = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp
    )
    */
)