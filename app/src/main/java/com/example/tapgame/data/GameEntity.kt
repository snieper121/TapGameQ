package com.example.tapgame.data

import androidx.room.Entity
import androidx.room.PrimaryKey
// import androidx.room.TypeConverter // Эти импорты больше не нужны здесь
// import androidx.room.TypeConverters // Эти импорты больше не нужны здесь
import android.graphics.Bitmap // Этот импорт может быть не нужен, если вы не используете Bitmap напрямую в GameEntity
import android.graphics.BitmapFactory // Этот импорт может быть не нужен
import androidx.compose.ui.graphics.ImageBitmap // Этот импорт может быть не нужен
import androidx.compose.ui.graphics.asAndroidBitmap // Этот импорт может быть не нужен
import androidx.compose.ui.graphics.asImageBitmap // Этот импорт может быть не нужен
import java.io.ByteArrayOutputStream // Этот импорт может быть не нужен

@Entity(tableName = "games")
// @TypeConverters(BitmapConverter::class) // Эту аннотацию нужно оставить в AppDatabase.kt
data class GameEntity(
    @PrimaryKey val packageName: String,
    val name: String,
    val icon: ByteArray? = null // icon теперь просто ByteArray
)
