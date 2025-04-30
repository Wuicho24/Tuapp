package com.example.tuapp.fonetica

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AbecedarioScreen(navController: NavController) {
    val letras = remember { ('A'..'L').toList() }

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
        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            contentPadding = PaddingValues(16.dp),
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(letras) { letra ->
                Box(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .background(Color.White.copy(alpha = 0.8f), shape = RoundedCornerShape(12.dp))
                        .clickable {
                            navController.navigate("detalle_letra/$letra")
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = letra.toString(),
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }
            }
        }
    }
}
