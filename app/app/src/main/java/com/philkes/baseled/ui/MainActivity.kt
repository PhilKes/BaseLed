package com.philkes.baseled.ui

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.lifecycleScope
import com.google.accompanist.pager.*
import com.philkes.baseled.Settings
import com.philkes.baseled.service.EspNowAction
import com.philkes.baseled.service.EspRestClient
import com.philkes.baseled.ui.tabs.TabItem
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

    lateinit var currentAction: EspNowAction
    lateinit var currentColor: String

    @OptIn(ExperimentalPagerApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        masterNodeIp = settings.lastMasterIp
        espRestClient.setOnActionReceived(::onActionReceived)
        setContent {
            BaseLedTheme(darkTheme = true) {
                MainScreen()
            }
        }
    }

    @OptIn(ExperimentalPagerApi::class)
    private var pagerState: PagerState? = null

    @ExperimentalPagerApi
    @Composable
    @SuppressLint("UnusedMaterialScaffoldPaddingParameter")
    fun MainScreen() {
        val tabs = listOf(TabItem.Rgb(currentColor,::onSendAction), TabItem.Animation, TabItem.Music)
        pagerState = rememberPagerState(currentAction.actionId)
        Scaffold(
            topBar = { },
        ) { padding ->
            Column(modifier = Modifier.padding(padding)) {
                Tabs(tabs = tabs, pagerState = pagerState!!)
                TabsContent(tabs = tabs, pagerState = pagerState!!)
            }
        }
    }

    var lastEspNowJob: Job? = null

    fun onSendAction(action: EspNowAction, payload: String) {
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
    @Preview(showBackground = true)
    @Composable
    fun MainScreenPreview() {
        BaseLedTheme(darkTheme = true) {
            MainScreen()
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
                    Modifier.pagerTabIndicatorOffset(pagerState, tabPositions)
                )
            }) {
            tabs.forEachIndexed { index, tab ->
                // OR Tab()
                Tab(
                    text = { Text(tab.title) },
                    selected = pagerState.currentPage == index,
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
