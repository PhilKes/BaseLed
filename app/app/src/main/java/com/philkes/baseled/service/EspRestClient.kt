package com.philkes.baseled.service


import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.philkes.baseled.Settings
import com.philkes.baseled.Util
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.isActive
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import java.time.Duration
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

class EspRestClient(private val settings: Settings) {
    private val TAG = "EspRestClient";
    private val WEBSOCKET_PORT = 81

    private val client = OkHttpClient.Builder()
        .connectTimeout(4, TimeUnit.SECONDS)
        .pingInterval(Duration.ofSeconds(3))
        .build()
    private lateinit var ws: WebSocket
    private lateinit var listener: EspWebSocketListener
    private lateinit var onPingFailed: () -> Unit

    fun setOnActionReceived(onActionReceived: (EspNowAction, String) -> Unit) {
        listener.setOnMessageReceived { msg ->
            onActionReceived(
                EspNowAction.fromActionId(msg.substring(0, 1).toInt()),
                msg.substring(2)
            )
        }
    }

    fun setOnPingFailed(onPingFailed: () -> Unit) {
        listener.setOnPingFailed(onPingFailed)
    }

    @RequiresApi(Build.VERSION_CODES.S)
    @Throws(CancellationException::class)
    suspend fun searchMasterNodeIp(coroutineScope: CoroutineScope): String? {
        if (settings.debug) {
            tryConnectToWebSocket("ip")
            return "master-node-ip"
        }
        for (ip in settings.nodeIps.sortedBy { it.compareTo(settings.lastMasterIp) }) {
            try {
                val future: CompletableFuture<Boolean> =
                    tryConnectToWebSocket(ip)
                if (future.join()) {
//                    client.dispatcher.executorService.shutdown()
                    return ip
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            if (!coroutineScope.isActive) {
                throw CancellationException("Search Master Node was cancelled")
            }
        }
        return null
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun tryConnectToWebSocket(ip: String): CompletableFuture<Boolean> {
        val request: Request = Request.Builder().url("ws://$ip:$WEBSOCKET_PORT").build()
        val future: CompletableFuture<Boolean> =
            CompletableFuture<Boolean>().newIncompleteFuture()
        listener = EspWebSocketListener(future)
        ws = client.newWebSocket(
            request,
            listener
        )
        return future
    }

    suspend fun sendAction(action: EspNowAction, payload: String) {
        if (settings.debug) {
            Thread.sleep(100)
            Log.d(
                TAG,
                "Successfully sent: action: '$action' payload: '$payload'"
            )
            return
        }
        try {
            val msg = "${action.actionId}-$payload";
            if (ws.send(msg)) {
                Log.d(TAG, "Sent msg: '$msg'")
            } else {
                Log.w(TAG, "Failed to send msg: '$msg'")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    companion object {
        fun formatPayload(color: Color, brightness: Int) =
            "${Util.argbToRGBHexStr(color.toArgb())}-${"%02x".format(brightness)}"
    }

}