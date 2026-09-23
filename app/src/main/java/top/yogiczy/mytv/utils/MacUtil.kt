package top.yogiczy.mytv.utils

import android.content.Context
import android.content.SharedPreferences
import java.io.BufferedReader
import java.io.FileReader
import java.util.UUID

object MacUtil {
    private const val SP_NAME = "device_instance"
    private const val KEY_INSTANCE_UUID = "instance_uuid"

    fun getEthernetMac(): String {
        return try {
            val br = BufferedReader(FileReader("/sys/class/net/eth0/address"))
            val mac = br.readLine().trim().uppercase()
            br.close()
            if (mac.matches(Regex("[0-9A-F]{2}:[0-9A-F]{2}:[0-9A-F]{2}:[0-9A-F]{2}:[0-9A-F]{2}:[0-9A-F]{2}"))) {
                mac
            } else {
                ""
            }
        } catch (e: Exception) {
            ""
        }
    }

    fun getAppInstanceId(context: Context): String {
        val sp: SharedPreferences = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE)
        var uuid = sp.getString(KEY_INSTANCE_UUID, null)
        if (uuid.isNullOrBlank()) {
            uuid = UUID.randomUUID().toString()
            sp.edit().putString(KEY_INSTANCE_UUID, uuid).apply()
        }
        return uuid
    }
}
