package com.philkes.baseled.service


import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.philkes.baseled.Settings
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import java.util.concurrent.CompletableFuture

class EspRestClient(private val settings: Settings) {
    private val TAG = "EspRestClient";
    private val WEBSOCKET_PORT = 81

    private val client = OkHttpClient()
    private lateinit var ws: WebSocket
    private lateinit var listener: EspWebSocketListener

    fun setOnActionReceived(onActionReceived: (EspNowAction, String) -> Unit) {
        listener.setOnMessageReceived { msg ->
            onActionReceived(
                EspNowAction.fromActionId(msg.substring(0, 1).toInt()),
                msg.substring(2)
            )
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    suspend fun searchMasterNodeIp(): String? {
        if (settings.debug) {
            return "master-node-ip"
        }
        for (ip in settings.nodeIps.sortedBy { it.compareTo(settings.lastMasterIp) }) {
            try {
                val future: CompletableFuture<Boolean> =
                    tryConnectToWebSocket(ip)
                if (future.join()) {
                    client.dispatcher.executorService.shutdown()
                    return ip
                }
            } catch (e: Exception) {
                e.printStackTrace()
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
            Thread.sleep(1000)
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
}