package com.example.myapplication.ui.screens.motion

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myapplication.core.MotionViewModel
import com.example.myapplication.model.MotionMode
import com.example.myapplication.ui.components.Chip
import com.example.myapplication.ui.components.PrimaryButton
import com.example.myapplication.ui.components.SecondaryButton
import kotlin.math.roundToInt

@Composable
fun MotionScreen(padding: PaddingValues, motionVm: MotionViewModel) {
    val ui by motionVm.ui.collectAsState()

    androidx.compose.foundation.layout.Column(
        modifier = Modifier.padding(padding).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Скорость и путь (IMU)", style = MaterialTheme.typography.titleLarge)

        Card(modifier = Modifier.fillMaxWidth()) {
            androidx.compose.foundation.layout.Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Режим: ${ui.quality}")
                Text("Покой: ${if (ui.isRest) "ДА" else "НЕТ"}", color = MaterialTheme.colorScheme.onSurfaceVariant)

                Text("Скорость: %.2f м/с (%.1f км/ч)".format(ui.speedMps, ui.speedMps * 3.6))
                Text("Дистанция: %.2f м".format(ui.distanceM))

                if (ui.mode == MotionMode.WALK) {
                    Text("Шаги: ${ui.steps}")
                    Text("Длина шага (последняя): %.2f м".format(ui.stepLengthM))
                }
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            androidx.compose.foundation.layout.Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Выбор режима", style = MaterialTheme.typography.titleMedium)

                androidx.compose.foundation.layout.Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Chip(text = "1-10 km/h", onClick = { motionVm.setMode(MotionMode.WALK) })
                    Chip(text = "> 10 km/h", onClick = { motionVm.setMode(MotionMode.METRO) })
                    Chip(text = "Рулетка", onClick = { motionVm.setMode(MotionMode.TAPE) })
                }

                if (ui.mode == MotionMode.WALK) {
                    Text("Weinberg k: ${((ui.stepK) * 100).roundToInt() / 100.0}")
                    Slider(
                        value = ui.stepK.toFloat(),
                        onValueChange = { motionVm.setStepK(it.toDouble()) },
                        valueRange = 0.10f..1.00f
                    )
                    Text("Подбери k на известной дистанции (50–200 м).", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        if (!ui.isRunning) {
            PrimaryButton(
                text = "Старт",
                onClick = { motionVm.start() },
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            SecondaryButton(
                text = "Стоп",
                onClick = { motionVm.stop() },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}