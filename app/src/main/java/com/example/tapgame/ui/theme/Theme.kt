package com.example.tapgame.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// НОВЫЕ ЦВЕТОВЫЕ СХЕМЫ
private val CustomDarkColorScheme =
    darkColorScheme(
        primary = DarkPrimary,
        onPrimary = DarkOnPrimary,
        secondary = DarkSecondary,
        onSecondary = DarkOnSecondary,
        background = DarkBackground,
        onBackground = DarkOnBackground,
        surface = DarkSurface,
        onSurface = DarkOnSurface,
        surfaceVariant = DarkSurfaceVariant, // Используем новый SurfaceVariant
        onSurfaceVariant = DarkOnSurfaceVariant, // Используем новый OnSurfaceVariant

        // Цвета для кнопок "Удалить" и "Добавить"
        error = RemoveButtonColor, // Цвет для кнопки "Удалить"
        onError = White, // Цвет текста на кнопке "Удалить" (на темном фоне)
        inversePrimary = AddButtonColor, // Цвет для кнопки "Добавить"
        // inverseOnSurface и tertiary для загрузочного экрана
        inverseOnSurface = LoadingScreenBackground, // Цвет фона загрузочного экрана
        tertiary = LoadingAnimationColor // Цвет анимации загрузки
    )

private val CustomLightColorScheme =
    lightColorScheme(
        primary = LightPrimary,
        onPrimary = LightOnPrimary,
        secondary = LightSecondary,
        onSecondary = LightOnSecondary,
        background = LightBackground,
        onBackground = LightOnBackground,
        surface = LightSurface,
        onSurface = LightOnSurface,
        surfaceVariant = LightSurfaceVariant, // Используем новый SurfaceVariant
        onSurfaceVariant = LightOnSurfaceVariant, // Используем новый OnSurfaceVariant

        // Цвета для кнопок "Удалить" и "Добавить"
        error = RemoveButtonColor, // Цвет для кнопки "Удалить"
        onError = White, // Цвет текста на кнопке "Удалить" (на светлом фоне)
        inversePrimary = AddButtonColor, // Цвет для кнопки "Добавить"
        // inverseOnSurface и tertiary для загрузочного экрана
        inverseOnSurface = LoadingScreenBackground, // Цвет фона загрузочного экрана
        tertiary = LoadingAnimationColor // Цвет анимации загрузки
    )

@Composable

fun TapGameTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> CustomDarkColorScheme
        else -> CustomLightColorScheme
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window,
                view).isAppearanceLightStatusBars = darkTheme
        }
    }

    MaterialTheme(colorScheme = colorScheme, typography = Typography,
        content = content)
}