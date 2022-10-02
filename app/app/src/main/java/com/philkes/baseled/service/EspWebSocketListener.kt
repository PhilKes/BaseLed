package com.philkes.baseled.service

import android.util.Log
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString
import java.util.*
import java.util.concurrent.CompletableFuture


class EspWebSocketListener(
    val connectFuture: CompletableFuture<Boolean>
) : WebSocketListener() {
    private val TAG = "EspWebSocketListener"

    private val messageBuffer: MutableList<String> = mutableListOf()
    private var onMessageReceived: ((String) -> Unit)? = null
    fun setOnMessageReceived(func: (String) -> Unit) {
        onMessageReceived = func
        for (msg in messageBuffer) {
            onMessageReceived!!(msg)
        }
        messageBuffer.clear()
    }

    override fun onOpen(webSocket: WebSocket, response: Response) {
/*        webSocket.send("Hello, it's SSaurel !")
        webSocket.send("What's up ?")
        webSocket.send(ByteString.decodeHex("deadbeef"))
        webSocket.close(NORMAL_CLOSURE_STATUS, "Goodbye !")*/
        Log.d(TAG, "Successfully connected, msg: ${response.message}")
        connectFuture.complete(true)
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        Log.d(TAG, "Receiving : $text")
        if (onMessageReceived == null) {
            messageBuffer.add(text)
        } else {
            onMessageReceived!!(text)
        }
    }

    override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
        Log.d(TAG, "Receiving bytes : " + bytes.hex())
    }

    override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
        webSocket.close(NORMAL_CLOSURE_STATUS, null)
        Log.d(TAG, "Closing : $code / $reason")
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        Log.d(TAG, "Error : " + t.message)
        connectFuture.complete(false)
    }

    companion object {
        private const val NORMAL_CLOSURE_STATUS = 1000
    }
}

