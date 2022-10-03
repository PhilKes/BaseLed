package com.philkes.baseled.ui

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.lifecycleScope
import com.google.accompanist.pager.*
import com.philkes.baseled.Settings
import com.philkes.baseled.service.EspNowAction
import com.philkes.baseled.service.EspRestClient
import com.philkes.baseled.ui.dialog.EditSettingsDialog
import com.philkes.baseled.ui.tab.TabItem
import com.philkes.baseled.ui.theme.BaseLedTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var espRestClient: EspRestClient

    @Inject
    lateinit var settings: Settings

    lateinit var masterNodeIp: String

    var currentAction: EspNowAction = EspNowAction.RGB
    var currentColor: String = "FFFFFF"
    var lastEspNowJob: Job? = null

    @OptIn(ExperimentalPagerApi::class)
    private var pagerState: PagerState? = null

    @OptIn(ExperimentalPagerApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        masterNodeIp = settings.lastMasterIp
        espRestClient.setOnActionReceived(::onActionReceived)
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
        val editDialogOpen = remember { mutableStateOf(false) }
        val tabs =
            listOf(TabItem.Rgb(currentColor, ::onSendAction), TabItem.Animation, TabItem.Music)
        pagerState = rememberPagerState(currentAction.actionId)
        Scaffold(
            topBar = { },
            floatingActionButton = {
                FloatingActionButton(
                    contentColor = MaterialTheme.colors.onSurface,
                    onClick = { editDialogOpen.value = !editDialogOpen.value }) {
                    Icon(Icons.Filled.Settings, "settings")
                }
            }
        ) { padding ->
            Column(modifier = Modifier.padding(padding)) {
                Tabs(tabs = tabs, pagerState = pagerState!!)
                TabsContent(tabs = tabs, pagerState = pagerState!!)
            }
            EditSettingsDialog(
                open = editDialogOpen.value,
                onClose = { editDialogOpen.value = false },
                settings = settings
            ) {
                editDialogOpen.value = false
                onFinishActivity();
            }

        }
    }


    private fun onSendAction(action: EspNowAction, payload: String) {
        if (lastEspNowJob != null && lastEspNowJob!!.isActive) {
            lastEspNowJob!!.cancel()
        }
        lastEspNowJob = lifecycleScope.launch(Dispatchers.IO) {
            espRestClient.sendAction(action, payload)
        }
    }

    @OptIn(ExperimentalPagerApi::class)
    fun onActionReceived(action: EspNowAction, payload: String) {
        currentAction = action
        currentColor = payload
        if (pagerState != null) {
            lifecycleScope.launch {
                pagerState!!.animateScrollToPage(currentAction.actionId)
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

}
