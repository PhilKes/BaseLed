package com.philkes.baseled.ui.tab

import android.content.Intent
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.philkes.baseled.R
import com.philkes.baseled.service.EspNowAction
import com.philkes.baseled.ui.MainActivity
import com.philkes.baseled.ui.State
import com.philkes.baseled.ui.component.AudioVisualizerComp
import com.philkes.baseled.ui.component.TextIconButton
import com.philkes.baseled.ui.tab.RecordMusicService.Companion.ACTION_STOP
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
fun MusicTab(
    state: MutableState<State>,
    debug: Boolean,
    onAction: (action: EspNowAction, rgbHex: String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.background)
            .wrapContentSize(Alignment.Center)
            .padding(20.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // TODO If recording start as background service with notification bar message
        // to keep recording even if other App is currently opened
        val isRecording = remember { mutableStateOf(false) }
        val context = LocalContext.current as MainActivity
        val maxFrequency = 5000.0f
        val step = 10
        val frequency = remember {
            mutableStateOf(0.0f)
        }
        val tone: MutableState<AudioTrack?> = remember {
            mutableStateOf(null)
        }
        val rgb: MutableState<androidx.compose.ui.graphics.Color> =
            remember { mutableStateOf(Color(0, 0, 0)) }

        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth(1f)
        ) {
            Column {
                Text(
                    text = buildString {
                        append(stringResource(R.string.txt_generate_tone))
                        append(frequency.value)
                        append(stringResource(R.string.txt_hertz))
                    },
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
        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth(1f)
        ) {
            AudioVisualizerComp(
                rgb.value,
                debug
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth(1f)
        ) {
            TextIconButton(
                fontSize = 24.sp,
                icon = if (isRecording.value) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                text = if (isRecording.value) stringResource(R.string.txt_stop) else stringResource(
                    R.string.txt_play
                ),
                onClick = {
                    if (!isRecording.value) {
                        val intentStart = Intent(context, RecordMusicService::class.java)
                        // TODO pass onAction
/*                        onAction(
                            EspNowAction.RGB,
                            EspRestClient.formatPayload(it, state.value.brightness)
                        )*/
                        context.startForegroundService(intentStart)
                        isRecording.value = true
                        /*checkAudioRecordPermission(context) {
                            isRecording.value = true
                        }*/
                    } else {
                        val intentStop = Intent(context, RecordMusicService::class.java)
                        intentStop.action = ACTION_STOP
                        context.startForegroundService(intentStop)
                        isRecording.value = false
//                        isRecording.value = false
                    }
                }
            )
        }
    }
}
