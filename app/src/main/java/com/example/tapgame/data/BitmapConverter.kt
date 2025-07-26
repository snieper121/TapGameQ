package com.example.tapgame.data

import androidx.room.TypeConverter
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import java.io.ByteArrayOutputStream

class BitmapConverter {
    @TypeConverter
    fun fromBitmap(bitmap: ImageBitmap?): ByteArray? {
        return bitmap?.asAndroidBitmap()?.let { androidBitmap ->
            val outputStream = ByteArrayOutputStream()
            androidBitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            outputStream.toByteArray()
        }
    }

    @TypeConverter
    fun toBitmap(byteArray: ByteArray?): ImageBitmap? {
        return byteArray?.let {
            if (it.isEmpty()) {
                null
            } else {
                BitmapFactory.decodeByteArray(it, 0, it.size)?.asImageBitmap()
            }
        }
    }
}
