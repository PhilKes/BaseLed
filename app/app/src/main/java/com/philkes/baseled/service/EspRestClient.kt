package com.philkes.baseled.service


import com.github.kittinunf.fuel.coroutines.awaitStringResponse
import com.github.kittinunf.fuel.httpHead
import com.philkes.baseled.Settings
import java.net.HttpURLConnection.HTTP_OK

class EspRestClient(private val settings: Settings) {

    suspend fun searchMasterNodeIp(): String? {
        if(settings.debug){
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
            }catch (e: Exception){
                e.printStackTrace()
            }
        }
        return null
    }
}