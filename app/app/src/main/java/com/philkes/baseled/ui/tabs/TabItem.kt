package com.philkes.baseled.ui.tabs

import androidx.compose.runtime.Composable
import com.philkes.baseled.R
import com.philkes.baseled.service.EspNowAction

typealias ComposableFun = @Composable () -> Unit

sealed class TabItem(var icon: Int?, var title: String, var screen: ComposableFun) {
    class Rgb(onAction: (action: EspNowAction, rgbHex: String) -> Unit) : TabItem(null,
        "RGB",
        { RgbTab(onAction) })

    object Animation : TabItem(
       null,
        "Animation",
        { AnimationTab() })

    object Music: TabItem(
        null,
        "Music",
        { MusicTab() })
}