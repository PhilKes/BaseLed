package com.philkes.baseled.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.lifecycleScope
import com.google.accompanist.pager.*
import com.philkes.baseled.FindMasterNodeActivity
import com.philkes.baseled.R
import com.philkes.baseled.Settings
import com.philkes.baseled.service.EspNowAction
import com.philkes.baseled.service.EspRestClient
import com.philkes.baseled.ui.dialog.EditSettingsDialog
import com.philkes.baseled.ui.tab.TabItem
import com.philkes.baseled.ui.theme.BaseLedTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject


data class State(
    var action: EspNowAction = EspNowAction.RGB,
    var color: Color = Color.White,
    var brightness: Int = 255
);
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var espRestClient: EspRestClient

    @Inject
    lateinit var settings: Settings

    lateinit var masterNodeIp: String

    @OptIn(ExperimentalPagerApi::class)
    private var pagerState: PagerState? = null

    private val state: MutableState<State> = mutableStateOf(State())

    var lastEspNowJob: Job? = null

    @OptIn(ExperimentalPagerApi::class, ExperimentalCoroutinesApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        masterNodeIp = settings.lastMasterIp
        espRestClient.setOnActionReceived(::onActionReceived)
        espRestClient.setOnPingFailed {
            lifecycleScope.launch {
                showToast(buildString {
                    append(getString(R.string.txt_connection_lost_1))
                    append(masterNodeIp)
                    append(getString(R.string.txt_connection_lost_2))
                })
            }
            startActivity(Intent(this, FindMasterNodeActivity::class.java))
            finish()
        }
        setContent {
            BaseLedTheme(darkTheme = true) {
                MainScreen(settings) {
                    finish()
                }
            }
        }
    }


    @ExperimentalPagerApi
    @Composable
    @SuppressLint("UnusedMaterialScaffoldPaddingParameter")
    fun MainScreen(settings: Settings, onFinishActivity: () -> Unit) {
        var editDialogOpen by remember { mutableStateOf(false) }
        val tabs =
            listOf(
                TabItem.Rgb(state, ::onSendAction),
                TabItem.Music(state, ::onSendAction, settings.debug)
            )
        pagerState = rememberPagerState(
            if (state.value.action.actionId == EspNowAction.RGB_WHEEL.actionId) {
                EspNowAction.RGB.actionId
            } else {
                state.value.action.actionId
            }
        )
        Scaffold(
            topBar = { },
            floatingActionButton = {
                FloatingActionButton(
                    contentColor = MaterialTheme.colors.onSurface,
                    onClick = { editDialogOpen = !editDialogOpen }) {
                    Icon(Icons.Filled.Settings, "settings")
                }
            }
        ) { padding ->
            Column(modifier = Modifier.padding(padding)) {
                Tabs(tabs = tabs, pagerState = pagerState!!)
                TabsContent(tabs = tabs, pagerState = pagerState!!)
            }
            EditSettingsDialog(
                open = editDialogOpen,
                onClose = { editDialogOpen = false },
                settings = settings
            ) {
                editDialogOpen = false
                onFinishActivity();
            }

        }
    }


    private fun onSendAction(action: EspNowAction, payload: String) {
        if (lastEspNowJob != null && lastEspNowJob!!.isActive) {
            lastEspNowJob!!.cancel()
        }
        state.value = state.value.copy(action = action)
        lastEspNowJob = lifecycleScope.launch(Dispatchers.IO) {
            espRestClient.sendAction(action, payload)
        }
    }

    @OptIn(ExperimentalPagerApi::class)
    fun onActionReceived(action: EspNowAction, payload: String) {
        state.value = state.value.copy(
            action = action,
            color = Color(payload.substring(0, 6).toInt(16)).copy(alpha = 1.0f),
            brightness = payload.substring(7, 9).toInt(16)
        )
        if (pagerState != null) {
            lifecycleScope.launch {
                if (state.value.action.actionId == EspNowAction.RGB_WHEEL.actionId) {
                    pagerState!!.animateScrollToPage(EspNowAction.RGB.actionId)
                } else {
                    pagerState!!.animateScrollToPage(state.value.action.actionId)
                }
            }
        }
    }

    @OptIn(ExperimentalPagerApi::class)
    @Composable
    fun Tabs(tabs: List<TabItem>, pagerState: PagerState) {
        val scope = rememberCoroutineScope()
        // OR ScrollableTabRow()
        TabRow(
            selectedTabIndex = pagerState.currentPage,
            backgroundColor = MaterialTheme.colors.primary,
            contentColor = Color.White,
            indicator = { tabPositions ->
                TabRowDefaults.Indicator(
                    modifier = Modifier.pagerTabIndicatorOffset(pagerState, tabPositions)
                )
            }) {
            tabs.forEachIndexed { index, tab ->
                val selected = pagerState.currentPage == index
                Tab(
                    modifier = if (selected) Modifier.background(MaterialTheme.colors.secondary) else Modifier.background(
                        MaterialTheme.colors.onSecondary
                    ),
                    text = { Text(tab.title) },
                    selected = selected,
                    selectedContentColor = MaterialTheme.colors.secondary,
                    onClick = {
                        scope.launch {
                            pagerState.animateScrollToPage(index)
                        }
                    },
                )
            }
        }
    }

    @OptIn(ExperimentalPagerApi::class)
    @Composable
    fun TabsContent(tabs: List<TabItem>, pagerState: PagerState) {
        HorizontalPager(state = pagerState, count = tabs.size, userScrollEnabled = false) { page ->
            tabs[page].screen()
        }
    }

    override fun onBackPressed() {
//        super.onBackPressed()
        startActivity(Intent(this, FindMasterNodeActivity::class.java))
    }

}
