package com.example.projekt.api

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class WordInfoResponse(
    val word: String,
    val meanings: List<Meaning>,
)

@JsonClass(generateAdapter = true)
data class Meaning(
    val definitions: List<Definition>,
    val synonyms: List<String>,
)

@JsonClass(generateAdapter = true)
data class Definition(
    val definition: String,
    val example: String?,
    val synonyms: List<String>,
)