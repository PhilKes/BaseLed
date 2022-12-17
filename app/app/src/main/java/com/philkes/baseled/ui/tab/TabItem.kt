package com.philkes.baseled.ui.tab

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import com.philkes.baseled.service.EspNowAction
import com.philkes.baseled.ui.State

typealias ComposableFun = @Composable () -> Unit

sealed class TabItem(
    var icon: Int?,
    var title: String,
    var screen: ComposableFun,
    val state: MutableState<State>,
    val onAction: (action: EspNowAction, rgbHex: String) -> Unit
) {
    class Rgb(
        state: MutableState<State>,
        onAction: (action: EspNowAction, rgbHex: String) -> Unit
    ) :
        TabItem(
            null,
            "RGB",
            { RgbTab(state, onAction) }, state, onAction
        )

    class Music(
        state: MutableState<State>,
        onAction: (action: EspNowAction, rgbHex: String) -> Unit,
        debug: Boolean
    ) : TabItem(
        null,
        "Music",
        { MusicTab(state, debug, onAction) }, state, onAction
    )
}