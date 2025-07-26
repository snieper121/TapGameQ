package com.example.tapgame.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.clickable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.core.content.ContextCompat.startActivity
import android.content.pm.PackageManager
import android.content.Context
import android.util.Log
import android.graphics.drawable.Drawable
import kotlinx.coroutines.launch
import coil.compose.AsyncImage
import coil.request.ImageRequest

import com.example.tapgame.data.SettingsDataStore
//import com.example.tapgame.utils.FeedbackManager
import com.example.tapgame.viewmodel.AppListViewModel
import com.example.tapgame.data.GameEntity
import com.example.tapgame.ui.theme.TapGameTheme
//import com.example.tapgame.ui.theme.Color

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: AppListViewModel = viewModel(),
    settingsDataStore: SettingsDataStore
) {
    val savedGames by viewModel.savedGames.collectAsStateWithLifecycle(initialValue = emptyList())
    val context = LocalContext.current
    //val feedbackManager = remember { FeedbackManager(context, settingsDataStore) }
    val scope = rememberCoroutineScope()
    //val bitmapConverter = remember { BitmapConverter() } // Создаем экземпляр конвертера

    Scaffold(
        topBar = {
            Surface(
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .padding(top = 4.dp),
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surface
            ) {
                TopAppBar(
                    title = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text("Мои Игры  ")
                        }
                    },
                    navigationIcon = {}, // Добавляем пустую иконку навигации
                    actions = {},
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = MaterialTheme.colorScheme.onSurface
                    )
                )
            }
        },
        content = { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (savedGames.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Нет добавленных приложений",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(
                            items = savedGames,
                            key = { game -> game.packageName }
                        ) { game ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                                    .clickable {
                                        scope.launch {
                                            //feedbackManager.performFeedback()
                                        }
                                        try {
                                            val pm = context.packageManager
                                            val intent = pm.getLaunchIntentForPackage(game.packageName)
                                            if (intent != null) {
                                                startActivity(context, intent, null)
                                            } else {
                                                Log.w("ProfileScreen", "No launch intent for ${game.packageName}")
                                            }
                                        } catch (e: Exception) {
                                            Log.e("ProfileScreen", "Failed to launch ${game.packageName}: ${e.message}")
                                        }
                                    }
                            ) {
                                // Используем Coil AsyncImage для асинхронной загрузки иконок из ByteArray
                                AsyncImage(
                                    model = ImageRequest.Builder(context)
                                        .data(game.icon)
                                        .size(128) 
                                        .error(null)
                                        .build(),
                                    contentDescription = game.name,
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = game.name,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.weight(1f)
                                )
                                Button(
                                    onClick = {
                                        scope.launch {
                                            //feedbackManager.performFeedback()
                                        }
                                        viewModel.deleteGame(game.packageName)
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.error, // <-- ИСПОЛЬЗУЕМ MaterialTheme.colorScheme
                                        contentColor = MaterialTheme.colorScheme.onPrimary
                                    ),
                                    modifier = Modifier.padding(start = 8.dp)
                                ) {
                                    Text("Удалить")
                                }
                            }
                        }
                    }
                }
            }
        }
    )
}