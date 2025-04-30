package com.example.tuapp.ruleta

import android.media.MediaPlayer
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.tuapp.R
import kotlinx.coroutines.launch
import kotlin.math.*


private fun DrawScope.drawSegmentText(
    text: String,
    index: Int,
    segmentAngle: Float,
    radius: Float,
    center: Offset
) {
    val angleInRadians = (index + 0.5f) * segmentAngle * PI.toFloat() / 180f
    val textRadius = radius * 0.65f
    val x = center.x + textRadius * cos(angleInRadians)
    val y = center.y + textRadius * sin(angleInRadians)

    with(drawContext.canvas.nativeCanvas) {
        val paint = android.graphics.Paint().apply {
            color = android.graphics.Color.BLACK
            textSize = 36f
            textAlign = android.graphics.Paint.Align.CENTER
            isAntiAlias = true
        }
        drawText(text, x, y, paint)
    }
}

@Composable
fun RuletaCanvas(
    options: List<String>,
    onSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var opciones by remember { mutableStateOf(options.toMutableList()) }
    val pastelColors = listOf(
        Color(0xFFFFCCBC), Color(0xFFE1BEE7), Color(0xFFB3E5FC),
        Color(0xFFC8E6C9), Color(0xFFFFF9C4), Color(0xFFFFF0E1), Color(0xFFD7CCC8)
    )
    val rotation = remember { Animatable(0f) }
    var isSpinning by remember { mutableStateOf(false) }
    var selectedIndex by remember { mutableStateOf(-1) }
    var isWaitingForConfirmation by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val segmentAngle = remember(opciones.size) { 360f / opciones.size }

    Box(
        modifier = modifier.clickable(enabled = !isSpinning) {
            if (opciones.isNotEmpty()) {
                if (isWaitingForConfirmation && selectedIndex != -1) {
                    // Si estamos esperando confirmación, eliminamos la opción seleccionada
                    opciones.removeAt(selectedIndex)
                    selectedIndex = -1
                    isWaitingForConfirmation = false
                } else {
                    val mediaPlayer = MediaPlayer.create(context, R.raw.ruleta_giro)
                    val audioDuration = mediaPlayer.duration
                    val rotationAmount = (1440..2000).random().toFloat()
                    val finalRotation = rotation.value + rotationAmount

                    mediaPlayer.start()
                    isSpinning = true

                    scope.launch {
                        rotation.animateTo(
                            finalRotation,
                            animationSpec = tween(
                                durationMillis = audioDuration,
                                easing = FastOutSlowInEasing
                            )
                        )

                        val normalizedRotation = (finalRotation % 360)
                        val calculatedIndex = ((opciones.size - (normalizedRotation / segmentAngle).toInt()) % opciones.size)
                            .let { if (it < 0) it + opciones.size else it }

                        selectedIndex = calculatedIndex
                        val palabraSeleccionada = opciones[selectedIndex]
                        onSelected(palabraSeleccionada)

                        isSpinning = false
                        isWaitingForConfirmation = true // Ahora esperamos al siguiente toque
                        mediaPlayer.release()
                    }
                }
            }
        },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasSize = size.minDimension
            val radius = canvasSize / 2f
            val center = Offset(radius, radius)
            val arcSize = Size(radius * 2, radius * 2)

            rotate(rotation.value, center) {
                opciones.forEachIndexed { i, option ->
                    val startAngle = i * segmentAngle

                    drawArc(
                        color = pastelColors[i % pastelColors.size],
                        startAngle = startAngle,
                        sweepAngle = segmentAngle,
                        useCenter = true,
                        size = arcSize,
                        topLeft = Offset.Zero
                    )

                    if (i == selectedIndex) {
                        drawArc(
                            color = Color.Red, // remarcar caida
                            startAngle = startAngle,
                            sweepAngle = segmentAngle,
                            useCenter = true,
                            size = arcSize,
                            topLeft = Offset.Zero,
                            style = Stroke(width = 10f)
                        )
                    }

                    drawSegmentText(option, i, segmentAngle, radius, center)
                }
            }
        }
    }
}



