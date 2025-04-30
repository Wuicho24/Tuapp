package com.example.tuapp.ruleta

import android.media.MediaPlayer
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.tuapp.R
import com.example.tuapp.ui.theme.RuletaGradient
import java.text.Normalizer


@Composable
fun RuletaScreen() {
    var selectedLevel by remember { mutableStateOf<WordLevel?>(null) }
    var resultado by remember { mutableStateOf<Resultado?>(null) }
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = RuletaGradient)
            .padding(16.dp)
    ) {
        selectedLevel?.let { nivel ->
            JuegoRuleta(
                nivel = nivel,
                resultado = resultado,
                onResultado = { resultado = it },
                onVolver = {
                    selectedLevel = null
                    resultado = null
                }
            )
        } ?: SeleccionNivel(onNivelSeleccionado = { selectedLevel = it })
    }
}

@Composable
fun SeleccionNivel(onNivelSeleccionado: (WordLevel) -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Selecciona un nivel", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))

        niveles.forEach { nivel ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.LightGray, shape = MaterialTheme.shapes.medium)
                    .clickable { onNivelSeleccionado(nivel) }
                    .padding(16.dp)
            ) {
                Text(nivel.id, fontSize = 18.sp)
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

sealed class Resultado {
    data class Correcto(val palabra: String) : Resultado()
    data class Incorrecto(val palabra: String) : Resultado()
}

@Composable
fun JuegoRuleta(
    nivel: WordLevel,
    resultado: Resultado?,
    onResultado: (Resultado) -> Unit,
    onVolver: () -> Unit
) {
    val context = LocalContext.current
    var opcionesAleatorias by remember { mutableStateOf<List<String>>(emptyList()) }
    var mostrarImagen by remember { mutableStateOf(false) }
    var palabraFormada by remember { mutableStateOf<String?>(null) } // Guarda la palabra original con acentos/ñ

    LaunchedEffect(nivel.id) {
        opcionesAleatorias = nivel.opciones
            .shuffled()
            .take(5)
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Clave: ${nivel.clave}",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        if (opcionesAleatorias.isNotEmpty()) {
            RuletaCanvas(
                options = opcionesAleatorias,
                onSelected = { fragmentoSeleccionado ->
                    val palabraCompleta = nivel.clave + fragmentoSeleccionado

                    // Guardamos la palabra original con acentos y ñ
                    palabraFormada = palabraCompleta

                    val esCorrecta = palabraCompleta in nivel.correctas

                    MediaPlayer.create(
                        context,
                        if (esCorrecta) R.raw.correcto else R.raw.incorrecto
                    ).start()

                    onResultado(
                        if (esCorrecta) Resultado.Correcto(palabraCompleta)
                        else Resultado.Incorrecto(palabraCompleta)
                    )
                },
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .aspectRatio(1f)
            )
        } else {
            CircularProgressIndicator()
        }

        resultado?.let { res ->
            val backgroundColor: Color
            val borderColor: Color
            val textColor: Color
            val mensaje: String

            when (res) {
                is Resultado.Correcto -> {
                    backgroundColor = Color(0xFFC8E6C9)
                    borderColor = Color(0xFF2E7D32)
                    textColor = Color(0xFF2E7D32)
                    mensaje = "${res.palabra} es CORRECTO"
                }
                is Resultado.Incorrecto -> {
                    backgroundColor = Color(0xFFFFE0B2)
                    borderColor = Color(0xFFFF6F00)
                    textColor = Color(0xFFE65100)
                    mensaje = "${res.palabra} es INCORRECTO"
                }
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth()
                        .background(backgroundColor, shape = RoundedCornerShape(12.dp))
                        .border(2.dp, borderColor, shape = RoundedCornerShape(12.dp))
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = mensaje,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                if (palabraFormada != null) {
                    Button(
                        onClick = { mostrarImagen = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF51506)),
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Text("Ver imagen")
                    }
                }
            }
        }

        Text(
            text = "Volver a niveles",
            color = Color.Blue,
            modifier = Modifier
                .clickable { onVolver() }
                .padding(12.dp)
        )
    }

    if (mostrarImagen && palabraFormada != null) {
        Dialog(onDismissRequest = { mostrarImagen = false }) {
            Box(
                modifier = Modifier
                    .background(Color.White, shape = RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                // Palabra original para mostrar
                val palabraOriginal = palabraFormada!!.lowercase()

                // Palabra normalizada para buscar la imagen
                val imageName = normalizeString(palabraOriginal)

                val imageResId = remember(imageName) {
                    context.resources.getIdentifier(imageName, "drawable", context.packageName)
                }
                val finalImage = if (imageResId != 0) imageResId else R.drawable.imagen_default

                androidx.compose.foundation.Image(
                    painter = painterResource(id = finalImage),
                    contentDescription = "Imagen de $palabraOriginal", // Usamos la palabra original aquí
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                )
            }
        }
    }
}

// Función para normalizar texto (quitar acentos y ñ)
fun normalizeString(input: String): String {
    val normalized = Normalizer.normalize(input, Normalizer.Form.NFD)
    return normalized.replace("[^\\p{ASCII}]".toRegex(), "").lowercase()
}




