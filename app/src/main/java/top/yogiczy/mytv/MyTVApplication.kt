override fun onCreate() {
    super.onCreate()
    AppGlobal.cacheDir = cacheDir
    // 新增这一行，初始化全局上下文
    AppGlobal.context = applicationContext

    // 原来其他初始化代码保留不动
}
