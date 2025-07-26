package com.example.tapgame.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.filled.VideogameAsset
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.SettingsSuggest
/*
sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Home : Screen("home", "Главная", Icons.Default.Home)
    object Profile : Screen("profile", "Профиль", Icons.Default.SportsEsports)
    object Settings : Screen("settings", "Настройки", Icons.Default.Settings)
}*/
sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    // Для HomeScreen: Выберите одну из этих или другую, которая вам нравится
    // Icons.Default.VideogameAsset - более современный геймпад
    // Icons.Default.SportsEsports - джойстик
    // Icons.Default.Dashboard - может быть абстрактной "домашней" иконкой
    object Home : Screen("home", "Главная", Icons.Default.VideogameAsset) // <-- Выберите здесь иконку для Home

    // Для ProfileScreen: Джойстик
    object Profile : Screen("profile", "Игры", Icons.Default.SportsEsports) // <-- Иконка джойстика

    // Для SettingsScreen: Выберите одну из этих или другую
    // Icons.Default.Tune - ползунки, современно
    // Icons.Default.SettingsSuggest - шестеренка с плюсом, может быть интересной
    object Settings : Screen("settings", "Настройки", Icons.Default.SettingsSuggest) // <-- Выберите здесь иконку для Settings
}