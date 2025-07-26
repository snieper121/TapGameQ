package com.example.tapgame.viewmodel

import android.app.Application
import android.graphics.Bitmap
import androidx.core.graphics.drawable.toBitmap
import android.content.pm.PackageManager
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.compose.ui.graphics.asImageBitmap
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlin.math.min
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.State

import com.example.tapgame.ui.AppInfo
import com.example.tapgame.data.AppDatabase
import com.example.tapgame.data.GameEntity
import com.example.tapgame.data.BitmapConverter // Добавлено

class AppListViewModel(application: Application) : AndroidViewModel(application) {
    private val _apps = mutableStateOf<List<AppInfo>>(emptyList())
    val apps: State<List<AppInfo>> = _apps
    private val _isLoading = mutableStateOf(true)
    val isLoading: State<Boolean> = _isLoading
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    val totalAppsCount: Int get() = allApps.size

    private var allApps = listOf<android.content.pm.ApplicationInfo>()
    private var currentPage = 0
    private val pageSize = 20
    private val labelCache = mutableMapOf<String, String>()
    // iconCache больше не нужен, так как мы сразу работаем с Bitmap
    // private val iconCache = mutableMapOf<String, Drawable?>()
    // bitmapCache больше не нужен здесь, так как иконки будут загружаться в UI-слое с помощью Coil
    // private val bitmapCache = mutableMapOf<String, Bitmap?>()

    private val gameDao = AppDatabase.getDatabase(application).gameDao()
    val savedGames: StateFlow<List<GameEntity>> = gameDao.getAllGames()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val savedGamePackageNames: StateFlow<List<String>> = savedGames
        .map { games -> games.map { it.packageName } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        Log.d("AppListViewModel", "Initializing ViewModel, isLoading: ${_isLoading.value}")
        loadInstalledApps()
    }

    private fun loadInstalledApps() {
        viewModelScope.launch(Dispatchers.IO) {
            val startTime = System.currentTimeMillis()
            Log.d("AppListViewModel", "Starting loadInstalledApps")
            val pm = getApplication<Application>().packageManager
            val installedApps = pm.getInstalledApplications(PackageManager.GET_META_DATA)
            val filteredApps = mutableListOf<android.content.pm.ApplicationInfo>()
            for (app in installedApps) {
                // Исключаем системные приложения, которые не имеют лаунчер-активности
                // и приложения MIUI, которые могут быть нерелевантны или вызывать проблемы.
                if (pm.getLaunchIntentForPackage(app.packageName) != null && !app.packageName.startsWith("com.miui")) {
                    filteredApps.add(app)
                    labelCache[app.packageName] = pm.getApplicationLabel(app).toString()
                }
            }
            allApps = filteredApps.sortedBy { labelCache[it.packageName] }
            Log.d("AppListViewModel", "Loaded ${allApps.size} apps")
            if (allApps.isNotEmpty()) {
                loadNextPage()
            } else {
                Log.w("AppListViewModel", "No apps found to load")
            }
            _isLoading.value = false
            Log.d("AppListViewModel", "Finished loadInstalledApps, isLoading: ${_isLoading.value}, total time: ${System.currentTimeMillis() - startTime} ms")
        }
    }

    fun loadNextPage() {
        viewModelScope.launch(Dispatchers.IO) {
            val startTime = System.currentTimeMillis()
            Log.d("AppListViewModel", "Starting loadNextPage, page: ${currentPage + 1}")
            val startIndex = currentPage * pageSize
            val endIndex = minOf(startIndex + pageSize, allApps.size)
            if (startIndex < allApps.size) {
                val pm = getApplication<Application>().packageManager
                val newApps = allApps.subList(startIndex, endIndex).map { appInfo ->
                    // Теперь AppInfo не содержит ImageBitmap, он будет загружаться в UI
                    AppInfo(
                        name = labelCache[appInfo.packageName] ?: pm.getApplicationLabel(appInfo).toString(),
                        packageName = appInfo.packageName
                    )
                }
                _apps.value = _apps.value + newApps
                currentPage++
                Log.d("AppListViewModel", "Loaded page $currentPage, apps: ${newApps.size}, took ${System.currentTimeMillis() - startTime} ms")
            }
        }
    }

    fun updateSearchQuery(query: String) {
        viewModelScope.launch {
            _searchQuery.value = query
            // Уменьшаем задержку, чтобы поиск был более отзывчивым
            // delay(300) // Можно убрать или уменьшить, если поиск быстрый
            searchApps(query)
        }
    }

    fun searchApps(query: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val startTime = System.currentTimeMillis()
            Log.d("AppListViewModel", "Starting searchApps with query: $query")
            val pm = getApplication<Application>().packageManager
            val filteredApps = if (query.isEmpty()) {
                // Если запрос пустой, показываем текущий загруженный список
                // без повторной обработки.
                _apps.value.map { appInfo ->
                    AppInfo(
                        name = appInfo.name,
                        packageName = appInfo.packageName
                    )
                }
            } else {
                // Фильтруем по всем приложениям и создаем новые AppInfo
                allApps.filter { labelCache[it.packageName]?.contains(query, ignoreCase = true) == true }
                    .map { appInfo ->
                        AppInfo(
                            name = labelCache[appInfo.packageName] ?: pm.getApplicationLabel(appInfo).toString(),
                            packageName = appInfo.packageName
                        )
                    }
            }
            _apps.value = filteredApps
            Log.d("AppListViewModel", "Search completed, found ${filteredApps.size} apps, took ${System.currentTimeMillis() - startTime} ms")
        }
    }

    fun addGame(app: AppInfo) {
        viewModelScope.launch(Dispatchers.IO) {
            val pm = getApplication<Application>().packageManager
            val appInfo = pm.getApplicationInfo(app.packageName, 0)
            val drawable = pm.getApplicationIcon(appInfo)
            val bitmap = if (drawable != null && drawable.intrinsicWidth > 0 && drawable.intrinsicHeight > 0) {
                drawable.toBitmap(width = 512, height = 512, config = Bitmap.Config.ARGB_8888).copy(Bitmap.Config.ARGB_8888, true)
            } else {
                null
            }
            val bitmapConverter = BitmapConverter()
            val iconByteArray = bitmapConverter.fromBitmap(bitmap?.asImageBitmap()) // Преобразуем Bitmap в ByteArray

            val game = GameEntity(
                packageName = app.packageName,
                name = app.name,
                icon = iconByteArray // Теперь icon - это ByteArray
            )
            gameDao.insertGame(game)
            Log.d("AppListViewModel", "Added game: ${app.name} (${app.packageName})")
        }
    }

    fun deleteGame(packageName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            gameDao.deleteGame(packageName)
            Log.d("AppListViewModel", "Deleted game with packageName: $packageName")
        }
    }

    override fun onCleared() {
        super.onCleared()
        // bitmapCache больше не управляется здесь, так как Coil будет управлять кэшированием
        // bitmapCache.values.forEach { bitmap ->
        //     bitmap?.recycle()
        // }
        // bitmapCache.clear()
        labelCache.clear()
        allApps = emptyList()
        viewModelScope.cancel()
        Log.d("AppListViewModel", "Cleared caches and allApps")
    }
}