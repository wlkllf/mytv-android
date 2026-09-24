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
 * 直播源【调试版：request直接加header，全链路异常捕获，强制不走缓存】
 */
class IptvRepository : FileCacheRepository("iptv.txt") {
    private val log = Logger.create(javaClass.simpleName)

    private suspend fun fetchSource(sourceUrl: String): String? = withContext(Dispatchers.IO) {
        return@withContext try {
            log.d("=====准备发起HTTP请求=====")
            val client = OkHttpClient.Builder().build()
            val request = Request.Builder()
                .url(sourceUrl)
                .header("device-id", "test123")
                .build()

            val resp = client.newCall(request).execute()
            log.d("HTTP状态码: ${resp.code}")
            val bodyStr = resp.body?.string() ?: ""
            log.d("PHP返回原始内容：$bodyStr")
            bodyStr
        } catch (ex: Exception) {
            log.e("fetchSource网络请求异常", ex)
            null
        }
    }

    private fun simplifyTest(group: IptvGroup, iptv: Iptv): Boolean {
        return iptv.name.lowercase().startsWith("cctv") || iptv.name.endsWith("卫视")
    }

    suspend fun getIptvGroupList(
        sourceUrl: String = "http://ys.lileifeng.top/mytvtgyy/getlist.php",
        cacheTime: Long,
        simplify: Boolean = false,
    ): IptvGroupList {
        val sourceData = fetchSource(sourceUrl)
        if (sourceData.isNullOrBlank()) {
            log.e("网络获取节目源返回空或者请求失败")
            throw Exception("网络请求获取节目源失败")
        }

        return try {
            val parser = IptvParser.instances.first { it.isSupport(sourceUrl, sourceData) }
            val groupList = parser.parse(sourceData)
            log.d("解析完成，分组数量=${groupList.size}")

            if (simplify) {
                IptvGroupList(groupList.map { group ->
                    IptvGroup(
                        name = group.name,
                        iptvList = IptvList(group.iptvList.filter { iptv ->
                            simplifyTest(group, iptv)
                        })
                    )
                }.filter { it.iptvList.isNotEmpty() })
            } else {
                groupList
            }
        } catch (ex: Exception) {
            log.e("解析节目源出错", ex)
            throw Exception("解析节目源失败：${ex.message}")
        }
    }
}
