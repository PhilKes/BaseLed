package com.godaddy.android.colorpicker.harmony

import android.util.Log
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
import com.godaddy.android.colorpicker.HsvColor

@Composable
internal fun BrightnessBar(
    modifier: Modifier = Modifier,
    onValueChange: (Float) -> Unit,
    currentColor: HsvColor
) {
    var dragCounter = 0;
    Text(
        "Brightness",
        modifier = Modifier.fillMaxWidth(),
        textAlign = TextAlign.Center,
        fontSize = 20.sp
    )
    Slider(
        modifier = modifier.padding(top= 0.dp),
        value = currentColor.value,
        onValueChange = {
            if ((++dragCounter) % 3 == 0) {
                Log.d("Slider", it.toString())
                onValueChange(it)
            }
        },
        colors = SliderDefaults.colors(
            activeTrackColor = MaterialTheme.colors.primary,
            thumbColor = MaterialTheme.colors.primary
        )
    )

}
