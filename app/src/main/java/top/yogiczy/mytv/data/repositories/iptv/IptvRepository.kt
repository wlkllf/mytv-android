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
 * 直播源获取
 */
class IptvRepository : FileCacheRepository("iptv.txt") {
    private val log = Logger.create(javaClass.simpleName)

    /**
     * 获取远程直播源数据
     */
    private suspend fun fetchSource(sourceUrl: String) = withContext(Dispatchers.IO) {
        log.d("获取远程直播源: $sourceUrl")

        val headerInterceptor = Interceptor { chain ->
            val newReq = chain.request().newBuilder()
                .addHeader("device-id", "test123") //硬编码固定值测试
                .build()
            chain.proceed(newReq)
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(headerInterceptor)
            .build()
        val request = Request.Builder().url(sourceUrl).build()

        try {
            val resp = client.newCall(request).execute()
            log.d("http code: ${resp.code}")
            val bodyStr = resp.body!!.string()
            log.d("接口返回内容：$bodyStr")
            return@withContext bodyStr
        } catch (ex: Exception) {
            log.e("获取远程直播源失败", ex)
            throw Exception("获取远程直播源失败，请检查网络连接", ex)
        }
    }

    /**
     * 简化规则
     */
    private fun simplifyTest(group: IptvGroup, iptv: Iptv): Boolean {
        return iptv.name.lowercase().startsWith("cctv") || iptv.name.endsWith("卫视")
    }

    /**
     * 获取直播源分组列表
     */
    suspend fun getIptvGroupList(
        sourceUrl: String = "http://ys.lileifeng.top/mytvtgyy/getlist.php",
        cacheTime: Long,
        simplify: Boolean = false,
    ): IptvGroupList {
        try {
            val sourceData = getOrRefresh(cacheTime) {
                fetchSource(sourceUrl)
            }

            val parser = IptvParser.instances.first { it.isSupport(sourceUrl, sourceData) }
            val groupList = parser.parse(sourceData)
            log.d("解析节目源数量：${groupList.size}")

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
        } catch (ex: Exception) {
            log.e("解析节目源异常", ex)
            throw ex
        }
    }
}
