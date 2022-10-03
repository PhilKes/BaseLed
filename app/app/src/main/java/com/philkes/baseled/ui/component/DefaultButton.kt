package com.philkes.baseled.ui.component

import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

@Composable
fun DefaultButton(
    modifier: Modifier = Modifier,
    text: String = "",
    color: Color = MaterialTheme.colors.secondary,
    onClick: (() -> Unit)? = null,
    fontSize: TextUnit = 16.sp
) {
    Button(
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(backgroundColor = color),
        onClick = onClick ?: {}
    ) {
        Text(text, fontSize = fontSize)
    }
}