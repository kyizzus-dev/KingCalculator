package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.math.AngleMode
import com.example.math.ExpressionParser
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor

@Composable
fun GraphScreen(
  modifier: Modifier = Modifier
) {
  var functionExpr by remember { mutableStateOf("x^3 - 3*x") }
  var xRange by remember { mutableFloatStateOf(10f) } // [-10, 10]
  var yRange by remember { mutableFloatStateOf(10f) }
  var offsetX by remember { mutableFloatStateOf(0f) }
  var offsetY by remember { mutableFloatStateOf(0f) }

  val parser = remember { ExpressionParser(AngleMode.RAD) }
  var errorMessage by remember { mutableStateOf<String?>(null) }

  // Quick preset functions for students
  val presets = listOf("x^2 - 4", "sin(x)", "cos(x)", "x^3 - 3*x", "1/x", "exp(-x^2)")

  Column(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 14.dp, vertical = 8.dp),
    verticalArrangement = Arrangement.spacedBy(10.dp)
  ) {
    // Top Input Area
    OutlinedTextField(
      value = functionExpr,
      onValueChange = {
        functionExpr = it
        errorMessage = null
      },
      label = { Text("Función f(x)") },
      modifier = Modifier.fillMaxWidth().testTag("graph_function_input"),
      singleLine = true,
      trailingIcon = {
        IconButton(
          onClick = {
            offsetX = 0f
            offsetY = 0f
            xRange = 10f
            yRange = 10f
          }
        ) {
          Icon(Icons.Default.RestartAlt, contentDescription = "Reiniciar gráfica")
        }
      }
    )

    // Presets Row
    LazyRow(
      horizontalArrangement = Arrangement.spacedBy(6.dp),
      modifier = Modifier.fillMaxWidth()
    ) {
      items(presets) { p ->
        SuggestionChip(
          onClick = { functionExpr = p },
          label = { Text(p, fontSize = 11.sp, fontFamily = FontFamily.Monospace) }
        )
      }
    }

    // Graph Canvas Area
    Surface(
      modifier = Modifier
        .fillMaxWidth()
        .weight(1f)
        .clip(RoundedCornerShape(16.dp)),
      color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
      shape = RoundedCornerShape(16.dp)
    ) {
      Box(modifier = Modifier.fillMaxSize()) {
        val primaryColor = MaterialTheme.colorScheme.primary
        val gridColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
        val axisColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)

        Canvas(
          modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
              detectTransformGestures { _, pan, zoom, _ ->
                xRange = (xRange / zoom).coerceIn(2f, 100f)
                yRange = (yRange / zoom).coerceIn(2f, 100f)
                offsetX += (pan.x / size.width) * (2f * xRange)
                offsetY -= (pan.y / size.height) * (2f * yRange)
              }
            }
        ) {
          val w = size.width
          val h = size.height

          // Center in screen pixels
          val cx = w / 2f + (offsetX / (2f * xRange)) * w
          val cy = h / 2f + (offsetY / (2f * yRange)) * h

          val scaleX = w / (2f * xRange)
          val scaleY = h / (2f * yRange)

          // 1. Draw Grid Lines
          val stepX = if (xRange > 20) 5f else if (xRange > 10) 2f else 1f
          val stepY = if (yRange > 20) 5f else if (yRange > 10) 2f else 1f

          var gx = -floor(xRange * 2 / stepX) * stepX
          while (gx <= xRange * 2) {
            val px = cx + gx * scaleX
            if (px in 0f..w) {
              drawLine(gridColor, Offset(px, 0f), Offset(px, h), strokeWidth = 1f)
            }
            gx += stepX
          }

          var gy = -floor(yRange * 2 / stepY) * stepY
          while (gy <= yRange * 2) {
            val py = cy - gy * scaleY
            if (py in 0f..h) {
              drawLine(gridColor, Offset(0f, py), Offset(w, py), strokeWidth = 1f)
            }
            gy += stepY
          }

          // 2. Draw Main Axes
          if (cy in 0f..h) {
            drawLine(axisColor, Offset(0f, cy), Offset(w, cy), strokeWidth = 2.5f)
          }
          if (cx in 0f..w) {
            drawLine(axisColor, Offset(cx, 0f), Offset(cx, h), strokeWidth = 2.5f)
          }

          // 3. Evaluate and Plot f(x)
          val curvePath = Path()
          var pathStarted = false
          val numPoints = w.toInt()

          for (i in 0..numPoints step 2) {
            val screenX = i.toFloat()
            val mathX = (screenX - cx) / scaleX

            try {
              val mathY = parser.evaluateReal(functionExpr, mathX.toDouble())
              if (mathY.isNaN() || mathY.isInfinite() || abs(mathY) > yRange * 10) {
                pathStarted = false
                continue
              }
              val screenY = cy - (mathY.toFloat() * scaleY)

              if (!pathStarted) {
                curvePath.moveTo(screenX, screenY)
                pathStarted = true
              } else {
                curvePath.lineTo(screenX, screenY)
              }
            } catch (e: Exception) {
              pathStarted = false
            }
          }

          drawPath(curvePath, color = primaryColor, style = Stroke(width = 3.5f))
        }

        // Overlay Zoom Controls
        Column(
          modifier = Modifier
            .align(Alignment.BottomEnd)
            .padding(12.dp),
          verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          SmallFloatingActionButton(
            onClick = {
              xRange = (xRange * 0.75f).coerceAtLeast(2f)
              yRange = (yRange * 0.75f).coerceAtLeast(2f)
            },
            containerColor = MaterialTheme.colorScheme.surface
          ) {
            Icon(Icons.Default.Add, contentDescription = "Zoom In")
          }
          SmallFloatingActionButton(
            onClick = {
              xRange = (xRange * 1.33f).coerceAtMost(100f)
              yRange = (yRange * 1.33f).coerceAtMost(100f)
            },
            containerColor = MaterialTheme.colorScheme.surface
          ) {
            Icon(Icons.Default.Remove, contentDescription = "Zoom Out")
          }
        }

        // Current coordinates scale info
        Text(
          text = "x: [${String.format(java.util.Locale.US, "%.1f", -xRange)}, ${String.format(java.util.Locale.US, "%.1f", xRange)}]",
          fontSize = 11.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier
            .align(Alignment.TopStart)
            .padding(8.dp)
        )
      }
    }

    // Values Table Quick Bar
    Surface(
      shape = RoundedCornerShape(12.dp),
      color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
      modifier = Modifier.fillMaxWidth()
    ) {
      Row(
        modifier = Modifier.padding(10.dp).fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceAround
      ) {
        listOf(-2.0, -1.0, 0.0, 1.0, 2.0).forEach { testX ->
          val evalY = try {
            val y = parser.evaluateReal(functionExpr, testX)
            String.format(java.util.Locale.US, "%.2f", y)
          } catch (e: Exception) {
            "?"
          }
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("f($testX)", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(evalY, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
          }
        }
      }
    }
  }
}
