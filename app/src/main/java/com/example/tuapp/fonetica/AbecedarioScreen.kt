package com.example.tuapp.fonetica

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AbecedarioScreen(navController: NavController) {
    val letras = remember { ('A'..'L').toList() }
    val context = LocalContext.current
    var letraSeleccionada by remember { mutableStateOf(letras[0]) }
    val listState = rememberLazyListState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Abecedario", style = MaterialTheme.typography.headlineMedium) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Atrás",
                            modifier = Modifier.size(28.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFFFD3D3),
                    titleContentColor = Color.Black
                )
            )
        },
        containerColor = Color(0xFFFFF5F5)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Scroll horizontal de letras
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .shadow(
                        elevation = 4.dp,
                        shape = RoundedCornerShape(16.dp)
                    )
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFFFE6E6))
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                LazyRow(
                    state = listState,
                    contentPadding = PaddingValues(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    itemsIndexed(letras) { index, letra ->
                        val isSelected = letra == letraSeleccionada
                        var isPressed by remember { mutableStateOf(false) }
                        val scale = animateFloatAsState(
                            targetValue = when {
                                isSelected -> 1.3f
                                isPressed -> 1.2f
                                else -> 1f
                            },
                            animationSpec = tween(200)
                        )

                        Box(
                            modifier = Modifier
                                .scale(scale.value)
                                .size(48.dp)
                                .shadow(
                                    elevation = if (isSelected || isPressed) 6.dp else 2.dp,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    color = when {
                                        isSelected -> Color(0xFFFF9E9E)
                                        isPressed -> Color(0xFFFFC7C7)
                                        else -> Color.White
                                    },
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .border(
                                    width = if (isSelected || isPressed) 2.dp else 1.dp,
                                    color = if (isSelected) Color(0xFFFF5252)
                                    else if (isPressed) Color(0xFFFF8080)
                                    else Color(0xFFE0E0E0),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable(
                                    onClick = { letraSeleccionada = letra },
                                    onClickLabel = "Seleccionar letra ${letra}"
                                )
                                .padding(4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = letra.toString(),
                                fontSize = 20.sp,
                                fontWeight = when {
                                    isSelected -> FontWeight.Bold
                                    isPressed -> FontWeight.SemiBold
                                    else -> FontWeight.Medium
                                },
                                color = when {
                                    isSelected -> Color.Black
                                    isPressed -> Color(0xFF333333)
                                    else -> Color(0xFF555555)
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Sección de la letra seleccionada
            AnimatedVisibility(
                visible = true,
                enter = fadeIn(animationSpec = tween(500)) +
                        expandVertically(animationSpec = tween(500))
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .shadow(
                            elevation = 8.dp,
                            shape = RoundedCornerShape(24.dp)
                        )
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color.White)
                        .padding(vertical = 24.dp, horizontal = 16.dp)
                ) {
                    // Letra grande centrada
                    Text(
                        text = letraSeleccionada.toString(),
                        fontSize = 120.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFF5252)
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    Button(
                        onClick = {
                            navController.navigate("detalle_letra/$letraSeleccionada")
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFF5252)
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth(0.7f)
                            .height(56.dp)
                            .shadow(4.dp, RoundedCornerShape(16.dp))
                    ) {
                        Text(
                            "Ver detalles de la letra",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}