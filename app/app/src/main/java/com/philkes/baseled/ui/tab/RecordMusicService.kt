package com.philkes.baseled.ui.tab

import android.Manifest
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.audiofx.Visualizer
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.philkes.baseled.R
import com.philkes.baseled.ui.MainActivity
import com.philkes.baseled.ui.component.FFT_NEEDED_PORTION
import com.philkes.baseled.ui.component.FFT_OFFSET
import com.philkes.baseled.ui.component.FFT_STEP
import com.philkes.baseled.ui.showToast
import kotlin.math.log10
import kotlin.math.min


// Source: https://betterprogramming.pub/what-is-foreground-service-in-android-3487d9719ab6
class RecordMusicService() : Service() {
    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    val visualizer: MutableState<Visualizer?> = mutableStateOf(null)
    var isRecording: Boolean = false


    val frequencyBands = 170

    val rgb: MutableState<FloatArray> = mutableStateOf(floatArrayOf(0f, 0f, 0f))
    val magnitudes: MutableState<FloatArray> = mutableStateOf(FloatArray(frequencyBands))

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action != null && intent.action.equals(
                ACTION_STOP, ignoreCase = true
            )
        ) {
            if (visualizer.value != null)
                visualizer.apply {

                    value!!.release()
                    value = null
                }
            stopSelf()
        } else {

            val channelId =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    createNotificationChannel("baseled_service", "BaseLed Service")
                } else {
                    // If earlier version channel ID is not used
                    // https://developer.android.com/reference/android/support/v4/app/NotificationCompat.Builder.html#NotificationCompat.Builder(android.content.Context)
                    ""
                }
            visualizer.apply {
                if (value == null) {
                    value = createAudioVisualizer(rgb, magnitudes, { })
//                TODO        value = createAudioVisualizer(rgb, magnitudes, onAction)
                }
                if (!value!!.enabled) {
                    value!!.apply { enabled = true }
                }
            }


            val pendingIntent: PendingIntent =
                Intent(this, RecordMusicService::class.java).let { notificationIntent ->
                    PendingIntent.getActivity(
                        this, 0, notificationIntent,
                        PendingIntent.FLAG_IMMUTABLE
                    )
                }

            val notification: Notification = Notification.Builder(this, channelId)
                .setContentTitle("BaseLed Music Record")
                .setContentText("Visualizing music with BaseLeds")
                .setSmallIcon(R.drawable.icon_notification)
                .setContentIntent(pendingIntent)
                .setTicker("Ticker")
                .build()

// Notification ID cannot be 0.
            val ONGOING_NOTIFICATION_ID = 100
            startForeground(ONGOING_NOTIFICATION_ID, notification)
        }
        return START_STICKY
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(channelId: String, channelName: String): String {
        val chan = NotificationChannel(
            channelId,
            channelName, NotificationManager.IMPORTANCE_NONE
        )
        chan.lightColor = android.graphics.Color.BLUE
        chan.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        val service = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        service.createNotificationChannel(chan)
        return channelId
    }


    private fun checkAudioRecordPermission(context: MainActivity, block: () -> Unit) {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    context,
                    Manifest.permission.RECORD_AUDIO
                )
            ) {
                context.showToast(context.getString(R.string.txt_audio_permission_hint))
            } else {
                ActivityCompat.requestPermissions(
                    context,
                    arrayOf(Manifest.permission.RECORD_AUDIO),
                    1
                )
            }
        } else {
            block()
        }
    }

    fun createAudioVisualizer(
        rgb: MutableState<FloatArray>,
        magnitudes: MutableState<FloatArray>,
        onAction: (color: androidx.compose.ui.graphics.Color) -> Unit
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
                                Log.d(
                                    "RGB Values",
                                    "r: ${newRgb[0]} g: ${newRgb[1]} b: ${newRgb[2]}"
                                )
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

    private fun convertFFTtoMagnitudes(
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

    private fun calculateMagnitude(r: Float, i: Float) =
        if (i == 0f && r == 0f) 0f else 10 * log10(r * r + i * i)


    companion object {
        const val ACTION_STOP = "RECORD_MUSIC_STOP"
    }
}