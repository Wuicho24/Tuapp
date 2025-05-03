package com.tuapp.ui.fourwordsoneimage

import android.content.Context
import android.media.MediaPlayer
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.speech.tts.TextToSpeech
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.tuapp.R
import kotlinx.coroutines.delay
import java.util.*

@Composable
fun FourWordsOneImageScreen(
    navController: NavHostController,
    viewModel: FourWordsOneImageViewModel = viewModel()
) {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.initialize(context)
    }

    val currentQuestion by viewModel.currentQuestion.collectAsState()
    val isAnswerCorrect by viewModel.isAnswerCorrect.collectAsState()
    val isGameFinished by viewModel.isGameFinished.collectAsState()
    val showMilestoneDialog by viewModel.showMilestoneDialog.collectAsState()
    val showContinueDialog by viewModel.showContinueDialog.collectAsState()
    val showResumeGameDialog by viewModel.showResumeGameDialog.collectAsState()

    var showIntroDialog by remember { mutableStateOf(true) }
    val shuffledOptions = remember(currentQuestion) { currentQuestion.options.shuffled() }

    val mediaPlayerCorrect = remember { MediaPlayer.create(context, R.raw.correcto) }
    val mediaPlayerIncorrect = remember { MediaPlayer.create(context, R.raw.incorrecto) }

    var tts by remember { mutableStateOf<TextToSpeech?>(null) }
    var isTtsReady by remember { mutableStateOf(false) }

// Intro
    LaunchedEffect(showIntroDialog, isTtsReady) {
        if (showIntroDialog && isTtsReady && !showResumeGameDialog) {
            delay(300)
            tts?.speak(
                "Observa la imagen y selecciona la palabra que se escriba correctamente. ¡Buena suerte!",
                TextToSpeech.QUEUE_FLUSH,
                null,
                null
            )
        }
    }

// Juego guardado
    LaunchedEffect(showResumeGameDialog, isTtsReady) {
        if (showResumeGameDialog && isTtsReady) {
            delay(600)
            tts?.speak(
                "Se encontró un juego guardado. ¿Deseas continuar donde lo dejaste o empezar un nuevo juego?",
                TextToSpeech.QUEUE_ADD,
                null,
                null
            )
        }
    }

// 5 aciertos
    LaunchedEffect(showMilestoneDialog, isTtsReady) {
        if (showMilestoneDialog && isTtsReady) {
            delay(800)
            tts?.speak(
                "¡Felicidades! Has respondido correctamente cinco niveles seguidos. ¡Sigue así!",
                TextToSpeech.QUEUE_ADD,
                null,
                null
            )
        }
    }

// Nivel 10
    LaunchedEffect(showContinueDialog, isTtsReady) {
        if (showContinueDialog && isTtsReady) {
            delay(1000)
            tts?.speak(
                "¡Nivel diez alcanzado! ¿Deseas continuar o volver al menú?",
                TextToSpeech.QUEUE_ADD,
                null,
                null
            )
        }
    }


    LaunchedEffect(Unit) {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale("es", "ES")
                isTtsReady = true
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            tts?.shutdown()
            mediaPlayerCorrect.release()
            mediaPlayerIncorrect.release()
        }
    }

    fun vibrate(context: Context) {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vm.defaultVibrator
        } else {
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(300, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            vibrator.vibrate(300)
        }
    }

    LaunchedEffect(Unit) {
        if (!viewModel.isGameStarted.value && !showResumeGameDialog) {
            viewModel.restartGame()
        } else if (!showResumeGameDialog) {
            viewModel.resetState()
        }
    }


    if (showResumeGameDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissResumeGameDialog() },
            title = { Text("Juego guardado") },
            text = { Text("Se encontró un juego guardado. ¿Deseas continuar donde lo dejaste o empezar un nuevo juego?") },
            confirmButton = {
                Button(onClick = { viewModel.resumeSavedGame() }) {
                    Text("Continuar")
                }
            },
            dismissButton = {
                Button(onClick = {
                    viewModel.dismissResumeGameDialog()
                    viewModel.restartGame()
                }) {
                    Text("Nuevo juego")
                }
            }
        )
    }

    if (showIntroDialog && !showResumeGameDialog) {
        AlertDialog(
            onDismissRequest = { showIntroDialog = false },
            confirmButton = {
                Button(onClick = { showIntroDialog = false }) {
                    Text("¡Entendido!")
                }
            },
            title = { Text("¿Cómo jugar?") },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Image(
                        painter = painterResource(id = R.drawable.instruccionesjuego),
                        contentDescription = "Imagen de ayuda",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Observa la imagen y selecciona la palabra que se escriba correctamente. ¡Buena suerte!")
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }

    if (isGameFinished) {
        AlertDialog(
            onDismissRequest = {
                viewModel.resetState()
                navController.popBackStack()
            },
            title = { Text("¡Juego Finalizado!") },
            text = { Text("¡Felicidades, completaste todos los niveles!") },
            confirmButton = {
                Button(onClick = {
                    viewModel.resetState()
                    navController.popBackStack()
                }) {
                    Text("Volver al Menú")
                }
            }
        )
    }

    if (showMilestoneDialog) {
        LaunchedEffect(Unit) {
            delay(2000)
            viewModel.dismissMilestoneDialog()
        }

        AlertDialog(
            onDismissRequest = { viewModel.dismissMilestoneDialog() },
            title = { Text("¡Felicidades!") },
            text = { Text("Has respondido correctamente 5 niveles seguidos. ¡Sigue así!") },
            confirmButton = {}
        )
    }

    if (showContinueDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissContinueDialog() },
            title = { Text("¡Nivel 10 alcanzado!") },
            text = { Text("¿Deseas continuar con más niveles o volver al menú?") },
            confirmButton = {
                Button(onClick = { viewModel.dismissContinueDialog() }) {
                    Text("Continuar")
                }
            },
            dismissButton = {
                Button(onClick = {
                    viewModel.exitToMenu()
                    viewModel.dismissContinueDialog()
                    navController.popBackStack()
                }) {
                    Text("Volver al Menú")
                }
            }
        )
    }

    if (!isGameFinished && !showResumeGameDialog) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFAF7AC5))
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Image(
                painter = painterResource(id = currentQuestion.imageResId),
                contentDescription = null,
                modifier = Modifier.size(250.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            shuffledOptions.forEach { option ->
                Button(
                    onClick = {
                        viewModel.checkAnswer(option)
                        if (option == currentQuestion.correctWord) {
                            mediaPlayerCorrect.start()
                        } else {
                            mediaPlayerIncorrect.start()
                            vibrate(context)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = option,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            when (isAnswerCorrect) {
                true -> {
                    Text(
                        "✅ ¡Correcto!",
                        color = Color(0xFF2ECC71),
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { viewModel.nextQuestion() }) {
                        Text("Siguiente")
                    }
                }

                false -> {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFCDD2)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = "❌ Incorrecto. Intenta de nuevo.",
                            modifier = Modifier.padding(16.dp),
                            color = Color.Red,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                else -> {}
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            if (!isGameFinished) {
                viewModel.saveGameProgress()
            }
        }
    }
}
