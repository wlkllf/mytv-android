package top.yogiczy.mytv.data.repositories.iptv

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import top.yogiczy.mytv.data.entities.Iptv
import top.yogiczy.mytv.data.entities.IptvGroup
import top.yogiczy.mytv.data.entities.IptvGroupList
import top.yogiczy.mytv.data.entities.IptvList
import top.yogiczy.mytv.data.repositories.FileCacheRepository
import top.yogiczy.mytv.data.repositories.iptv.parser.IptvParser
import top.yogiczy.mytv.utils.Logger

/**
 * 直播源获取【调试强制不走缓存版本】
 */
class IptvRepository : FileCacheRepository("iptv.txt") {
    private val log = Logger.create(javaClass.simpleName)

    private suspend fun fetchSource(sourceUrl: String) = withContext(Dispatchers.IO) {
        log.d("===== 真正发起HTTP请求 =====")

        val headerInterceptor = Interceptor { chain ->
            log.d("拦截器执行，添加device-id:test123")
            val newReq = chain.request().newBuilder()
                .addHeader("device-id", "test123")
                .build()
            return@Interceptor chain.proceed(newReq)
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(headerInterceptor)
            .build()
        val request = Request.Builder().url(sourceUrl).build()

        val resp = client.newCall(request).execute()
        log.d("http response code: ${resp.code}")
        val bodyStr = resp.body!!.string()
        log.d("php返回原始内容：$bodyStr")
        return@withContext bodyStr
    }

    private fun simplifyTest(group: IptvGroup, iptv: Iptv): Boolean {
        return iptv.name.lowercase().startsWith("cctv") || iptv.name.endsWith("卫视")
    }

    suspend fun getIptvGroupList(
        sourceUrl: String = "http://ys.lileifeng.top/mytvtgyy/getlist.php",
        cacheTime: Long,
        simplify: Boolean = false,
    ): IptvGroupList {
        // =========调试：强制直接网络，跳过本地缓存getOrRefresh========
        val sourceData = fetchSource(sourceUrl)

        val parser = IptvParser.instances.first { it.isSupport(sourceUrl, sourceData) }
        val groupList = parser.parse(sourceData)
        log.d("解析完成，分组数量=${groupList.size}")

        if (simplify) {
            return IptvGroupList(groupList.map { group ->
                IptvGroup(
                    name = group.name,
                    iptvList = IptvList(group.iptvList.filter { iptv ->
                        simplifyTest(group, iptv)
                    })
                )
            }.filter { it.iptvList.isNotEmpty() })
        }
        return groupList
    }
}
