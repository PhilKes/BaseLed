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
import com.github.kittinunf.fuel.Fuel
import com.google.accompanist.pager.*
import com.philkes.baseled.R
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

    lateinit var masterNodeIp: String

    @OptIn(ExperimentalPagerApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        masterNodeIp = intent.extras!!.getString(getString(R.string.intent_key_master_node))!!
        setContent {
            BaseLedTheme(darkTheme = true) {
                MainScreen()
            }
        }
    }

    @ExperimentalPagerApi
    @Composable
    @SuppressLint("UnusedMaterialScaffoldPaddingParameter")
    fun MainScreen() {
        val tabs = listOf(TabItem.Rgb(::onAction), TabItem.Animation, TabItem.Music)
        val pagerState = rememberPagerState()
        Scaffold(
            topBar = { },
        ) { padding ->
            Column(modifier = Modifier.padding(padding)) {
                Tabs(tabs = tabs, pagerState = pagerState)
                TabsContent(tabs = tabs, pagerState = pagerState)
            }
        }
    }

    var lastEspNowJob: Job? = null

    fun onAction(action: EspNowAction, payload: String) {
        if(lastEspNowJob != null && lastEspNowJob!!.isActive){
            lastEspNowJob!!.cancel()
        }
        lastEspNowJob = lifecycleScope.launch(Dispatchers.IO) {
            espRestClient.sendAction(masterNodeIp, action, payload)
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
