package com.philkes.baseled.ui.component

import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

@Composable
fun TextIconButton(
    modifier: Modifier = Modifier,
    text: String = "",
    icon: ImageVector? = null,
    color: Color = MaterialTheme.colors.secondary,
    onClick: (() -> Unit)? = null,
    fontSize: TextUnit = 16.sp
) {
    Button(
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(backgroundColor = color),
        onClick = onClick ?: {}
    ) {
        if (icon != null) {
            Icon(icon, "button-icon")
        }
        Text(text, fontSize = fontSize)
    }
}