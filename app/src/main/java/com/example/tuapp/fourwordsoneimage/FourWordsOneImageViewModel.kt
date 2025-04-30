package com.tuapp.ui.fourwordsoneimage

import androidx.lifecycle.ViewModel
import com.example.tuapp.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

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
    val currentQuestion = _currentQuestion.asStateFlow()

    private val _isAnswerCorrect = MutableStateFlow<Boolean?>(null)
    val isAnswerCorrect = _isAnswerCorrect.asStateFlow()

    private val _isGameFinished = MutableStateFlow(false)
    val isGameFinished = _isGameFinished.asStateFlow()

    fun checkAnswer(selectedWord: String) {
        _isAnswerCorrect.value = selectedWord == _currentQuestion.value.correctWord
    }

    fun nextQuestion() {
        if (currentIndex < allQuestions.size - 1) {
            currentIndex++
            _currentQuestion.value = allQuestions[currentIndex]
            _isAnswerCorrect.value = null
        } else {
            _isGameFinished.value = true
        }
    }

    fun restartGame() {
        currentIndex = 0
        _isGameFinished.value = false
        _isAnswerCorrect.value = null
        _currentQuestion.value = allQuestions.shuffled()[currentIndex]
    }
}
