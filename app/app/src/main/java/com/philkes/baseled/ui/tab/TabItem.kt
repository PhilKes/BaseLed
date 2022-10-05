package com.philkes.baseled.ui.tab

import androidx.compose.runtime.Composable
import com.philkes.baseled.service.EspNowAction

typealias ComposableFun = @Composable () -> Unit

sealed class TabItem(var icon: Int?, var title: String, var screen: ComposableFun) {
    class Rgb(initialColor: String, onAction: (action: EspNowAction, rgbHex: String) -> Unit) :
        TabItem(null,
            "RGB",
            { RgbTab(initialColor, onAction) })

    object Animation : TabItem(
        null,
        "Animation",
        { AnimationTab() })

    class Music(debug: Boolean, onAction: (action: EspNowAction, rgbHex: String) -> Unit) : TabItem(
        null,
        "Music",
        { MusicTab(debug, onAction) })
}