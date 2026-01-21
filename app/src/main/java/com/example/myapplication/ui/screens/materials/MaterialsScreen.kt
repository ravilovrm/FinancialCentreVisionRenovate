package com.example.myapplication.ui.screens.materials

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.myapplication.core.AppViewModel
import com.example.myapplication.model.MaterialsInput
import com.example.myapplication.ui.components.PrimaryButton

@Composable
fun MaterialsScreen(padding: PaddingValues, vm: AppViewModel) {
    val ui by vm.ui.collectAsState()

    val heightText = remember(ui.materialsInput.roomHeightM) { mutableStateOf(ui.materialsInput.roomHeightM.toString()) }
    val layersText = remember(ui.materialsInput.paintLayers) { mutableStateOf(ui.materialsInput.paintLayers.toString()) }
    val wasteText = remember(ui.materialsInput.wastePercent) { mutableStateOf(ui.materialsInput.wastePercent.toString()) }
    val packText = remember(ui.materialsInput.laminatePackM2) { mutableStateOf(ui.materialsInput.laminatePackM2.toString()) }

    androidx.compose.foundation.layout.Column(
        modifier = Modifier.padding(padding).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Смета материалов", style = MaterialTheme.typography.titleLarge)

        if (ui.plan == null) {
            Card(modifier = Modifier.fillMaxWidth()) {
                androidx.compose.foundation.layout.Column(modifier = Modifier.padding(16.dp)) {
                    Text("Сначала построй план на экране «Фото».", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            return
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            androidx.compose.foundation.layout.Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = heightText.value,
                    onValueChange = { heightText.value = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Высота потолка (м)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                OutlinedTextField(
                    value = layersText.value,
                    onValueChange = { layersText.value = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Слоёв краски") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                OutlinedTextField(
                    value = wasteText.value,
                    onValueChange = { wasteText.value = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Запас (%)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                OutlinedTextField(
                    value = packText.value,
                    onValueChange = { packText.value = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Ламинат: м² в упаковке") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                PrimaryButton(
                    text = "Пересчитать",
                    onClick = {
                        val h = heightText.value.replace(',', '.').toDoubleOrNull() ?: ui.materialsInput.roomHeightM
                        val l = layersText.value.toIntOrNull() ?: ui.materialsInput.paintLayers
                        val w = wasteText.value.toIntOrNull() ?: ui.materialsInput.wastePercent
                        val p = packText.value.replace(',', '.').toDoubleOrNull() ?: ui.materialsInput.laminatePackM2
                        vm.setMaterialsInput(
                            MaterialsInput(
                                roomHeightM = h.coerceIn(2.0, 4.5),
                                paintLayers = l.coerceIn(1, 4),
                                wastePercent = w.coerceIn(0, 40),
                                laminatePackM2 = p.coerceIn(1.0, 6.0),
                                paintCoverageM2PerL = ui.materialsInput.paintCoverageM2PerL
                            )
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            androidx.compose.foundation.layout.Column(modifier = Modifier.padding(16.dp)) {
                Text(ui.materials?.summary ?: "Нажми «Пересчитать».", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}