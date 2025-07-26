package com.example.tapgame.ui.theme

import androidx.compose.ui.graphics.Color
// НОВЫЕ ЦВЕТА ДЛЯ КАСТОМИЗАЦИИ

// Светлая тема: Темно-синие оттенки, без чистого белого
val LightPrimary = Color(0xFF1A237E) // Глубокий индиго (основной акцент)
val LightOnPrimary = Color(0xFFE0E0E0) // Светло-серый (текст на LightPrimary)
val LightSecondary = Color(0xFF42A5F5) // Голубой (вторичный акцент)
val LightOnSecondary = Color(0xFF212121) // Темно-серый (текст на LightSecondary)
val LightBackground = Color(0xFFE3F2FD) // Очень светло-голубой (фон)
val LightOnBackground = Color(0xFF212121) // Темно-серый (текст на LightBackground)
val LightSurface = Color(0xFFBBDEFB) // Светло-голубой (поверхности, карточки)
val LightOnSurface = Color(0xFF212121) // Темно-серый (текст на LightSurface)
val LightSurfaceVariant = Color(0xFF90CAF9) // Более насыщенный светло-голубой (для NavigationBar)
val LightOnSurfaceVariant = Color(0xFF424242) // Темно-серый (для невыбранных иконок NavigationBar)


// Темная тема: Почти черный с нотками очень темного синего
val DarkPrimary = Color(0xFF9FA8DA) // Светлый индиго (основной акцент)
val DarkOnPrimary = Color(0xFF212121) // Темно-серый (текст на DarkPrimary)
val DarkSecondary = Color(0xFF64B5F6) // Светло-голубой (вторичный акцент)
val DarkOnSecondary = Color(0xFF212121) // Темно-серый (текст на DarkSecondary)
val DarkBackground = Color(0xFF1A1A2E) // Очень темный синий (почти черный фон)
val DarkOnBackground = Color(0xFFE0E0E0) // Светло-серый (текст на DarkBackground)
val DarkSurface = Color(0xFF2C2C40) // Темный синий (поверхности, карточки)
val DarkOnSurface = Color(0xFFE0E0E0) // Светло-серый (текст на DarkSurface)
val DarkSurfaceVariant = Color(0xFF3F3F50) // Более насыщенный темный синий (для NavigationBar)
val DarkOnSurfaceVariant = Color(0xFFBDBDBD) // Серый (для невыбранных иконок NavigationBar)


// Специальные цвета для кнопок "Добавить" / "Удалить"
val AddButtonColor = Color(0xFF4CAF50) // Зеленый
val RemoveButtonColor = Color(0xFFF44336) // Красный
val White = Color(0xFFFFFFFF) // Белый для иконок

// Цвета для загрузочного экрана
val LoadingScreenBackground = Color(0xFF1A1A2E) // Используем DarkBackground для единообразия
val LoadingAnimationColor = Color(0xFF9FA8DA) // Используем DarkPrimary для единообразия