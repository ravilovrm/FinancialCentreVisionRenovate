package com.example.myapplication.ui.screens.scan

import android.net.Uri
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.myapplication.model.NPoint
import com.example.myapplication.ui.components.Chip

@Composable
fun CornerEditor(
    imageUri: Uri,
    imageW: Int,
    imageH: Int,
    corners: List<NPoint>,
    refA: NPoint?,
    refB: NPoint?,
    onSetCorners: (List<NPoint>) -> Unit,
    onSetRefA: (NPoint?) -> Unit,
    onSetRefB: (NPoint?) -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    val mode = remember { mutableStateOf(Mode.CORNERS) }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = when (mode.value) {
                Mode.CORNERS -> "Режим: Укажите 4 угла комнаты"
                Mode.REF_A -> "Режим: Точка A (начало замера)"
                Mode.REF_B -> "Режим: Точка B (конец замера)"
            },
            style = MaterialTheme.typography.labelLarge,
            color = scheme.primary
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Chip(text = "Углы", onClick = { mode.value = Mode.CORNERS })
            Chip(text = "A", onClick = { mode.value = Mode.REF_A })
            Chip(text = "B", onClick = { mode.value = Mode.REF_B })
            Chip(text = "Сброс", onClick = {
                onSetCorners(emptyList())
                onSetRefA(null)
                onSetRefB(null)
            })
        }

        val ratio = if (imageW > 0 && imageH > 0) imageW.toFloat() / imageH.toFloat() else 4f / 3f

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(ratio)
                .pointerInput(mode.value, corners, refA, refB) {
                    detectTapGestures { tap ->
                        val w = size.width
                        val h = size.height
                        if (w <= 1f || h <= 1f) return@detectTapGestures

                        val p = NPoint(tap.x / w, tap.y / h).clamp01()
                        when (mode.value) {
                            Mode.CORNERS -> {
                                val next = corners.toMutableList()
                                if (next.size < 4) next.add(p) else next[3] = p
                                onSetCorners(next.take(4))
                            }
                            Mode.REF_A -> onSetRefA(p)
                            Mode.REF_B -> onSetRefB(p)
                        }
                    }
                }
        ) {
            AsyncImage(
                model = imageUri,
                contentDescription = null,
                modifier = Modifier.fillMaxSize()
            )

            Canvas(modifier = Modifier.fillMaxSize()) {
                fun drawPoint(p: NPoint, color: Color) {
                    val x = p.x * size.width
                    val y = p.y * size.height
                    drawCircle(color = color, radius = 12f, center = Offset(x, y))
                    drawCircle(color = Color.White, radius = 16f, center = Offset(x, y), style = Stroke(width = 4f))
                }

                // Рисуем углы и линии стен
                corners.forEach { drawPoint(it, scheme.primary) }
                if (corners.size == 4) {
                    for (i in 0..3) {
                        val a = corners[i]
                        val b = corners[(i + 1) % 4]
                        drawLine(
                            color = scheme.primary,
                            start = Offset(a.x * size.width, a.y * size.height),
                            end = Offset(b.x * size.width, b.y * size.height),
                            strokeWidth = 8f
                        )
                    }
                }

                // Рисуем эталонную линию (масштаб)
                if (refA != null) drawPoint(refA, scheme.tertiary)
                if (refB != null) drawPoint(refB, scheme.tertiary)
                if (refA != null && refB != null) {
                    drawLine(
                        color = scheme.tertiary,
                        start = Offset(refA.x * size.width, refA.y * size.height),
                        end = Offset(refB.x * size.width, refB.y * size.height),
                        strokeWidth = 6f
                    )
                }
            }
        }
    }
}

private enum class Mode { CORNERS, REF_A, REF_B }