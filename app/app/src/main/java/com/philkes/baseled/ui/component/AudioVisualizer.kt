package com.philkes.baseled.ui.component

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

const val FFT_STEP = 2
const val FFT_OFFSET = 4
const val FFT_NEEDED_PORTION = 3// 1/3
const val MAX_VOLUME = 16

@Composable
fun AudioBar(
    maxHeight: Float,
    height: Float,
    color: Color,
    width: Float = 100.0f,
    percentWidth: Float? = null
) {
    var modifier = Modifier
        .height(height.dp)
        .background(color)
    modifier = if (percentWidth != null) {
        modifier.fillMaxWidth(percentWidth)
    } else {
        modifier.width(width.dp)
    }
    Column() {
        Spacer(modifier = Modifier.height((maxHeight - height).dp))
        Box(
            modifier = modifier
        )

    }
}

@Composable
fun AudioVisualizerComp(
    rgb: Color,
    debug: Boolean
) {
    val ctx = LocalContext.current


    val maxBarHeight = if (debug) 150.0f else 300.0f
    var red = (maxBarHeight * (rgb.red / rgb.colorSpace.getMaxValue(0)))
    var green = (maxBarHeight * rgb.green / rgb.colorSpace.getMaxValue(1))
    var blue = (maxBarHeight * rgb.blue / rgb.colorSpace.getMaxValue(2))
    Log.d("Bar RGB Values", "r: ${red} g: ${green} b: ${blue}")

    Column(modifier = Modifier.fillMaxWidth(0.8f)) {
        /* if (true) {
             val colors = listOf(
                 Color.Red,
                 Color.Blue,
                 Color.Green,
                 Color.Cyan,
                 Color.Yellow,
                 Color.White,
                 Color.Gray
             )
             Row(
                 modifier = Modifier
                     .border(1.0.dp, Color.White)
             ) {
                 for ((idx, magnitude) in magnitudes.value.withIndex()) {
                     AudioBar(
                         maxHeight = maxBarHeight,
                         height = magnitude * maxBarHeight,
                         color = colors[idx % colors.size],
                         percentWidth = 1.0f / magnitudes.value.size
                     )
                 }
             }
         }*/
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier
                .border(1.0.dp, Color.White)
        ) {
            AudioBar(maxHeight = maxBarHeight, height = red, color = Color.Red)
            AudioBar(maxHeight = maxBarHeight, height = green, color = Color.Green)
            AudioBar(maxHeight = maxBarHeight, height = blue, color = Color.Blue)
        }
    }

}


