package com.philkes.baseled.ui.tab

import android.Manifest
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Slider
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.philkes.baseled.service.EspNowAction
import com.philkes.baseled.ui.MainActivity
import com.philkes.baseled.ui.component.AudioVisualizerComp
import com.philkes.baseled.ui.component.TextIconButton
import com.philkes.baseled.ui.showToast
import kotlin.math.sin

fun generateTone(freqHz: Double, durationMs: Int): AudioTrack {
    val count = (44100.0 * 2.0 * (durationMs / 1000.0)).toInt() and 1.inv()
    val samples = ShortArray(count)
    var i = 0
    while (i < count) {
        val sample = (sin(2 * Math.PI * i / (44100.0 / freqHz)) * 0x7FFF).toInt().toShort()
        samples[i + 0] = sample
        samples[i + 1] = sample
        i += 2
    }
    val track = AudioTrack(
        AudioManager.STREAM_MUSIC, 44100,
        AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_16BIT,
        count * (java.lang.Short.SIZE / 8), AudioTrack.MODE_STATIC
    )
    track.write(samples, 0, count)
    return track
}

@Composable
fun MusicTab(debug: Boolean, onAction: (action: EspNowAction, rgbHex: String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.background)
            .wrapContentSize(Alignment.Center)
            .verticalScroll(rememberScrollState())
    ) {
        val isRecording = remember { mutableStateOf(false) }
        val context = LocalContext.current as MainActivity
        val maxFrequency = 5000.0f
        val step = 10
        if (true) {
            val frequency = remember {
                mutableStateOf(0.0f)
            }
            val tone: MutableState<AudioTrack?> = remember {
                mutableStateOf(null)
            }
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth(1f)
            ) {
                Column {
                    Text(
                        text = "Generate Tone: ${frequency.value} Hz",
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Slider(
                        value = frequency.value,
                        onValueChange = {
                            frequency.value = it
                            tone.value?.apply { stop() }
                            if (it > 0) {
                                tone.value = generateTone(it.toDouble(), 10000)
                                tone.value!!.setLoopPoints(0, tone.value!!.bufferSizeInFrames, -1)
                                tone.value!!.play()
                            }
                        },
                        valueRange = 0.0f..maxFrequency,
                        steps = (maxFrequency / step).toInt()
                    )
                }
            }
        }
        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth(1f)
        ) {
            AudioVisualizerComp(isRecording.value, onAction, debug)
        }
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth(1f)
        ) {
            TextIconButton(
                fontSize = 24.sp,
                icon = if (isRecording.value) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                text = if (isRecording.value) "Stop" else "Play",
                onClick = {
                    if (!isRecording.value) {
                        CheckAudioRecordPermission(context) {
                            isRecording.value = true
                        }
                    } else {
                        isRecording.value = false
                    }
                }
            )
        }
    }
}

fun CheckAudioRecordPermission(context: MainActivity, block: () -> Unit) {
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
            context.showToast("You need to grant the record audio permission in order to use the Music mode!")
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