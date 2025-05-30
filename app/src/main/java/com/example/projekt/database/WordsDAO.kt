package com.example.projekt.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.projekt.data.Word
import kotlinx.coroutines.flow.Flow

@Dao
interface WordsDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWord(word: Word)

    @Update
    suspend fun updateWord(word: Word)

    @Delete
    suspend fun deleteWord(word: Word)

    @Query("SELECT * FROM words_list")
    fun getAllWords(): Flow<List<Word>>

    @Query("SELECT * FROM words_list WHERE wordId = :wordId")
    fun getWordById(wordId: Long): Flow<Word?>
}