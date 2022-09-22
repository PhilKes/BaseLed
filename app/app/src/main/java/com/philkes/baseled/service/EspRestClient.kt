package com.philkes.baseled.service


import android.util.Log
import com.github.kittinunf.fuel.core.requests.suspendable
import com.github.kittinunf.fuel.coroutines.awaitStringResponse
import com.github.kittinunf.fuel.httpHead
import com.github.kittinunf.fuel.httpPost
import com.philkes.baseled.Settings
import java.net.HttpURLConnection.HTTP_OK

class EspRestClient(private val settings: Settings) {
    private val TAG = "EspRestClient";

    suspend fun searchMasterNodeIp(): String? {
        if (settings.debug) {
            return "master-node-ip"
        }
        for (ip in settings.nodeIps) {
            try {
                val response = "http://$ip:8080/"
                    .httpHead()
                    .timeout(2000)
                    .awaitStringResponse()
                    .second

                if (HTTP_OK == response.statusCode) {
                    return ip
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return null
    }

    suspend fun sendAction(masterNodeIp: String, action: EspNowAction, payload: String) {
        if(settings.debug){
            Thread.sleep(1000)
            Log.d(TAG,"Successfully sent: action: '$action' payload: '$payload' to '$masterNodeIp'")
            return
        }
        try {
            val response = "http://$masterNodeIp:8080"
                .httpPost(
                    listOf(
                        "action" to action.actionId,
                        "rgb" to payload
                    )
                )
                .suspendable()
                .timeout(2000)
                .awaitStringResponse()
                .second

            if (HTTP_OK == response.statusCode) {
                Log.d(TAG,"Successfully sent $action payload: $payload to $masterNodeIp")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}