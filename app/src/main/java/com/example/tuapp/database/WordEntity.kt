package com.example.tuapp.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "words")
data class WordEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val word: String,
    val level: Int = 1,
    val syllables: String? = null // silabas
)
