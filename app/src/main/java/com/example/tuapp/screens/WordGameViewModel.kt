package com.example.tuapp.screens

import android.app.Application
import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.tuapp.R
import com.example.tuapp.database.WordDatabase
import com.tuapp.ui.fourwordsoneimage.ImageWordOption
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class WordState(
    val originalWord: String = "",
    val currentWord: List<Char?> = emptyList(),
    val availableLetters: List<String> = emptyList(),
    val isInsertedByUser: List<Boolean> = emptyList(),
    val currentFragments: List<String?> = emptyList(),
    val availableFragments: List<String> = emptyList(),
    val isFragmentInsertedByUser: List<Boolean> = emptyList(),
    val isCorrect: Boolean = false,
    val showAlert: Boolean = false,
    val shakeEffect: Boolean = false,
    val wrongLetterIndex: Int? = null,
    val wrongAttempts: Int = 0,
    val attemptsByIndex: Map<Int, Int> = emptyMap(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val showImageDialog: Boolean = false

)

class WordGameViewModel(application: Application) : AndroidViewModel(application) {
    // Creamos un scope específico para la base de datos
    private val dbScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun showImageDialog() {
        _word.update { it.copy(showImageDialog = true) }
    }

    fun dismissImageDialog() {
        _word.update { it.copy(showImageDialog = false) }
    }

    // Inicializamos la base de datos de forma lazy
    private val database by lazy {
        WordDatabase.getDatabase(application, dbScope)
    }

    private val wordDao by lazy {
        database.wordDao()
    }

    // Estado de la palabra actual
    private val _word = MutableStateFlow(WordState(isLoading = true))
    val word: StateFlow<WordState> get() = _word

    // Audio player
    private var mediaPlayer: MediaPlayer? = null

    // Nivel actual
    private val _currentLevel = MutableStateFlow(1)
    val currentLevel: StateFlow<Int> get() = _currentLevel

    init {
        // Comprobamos que la base de datos está inicializada y tiene datos
        viewModelScope.launch {
            try {
                val wordCount = withContext(Dispatchers.IO) {
                    wordDao.getAllWords().size
                }
                //log es para el manejo de mensaje en el LogCat
                Log.d("WordGameViewModel", "Base de datos inicializada con $wordCount palabras")
                if (wordCount > 0) {
                    fetchNewWord()
                } else {
                    Log.e("WordGameViewModel", "No hay palabras en la base de datos")
                    _word.value = _word.value.copy(
                        error = "No hay palabras disponibles en la base de datos",
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                Log.e("WordGameViewModel", "Error al inicializar la base de datos: ${e.message}")
                e.printStackTrace()
                _word.value = _word.value.copy(
                    error = "Error al cargar la base de datos: ${e.message}",
                    isLoading = false
                )
            }
        }
    }

    fun playAudio(context: Context, word: String) {
        val resourceName = normalizeString(word.lowercase())
        val resourceId = context.resources.getIdentifier(resourceName, "raw", context.packageName)

        if (resourceId != 0) {
            val mediaPlayer = MediaPlayer.create(context, resourceId)
            mediaPlayer.setOnCompletionListener {
                it.release()
            }
            mediaPlayer.start()
        } else {
            Toast.makeText(context, "No se encontró audio para \"$word\"", Toast.LENGTH_SHORT).show()
        }
    }


    private fun getAudioResourceId(word: String, context: Context): Int? {
        return context.resources.getIdentifier(word.lowercase(), "raw", context.packageName)
            .takeIf { it != 0 }
    }

    override fun onCleared() {
        super.onCleared()
        mediaPlayer?.release()
    }

    fun changeLevel(newLevel: Int) {
        _currentLevel.value = newLevel.coerceIn(1, 4)
        fetchNewWord()
    }

    fun fetchNewWord() {
        _word.value = _word.value.copy(isLoading = true, error = null)

        viewModelScope.launch {
            try {
                val wordEntity = withContext(Dispatchers.IO) {
                    wordDao.getRandomWordByLevel(_currentLevel.value)
                }

                if (wordEntity == null) {
                    Log.e("WordGameViewModel", "No se encontró ninguna palabra para el nivel ${_currentLevel.value}")
                    _word.value = _word.value.copy(
                        error = "No hay palabras disponibles para el nivel ${_currentLevel.value}",
                        isLoading = false
                    )
                    return@launch
                }

                Log.d("WordGameViewModel", "Palabra obtenida: ${wordEntity.word} (nivel: ${wordEntity.level})")
                val word = wordEntity.word.uppercase()

                if (_currentLevel.value == 4) {
                    // Nivel 4: trabajar con sílabas
                    val fragments = wordEntity.syllables?.split("-")?.map { it.uppercase() } ?: run {
                        // Fallback a fragmentos de 2 letras si no hay sílabas
                        val fallback = mutableListOf<String>()
                        var i = 0
                        while (i < word.length) {
                            val end = (i + 2).coerceAtMost(word.length)
                            fallback.add(word.substring(i, end))
                            i += 2
                        }
                        fallback
                    }

                    val totalFragmentsToHide = minOf(2, fragments.size)
                    val indicesToHide = fragments.indices.shuffled().take(totalFragmentsToHide).toSet()
                    val isFragmentInsertedByUser = fragments.indices.map { it in indicesToHide }

                    val hiddenFragments = fragments.mapIndexed { index, fragment ->
                        if (index in indicesToHide) null else fragment
                    }

                    val correctFragments = indicesToHide.map { fragments[it] }
                    val distractors = listOf("TO", "JO", "LA", "DE", "MA", "PA")
                    val availableFragments = (correctFragments + distractors.shuffled().take(4)).shuffled()

                    _word.value = WordState(
                        originalWord = word,
                        currentFragments = hiddenFragments,
                        availableFragments = availableFragments,
                        isFragmentInsertedByUser = isFragmentInsertedByUser,
                        isCorrect = false,
                        showAlert = false,
                        attemptsByIndex = emptyMap(),
                        isLoading = false
                    )
                } else {
                    // Niveles 1, 2 y 3
                    val letters = word.toList().map { it.toString() }
                    val totalLettersToHide = minOf(3, letters.size)
                    val indicesToHide = letters.indices.shuffled().take(totalLettersToHide).toSet()

                    val hiddenLetters = letters.mapIndexed { index, letter ->
                        if (index in indicesToHide) null else letter
                    }

                    val correctLetters = indicesToHide.map { letters[it] }
                    val distractors = listOf("A", "E", "I", "O", "U", "P", "M")
                    val availableLetters = (correctLetters + distractors.shuffled().take(4)).shuffled()

                    _word.value = WordState(
                        originalWord = word,
                        currentWord = hiddenLetters.map { it?.firstOrNull() },
                        availableLetters = availableLetters,
                        isInsertedByUser = List(word.length) { it in indicesToHide },
                        isCorrect = false,
                        showAlert = false,
                        attemptsByIndex = emptyMap(),
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                Log.e("WordGameViewModel", "Error al cargar palabra: ${e.message}")
                e.printStackTrace()
                _word.value = _word.value.copy(
                    error = "Error al cargar la palabra: ${e.message}",
                    isLoading = false
                )
            }
        }
    }


    fun selectLetter(letter: String) {
        val currentList = _word.value.currentWord.toMutableList()
        val insertedList = _word.value.isInsertedByUser.toMutableList()
        val firstEmptyIndex = currentList.indexOfFirst { it == null }

        if (firstEmptyIndex != -1) {
            currentList[firstEmptyIndex] = letter[0]
            insertedList[firstEmptyIndex] = true

            _word.value = _word.value.copy(
                currentWord = currentList,
                availableLetters = _word.value.availableLetters - letter,
                isInsertedByUser = insertedList
            )
        }
    }

    fun removeLetterAt(index: Int) {
        if (!_word.value.isInsertedByUser[index]) return

        val currentList = _word.value.currentWord.toMutableList()
        val insertedList = _word.value.isInsertedByUser.toMutableList()
        val removedLetter = currentList[index]

        if (removedLetter != null) {
            currentList[index] = null
            insertedList[index] = false

            val updatedAvailableLetters = (_word.value.availableLetters + removedLetter.toString()).sorted()
            val updatedAttempts = _word.value.attemptsByIndex.toMutableMap()
            updatedAttempts.remove(index)

            _word.value = _word.value.copy(
                currentWord = currentList,
                availableLetters = updatedAvailableLetters,
                isInsertedByUser = insertedList,
                wrongLetterIndex = null,
                attemptsByIndex = updatedAttempts
            )
        }
    }

    fun selectFragment(fragment: String) {
        if (_currentLevel.value != 4) return

        val currentFragments = _word.value.currentFragments.toMutableList()
        val isFragmentInsertedByUser = _word.value.isFragmentInsertedByUser.toMutableList()
        val firstEmptyIndex = currentFragments.indexOfFirst { it == null }

        if (firstEmptyIndex != -1) {
            currentFragments[firstEmptyIndex] = fragment
            isFragmentInsertedByUser[firstEmptyIndex] = true

            _word.value = _word.value.copy(
                currentFragments = currentFragments,
                isFragmentInsertedByUser = isFragmentInsertedByUser,
                availableFragments = _word.value.availableFragments - fragment
            )
        }
    }

    fun removeFragmentAt(index: Int) {
        if (_currentLevel.value != 4) return
        if (!_word.value.isFragmentInsertedByUser[index]) return

        val currentFragments = _word.value.currentFragments.toMutableList()
        val isFragmentInsertedByUser = _word.value.isFragmentInsertedByUser.toMutableList()
        val removedFragment = currentFragments[index]

        if (removedFragment != null) {
            currentFragments[index] = null
            isFragmentInsertedByUser[index] = false

            val updatedAvailableFragments = (_word.value.availableFragments + removedFragment).sorted()
            val updatedAttempts = _word.value.attemptsByIndex.toMutableMap()
            updatedAttempts.remove(index)

            _word.value = _word.value.copy(
                currentFragments = currentFragments,
                isFragmentInsertedByUser = isFragmentInsertedByUser,
                availableFragments = updatedAvailableFragments,
                wrongLetterIndex = null,
                attemptsByIndex = updatedAttempts
            )
        }
    }

    fun checkAnswer() {
        if (_currentLevel.value == 4) {
            // Verificar nivel 4 (sílabas)
            if (_word.value.currentFragments.contains(null)) {
                _word.value = _word.value.copy(showAlert = true)
                return
            }

            val userWord = _word.value.currentFragments.joinToString("")

            if (userWord == _word.value.originalWord) {
                _word.value = _word.value.copy(isCorrect = true)
                return
            }

            // Buscar la primera sílaba incorrecta
            var firstWrongIndex: Int? = null

            // Reconstruir las sílabas correctas
            val originalWordSyllables = _word.value.originalWord
            var currentIndex = 0
            val correctFragments = mutableListOf<String>()

            for (fragment in _word.value.currentFragments) {
                if (fragment != null) {
                    val fragmentLength = fragment.length
                    val correctFragment = if (currentIndex + fragmentLength <= originalWordSyllables.length) {
                        originalWordSyllables.substring(currentIndex, currentIndex + fragmentLength)
                    } else {
                        originalWordSyllables.substring(currentIndex)
                    }
                    correctFragments.add(correctFragment)
                    currentIndex += fragmentLength
                }
            }

            // Encontrar el primer fragmento incorrecto
            for (i in _word.value.currentFragments.indices) {
                val userFragment = _word.value.currentFragments[i]
                val correctFragment = correctFragments.getOrNull(i)

                if (userFragment != null && correctFragment != null && userFragment != correctFragment) {
                    firstWrongIndex = i
                    break
                }
            }

            if (firstWrongIndex != null) {
                val currentAttempts = _word.value.attemptsByIndex.toMutableMap()
                val attempts = currentAttempts.getOrDefault(firstWrongIndex, 0) + 1
                currentAttempts[firstWrongIndex] = attempts

                _word.value = _word.value.copy(
                    shakeEffect = true,
                    wrongLetterIndex = firstWrongIndex,
                    wrongAttempts = attempts,
                    attemptsByIndex = currentAttempts
                )

                viewModelScope.launch {
                    delay(500)
                    _word.value = _word.value.copy(shakeEffect = false)
                }
            }
        } else {
            // Verificar niveles 1-3 (letras)
            if (_word.value.currentWord.contains(null)) {
                _word.value = _word.value.copy(showAlert = true)
                return
            }

            val userWord = _word.value.currentWord.joinToString("")

            if (userWord == _word.value.originalWord) {
                _word.value = _word.value.copy(isCorrect = true)
                return
            }

            var firstWrongIndex: Int? = null
            for (i in _word.value.currentWord.indices) {
                val userChar = _word.value.currentWord[i]
                val correctChar = _word.value.originalWord[i]

                if (userChar != null && userChar != correctChar) {
                    firstWrongIndex = i
                    break
                }
            }

            if (firstWrongIndex != null) {
                val currentAttempts = _word.value.attemptsByIndex.toMutableMap()
                val attempts = currentAttempts.getOrDefault(firstWrongIndex, 0) + 1
                currentAttempts[firstWrongIndex] = attempts

                _word.value = _word.value.copy(
                    shakeEffect = true,
                    wrongLetterIndex = firstWrongIndex,
                    wrongAttempts = attempts,
                    attemptsByIndex = currentAttempts
                )

                viewModelScope.launch {
                    delay(500)
                    _word.value = _word.value.copy(shakeEffect = false)
                }
            }
        }
    }

    fun dismissAlert() {
        _word.value = _word.value.copy(showAlert = false)
    }
}