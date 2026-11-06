package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.ChatContact
import com.example.data.model.ChatMessage

@Database(entities = [ChatContact::class, ChatMessage::class], version = 2, exportSchema = false)
abstract class TelegramDatabase : RoomDatabase() {
    abstract fun contactDao(): ContactDao
    abstract fun messageDao(): MessageDao

    companion object {
        @Volatile
        private var INSTANCE: TelegramDatabase? = null

        fun getDatabase(context: Context): TelegramDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TelegramDatabase::class.java,
                    "telegram_chat.db"
                ).fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
