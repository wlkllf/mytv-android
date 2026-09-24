package top.yogiczy.mytv.ui.screens.leanback

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import top.yogiczy.mytv.data.repositories.iptv.IptvRepository
import top.yogiczy.mytv.ui.screens.leanback.LeanbackMainUiState

class MainViewModel : ViewModel() {
    private val iptvRepository = IptvRepository()

    private val _uiState = MutableStateFlow<LeanbackMainUiState>(LeanbackMainUiState.Loading)
    val uiState: StateFlow<LeanbackMainUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            loadIptvSource()
        }
    }

    private suspend fun loadIptvSource() {
        // =========强制写死你的接口，不再读取SP设置========
        val sourceUrl = "http://ys.lileifeng.top/mytvtgyy/getlist.php"
        val cacheTime = 0L
        val simplify = false

        _uiState.value = LeanbackMainUiState.Loading
        try {
            val iptvGroupList = iptvRepository.getIptvGroupList(
                sourceUrl = sourceUrl,
                cacheTime = cacheTime,
                simplify = simplify
            )
            _uiState.value = LeanbackMainUiState.Ready(iptvGroupList)
        } catch (e: Exception) {
            _uiState.value = LeanbackMainUiState.Error(e.message ?: "加载失败")
        }
    }
}
