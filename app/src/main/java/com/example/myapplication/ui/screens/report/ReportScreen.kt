package com.example.myapplication.ui.screens.report

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.myapplication.core.AppViewModel
import com.example.myapplication.ui.components.PrimaryButton
import com.example.myapplication.util.PdfReport
import com.example.myapplication.util.YandexManager
import kotlinx.coroutines.launch

@Composable
fun ReportScreen(padding: PaddingValues, vm: AppViewModel) {
    val ctx = LocalContext.current
    val ui by vm.ui.collectAsState()
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    // Лаунчер для сохранения PDF на устройство
    val createDocLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/pdf"),
        onResult = { uri ->
            if (uri == null) return@rememberLauncherForActivityResult
            val bytes = PdfReport.buildPdfBytes(ui)
            ctx.contentResolver.openOutputStream(uri)?.use { it.write(bytes) }
            Toast.makeText(ctx, "PDF сохранен на устройство!", Toast.LENGTH_SHORT).show()
        }
    )

    Column(
        modifier = Modifier
            .padding(padding)
            .padding(16.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Финальный отчет", style = MaterialTheme.typography.titleLarge)

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Проект: ${ui.projectName}", style = MaterialTheme.typography.titleMedium)
                if (ui.totalPrice > 0) {
                    Text("Итого: ${ui.totalPrice} руб.", color = MaterialTheme.colorScheme.primary)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(ui.materials?.summary ?: "Нет данных")
            }
        }

        Text("Облако Яндекс.Диск", style = MaterialTheme.typography.titleMedium)
        PrimaryButton(
            text = "☁️ Сохранить смету в Облако",
            onClick = {
                scope.launch {
                    val fileName = "Smeta_${ui.projectName.replace(" ", "_")}.pdf"
                    val bytes = PdfReport.buildPdfBytes(ui)

                    Toast.makeText(ctx, "Загрузка началась...", Toast.LENGTH_SHORT).show()

                    val success = YandexManager.uploadPdf(fileName, bytes)

                    if (success) {
                        Toast.makeText(ctx, "Успешно загружено!", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(ctx, "Ошибка загрузки (проверьте токен)", Toast.LENGTH_SHORT).show()
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        Text("Локальный экспорт", style = MaterialTheme.typography.titleMedium)
        Button(
            onClick = { createDocLauncher.launch("Report_${ui.projectName}.pdf") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Сохранить PDF в память телефона")
        }
    }
}