package com.philkes.baseled.ui.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.ZeroCornerSize
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.TextFieldDefaults.indicatorLine
import com.philkes.baseled.ui.component.TextIconButton;
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.philkes.baseled.Settings
import com.philkes.baseled.ui.component.CustomTextField


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
        val debug = remember {
            mutableStateOf(settings.debug)
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
                        modifier = Modifier.fillMaxHeight(0.5f),
                        contentPadding = PaddingValues(start = 4.dp, end = 4.dp)
                    ) {
                        itemsIndexed(ips.value) { idx, ip ->
                            CustomTextField(
                                value = ip,
                                singleLine = true,
                                shape= RoundedCornerShape(0.dp),
                                onValueChange = {
                                    val ipList = ips.value.toMutableList()
                                    ipList[idx] = it
                                    ips.value = ipList
                                },
                            )
                        }
                    }
                    Row(
                        modifier = Modifier
                            .padding(8.dp)
                            .fillMaxHeight(0.2f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(checked = debug.value, onCheckedChange = { debug.value = it })
                        Text(text = "Debug", textAlign = TextAlign.Center)
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
                        settings.debug = debug.value
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