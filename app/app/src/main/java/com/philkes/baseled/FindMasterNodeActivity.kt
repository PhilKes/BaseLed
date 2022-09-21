package com.philkes.baseled

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.philkes.baseled.main.MainActivity
import com.philkes.baseled.service.EspRestClient
import com.philkes.baseled.ui.theme.BaseLedTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class FindMasterNodeActivity : ComponentActivity() {
    @Inject
    lateinit var espRestClient: EspRestClient


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BaseLedTheme {
                Content()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        lifecycleScope.launch {
            val foundMasterNode = espRestClient.searchMasterNodeIp()
            if (foundMasterNode == null) {
                TODO("SHOW DIALOG THAT MASTER COULD NOT BE FOUND")
            } else {
                val intent = Intent(this@FindMasterNodeActivity, MainActivity::class.java).apply {
                    putExtra(getString(R.string.intent_key_master_node), foundMasterNode)
                }
                startActivity(intent)

            }
        }
    }


}

@Preview(showBackground = true)
@Composable
fun Content() {
    // A surface container using the 'background' color from the theme
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "Trying to find BaseLed Master Node",
            textAlign = TextAlign.Center,
            fontSize = 32.sp
        )
        Spacer(modifier = Modifier.height(20.dp))
        CircularProgressIndicator(
            modifier = Modifier.size(64.dp),
            strokeWidth = 10.dp,
            progress = -1f
        )
    }

}