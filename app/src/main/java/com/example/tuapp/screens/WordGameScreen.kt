package com.example.tuapp.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.tuapp.R
import kotlinx.coroutines.delay
import java.text.Normalizer

//Fondo
@Composable
fun FloatingImageBackground(iconList: List<Int>) {
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp

    val columns = 6
    val rows = 8
    val horizontalSpacing = screenWidth / columns
    val verticalSpacing = screenHeight / rows

    val positions = List(columns * rows) { index ->
        val column = index % columns
        val row = index / columns
        Pair(
            (horizontalSpacing * column).value,
            (verticalSpacing * row).value
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        positions.forEachIndexed { index, (x, y) ->
            val iconResId = if (index % 2 == 0) iconList[0] else iconList[1]
            val offsetY = remember { Animatable(y) }

            LaunchedEffect(Unit) {
                offsetY.animateTo(
                    targetValue = y + 10f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(durationMillis = 6000),
                        repeatMode = RepeatMode.Reverse
                    )
                )
            }

            Image(
                painter = painterResource(id = iconResId),
                contentDescription = null,
                modifier = Modifier
                    .size(36.dp)
                    .offset(x = x.dp, y = offsetY.value.dp)
                    .alpha(0.1f) // opacidad imagen
            )
        }
    }
}



