package com.philkes.baseled.ui.theme

import androidx.compose.material.MaterialTheme
import androidx.compose.material.ProvideTextStyle
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle

private val DarkColorPalette = darkColors(
    primary = Grey500,
    primaryVariant = Grey700,
    secondary = Orange700Dark,
    secondaryVariant = Orange500,
    background = Grey900,
    onSurface = Color.White,
    onSecondary = Orange1000
)

private val LightColorPalette = lightColors(
    primary = Orange500,
    primaryVariant = Orange700,
    secondary = Blue500,
    background = Orange50,

    /* Other default colors to override
    background = Color.White,
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = Color.Black,
    onSurface = Color.Black,
    */
)

@Composable
fun BaseLedTheme(darkTheme: Boolean = true, content: @Composable () -> Unit) {
    val colors = if (darkTheme) {
        DarkColorPalette
    } else {
        LightColorPalette
    }

    MaterialTheme(
        colors = colors,
        typography = Typography,
        shapes = Shapes,
        content = {
            ProvideTextStyle(
                value = TextStyle(color = White),
                content = content
            )
        }
    )
}