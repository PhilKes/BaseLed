package com.philkes.baseled.ui.dialog

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.*
import com.philkes.baseled.ui.component.TextIconButton;
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.philkes.baseled.Settings


@Composable
fun EditSettingsDialog(
    open: Boolean,
    onClose: () -> Unit,
    settings: Settings,
    onConfirm: () -> Unit,
) {

    if (open) {
        val ips = remember {
            mutableStateOf(settings.nodeIps)
        }
        AlertDialog(
            modifier = Modifier.fillMaxHeight(0.75f),
            onDismissRequest = {
                onClose()
            },
            title = {
                Text(
                    text = "Edit Settings",
                    textAlign = TextAlign.Center,
                    fontSize = 20.sp,
                )
            },
            text = {
                Column {
                    Text(
                        text = "Your BaseLed nodes' IP Addresses:",
                        modifier = Modifier.fillMaxHeight(0.1f)
                    )
                    LazyColumn(
                        modifier = Modifier.fillMaxHeight(0.6f),
                        contentPadding = PaddingValues(start = 4.dp, end = 4.dp)
                    ) {
                        itemsIndexed(ips.value) { idx, ip ->
                            TextField(
                                value = ip,
                                onValueChange = {
                                    val ipList = ips.value.toMutableList()
                                    ipList[idx] = it
                                    ips.value = ipList
                                },
                            )
                        }
                    }
                    Text(
                        text = "Note: If you press 'Save' the connection to the BaseLed master will be refreshed",
                        fontSize = 14.sp,
                        modifier = Modifier.fillMaxHeight(0.3f)
                    )
                }
            },
            confirmButton = {
                TextIconButton(
                    text = "Save",
                    onClick = {
                        settings.nodeIps = ips.value
                        onConfirm()
                    })
            },
            dismissButton = {
                TextIconButton(
                    text = "Cancel",
                    color = MaterialTheme.colors.primary,
                    onClick = onClose,
                    modifier = Modifier.fillMaxHeight(0.05f),
                )
            }

        )
    }
}