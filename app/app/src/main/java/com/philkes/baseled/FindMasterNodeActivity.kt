package com.philkes.baseled

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.philkes.baseled.ui.MainActivity
import com.philkes.baseled.service.EspRestClient
import com.philkes.baseled.ui.component.DefaultButton
import com.philkes.baseled.ui.dialog.EditSettingsDialog
import com.philkes.baseled.ui.theme.BaseLedTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class FindMasterNodeActivity : ComponentActivity() {
    @Inject
    lateinit var espRestClient: EspRestClient

    @Inject
    lateinit var settings: Settings

    private var activeDialog: MutableState<Dialog> = mutableStateOf(Dialog.NONE)

    private var currentJob: Job? = null

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
                    onChangeDialog = ::changeActiveDialog,
                    activeDialog = activeDialog,
                    settings = settings
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
        currentJob = lifecycleScope.launch(Dispatchers.IO) {
            val foundMasterNode = espRestClient.searchMasterNodeIp()
            if (foundMasterNode == null) {
                activeDialog.value = Dialog.ERROR
            } else {
                settings.lastMasterIp = foundMasterNode
                val intent = Intent(this@FindMasterNodeActivity, MainActivity::class.java)
                startActivity(intent)
            }
        }
    }

    private fun changeActiveDialog(newActiveDialog: Dialog) {
        if (currentJob !== null && currentJob!!.isActive) {
            currentJob!!.cancel()
        }
        activeDialog.value = newActiveDialog

    }
}


@Composable
fun Content(
    onSearchMasterNode: () -> Unit,
    onChangeDialog: (FindMasterNodeActivity.Dialog) -> Unit,
    activeDialog: MutableState<FindMasterNodeActivity.Dialog>,
    settings: Settings
) {
    Scaffold(
        modifier = Modifier.background(MaterialTheme.colors.primary),
        topBar = { },
        floatingActionButton = {
            FloatingActionButton(
                contentColor = MaterialTheme.colors.onSurface,
                onClick = { activeDialog.value = FindMasterNodeActivity.Dialog.SETTINGS }) {
                Icon(Icons.Filled.Settings, "settings")
            }
        }
    ) { padding ->
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(padding)
                .fillMaxHeight(1f)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxHeight(0.4f)
            ) {
                Text(
                    text = "Connecting to BaseLed Master Node...",
                    textAlign = TextAlign.Center,
                    fontSize = 32.sp
                )
            }
            if (activeDialog.value == FindMasterNodeActivity.Dialog.NONE) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(64.dp),
                    strokeWidth = 10.dp,
                    color = MaterialTheme.colors.secondary
                )
            } else {
                Icon(
                    Icons.Filled.Warning,
                    "warning",
                    modifier = Modifier.size(60.dp)
                )
            }
            MasterNotFoundDialog(
                activeDialog = activeDialog,
                onChangeDialog = onChangeDialog,
                onConfirm = {
                    onSearchMasterNode()
                },
            )
            EditSettingsDialog(
                open = activeDialog.value == FindMasterNodeActivity.Dialog.SETTINGS,
                settings = settings,
                onConfirm = onSearchMasterNode,
                onClose = { onChangeDialog(FindMasterNodeActivity.Dialog.NONE) }
            )
        }
    }
}

@Composable
fun MasterNotFoundDialog(
    activeDialog: MutableState<FindMasterNodeActivity.Dialog>,
    onChangeDialog: (FindMasterNodeActivity.Dialog) -> Unit,
    onConfirm: () -> Unit,
) {
    if (activeDialog.value == FindMasterNodeActivity.Dialog.ERROR) {
        AlertDialog(
            onDismissRequest = {
                onChangeDialog(FindMasterNodeActivity.Dialog.NONE)
            },
            title = {
                Text(
                    text = "Active Master node not found",
                    textAlign = TextAlign.Center,
                    fontSize = 20.sp,
                )
            },
            text = {
                Row {
                    Icon(Icons.Filled.Warning, "warning", modifier = Modifier.size(30.dp))
                    Spacer(modifier = Modifier.width(20.dp))
                    Text("Please make sure at least one BaseLed is powered on and connected to your local WiFi")
                    Spacer(modifier = Modifier.height(40.dp))
                }
            },
            confirmButton = {
                DefaultButton(
                    text = "Retry",
                    onClick = {
                        onChangeDialog(FindMasterNodeActivity.Dialog.NONE)
                        onConfirm()
                    })
            },
            dismissButton = {
                DefaultButton(
                    text = "Edit Settings",
                    color = MaterialTheme.colors.primary,
                    onClick = {
                        onChangeDialog(FindMasterNodeActivity.Dialog.SETTINGS)
                    })
            }

        )
    }
}