@Composable
fun WordGameScreen(navController: NavController) {
    val viewModel: WordGameViewModel = viewModel()
    val wordState by viewModel.word.collectAsState()
    val currentLevel by viewModel.currentLevel.collectAsState()
    val context = LocalContext.current
    val showImageDialog = remember { mutableStateOf(false) }

    // Iniciar automáticamente en el nivel 1
    LaunchedEffect(Unit) {
        viewModel.changeLevel(1)
    }

    val gradientColors = when (currentLevel) {
        1 -> listOf(Color(0xFF22C1C3), Color(0xFFFDDD2D))
        2 -> listOf(Color(0xFFFF5733), Color(0xFFDAF7A6))
        3 -> listOf(Color(0xFFAF7AC5), Color(0xFF1ABC9C))
        4 -> listOf(Color(0xFF007AFF), Color(0xFFFDDC22))
        else -> listOf(Color(0xFF22C1C3), Color(0xFFFDDD2D))
    }

    val floatingIcons = listOf(
        R.drawable.ic_pencil,
        R.drawable.ic_eraser
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.linearGradient(
                    colors = gradientColors,
                    start = Offset(0f, 0f),
                    end = Offset(1000f, 1000f)
                )
            )
    ) {
        FloatingImageBackground(iconList = floatingIcons)

        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
        ) {
            // Botón de audio
            IconButton(
                onClick = { viewModel.playAudio(context, wordState.originalWord) },
                modifier = Modifier
                    .size(50.dp)
                    .background(Color.Blue, shape = CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Filled.VolumeUp,
                    contentDescription = "Reproducir audio",
                    tint = Color.White
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Botón de imagen
            IconButton(
                onClick = { showImageDialog.value = true },
                modifier = Modifier
                    .size(50.dp)
                    .background(Color(0xFF4CAF50), shape = CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Filled.Image,
                    contentDescription = "Mostrar imagen",
                    tint = Color.White
                )
            }
        }

        LevelSelector(
            currentLevel = currentLevel,
            onLevelChange = { viewModel.changeLevel(it) }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 80.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Completa la palabra:",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(10.dp))

            val shakeOffset = remember { mutableStateOf(0f) }
            LaunchedEffect(wordState.shakeEffect) {
                if (wordState.shakeEffect) {
                    repeat(4) {
                        shakeOffset.value = if (it % 2 == 0) 10f else -10f
                        delay(50)
                    }
                    shakeOffset.value = 0f
                }
            }

            //Nivel 4 fragmentacion
            if (currentLevel == 4) {
                // Tamaño dinámico de cada casilla para fragmentos
                val boxSize = when {
                    wordState.currentFragments.size <= 5 -> 65.dp
                    wordState.currentFragments.size <= 8 -> 55.dp
                    else -> 48.dp
                }

                // FRAGMENTOS DE LA PALABRA
                Row(
                    modifier = Modifier
                        .horizontalScroll(rememberScrollState())
                        .offset(x = shakeOffset.value.dp)
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    wordState.currentFragments.forEachIndexed { index, fragment ->
                        val scale = remember { mutableStateOf(1f) }
                        val attemptsForThisIndex = wordState.attemptsByIndex[index] ?: 0
                        val shouldJump = wordState.wrongLetterIndex == index && attemptsForThisIndex <= 1
                        val shouldShowRedDot = wordState.wrongLetterIndex == index && attemptsForThisIndex >= 1

                        LaunchedEffect(wordState.wrongLetterIndex, attemptsForThisIndex) {
                            if (shouldJump) {
                                repeat(2) {
                                    scale.value = 1.2f
                                    delay(150)
                                    scale.value = 1f
                                    delay(150)
                                }
                            }
                        }

                        Box(
                            modifier = Modifier
                                .padding(4.dp)
                                .size(boxSize)
                                .scale(scale.value)
                                .background(Color.White.copy(alpha = 0.7f), shape = RoundedCornerShape(10.dp))
                                .border(1.dp, Color.Gray.copy(alpha = 0.5f), shape = RoundedCornerShape(10.dp))
                                // Añadir condición para clickable
                                .clickable(enabled = wordState.isFragmentInsertedByUser.getOrNull(index) == true) {
                                    viewModel.removeFragmentAt(index)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = fragment ?: "__",
                                fontSize = if (boxSize == 48.dp) 16.sp else 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (shouldShowRedDot) Color.Red else Color.Black
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // FRAGMENTOS DISPONIBLES
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    val fragmentBoxSize = if (wordState.availableFragments.size > 6) 55.dp else 65.dp

                    wordState.availableFragments.forEach { fragment ->
                        Box(
                            modifier = Modifier
                                .padding(6.dp)
                                .size(fragmentBoxSize)
                                .background(Color.LightGray.copy(alpha = 0.6f), shape = RoundedCornerShape(10.dp))
                                .border(1.dp, Color.Gray.copy(alpha = 0.4f), shape = RoundedCornerShape(10.dp))
                                .clickable { viewModel.selectFragment(fragment) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = fragment,
                                fontSize = if (fragmentBoxSize == 55.dp) 18.sp else 22.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.Black
                            )
                        }
                    }
                }
            } else {
                // Niveles 1-3
                // Tamaño dinámico de cada casilla
                val boxSize = when {
                    wordState.currentWord.size <= 6 -> 48.dp
                    wordState.currentWord.size <= 10 -> 40.dp
                    else -> 32.dp
                }

                // LETRAS DE LA PALABRA
                Row(
                    modifier = Modifier
                        .horizontalScroll(rememberScrollState())
                        .offset(x = shakeOffset.value.dp)
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    wordState.currentWord.forEachIndexed { index, char ->
                        val scale = remember { mutableStateOf(1f) }
                        val attemptsForThisIndex = wordState.attemptsByIndex[index] ?: 0
                        val shouldJump = wordState.wrongLetterIndex == index && attemptsForThisIndex <= 2
                        val shouldShowRedDot = wordState.wrongLetterIndex == index && attemptsForThisIndex >= 3

                        LaunchedEffect(wordState.wrongLetterIndex, attemptsForThisIndex) {
                            if (shouldJump) {
                                repeat(2) {
                                    scale.value = 1.2f
                                    delay(150)
                                    scale.value = 1f
                                    delay(150)
                                }
                            }
                        }

                        Box(
                            modifier = Modifier
                                .padding(4.dp)
                                .size(boxSize)
                                .scale(scale.value)
                                .background(Color.White.copy(alpha = 0.7f), shape = RoundedCornerShape(10.dp))
                                .border(1.dp, Color.Gray.copy(alpha = 0.5f), shape = RoundedCornerShape(10.dp))
                                .clickable { viewModel.removeLetterAt(index) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = char?.toString() ?: "_",
                                fontSize = if (boxSize == 32.dp) 18.sp else 26.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (shouldShowRedDot) Color.Red else Color.Black
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // LETRAS DISPONIBLES
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    val letterBoxSize = if (wordState.availableLetters.size > 8) 40.dp else 48.dp

                    wordState.availableLetters.forEach { letter ->
                        Box(
                            modifier = Modifier
                                .padding(6.dp)
                                .size(letterBoxSize)
                                .background(Color.LightGray.copy(alpha = 0.6f), shape = RoundedCornerShape(10.dp))
                                .border(1.dp, Color.Gray.copy(alpha = 0.4f), shape = RoundedCornerShape(10.dp))
                                .clickable { viewModel.selectLetter(letter) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = letter,
                                fontSize = if (letterBoxSize == 40.dp) 20.sp else 24.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.Black
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(onClick = { viewModel.checkAnswer() }) {
                Text("Verificar")
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (wordState.isCorrect) {
                Text(
                    "¡Correcto!",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Green
                )
                Spacer(modifier = Modifier.height(10.dp))
                Button(onClick = { viewModel.fetchNewWord() }) {
                    Text("Siguiente")
                }
            }

            if (wordState.showAlert) {
                AlertDialog(
                    onDismissRequest = { viewModel.dismissAlert() },
                    title = { Text("Atención") },
                    text = { Text("Completa todas las letras antes de verificar") },
                    confirmButton = {
                        Button(onClick = { viewModel.dismissAlert() }) {
                            Text("Entendido")
                        }
                    }
                )
            }
        }

        // Diálogo para mostrar la imagen relacionada con la palabra
        if (showImageDialog.value) {
            ImageDialog(
                word = wordState.originalWord,
                onDismiss = { showImageDialog.value = false }
            )
        }
    }
}

@Composable
fun ImageDialog(word: String, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 450.dp)
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),

        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = word,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Aquí cargamos la imagen
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp)
                        .background(Color.LightGray.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    WordImage(word = word)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Cerrar")
                }
            }
        }
    }
}


//testeo
fun normalizeString(input: String): String {
    val normalized = Normalizer.normalize(input, Normalizer.Form.NFD)
    return normalized.replace("[^\\p{ASCII}]".toRegex(), "")
}

@Composable
fun WordImage(word: String) {
    val context = LocalContext.current

    // Obtener el identificador del recurso basado en el nombre normalizado de la palabra
    val resourceId = remember(word) {
        val resourceName = normalizeString(word.lowercase())
        context.resources.getIdentifier(resourceName, "drawable", context.packageName)
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        if (resourceId != 0) {
            // La imagen existe en los recursos
            Image(
                painter = painterResource(id = resourceId),
                contentDescription = "Imagen para $word",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
        } else {
            // La imagen no se encontró en los recursos
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.BrokenImage,
                    contentDescription = "Imagen no encontrada",
                    modifier = Modifier.size(48.dp),
                    tint = Color.Gray
                )
                Text(
                    text = "No se encontró imagen para \"$word\"",
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}




@Composable
fun LevelSelector(currentLevel: Int, onLevelChange: (Int) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val levels = listOf(1, 2, 3, 4)

    Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(50.dp)
                .background(Color(0xFF007AFF), shape = CircleShape)
                .clickable { expanded = !expanded },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = currentLevel.toString(),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically(),
            modifier = Modifier.align(Alignment.TopEnd)
        ) {
            Column(
                modifier = Modifier
                    .width(50.dp)
                    .background(Color.White, shape = RoundedCornerShape(10.dp))
                    .shadow(4.dp)
            ) {
                levels.forEach { level ->
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .background(if (level == currentLevel) Color(0xFF007AFF) else Color.White, shape = CircleShape)
                            .clickable {
                                onLevelChange(level)
                                expanded = false
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = level.toString(),
                            fontSize = 16.sp,
                            color = if (level == currentLevel) Color.White else Color.Black
                        )
                    }
                }
            }
        }
    }
}
