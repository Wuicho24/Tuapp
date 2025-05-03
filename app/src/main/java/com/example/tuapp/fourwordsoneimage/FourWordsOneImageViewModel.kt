package com.tuapp.ui.fourwordsoneimage

import androidx.lifecycle.ViewModel
import com.example.tuapp.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.flow.StateFlow
import android.content.Context
import android.content.SharedPreferences

data class ImageWordOption(
    val imageResId: Int,
    val correctWord: String,
    val options: List<String>
)

class FourWordsOneImageViewModel : ViewModel() {

    private val allQuestions = listOf(
        ImageWordOption(
            imageResId = R.drawable.caballo,
            correctWord = "Caballo",
            options = listOf("Caballo", "Kavallo", "Cabayo", "Cavallo")
        ),
        ImageWordOption(
            imageResId = R.drawable.silla,
            correctWord = "Silla",
            options = listOf("Silla", "Ciyaa", "Sylla", "Cilla")
        ),
        ImageWordOption(
            imageResId = R.drawable.casa,
            correctWord = "Casa",
            options = listOf("Casa", "Kasa", "Caza", "Casá")
        ),
        ImageWordOption(
            imageResId = R.drawable.arbol,
            correctWord = "Árbol",
            options = listOf("Árbol", "Arbol", "Arból", "Árvol")
        ),
        ImageWordOption(
            imageResId = R.drawable.ambulancia,
            correctWord = "Ambulancia",
            options = listOf("Ambulancia", "Ambulansia", "Ambulamcia", "Anbulancia")
        ),
        ImageWordOption(
            imageResId = R.drawable.escuela,
            correctWord = "Escuela",
            options = listOf("Escuela", "Ezcuela", "Eskuela", "Escüela")
        ),
        ImageWordOption(
            imageResId = R.drawable.lapiz,
            correctWord = "Lápiz",
            options = listOf("Lápiz", "Lápis", "Lapis", "Lapiz")
        ),
        ImageWordOption(
            imageResId = R.drawable.bicicleta,
            correctWord = "Bicicleta",
            options = listOf("Bicicleta", "Bisicleta", "Bizicleta", "Bicikleta")
        ),
        ImageWordOption(
            imageResId = R.drawable.abeja,
            correctWord = "Abeja",
            options = listOf("Abeja", "Aveja", "Abega", "Abeha")
        ),
        ImageWordOption(
            imageResId = R.drawable.balon,
            correctWord = "Balón",
            options = listOf("Balón", "Valón", "Balon", "Valon")
        ),
        ImageWordOption(
            imageResId = R.drawable.burro,
            correctWord = "Burro",
            options = listOf("Búrro", "Burro", "Buro", "Vurro")
        ),
        ImageWordOption(
            imageResId = R.drawable.murcielago,
            correctWord = "Murciélago",
            options = listOf("Murcielago", "Murzielago", "Murciélago", "Mursiélago")
        ),
        ImageWordOption(
            imageResId = R.drawable.tijera,
            correctWord = "Tijera",
            options = listOf("Tigera", "Tijiera", "Tiguera", "Tijera")
        ),
        ImageWordOption(
            imageResId = R.drawable.reloj,
            correctWord = "Reloj",
            options = listOf("Reloj", "Relog", "Relój", "Rreloj")
        ),
        ImageWordOption(
            imageResId = R.drawable.pizarron,
            correctWord = "Pizarrón",
            options = listOf("Pizarron", "Pisarrón", "Pizarrón", "Pizárron")
        ),
        ImageWordOption(
            imageResId = R.drawable.zanahoria,
            correctWord = "Zanahoria",
            options = listOf("Zanoria", "Zanaoria", "Sanahoria", "Zanahoria")
        ),
        ImageWordOption(
            imageResId = R.drawable.vaso,
            correctWord = "Vaso",
            options = listOf("Vaso", "Baso", "Bazo", "Vazo")
        ),
        ImageWordOption(
            imageResId = R.drawable.television,
            correctWord = "Televisión",
            options = listOf("Telebisión", "Televisión", "Telebicion", "Televición")
        ),
        ImageWordOption(
            imageResId = R.drawable.almohada,
            correctWord = "Almohada",
            options = listOf("Almojada", "Almoada", "Almohada", "Almuada")
        ),
        ImageWordOption(
            imageResId = R.drawable.cangrejo,
            correctWord = "Cangrejo",
            options = listOf("Cangreho", "Cangrego", "Kangrejo", "Cangrejo")
        )
    ).shuffled() // Para que el orden sea aleatorio pero sin repetir

