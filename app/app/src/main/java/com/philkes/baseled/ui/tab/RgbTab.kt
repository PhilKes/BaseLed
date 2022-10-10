package com.philkes.baseled.ui.tab

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.godaddy.android.colorpicker.HsvColor
import com.godaddy.android.colorpicker.harmony.BrightnessBar
import com.godaddy.android.colorpicker.harmony.ColorHarmonyMode
import com.godaddy.android.colorpicker.harmony.CustomColorPicker
import com.philkes.baseled.service.EspNowAction
import com.philkes.baseled.service.EspRestClient
import com.philkes.baseled.ui.State
import com.philkes.baseled.ui.theme.BaseLedTheme

@Composable
fun RgbTab(
    state: MutableState<State>,
    sendAction: (action: EspNowAction, payload: String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.background)
            .wrapContentSize(Alignment.Center)
            .padding(start = 16.dp, end = 16.dp)
    ) {


        CustomColorPicker(
            harmonyMode = ColorHarmonyMode.NONE,
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.5f)
                .padding(10.dp),
            color = state.value.color,
            onColorChanged = { color: HsvColor ->
                state.value = state.value.copy(color = color.toColor())
                sendAction.invoke(
                    EspNowAction.RGB,
                    EspRestClient.formatPayload(state.value.color, state.value.brightness)
                )
            }
        )
        BrightnessBar(
            modifier = Modifier
                .padding(top = 4.dp)
                .fillMaxWidth()
                .fillMaxHeight(0.1f),
            onValueChange = { value ->
                state.value = state.value.copy(brightness = value)
                sendAction.invoke(
                    state.value.action,
                    EspRestClient.formatPayload(state.value.color, state.value.brightness)
                )
            },
            currentBrightness = state.value.brightness
        )
        Spacer(modifier = Modifier.height(20.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.45f),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AnimationItem(
                ANIMATIONS[0],
                active = state.value.action == EspNowAction.RGB_WHEEL
            ) {
                sendAction(
                    EspNowAction.RGB_WHEEL,
                    EspRestClient.formatPayload(state.value.color, state.value.brightness)
                )
            }
        }

    }

}

@Preview
@Composable
fun RgbTabPreview() {
    BaseLedTheme(darkTheme = true) {
        RgbTab(remember {
            mutableStateOf(State())
        }) { _, _ -> }

    }
}