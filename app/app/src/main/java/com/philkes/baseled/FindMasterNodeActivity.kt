package com.philkes.baseled

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.philkes.baseled.service.EspRestClient
import com.philkes.baseled.ui.MainActivity
import com.philkes.baseled.ui.component.TextIconButton
import com.philkes.baseled.ui.dialog.EditSettingsDialog
import com.philkes.baseled.ui.theme.BaseLedTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import javax.inject.Inject

@AndroidEntryPoint
class FindMasterNodeActivity : ComponentActivity() {

    private val TAG = "FindMasterNodeActivity"

    @Inject
    lateinit var espRestClient: EspRestClient

    @Inject
    lateinit var settings: Settings

    private var activeDialog: MutableState<Dialog> = mutableStateOf(Dialog.NONE)

    private var currentJob: MutableState<Job?> = mutableStateOf(null)

    enum class Dialog(val id: Int) {
        NONE(0),
        ERROR(1),
        SETTINGS(2);
    }

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BaseLedTheme(darkTheme = true) {
                Content(
                    onSearchMasterNode = ::searchMasterNode,
                    onChangeDialog = {
                        lifecycleScope.launch {
                            changeActiveDialog(it)

                        }
                    },
                    activeDialog = activeDialog,
                    settings = settings,
                    jobActive = if (currentJob.value != null) currentJob.value!!.isActive else false
                )
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onStart() {
        super.onStart()
        searchMasterNode()
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun searchMasterNode() {
        currentJob.value = lifecycleScope.launch(Dispatchers.IO) {
            try {
                val foundMasterNode = espRestClient.searchMasterNodeIp(this)
                if (foundMasterNode == null) {
                    activeDialog.value = Dialog.ERROR
                } else {
                    settings.lastMasterIp = foundMasterNode
                    val intent = Intent(this@FindMasterNodeActivity, MainActivity::class.java)
                    startActivity(intent)
                }
            } catch (e: CancellationException) {
                Log.d(TAG, e.message!!)
            }
            currentJob.value = null
        }
    }

    private fun changeActiveDialog(newActiveDialog: Dialog) {
        if (currentJob.value != null && currentJob.value!!.isActive) {
            currentJob.value!!.cancel()
            currentJob.value = null
        }
        activeDialog.value = newActiveDialog
    }
}


@Composable
fun Content(
    onSearchMasterNode: () -> Unit,
    onChangeDialog: (FindMasterNodeActivity.Dialog) -> Unit,
    activeDialog: MutableState<FindMasterNodeActivity.Dialog>,
    jobActive: Boolean,
    settings: Settings
) {
    Scaffold(
        modifier = Modifier
            .background(MaterialTheme.colors.primary),
        topBar = { },
        floatingActionButton = {
            FloatingActionButton(
                contentColor = MaterialTheme.colors.onSurface,
                onClick = { onChangeDialog(FindMasterNodeActivity.Dialog.SETTINGS) }) {
                Icon(Icons.Filled.Settings, "settings")
            }
        }
    ) { _ ->
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(start = 10.dp, end = 10.dp)
                .fillMaxHeight(1f)
                .fillMaxWidth(1f)
        ) {
            Spacer(modifier = Modifier.fillMaxHeight(0.1f))
            Text(
                stringResource(R.string.app_name),
                fontSize = 46.sp,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.fillMaxHeight(0.02f))
            Image(
                painter = painterResource(id = R.drawable.icon),
                contentDescription = "icon",
                modifier = Modifier.fillMaxHeight(0.25f)
            )
            Spacer(modifier = Modifier.fillMaxHeight(0.075f))

            Crossfade(targetState = jobActive, animationSpec = tween(300)) { active ->
                if (active) {
                    val visibility by rememberInfiniteTransition().animateFloat(
                        initialValue = 0f,
                        targetValue = 1.0f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1000, easing = LinearEasing),
                            repeatMode = RepeatMode.Reverse
                        )
                    )
                    Icon(
                        Icons.Default.Wifi,
                        "warning",
                        modifier = Modifier.size(160.dp),
                        tint = MaterialTheme.colors.secondary.copy(alpha = visibility)
                    )
                } else {
                    IconButton(onClick = onSearchMasterNode) {
                        Icon(
                            Icons.Filled.Refresh,
                            "warning",
                            modifier = Modifier.size(160.dp),
                            tint = MaterialTheme.colors.secondary
                        )
                    }
                }
            }

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxHeight(0.25f)
            ) {
                Text(
                    text = if (jobActive) stringResource(R.string.txt_search_master) else stringResource(
                                            R.string.txt_retry_search),
                    textAlign = TextAlign.Center,
                    fontSize =  22.sp
                )
            }
            MasterNotFoundDialog(
                open = activeDialog.value == FindMasterNodeActivity.Dialog.ERROR,
                onConfirm = {
                    onChangeDialog(FindMasterNodeActivity.Dialog.NONE)
                    onSearchMasterNode()
                },
                onClose = { onChangeDialog(FindMasterNodeActivity.Dialog.NONE) }
            )
            EditSettingsDialog(
                open = activeDialog.value == FindMasterNodeActivity.Dialog.SETTINGS,
                settings = settings,
                onConfirm = {
                    onChangeDialog(FindMasterNodeActivity.Dialog.NONE)
                    onSearchMasterNode()
                },
                onClose = { onChangeDialog(FindMasterNodeActivity.Dialog.NONE) }
            )
        }
    }
}

@Composable
fun MasterNotFoundDialog(
    open: Boolean,
    onConfirm: () -> Unit,
    onClose: () -> Unit,
) {
    if (open) {
        AlertDialog(
            onDismissRequest = {
                onClose()
            },
            title = {
                Text(
                    text = stringResource(R.string.txt_master_not_found),
                    textAlign = TextAlign.Center,
                    fontSize = 20.sp,
                )
            },
            text = {
                Row {
                    Icon(Icons.Filled.Warning, "warning", modifier = Modifier.size(30.dp))
                    Spacer(modifier = Modifier.width(20.dp))
                    Text(stringResource(R.string.txt_master_not_found_hint))
                    Spacer(modifier = Modifier.height(40.dp))
                }
            },
            confirmButton = {
                TextIconButton(
                    text = stringResource(R.string.txt_rety),
                    onClick = {
                        onConfirm()
                    })
            },
            dismissButton = {
                TextIconButton(
                    text = stringResource(R.string.txt_edit_settings),
                    color = MaterialTheme.colors.primary,
                    onClick = {
                        onClose()
                    })
            }

        )
    }
}

