package com.philkes.baseled.ui.component

import android.media.audiofx.Visualizer
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlin.math.log10
import kotlin.math.min

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
    isRecording: Boolean,
    onAction: (color: Color) -> Unit,
    debug: Boolean
) {
    val ctx = LocalContext.current

    val frequencyBands = 170
    val visualizer: MutableState<Visualizer?> = remember {
        mutableStateOf(null)
    }

    val rgb = remember { mutableStateOf(floatArrayOf(0f, 0f, 0f)) }
    val magnitudes = remember { mutableStateOf(FloatArray(frequencyBands)) }

    visualizer.apply {
        if (isRecording) {
            if (value == null) {
                value = createAudioVisualizer(rgb, magnitudes, onAction)
            }
            if (!value!!.enabled) {
                value!!.apply { enabled = true }
            }
        } else if (value != null && value!!.enabled) {
            value!!.release()
            value = null
        }
    }

    val maxBarHeight = if (debug) 150.0f else 300.0f
    var red = (maxBarHeight * rgb.value[0])
    var green = (maxBarHeight * rgb.value[1])
    var blue = (maxBarHeight * rgb.value[2])
    Log.d("Bar RGB Values", "r: ${red} g: ${green} b: ${blue}")

    Column(modifier = Modifier.fillMaxWidth(0.8f)) {
        if (true) {
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
        }
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

fun createAudioVisualizer(
    rgb: MutableState<FloatArray>,
    magnitudes: MutableState<FloatArray>,
    onAction: (color: Color) -> Unit
): Visualizer {
    val smoothingFactor = 0.2f
    val maxMagnitude = calculateMagnitude(128f, 128f)
    Log.d("Visualizer Range", Visualizer.getMaxCaptureRate().toString())
    var startTime = System.currentTimeMillis();
    var endtime = System.currentTimeMillis();
    return Visualizer(0)
        .apply {
            captureSize = Visualizer.getCaptureSizeRange()[1]
            setDataCaptureListener(
                object : Visualizer.OnDataCaptureListener {
                    override fun onFftDataCapture(
                        v: Visualizer?,
                        data: ByteArray?,
                        sampleRate: Int
                    ) {
                        endtime = System.currentTimeMillis()
                        Log.d("TIMING", endtime.minus(startTime).toString())
                        data?.let {
                            magnitudes.value = convertFFTtoMagnitudes(
                                magnitudes.value,
                                maxMagnitude,
                                smoothingFactor,
                                data
                            )
                            val newRgb = floatArrayOf(0f, 0f, 0f)

                            val redRange = 4..10;
                            val greenRange = 50..80;
                            val blueRange = 120..169;
                            newRgb[0] = calcMeanOfRange(magnitudes.value, redRange) * 0.8f
                            newRgb[1] = calcMeanOfRange(magnitudes.value, greenRange) * 1.2f
                            newRgb[2] = calcMeanOfRange(magnitudes.value, blueRange) * 1.6f

                            if (newRgb[0] > newRgb[1] && newRgb[0] > newRgb[2]) {
                                newRgb[0] = min(1.0f, newRgb[0] * 1.3f)
                                newRgb[1] = min(1.0f, newRgb[1] * 0.8f)
                                newRgb[2] = min(1.0f, newRgb[2] * 0.8f)
                            } else if (newRgb[1] > newRgb[0] && newRgb[1] > newRgb[2]) {
                                newRgb[1] = min(1.0f, newRgb[1] * 1.3f)
                                newRgb[0] = min(1.0f, newRgb[0] * 0.8f)
                                newRgb[2] = min(1.0f, newRgb[2] * 0.8f)
                            } else if (newRgb[2] > newRgb[0] && newRgb[2] > newRgb[1]) {
                                newRgb[2] = min(1.0f, newRgb[2] * 1.3f)
                                newRgb[0] = min(1.0f, newRgb[0] * 0.8f)
                                newRgb[1] = min(1.0f, newRgb[1] * 0.8f)
                            }

                            rgb.value = newRgb
//                            Log.d("AudioVisualizer", rgb.value.joinToString { it.toString() })
                            Log.d("RGB Values", "r: ${newRgb[0]} g: ${newRgb[1]} b: ${newRgb[2]}")
                            startTime = System.currentTimeMillis()
                            onAction(Color(newRgb[0], newRgb[1], newRgb[2]))
                        }
                    }

                    private fun calcMeanOfRange(data: FloatArray, range: IntRange): Float {
                        var x = 0.0f
                        for (i in range) {
                            x = x + data[i]
                        }
                        return x / (range.last - range.first)
                    }

                    override fun onWaveFormDataCapture(
                        v: Visualizer?,
                        data: ByteArray?,
                        sampleRate: Int
                    ) = Unit
                },
                Visualizer.getMaxCaptureRate(),
                false,
                true
            )
        }
}

fun convertFFTtoMagnitudes(
    magnitudes: FloatArray,
    maxMagnitude: Float,
    smoothingFactor: Float,
    fft: ByteArray
): FloatArray {
    if (fft.isEmpty()) {
        return floatArrayOf()
    }

    val n: Int = fft.size / FFT_NEEDED_PORTION
    val curMagnitudes = FloatArray(n / 2)

    var prevMagnitudes = magnitudes
    if (prevMagnitudes.isEmpty()) {
        prevMagnitudes = FloatArray(n)
    }

    for (k in 0 until n / 2 - 1) {
        val index = k * FFT_STEP + FFT_OFFSET
        val real: Byte = fft[index]
        val imaginary: Byte = fft[index + 1]

        val curMagnitude = calculateMagnitude(real.toFloat(), imaginary.toFloat())
        curMagnitudes[k] = curMagnitude + (prevMagnitudes[k] - curMagnitude) * smoothingFactor
    }
    return curMagnitudes.map { it / maxMagnitude }.toFloatArray()
}

fun calculateMagnitude(r: Float, i: Float) =
    if (i == 0f && r == 0f) 0f else 10 * log10(r * r + i * i)
