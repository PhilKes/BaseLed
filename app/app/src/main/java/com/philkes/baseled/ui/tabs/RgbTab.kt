package com.philkes.baseled.ui.tabs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.godaddy.android.colorpicker.HsvColor
import com.godaddy.android.colorpicker.harmony.ColorHarmonyMode
import com.godaddy.android.colorpicker.harmony.CustomColorPicker
import com.godaddy.android.colorpicker.harmony.HarmonyColorPicker
import com.philkes.baseled.Util
import com.philkes.baseled.service.EspNowAction
import com.philkes.baseled.ui.theme.BaseLedTheme

@Composable
fun RgbTab(
    initialColor: String, sendAction: (action: EspNowAction, rgbHex: String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.background)
            .wrapContentSize(Alignment.Center)
    ) {

        CustomColorPicker(
            harmonyMode = ColorHarmonyMode.NONE,
            modifier = Modifier
                .fillMaxWidth()
                .height(450.dp)
                .padding(10.dp),
            color = Color(initialColor.toInt(16)).copy(alpha=1.0f),
            onColorChanged = { color: HsvColor ->
                sendAction.invoke(EspNowAction.RGB, Util.intToHexStr(color.toColor().toArgb()))
            }
        )
    }
}

@Preview
@Composable
fun RgbTabPreview() {
    BaseLedTheme(darkTheme = true) {
        RgbTab("FFFFFF") { _, _ -> }
    }
}