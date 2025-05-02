package com.example.tuapp.fonetica

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AbecedarioScreen(navController: NavController) {
    val letras = remember { ('A'..'Z').toList() }
    val listState = rememberLazyListState()
    val context = LocalContext.current

    val centeredIndex = remember {
        derivedStateOf {
            val visibleItems = listState.layoutInfo.visibleItemsInfo
            if (visibleItems.isNotEmpty()) {
                val center = listState.layoutInfo.viewportEndOffset / 2
                visibleItems.minByOrNull { kotlin.math.abs(it.offset + it.size / 2 - center) }?.index ?: 0
            } else 0
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Abecedario") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Atrás")
                    }
                }
            )
        },
        containerColor = Color(0xFFFFD3D3)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(top = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // LazyRow con scroll horizontal de letras
            LazyRow(
                state = listState,
                contentPadding = PaddingValues(horizontal = 32.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
            ) {
                itemsIndexed(letras) { index, letra ->
                    Box(
                        modifier = Modifier
                            .background(
                                if (index == centeredIndex.value) Color.White else Color.LightGray,
                                shape = RoundedCornerShape(10.dp)
                            )
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .clickable {
                                navController.navigate("detalle_letra/$letra")
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = letra.toString(),
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Letra grande centrada
            Text(
                text = letras[centeredIndex.value].toString(),
                fontSize = 120.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    navController.navigate("detalle_letra/${letras[centeredIndex.value]}")
                },
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Ver detalles de la letra")
            }
        }
    }
}



@Composable
fun AbecedarioBar(
    letras: List<Char> = ('A'..'Z').toList(),
    letraSeleccionada: Char,
    onLetraSeleccionada: (Char) -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(letras) { letra ->
            val isSelected = letra.toChar() == letraSeleccionada
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) Color.White else Color.LightGray)
                    .clickable { onLetraSeleccionada(letra.toChar()) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = letra.toString(),
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (isSelected) Color.Black else Color.DarkGray,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}


