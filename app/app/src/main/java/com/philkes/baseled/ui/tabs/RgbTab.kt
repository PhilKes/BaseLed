package com.philkes.baseled.ui.tabs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.skydoves.colorpicker.compose.ColorEnvelope
import com.github.skydoves.colorpicker.compose.HsvColorPicker
import com.github.skydoves.colorpicker.compose.rememberColorPickerController
import com.philkes.baseled.service.EspNowAction
import com.philkes.baseled.ui.theme.BaseLedTheme

@Composable
fun RgbTab(onAction: (action: EspNowAction, rgbHex: String) -> Unit) {
    val controller = rememberColorPickerController()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.background)
            .wrapContentSize(Alignment.Center)
    ) {

        HsvColorPicker(
            modifier = Modifier
                .fillMaxWidth()
                .height(450.dp)
                .padding(10.dp),
            controller = controller,
            onColorChanged = { colorEnvelope: ColorEnvelope ->
                onAction.invoke(EspNowAction.RGB, "0x${Integer.toHexString(colorEnvelope.color.toArgb()).substring(2)}")
            }
        )
    }
}

@Preview
@Composable
fun RgbTabPreview() {
    BaseLedTheme(darkTheme = true) {
        RgbTab { _, _ -> }
    }
}