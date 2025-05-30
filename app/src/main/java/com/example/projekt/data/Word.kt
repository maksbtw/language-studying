package com.example.projekt.data

import androidx.compose.ui.graphics.Color
import androidx.room.Entity
import androidx.room.PrimaryKey

enum class levels(val color: Color) {
    UNDEFINED(Color(189, 189, 189)),
    BAD(Color(194, 17, 17)),
    AVERAGE(Color(222, 199, 30, 255)),
    GOOD(Color(76, 175, 80, 255)),
    EXCELLENT(Color(3, 169, 244, 255))
}

@Entity(tableName = "words_list")
data class Word(
    val original: String,
    val translation: String,
    val definition: String?,
    val example: String?,
    val knowledgeLevel: levels = levels.UNDEFINED,
    @PrimaryKey
    val wordId: Long = System.nanoTime()
)