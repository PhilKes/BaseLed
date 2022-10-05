package com.philkes.baseled.ui.component

import android.content.Context
import android.media.AudioManager
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
import androidx.compose.ui.graphics.Color.Companion.Blue
import androidx.compose.ui.graphics.Color.Companion.Green
import androidx.compose.ui.graphics.Color.Companion.Red
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlin.math.log10

const val FFT_STEP = 2
const val FFT_OFFSET = 4
const val FFT_NEEDED_PORTION = 3// 1/3
const val MAX_VOLUME = 16

@Composable
fun AudioVisualizerComp(isRecording: Boolean) {

    // on below line we are creating a
    // variable to get current context.
    val ctx = LocalContext.current

    // on below line we are initializing our audio manager.
    val audioManager: AudioManager = remember {
        ctx.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    }


    val visualizer: MutableState<Visualizer?> = remember {
        mutableStateOf(null)
    }

    val rgb = remember { mutableStateOf(floatArrayOf(0f, 0f, 0f)) }

    if (isRecording) {
        if (visualizer.value == null) {
            visualizer.value = createAudioVisualizer(rgb, audioManager)
        }
        if (!visualizer.value!!.enabled) {
            visualizer.value!!.apply { enabled = true }
        }
    } else if (visualizer.value != null && visualizer.value!!.enabled) {
        visualizer.value!!.release()
        visualizer.value = null
    }
    val maxBarHeight = 300
    val red = (maxBarHeight * rgb.value[0]*2)
    val green = (maxBarHeight * rgb.value[1]*2)
    val blue = (maxBarHeight * rgb.value[2]*2)

    Row(modifier = Modifier.border(1.0.dp, Color.White )) {
        Column() {
            Spacer(modifier = Modifier.height((maxBarHeight - red).dp))
            Box(
                modifier = Modifier
                    .width(100.dp)
                    .height(red.dp)
//                    .height(200.dp)
                    .background(Red)
            )

        }
        Column() {
            Spacer(modifier = Modifier.height((maxBarHeight - green).dp))
            Box(
                modifier = Modifier
                    .width(100.dp)
                    .height(green.dp)
                    .background(Green)
            )
        }
        Column() {
            Spacer(modifier = Modifier.height((maxBarHeight - blue).dp))
            Box(
                modifier = Modifier
                    .width(100.dp)
                    .height(blue.dp)
                    .background(Blue)
            )
            Spacer(modifier = Modifier.height(100.dp))

        }
    }

}

fun createAudioVisualizer(rgb: MutableState<FloatArray>, audioManager: AudioManager): Visualizer {
    val smoothingFactor = 0.2f
    var magnitudes = floatArrayOf()
    val maxMagnitude = calculateMagnitude(128f, 128f)
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
                        data?.let {
                            magnitudes = convertFFTtoMagnitudes(
                                magnitudes,
                                maxMagnitude,
                                smoothingFactor,
                                data
                            )
                            val stepsPerColor = magnitudes.size / 3
                            val newRgb = floatArrayOf(0f, 0f, 0f)
                            for (i in magnitudes.indices) {
                                if (i < stepsPerColor) {
                                    //RED
                                    newRgb[0] += magnitudes[i]
                                } else if (i < stepsPerColor * 2) {
                                    //GREEN
                                    newRgb[1] += magnitudes[i]
                                } else {
                                    //BLUE
                                    newRgb[2] += magnitudes[i]
                                }
                            }
                            newRgb[0] = newRgb[0] / stepsPerColor
                            newRgb[1] = newRgb[1] / stepsPerColor
                            newRgb[2] = newRgb[2] / stepsPerColor
                            /* Giving more INTENSITY to the most DOMINANT frequencies (and reducing the rest):*/

/*                            if ((newRgb[0] > newRgb[1]) && (newRgb[1] > newRgb[2])) {
                                newRgb[0] = newRgb[0] * 1.2f
                                newRgb[1] = newRgb[1] * 0.8f;
                                newRgb[2] = newRgb[2] * 0.8f;
                            } else if ((newRgb[2] > newRgb[0]) && (newRgb[2] > newRgb[1])) {
                                newRgb[2] = newRgb[2] * 1.2f;
                                newRgb[1] = newRgb[1] * 0.8f;
                                newRgb[0] = newRgb[0] * 0.8f;
                            } else if ((newRgb[1] > newRgb[2]) && (newRgb[1] > newRgb[0])) {
                                newRgb[1] = newRgb[1] * 1.2f;
                                newRgb[0] = newRgb[0] * 0.8f;
                                newRgb[2] = newRgb[2] * 0.8f;
                            }*/
/*                            newRgb[0]  = 255 * convBrightness(newRgb[0]);
                            newRgb[1]  = 255 * convBrightness(newRgb[1]);
                            newRgb[2]  = 255 * convBrightness(newRgb[2]);*/
                            rgb.value = newRgb
                            Log.d("AudioVisualizer", magnitudes.joinToString { it.toString() })
                            Log.d("AudioVisualizer", rgb.value.joinToString { it.toString() })
                        }
                    }

                    override fun onWaveFormDataCapture(
                        v: Visualizer?,
                        data: ByteArray?,
                        sampleRate: Int
                    ) = Unit
                },
                Visualizer.getMaxCaptureRate() * 2 / 3,
                false,
                true
            )
        }
}

fun convBrightness(b: Float): Float {
    var c =
        b / 614 // The maximun intensity value in theory is 31713 (but we are never having the volume that high)
    if (c < 0.2f) c = 0.0f else if (c > 1) c = 1.00f
    return c
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
