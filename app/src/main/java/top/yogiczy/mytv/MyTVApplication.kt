package top.yogiczy.mytv

import android.app.Application
import top.yogiczy.mytv.utils.DeviceMacInterceptor

class MyTVApplication : Application() {
    companion object {
        lateinit var instance: MyTVApplication
        lateinit var okHttpClient: okhttp3.OkHttpClient
    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        okHttpClient = okhttp3.OkHttpClient.Builder()
            .addInterceptor(DeviceMacInterceptor(this))
            .build()
    }
}
