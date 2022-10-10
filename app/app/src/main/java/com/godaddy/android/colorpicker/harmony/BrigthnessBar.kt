package com.godaddy.android.colorpicker.harmony

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Slider
import androidx.compose.material.SliderDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

@Composable
internal fun BrightnessBar(
    modifier: Modifier = Modifier,
    onValueChange: (Int) -> Unit,
    currentBrightness: Int
) {
    var dragCounter = 0;
    Text(
        "Brightness",
        modifier = Modifier.fillMaxWidth(),
        textAlign = TextAlign.Center,
        fontSize = 20.sp
    )
    Slider(
        modifier = modifier.padding(top = 0.dp),
        value = currentBrightness.toFloat(),
        valueRange = 0f..255f,
        onValueChange = {
            if ((++dragCounter) % 3 == 0) {
                val rounded = it.roundToInt()
                onValueChange(rounded)
            }
        },
        colors = SliderDefaults.colors(
            activeTrackColor = MaterialTheme.colors.primary,
            thumbColor = MaterialTheme.colors.primary
        )
    )

}
