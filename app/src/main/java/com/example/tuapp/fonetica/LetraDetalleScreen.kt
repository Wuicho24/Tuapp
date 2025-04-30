package com.example.tuapp.fonetica

import android.content.Context
import android.media.MediaPlayer
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

fun reproducirAudio(context: Context, resId: Int) {
    val mediaPlayer = MediaPlayer.create(context, resId)
    mediaPlayer.setOnCompletionListener { it.release() }
    mediaPlayer.start()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LetraDetalleScreen(navController: NavController, letra: String) {
    val context = LocalContext.current
    val contenido = contenidoPorLetra[letra.uppercase()] ?: return

    // Selección aleatoria cada vez que entra a la pantalla
    val (randomImagenes, randomAudios) = remember {
        val indices = (0..4).shuffled().take(2) // Tomamos 2 aleatorios entre los primeros 4
        val imagenes = listOf(
            contenido.imagenes[indices[0]],
            contenido.imagenes[5], // Imagen fija al centro
            contenido.imagenes[indices[1]]
        )
        val audios = listOf(
            contenido.audios[indices[0]],
            contenido.audios[5], // Audio fijo
            contenido.audios[indices[1]]
        )
        imagenes to audios
    }

    val nombresAudios = listOf(
        context.resources.getResourceEntryName(randomAudios[0]),
        context.resources.getResourceEntryName(randomAudios[1]),
        context.resources.getResourceEntryName(randomAudios[2])
    )

    val pintores = randomImagenes.map { painterResource(id = it) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Letra $letra") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Atrás")
                    }
                }
            )
        },
        containerColor = Color(0xFFFFD3D3)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                for (i in 0..2) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Image(painter = pintores[i], contentDescription = null, modifier = Modifier.size(100.dp))
                        Spacer(Modifier.height(8.dp))
                        Button(onClick = { reproducirAudio(context, randomAudios[i]) }) {
                            Text(nombresAudios[i].replace("_", " ").replaceFirstChar { it.uppercase() })
                        }
                    }
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

