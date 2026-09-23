package top.yogiczy.mytv.utils

import android.content.Context
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response

class DeviceMacInterceptor(private val appContext: Context) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest: Request = chain.request()

        val mac = MacUtil.getEthernetMac()
        val appInstanceId = MacUtil.getAppInstanceId(appContext)

        val builder: Request.Builder = originalRequest.newBuilder()
        if (mac.isNotEmpty()) {
            builder.header("X‑Device‑Mac", mac)
        }
        if (appInstanceId.isNotEmpty()) {
            builder.header("X‑App‑InstanceId", appInstanceId)
        }

        val newRequest = builder.build()
        return chain.proceed(newRequest)
    }
}
