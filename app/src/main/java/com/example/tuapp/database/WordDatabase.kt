package com.example.tuapp.database

import android.content.Context
import android.util.Log
import androidx.room.*
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Database(entities = [WordEntity::class], version = 3, exportSchema = false)
abstract class WordDatabase : RoomDatabase() {
    abstract fun wordDao(): WordDao

    companion object {
        private const val TAG = "WordDatabase"

        @Volatile
        private var INSTANCE: WordDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): WordDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    WordDatabase::class.java,
                    "word_database"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(WordDatabaseCallback(scope))
                    .build()

                INSTANCE = instance
                instance
            }
        }

        private class WordDatabaseCallback(private val scope: CoroutineScope) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                Log.d(TAG, "onCreate: Creando base de datos por primera vez")
                scope.launch {
                    val instance = INSTANCE
                    if (instance != null) {
                        populateDatabase(instance.wordDao())
                    } else {
                        Log.e(TAG, "onCreate: INSTANCE es null, no se puede poblar la base de datos")
                    }
                }
            }

            override fun onOpen(db: SupportSQLiteDatabase) {
                super.onOpen(db)
                scope.launch {
                    val instance = INSTANCE
                    if (instance != null) {
                        val wordCount = withContext(Dispatchers.IO) {
                            instance.wordDao().getWordCount()
                        }
                        Log.d(TAG, "onOpen: Base de datos abierta con $wordCount palabras")

                        if (wordCount == 0) {
                            Log.d(TAG, "onOpen: La base de datos está vacía, poblando...")
                            populateDatabase(instance.wordDao())
                        }
                    }
                }
            }
        }

        private suspend fun populateDatabase(wordDao: WordDao) {
            withContext(Dispatchers.IO) {
                try {
                    // Verificar si ya hay datos
                    val count = wordDao.getWordCount()
                    if (count > 0) {
                        Log.d(TAG, "populateDatabase: Ya hay $count palabras en la base de datos")
                        return@withContext
                    }

                    // Palabras de nivel 1
                    val nivel1Palabras = listOf(
                        "mesa", "silla", "vaso", "plato", "cama", "taza", "llave", "reloj", "libro", "puerta",
                        "pan", "tren", "flor", "luz", "sal", "tigre", "pato", "globo", "fruta", "nube"
                    ).map { WordEntity(word = it, level = 1) }

                    // Palabras de nivel 2
                    val nivel2Palabras = listOf(
                        "ventana", "cocina", "camino", "zapato", "tijera", "cuaderno", "teléfono", "comida", "escuela", "familia",
                        "botella", "pelota", "mochila", "camisa", "trabajo", "sombrilla", "manzana", "escoba", "almohada", "juguete"
                    ).map { WordEntity(word = it, level = 2) }

                    // Palabras de nivel 3
                    val nivel3Palabras = listOf(
                        "computadora", "biblioteca", "medicina", "calendario", "diccionario", "automóvil", "fotografia", "temperatura", "periódico", "restaurante",
                        "instrumento", "televisión", "estudiante", "vitamina", "ejercicio", "murciélago", "mariposa", "refrigerador", "laboratorio", "transporte"
                    ).map { WordEntity(word = it, level = 3) }

                    // Palabras de nivel 4 con sílabas
                    val silabasPorPalabraNivel4 = mapOf(
                        "comunicación" to listOf("co", "mu", "ni", "ca", "ción"),
                        "universidad" to listOf("u", "ni", "ver", "si", "dad"),
                        "información" to listOf("in", "for", "ma", "ción"),
                        "tecnología" to listOf("tec", "no", "lo", "gía"),
                        "investigación" to listOf("in", "ves", "ti", "ga", "ción"),
                        "electricidad" to listOf("e", "lec", "tri", "ci", "dad"),
                        "supermercado" to listOf("su", "per", "mer", "ca", "do"),
                        "responsabilidad" to listOf("res", "pon", "sa", "bi", "li", "dad"),
                        "creatividad" to listOf("cre", "a", "ti", "vi", "dad"),
                        "matemáticas" to listOf("ma", "te", "má", "ti", "cas"),
                        "oportunidad" to listOf("o", "por", "tu", "ni", "dad"),
                        "celebración" to listOf("ce", "le", "bra", "ción"),
                        "imaginación" to listOf("i", "ma", "gi", "na", "ción"),
                        "conocimiento" to listOf("co", "no", "ci", "mien", "to"),
                        "experiencia" to listOf("ex", "pe", "rien", "cia"),
                        "electrodoméstico" to listOf("e", "lec", "tro", "do", "més", "ti", "co"),
                        "telecomunicación" to listOf("te", "le", "co", "mu", "ni","ca","ción"),
                        "aceleración" to listOf("a", "ce", "le", "ra", "ción"),
                        "radiografía" to listOf("ra", "dio", "gra", "fí", "a"),
                        "electrocardiograma" to listOf("e", "lec", "tro", "car", "dio", "gra", "ma")
                    )

                    val nivel4Palabras = silabasPorPalabraNivel4.map { (palabra, silabas) ->
                        WordEntity(word = palabra, level = 4, syllables = silabas.joinToString("-"))
                    }

                    // Combinar todas las palabras
                    val allWords = nivel1Palabras + nivel2Palabras + nivel3Palabras + nivel4Palabras
                    Log.d(TAG, "populateDatabase: Insertando ${allWords.size} palabras en la base de datos")

                    try {
                        // Insertar palabras
                        wordDao.insertWords(allWords)

                        // Verificar inserción
                        val newCount = wordDao.getWordCount()
                        Log.d(TAG, "populateDatabase: Se insertaron palabras correctamente. Total actual: $newCount")
                    } catch (e: Exception) {
                        Log.e(TAG, "populateDatabase: Error al insertar palabras", e)
                        throw e
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "populateDatabase: Error general", e)
                    throw e
                }
            }
        }
    }
}