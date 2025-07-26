package com.example.tapgame.ui.screens

import android.content.pm.PackageManager
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.clickable
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.shape.RoundedCornerShape
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.util.Log
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import kotlin.math.min
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch

import com.example.tapgame.ui.AppInfo
import com.example.tapgame.viewmodel.AppListViewModel
//import com.example.tapgame.utils.FeedbackManager
import com.example.tapgame.ui.theme.TapGameTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppScreen(
    onClose: () -> Unit,
    viewModel: AppListViewModel = viewModel(),
    //feedbackManager: FeedbackManager
) {
    val listState = rememberLazyListState()
    val isLoading by viewModel.isLoading
    val apps by viewModel.apps
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val savedGamePackageNames by viewModel.savedGamePackageNames.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopAppBar(
                title = { Text("Установленные приложения") },
                actions = {
                    TextButton(onClick = onClose) {
                        Text("Закрыть", color = MaterialTheme.colorScheme.primary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.updateSearchQuery(it) },
                label = { Text("Поиск", color = MaterialTheme.colorScheme.onSurface) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = {}),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    cursorColor = MaterialTheme.colorScheme.primary,
                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                    unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
            LaunchedEffect(Unit) {
                snapshotFlow {
                    listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index to listState.isScrollInProgress
                }
                .distinctUntilChanged()
                .collect { (lastVisibleItem, isScrolling) ->
                    if (!isLoading &&
                        !isScrolling &&
                        searchQuery.isEmpty() &&
                        lastVisibleItem != null &&
                        lastVisibleItem >= apps.size - 5 &&
                        apps.size < viewModel.totalAppsCount
                    ) {
                        Log.d("AppScreen", "Reached end of list, loading next page, lastVisibleItem=$lastVisibleItem, apps.size=${apps.size}")
                        viewModel.loadNextPage()
                    }
                }
            }

            LazyColumn(
                state = listState,
                contentPadding = PaddingValues(vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(
                    items = apps,
                    key = { app -> app.packageName },
                    contentType = { "app_item" }
                ) { app ->
                    // Получаем Drawable иконки приложения
                    val appIconDrawable: Drawable? = remember(app.packageName) {
                        try {
                            context.packageManager.getApplicationIcon(app.packageName)
                        } catch (e: PackageManager.NameNotFoundException) {
                            Log.e("AppScreen", "Package ${app.packageName} not found for icon", e)
                            null
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .wrapContentHeight()
                            .clickable {}
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(appIconDrawable)
                                .size(48)
                                .error(null)
                                .build(),
                            contentDescription = app.name,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(8.dp))
                        )

                        Spacer(modifier = Modifier.width(12.dp))
                        
                        Text(
                            text = app.name,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier
                                .weight(1f)
                                .wrapContentHeight()
                        )
                        
                        val isSaved by remember { derivedStateOf { savedGamePackageNames.contains(app.packageName) } }
                        
                        Button(
                            onClick = {
                                scope.launch {
                                    //feedbackManager.performFeedback()
                                }
                                if (isSaved) {
                                    viewModel.deleteGame(app.packageName)
                                } else {
                                    viewModel.addGame(app)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSaved) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.inversePrimary, // <-- ИСПОЛЬЗУЕМ MaterialTheme.colorScheme
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            modifier = Modifier
                                .padding(start = 8.dp)
                                .wrapContentSize()
                        ) {
                            Text(if (isSaved) "Удалить" else "Добавить")
                        }
                    }
                }
                if (isLoading && apps.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.tertiary
                            )
                        }
                    }
                }
            }
        }
    }
}