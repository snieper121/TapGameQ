package com.example.tapgame.ui

import com.example.tapgame.ui.screens.HomeScreen
import com.example.tapgame.ui.screens.ProfileScreen
import com.example.tapgame.ui.screens.SettingsScreen
import com.example.tapgame.ui.screens.AppScreen
import com.example.tapgame.ui.screens.LoadScreen
import com.example.tapgame.ui.screens.InfoScreen
import com.example.tapgame.ui.screens.overlay.ImageOverlayScreen
import com.example.tapgame.ui.screens.PermissionScreen
import com.example.tapgame.ui.screens.DeveloperOptionsScreen
import com.example.tapgame.ui.theme.TapGameTheme
import com.example.tapgame.data.SettingsDataStore
import com.example.tapgame.viewmodel.AppListViewModel
import com.example.tapgame.utils.FeedbackManager
import com.example.tapgame.ui.screens.PermissionTestScreen

import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.draw.clip
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import android.util.Log

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun MainScreen(settingsDataStore: SettingsDataStore, snackbarHostState: SnackbarHostState) {
    val navController = rememberNavController()
    val viewModel: AppListViewModel = viewModel()
    val apps by viewModel.apps
    val context = LocalContext.current
    val feedbackManager = remember { FeedbackManager(context, settingsDataStore) }
    val screens = listOf(
        Screen.Home, Screen.Profile, Screen.Settings
    )
    
    var showAppScreen by remember { mutableStateOf(false) }
    var hideBottomBar by remember { mutableStateOf(false) }
    
    Scaffold(
        bottomBar = {
            if (!showAppScreen && !hideBottomBar) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                ) {
                    val currentDestination = navController.currentBackStackEntryAsState().value?.destination?.route
                    screens.forEach { screen ->
                        val selected = currentDestination == screen.route
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                hideBottomBar = false
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                AnimatedContent(
                                    targetState = selected,
                                    transitionSpec = {
                                        fadeIn(animationSpec = tween(50)) + scaleIn(animationSpec = tween(50)) with
                                        fadeOut(animationSpec = tween(50)) + scaleOut(animationSpec = tween(50))
                                    },
                                    label = "Navigation Item"
                                ) { isSelected ->
                                    if (isSelected) {
                                        Text(
                                            text = screen.title,
                                            color = MaterialTheme.colorScheme.primary,
                                            style = MaterialTheme.typography.labelLarge
                                        )
                                    } else {
                                        Icon(
                                            imageVector = screen.icon,
                                            contentDescription = screen.title,
                                            modifier = Modifier.size(28.dp),
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                            ),
                            modifier = Modifier.clip(MaterialTheme.shapes.large)
                        )
                    }
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            NavHost(
                navController = navController,
                startDestination = Screen.Home.route,
                modifier = Modifier.fillMaxSize()
            ) {
                composable("settings") {
                    SettingsScreen(settingsDataStore, navController)
                }
                
                composable(Screen.Home.route) {
                    hideBottomBar = false
                    HomeScreen(
                        navController = navController,
                        onShowApps = { showAppScreen = true }
                    )
                }
                
                composable(Screen.Profile.route) {
                    hideBottomBar = false
                    ProfileScreen(
                        viewModel = viewModel,
                        settingsDataStore = settingsDataStore
                    )
                }
                composable(Screen.Settings.route) {
                    hideBottomBar = false
                    SettingsScreen(settingsDataStore = settingsDataStore, navController = navController)
                }
                composable("info") {
                    hideBottomBar = false
                    InfoScreen(onDismiss = { navController.popBackStack() })
                }
                composable("imageOverlay") {
                    hideBottomBar = true
                    ImageOverlayScreen(onDismiss = { navController.popBackStack() })
                }
                composable("permissions") {
                    hideBottomBar = true
                    PermissionScreen(
                        onDismiss = { navController.popBackStack() },
                        settingsDataStore = settingsDataStore
                    )
                }
                composable("developer_options") {
                    hideBottomBar = true
                    DeveloperOptionsScreen(
                        onBack = { navController.popBackStack() },
                        //onDismiss = { navController.popBackStack() }//,
                        settingsDataStore = settingsDataStore
                    )
                }
                composable("permission_test") {
                    hideBottomBar = true
                    PermissionTestScreen()
                }
            }
        
            if (showAppScreen) {
                AppScreen(
                    onClose = { showAppScreen = false },
                    viewModel = viewModel
                )
            }
        }
    }
}