    private var currentIndex = 0

    private val _currentQuestion = MutableStateFlow(allQuestions[currentIndex])
    val currentQuestion: StateFlow<ImageWordOption> = _currentQuestion

    private val _isAnswerCorrect = MutableStateFlow<Boolean?>(null)
    val isAnswerCorrect: StateFlow<Boolean?> = _isAnswerCorrect

    private val _isGameFinished = MutableStateFlow(false)
    val isGameFinished: StateFlow<Boolean> = _isGameFinished

    private var correctStreak = 0
    private val _showMilestoneDialog = MutableStateFlow(false)
    val showMilestoneDialog: StateFlow<Boolean> = _showMilestoneDialog

    private val _showContinueDialog = MutableStateFlow(false)
    val showContinueDialog: StateFlow<Boolean> = _showContinueDialog

    private val _isGameStarted = mutableStateOf(false)
    val isGameStarted: State<Boolean> = _isGameStarted

    // Nuevo estado para continuar
    private val _showResumeGameDialog = MutableStateFlow(false)
    val showResumeGameDialog: StateFlow<Boolean> = _showResumeGameDialog

    // Referencia a las SharedPreferences
    private var prefs: SharedPreferences? = null

    // Inicializar el ViewModel con contexto
    fun initialize(context: Context) {
        prefs = context.getSharedPreferences("FourWordsOneImagePrefs", Context.MODE_PRIVATE)

        // Verificar si hay un juego guardado
        val savedIndex = prefs?.getInt("saved_game_index", -1) ?: -1
        if (savedIndex > 0) {
            _showResumeGameDialog.value = true
        }
    }

    // Guardar el progreso cuando el usuario sale
    fun saveGameProgress() {
        prefs?.edit()?.apply {
            putInt("saved_game_index", currentIndex)
            apply()
        }
    }

    // Limpiar el progreso guardado
    fun clearSavedProgress() {
        prefs?.edit()?.apply {
            remove("saved_game_index")
            apply()
        }
    }

    // Funciones verificacion y siguiente
    fun checkAnswer(selectedWord: String) {
        val isCorrect = selectedWord == _currentQuestion.value.correctWord
        _isAnswerCorrect.value = isCorrect

        if (isCorrect) {
            correctStreak++
            if (correctStreak % 5 == 0) {
                _showMilestoneDialog.value = true
            }
        } else {
            correctStreak = 0
        }
    }

    fun nextQuestion() {
        if (currentIndex < allQuestions.lastIndex) {
            currentIndex++
            _currentQuestion.value = allQuestions[currentIndex]
            _isAnswerCorrect.value = null
            _showContinueDialog.value = (currentIndex == 10)
        } else {
            _isGameFinished.value = true
            clearSavedProgress() // Al finalizar el juego, borrar progreso guardado
        }
    }

    fun restartGame() {
        currentIndex = 0
        correctStreak = 0
        _isGameFinished.value = false
        _isAnswerCorrect.value = null
        _showMilestoneDialog.value = false
        _showContinueDialog.value = false
        _currentQuestion.value = allQuestions[currentIndex]
        _isGameStarted.value = true
        clearSavedProgress() // Borrar progreso guardado al reiniciar
    }

    fun resumeFrom(index: Int) {
        currentIndex = index.coerceIn(0, allQuestions.lastIndex)
        _currentQuestion.value = allQuestions[currentIndex]
        _isAnswerCorrect.value = null
        _isGameFinished.value = false
        _showMilestoneDialog.value = false
        _showContinueDialog.value = (currentIndex == 10)
        _isGameStarted.value = true
    }

    fun resumeSavedGame() {
        val savedIndex = prefs?.getInt("saved_game_index", 0) ?: 0
        resumeFrom(savedIndex)
        _showResumeGameDialog.value = false
    }

    fun dismissMilestoneDialog() {
        _showMilestoneDialog.value = false
    }

    fun dismissContinueDialog() {
        _showContinueDialog.value = false
    }

    fun dismissResumeGameDialog() {
        _showResumeGameDialog.value = false
    }

    fun resetState() {
        _isAnswerCorrect.value = null
        _isGameFinished.value = false
        _showMilestoneDialog.value = false
    }

    fun exitToMenu() {
        saveGameProgress() // Guardar progreso al salir al menú
    }

    fun getCurrentIndex(): Int = currentIndex
}
