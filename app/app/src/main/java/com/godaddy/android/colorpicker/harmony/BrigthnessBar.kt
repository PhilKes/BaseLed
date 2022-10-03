package com.godaddy.android.colorpicker.harmony

import android.util.Log
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Slider
import androidx.compose.material.SliderDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.godaddy.android.colorpicker.HsvColor

@Composable
internal fun BrightnessBar(
    modifier: Modifier = Modifier,
    onValueChange: (Float) -> Unit,
    currentColor: HsvColor
) {
    var dragCounter = 0;
    Slider(
        modifier = modifier,
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
