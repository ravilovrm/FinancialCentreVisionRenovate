package com.example.myapplication.ui.screens.notes

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myapplication.data.notes.NotesViewModel
import com.example.myapplication.ui.components.PrimaryButton
import com.example.myapplication.ui.components.SecondaryButton

@Composable
fun NotesScreen(padding: PaddingValues, vm: NotesViewModel) {
    // Получаем список заметок из ViewModel
    val notes by vm.notes.collectAsState()

    var title by remember { mutableStateOf("") }
    var body by remember { mutableStateOf("") }
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .padding(padding)
            .padding(16.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Заметки проекта", style = MaterialTheme.typography.titleLarge)

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Статус: Яндекс.Диск подключен ✅", color = MaterialTheme.colorScheme.primary)

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Заголовок") }
                )
                OutlinedTextField(
                    value = body,
                    onValueChange = { body = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Текст заметки") }
                )
                PrimaryButton(
                    text = "Добавить заметку",
                    onClick = {
                        if (title.isNotBlank() || body.isNotBlank()) {
                            vm.add(title.ifEmpty { "Без названия" }, body.ifEmpty { "—" })
                            title = ""
                            body = ""
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                // Кнопка синхронизации
                SecondaryButton(
                    text = "Синхронизировать с Облаком",
                    onClick = { vm.uploadNotesToYandex() },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Отображение списка заметок
        notes.forEach { noteItem ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(noteItem.title, style = MaterialTheme.typography.titleMedium)
                    Text(noteItem.body, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}