package com.example.myapplication.ui.screens.scan

import android.graphics.ImageDecoder
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.myapplication.core.AppViewModel
import com.example.myapplication.navigation.Destinations
import com.example.myapplication.ui.components.PrimaryButton
import com.example.myapplication.ui.components.SecondaryButton
import com.example.myapplication.util.AutoPlan

@Composable
fun ScanScreen(padding: PaddingValues, vm: AppViewModel, nav: NavHostController) {
    val ctx = LocalContext.current
    val ui by vm.ui.collectAsState()
    val scrollState = rememberScrollState()

    val pickLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> vm.setImage(uri) }
    )

    val refLenText = remember(ui.referenceLengthM) { mutableStateOf(ui.referenceLengthM.toString()) }

    Column(
        modifier = Modifier
            .padding(padding)
            .padding(16.dp)
            .fillMaxSize()
            .verticalScroll(scrollState), // Добавили скролл
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("План помещения по фото", style = MaterialTheme.typography.titleLarge)

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                PrimaryButton(
                    text = "Выбрать фото (галерея)",
                    onClick = {
                        pickLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                SecondaryButton(
                    text = "Сделать фото (камера)",
                    onClick = { nav.navigate(Destinations.Camera.route) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        if (ui.imageUri == null) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Инструкция: выберите фото, расставьте 4 угла комнаты и 2 точки эталона (A-B), введите реальную длину между A и B.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            val uri = Uri.parse(ui.imageUri)

            LaunchedEffect(ui.imageUri) {
                val bmp = loadBitmap(ctx, uri)
                if (bmp != null) vm.setImageSize(bmp.width, bmp.height)
            }

            // Компонент редактора
            CornerEditor(
                imageUri = uri,
                imageW = ui.imageW,
                imageH = ui.imageH,
                corners = ui.corners,
                refA = ui.refA,
                refB = ui.refB,
                onSetCorners = vm::setCorners,
                onSetRefA = vm::setRefA,
                onSetRefB = vm::setRefB
            )

            // Поле ввода длины
            OutlinedTextField(
                value = refLenText.value,
                onValueChange = { refLenText.value = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Эталон длины (м) между точками A–B") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            PrimaryButton(
                text = "Применить масштаб и расчет",
                onClick = {
                    val v = refLenText.value.replace(',', '.').toDoubleOrNull()
                    if (v != null) vm.setReferenceLengthMeters(v)
                },
                modifier = Modifier.fillMaxWidth()
            )

            SecondaryButton(
                text = "Авто-разметка (AI)",
                onClick = {
                    val bmp = loadBitmap(ctx, uri) ?: return@SecondaryButton
                    vm.setCorners(AutoPlan.guessRectangle(bmp))
                },
                modifier = Modifier.fillMaxWidth()
            )

            // Отображение результатов
            if (ui.plan != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text("Результаты:", style = MaterialTheme.typography.titleMedium)
                        Text("Площадь: ${ui.plan!!.areaM2} м²")
                        Text("Периметр: ${ui.plan!!.perimeterM} м")

                        Button(
                            onClick = { nav.navigate(Destinations.Materials.route) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Перейти к смете материалов")
                        }
                    }
                }
            }
        }
    }
}

// Вспомогательная функция загрузки Bitmap
private fun loadBitmap(context: android.content.Context, uri: Uri): android.graphics.Bitmap? {
    return try {
        val src = ImageDecoder.createSource(context.contentResolver, uri)
        ImageDecoder.decodeBitmap(src) { decoder, _, _ -> decoder.isMutableRequired = false }
    } catch (_: Throwable) {
        try {
            @Suppress("DEPRECATION")
            MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
        } catch (_: Throwable) { null }
    }
}