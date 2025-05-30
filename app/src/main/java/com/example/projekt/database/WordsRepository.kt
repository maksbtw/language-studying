package com.example.projekt.database

import com.example.projekt.api.RetrofitClient
import com.example.projekt.data.Word
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class WordsRepository(private val dao: WordsDAO) {
    val wordsFlow = dao.getAllWords()

    fun getWordById(wordId: Long) = dao.getWordById(wordId)

    fun getAllWords() = dao.getAllWords()

    suspend fun addWord(word: Word) {
        withContext(Dispatchers.IO) {
            dao.insertWord(word)
        }
    }
    
    suspend fun updateWord(word: Word) {
        withContext(Dispatchers.IO) {
            dao.updateWord(word)
        }
    }

    suspend fun deleteWord(word: Word) {
        withContext(Dispatchers.IO) {
            dao.deleteWord(word)
        }
    }

    suspend fun getWordDefinition(wordString: String): Pair<String?, String?> {
        return withContext(Dispatchers.IO) {
            try {
                val response = RetrofitClient.apiService.getWordInfo(wordString)

                if (response.isNotEmpty()) {
                    val wordInfo = response[0]
                    var foundDefinition: String? = null
                    var foundExample: String? = null

                    for (meaning in wordInfo.meanings) {
                        for (definitionObj in meaning.definitions) {

                            if (foundDefinition == null && definitionObj.definition.isNotEmpty()) {
                                foundDefinition = definitionObj.definition
                            }

                            if (definitionObj.example != null && definitionObj.example.isNotEmpty()) {
                                if (foundExample == null) {
                                    foundExample = definitionObj.example
                                }
                            } else {}
                        }
                    }
                    Pair(foundDefinition, foundExample)
                } else {
                    Pair(null, null)
                }
            } catch (e: Exception) {
                Pair(null, null)
            }
        }
    }
}