package com.philkes.baseled.ui.tab

import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.philkes.baseled.ui.MainActivity
import com.philkes.baseled.ui.component.AudioVisualizerComp
import com.philkes.baseled.ui.component.TextIconButton
import com.philkes.baseled.ui.showToast

@Composable
fun MusicTab() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.background)
            .wrapContentSize(Alignment.Center)
    ) {
        val isRecording = remember { mutableStateOf(false) }
        val context = LocalContext.current as MainActivity
        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth(1f)
        ) {
            AudioVisualizerComp(isRecording.value)
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