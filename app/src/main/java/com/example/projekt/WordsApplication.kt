package com.example.projekt

import android.app.Application
import com.example.projekt.database.WordsDatabase
import com.example.projekt.database.WordsRepository

class WordsApplication: Application() {
    lateinit var wordsRepository: WordsRepository
        private set

    override fun onCreate() {
        super.onCreate()
        wordsRepository = WordsRepository(WordsDatabase.getDatabase(this).dao())
    }
}