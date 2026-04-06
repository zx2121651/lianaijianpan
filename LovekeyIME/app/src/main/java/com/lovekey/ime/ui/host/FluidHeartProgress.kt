package com.lovekey.ime.ui.host

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.sin

@Composable
fun FluidHeartProgress(
    percentage: Float,
    modifier: Modifier = Modifier,
    frontWaveColor: Color = Color(0xFFE91E63),
    backWaveColor: Color = Color(0x88E91E63),
    backgroundColor: Color = Color(0xFFFCE4EC)
) {
    val infiniteTransition = rememberInfiniteTransition()

    // Wave 1 translation
    val waveOffset1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    // Wave 2 translation (slightly slower)
    val waveOffset2 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // Calculate heart path
            val heartPath = Path().apply {
                val scaleX = width / 1.5f
                val scaleY = height / 1.5f
                val offsetX = width / 2f
                val offsetY = height / 2.5f

                // Parametric heart equation
                moveTo(offsetX, offsetY - scaleY * 0.3f)

                // Using a standard bezier heart curve approximated to the bounding box
                cubicTo(
                    offsetX + scaleX * 0.7f, offsetY - scaleY * 0.9f,
                    offsetX + scaleX * 1.5f, offsetY + scaleY * 0.1f,
                    offsetX, offsetY + scaleY * 1.2f
                )

                cubicTo(
                    offsetX - scaleX * 1.5f, offsetY + scaleY * 0.1f,
                    offsetX - scaleX * 0.7f, offsetY - scaleY * 0.9f,
                    offsetX, offsetY - scaleY * 0.3f
                )
                close()
            }

            // Draw background heart
            drawPath(heartPath, color = backgroundColor)

            // Clip to heart shape
            clipPath(heartPath) {
                // Calculate wave base height based on percentage
                // 0% -> bottom (height), 100% -> top (0f)
                val baseHeight = height - (height * percentage)
                val waveAmplitude = 12.dp.toPx()

                // Draw back wave
                val backWavePath = Path().apply {
                    moveTo(0f, height)
                    lineTo(0f, baseHeight)

                    for (x in 0..width.toInt() step 5) {
                        val normalizedX = x / width
                        val y = baseHeight + waveAmplitude * sin(normalizedX * 2 * Math.PI + waveOffset2).toFloat()
                        lineTo(x.toFloat(), y)
                    }

                    lineTo(width, height)
                    close()
                }
                drawPath(backWavePath, color = backWaveColor)

                // Draw front wave
                val frontWavePath = Path().apply {
                    moveTo(0f, height)
                    lineTo(0f, baseHeight)

                    for (x in 0..width.toInt() step 5) {
                        val normalizedX = x / width
                        val y = baseHeight + waveAmplitude * sin(normalizedX * 2 * Math.PI + waveOffset1).toFloat()
                        lineTo(x.toFloat(), y)
                    }

                    lineTo(width, height)
                    close()
                }
                drawPath(frontWavePath, color = frontWaveColor)
            }
        }

        // Percentage Text
        Text(
            text = "${(percentage * 100).toInt()}%",
            color = Color.White,
            fontSize = 32.sp,
            fontWeight = FontWeight.Black,
            style = androidx.compose.ui.text.TextStyle(
                shadow = Shadow(
                    color = Color(0x66000000),
                    offset = Offset(2f, 2f),
                    blurRadius = 4f
                )
            )
        )
    }
}
