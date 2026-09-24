package top.yogiczy.mytv.data.repositories.iptv

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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
 * 直播源，保留原生缓存逻辑，直接在Request添加header
 */
class IptvRepository : FileCacheRepository("iptv.txt") {
    private val log = Logger.create(javaClass.simpleName)

    private suspend fun fetchSource(sourceUrl: String) = withContext(Dispatchers.IO) {
        log.d("===== 发起HTTP请求 =====")
        val client = OkHttpClient.Builder().build()
        val request = Request.Builder()
            .url(sourceUrl)
            .header("device-id", "test123")
            .build()

        val resp = client.newCall(request).execute()
        log.d("http code: ${resp.code}")
        val bodyStr = resp.body!!.string()
        log.d("php返回内容：$bodyStr")
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
        val sourceData = getOrRefresh(cacheTime) {
            fetchSource(sourceUrl)
        }

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
