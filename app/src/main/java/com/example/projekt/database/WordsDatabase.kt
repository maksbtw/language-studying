package com.example.projekt.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.projekt.data.Word
import android.content.Context

@Database(entities = [Word::class], version = 1)
abstract class WordsDatabase() : RoomDatabase() {
    abstract fun dao(): WordsDAO

    companion object {
        const val DATABASE_NAME = "words_database"

        @Volatile
        private var INSTANCE: WordsDatabase? = null

        fun getDatabase(context: Context): WordsDatabase {

            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    WordsDatabase::class.java,
                    DATABASE_NAME
                ).fallbackToDestructiveMigration()
                    .build().also { INSTANCE = it }
            }
        }
    }
}