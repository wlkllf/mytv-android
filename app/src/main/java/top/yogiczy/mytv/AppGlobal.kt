package top.yogiczy.mytv

import android.content.Context
import java.io.File

object AppGlobal {
    lateinit var cacheDir: File
    // 新增全局application上下文
    lateinit var context: Context
}
