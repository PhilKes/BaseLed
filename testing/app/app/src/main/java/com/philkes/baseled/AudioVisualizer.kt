package ir.imn.audiovisualizer.visualizer

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Paint.ANTI_ALIAS_FLAG
import android.graphics.RectF
import android.media.MediaPlayer
import android.media.audiofx.Visualizer
import android.util.AttributeSet
import android.util.Log
import android.view.View
import kotlin.math.log10
import kotlin.math.max


class AudioVisualizer {

    private val smoothingFactor = 0.2f

    private var magnitudes = floatArrayOf()

    private var visualizer: Visualizer? = null

    private val dataCaptureListener = object : Visualizer.OnDataCaptureListener {

        override fun onFftDataCapture(v: Visualizer?, data: ByteArray?, sampleRate: Int) {
            data?.let {
                magnitudes = convertFFTtoMagnitudes(data)
                Log.d("FFT", magnitudes.joinToString { it.toString() })
//                visualizeData()
            }
        }

        override fun onWaveFormDataCapture(v: Visualizer?, data: ByteArray?, sampleRate: Int) = Unit
    }

    fun link() {
        if (visualizer != null) return
        visualizer = Visualizer(0)
            .apply {
                captureSize = Visualizer.getCaptureSizeRange()[1]
                setDataCaptureListener(
                    dataCaptureListener,
                    Visualizer.getMaxCaptureRate() * 2 / 3,
                    false,
                    true
                )
                enabled = true
            }

        //mediaPlayer.setOnCompletionListener { visualizer?.enabled = false }
    }


    private val maxMagnitude = calculateMagnitude(128f, 128f)

    private fun convertFFTtoMagnitudes(fft: ByteArray): FloatArray {
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

    private fun calculateMagnitude(r: Float, i: Float) =
        if (i == 0f && r == 0f) 0f else 10 * log10(r * r + i * i)


    companion object {
        private const val FFT_STEP = 2
        private const val FFT_OFFSET = 2
        private const val FFT_NEEDED_PORTION = 3 // 1/3
    }
}