package com.philkes.baseled

import android.content.Context
import android.content.SharedPreferences


class Settings(val context: Context) {
    private val DEFAULT_IPS = "192.168.178.33,192.168.178.41,192.168.178.41";
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(
        context.resources.getString(R.string.app_name),
        Context.MODE_PRIVATE
    )

    var nodeIps: List<String>
        get() {
            var ips = sharedPreferences.getString(context.getString(R.string.key_node_ips), null)
            if (ips == null)
                ips = DEFAULT_IPS
            // Encode Boards name + id in one String
            return ips.split(",")
        }
        set(value) {
            val edit = sharedPreferences.edit()
            edit.putString(context.getString(R.string.key_node_ips), value.joinToString { "," })
                .apply()
        }
}
