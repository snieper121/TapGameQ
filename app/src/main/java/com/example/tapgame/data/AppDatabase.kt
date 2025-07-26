package com.example.tapgame.data

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.room.TypeConverters
import android.content.Context

@Database(entities = [GameEntity::class], version = 2) // Версия 2, если вы уже использовали TypeConverters
@TypeConverters(BitmapConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun gameDao(): GameDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // Если вы уже запускали приложение с предыдущей версией базы данных,
        // и хотите сохранить данные, вам нужна миграция.
        // Если вы можете удалить приложение и начать с чистого листа,
        // то эта миграция не нужна, и версию можно оставить 1.
        // Для текущего изменения с ImageBitmap на ByteArray, если у вас уже были ImageBitmap,
        // вам потребуется более сложная миграция, которая преобразует данные.
        // Для простоты, если вы только что добавили ImageBitmap и хотите перейти на ByteArray,
        // лучше всего удалить приложение с устройства и установить заново.
        // Если вы хотите сохранить данные, то вам нужно будет увеличить версию базы данных
        // и написать миграцию, которая преобразует столбец.
        // Например, если текущая версия 2, то следующая будет 3, и миграция 2->3.
        // val MIGRATION_2_3 = object : Migration(2, 3) {
        //     override fun migrate(database: SupportSQLiteDatabase) {
        //         // Пример миграции для изменения типа столбца (может быть сложным)
        //         // database.execSQL("CREATE TABLE games_new (packageName TEXT NOT NULL PRIMARY KEY, name TEXT NOT NULL, icon BLOB)")
        //         // database.execSQL("INSERT INTO games_new (packageName, name, icon) SELECT packageName, name, icon FROM games")
        //         // database.execSQL("DROP TABLE games")
        //         // database.execSQL("ALTER TABLE games_new RENAME TO games")
        //     }
        // }


        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                )
                    // Если вы удалили приложение, то миграция не нужна.
                    // Если вы хотите сохранить данные и у вас уже была колонка 'icon BLOB',
                    // то вам нужно будет увеличить версию базы данных и написать правильную миграцию.
                    // .addMigrations(MIGRATION_2_3) // Раскомментируйте, если нужна миграция
                    // .fallbackToDestructiveMigration() // Используйте только если готовы потерять данные
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}