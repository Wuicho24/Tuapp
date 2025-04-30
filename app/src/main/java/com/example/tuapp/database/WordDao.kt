package com.example.tuapp.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface WordDao {
    @Insert
    suspend fun insertWords(words: List<WordEntity>)

    @Query("SELECT * FROM words")
    suspend fun getAllWords(): List<WordEntity>

    @Query("SELECT * FROM words WHERE level = :level ORDER BY RANDOM() LIMIT 1")
    suspend fun getRandomWordByLevel(level: Int): WordEntity?

    @Query("SELECT * FROM words WHERE level = :level")
    suspend fun getWordsByLevel(level: Int): List<WordEntity>

    @Query("SELECT * FROM words WHERE level = :level LIMIT :limit")
    suspend fun getLimitedWordsByLevel(level: Int, limit: Int): List<WordEntity>

    @Query("SELECT * FROM words WHERE level = 4 AND syllables IS NOT NULL")
    suspend fun getWordsWithSyllables(): List<WordEntity>

    @Query("SELECT COUNT(*) FROM words")
    suspend fun getWordCount(): Int

}