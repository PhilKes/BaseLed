package com.philkes.baseled.ui.tabs

import androidx.compose.runtime.Composable
import com.philkes.baseled.R

typealias ComposableFun = @Composable () -> Unit

sealed class TabItem(var icon: Int?, var title: String, var screen: ComposableFun) {
    object Rgb : TabItem(null,
        "RGB",
        { RgbTab() })

    object Animation : TabItem(
       null,
        "Animation",
        { AnimationTab() })

    object Music: TabItem(
        null,
        "Music",
        { MusicTab() })
}