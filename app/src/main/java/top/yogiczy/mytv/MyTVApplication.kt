package top.yogiczy.mytv

import android.app.Application
import okhttp3.OkHttpClient
import top.yogiczy.mytv.utils.DeviceMacInterceptor

class MyTVApplication : Application() {
    companion object {
        lateinit var instance: MyTVApplication
        lateinit var okHttpClient: OkHttpClient
    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        okHttpClient = OkHttpClient.Builder()
            .addInterceptor(DeviceMacInterceptor(this))
            .build()
    }
}
