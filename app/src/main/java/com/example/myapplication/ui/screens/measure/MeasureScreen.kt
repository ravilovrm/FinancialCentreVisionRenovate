package com.example.myapplication.ui.screens.measure

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.text.DecimalFormat
import kotlin.math.abs

// === ЦВЕТА ИНТЕРФЕЙСА ===
val HudColor = Color(0xFF00FFCC) // Неоновый циан
val HudWarning = Color(0xFFFF3333) // Красный

@Composable
fun MeasureScreen(padding: PaddingValues) {
    val context = LocalContext.current

    // Переменные для хранения данных сенсоров
    var pitch by remember { mutableFloatStateOf(0f) }
    var roll by remember { mutableFloatStateOf(0f) }
    var azimuth by remember { mutableFloatStateOf(0f) }

    // Переменные для GPS
    var locationInfo by remember { mutableStateOf("GPS: Поиск...") }
    var altitude by remember { mutableStateOf("Alt: ---") }

    // Логика запроса разрешений (Камера + Геолокация)
    var hasPermissions by remember { mutableStateOf(false) }
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        hasPermissions = perms.values.all { it }
    }

    // При запуске проверяем, есть ли права. Если нет — спрашиваем.
    LaunchedEffect(Unit) {
        val camPerm = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
        val locPerm = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)

        if (camPerm != PackageManager.PERMISSION_GRANTED || locPerm != PackageManager.PERMISSION_GRANTED) {
            launcher.launch(arrayOf(Manifest.permission.CAMERA, Manifest.permission.ACCESS_FINE_LOCATION))
        } else {
            hasPermissions = true
        }
    }

    // --- БЛОК СЕНСОРОВ (Гироскоп/Акселерометр) ---
    DisposableEffect(Unit) {
        val sm = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val rotSensor = sm.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                if (event.sensor.type == Sensor.TYPE_ROTATION_VECTOR) {
                    val rotationMatrix = FloatArray(9)
                    SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)

                    // Пересчитываем координаты для вертикального положения телефона
                    val adjustedRotationMatrix = FloatArray(9)
                    SensorManager.remapCoordinateSystem(rotationMatrix, SensorManager.AXIS_X, SensorManager.AXIS_Z, adjustedRotationMatrix)

                    val orientation = FloatArray(3)
                    SensorManager.getOrientation(adjustedRotationMatrix, orientation)

                    // Переводим радианы в градусы
                    val azDeg = (Math.toDegrees(orientation[0].toDouble()) + 360) % 360
                    val ptDeg = Math.toDegrees(orientation[1].toDouble())
                    val rlDeg = Math.toDegrees(orientation[2].toDouble())

                    azimuth = azDeg.toFloat()
                    pitch = ptDeg.toFloat()
                    roll = rlDeg.toFloat()
                }
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        if (rotSensor != null) {
            sm.registerListener(listener, rotSensor, SensorManager.SENSOR_DELAY_GAME)
        }
        onDispose { sm.unregisterListener(listener) }
    }

    // --- БЛОК GPS ---
    DisposableEffect(hasPermissions) {
        if (!hasPermissions) return@DisposableEffect onDispose {}

        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val locListener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                val df = DecimalFormat("0.0000")
                locationInfo = "${df.format(location.latitude)}, ${df.format(location.longitude)}"
                altitude = "Alt: ${location.altitude.toInt()}m"
            }
            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
            override fun onProviderEnabled(provider: String) {}
            override fun onProviderDisabled(provider: String) {}
        }

        try {
            // Обновляем раз в 2 секунды или каждые 10 метров
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000L, 10f, locListener)
        } catch (e: SecurityException) {
            locationInfo = "Нет прав GPS"
        }

        onDispose {
            try { locationManager.removeUpdates(locListener) } catch (e: Exception) {}
        }
    }

    // --- ОТРИСОВКА ЭКРАНА ---
    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        if (hasPermissions) {
            // 1. Слой КАМЕРЫ (На заднем плане)
            CameraPreview(modifier = Modifier.fillMaxSize())

            // 2. Слой HUD (Графика поверх камеры)
            HudOverlay(
                pitch = pitch,
                roll = roll,
                azimuth = azimuth,
                locationInfo = locationInfo,
                altitude = altitude,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            // Если прав нет, показываем текст
            Text(
                "Для работы Spyglass нужны разрешения на Камеру и Геолокацию.\nПожалуйста, дайте права в настройках.",
                color = Color.White,
                modifier = Modifier.align(Alignment.Center).padding(20.dp)
            )
        }
    }
}

// Компонент для отображения камеры
@Composable
fun CameraPreview(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    AndroidView(
        factory = { ctx ->
            PreviewView(ctx).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                scaleType = PreviewView.ScaleType.FILL_CENTER
            }
        },
        modifier = modifier,
        update = { previewView ->
            val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder().build()
                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                preview.setSurfaceProvider(previewView.surfaceProvider)
                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview)
                } catch (e: Exception) {
                    Log.e("Camera", "Ошибка запуска камеры", e)
                }
            }, ContextCompat.getMainExecutor(context))
        }
    )
}

// Рисование интерфейса (HUD)
@Composable
fun HudOverlay(
    pitch: Float,
    roll: Float,
    azimuth: Float,
    locationInfo: String,
    altitude: String,
    modifier: Modifier
) {
    val textMeasurer = rememberTextMeasurer()

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h / 2f

        // 1. Центральный прицел
        drawLine(HudColor, Offset(cx - 40f, cy), Offset(cx + 40f, cy), 4f)
        drawLine(HudColor, Offset(cx, cy - 40f), Offset(cx, cy + 40f), 4f)
        drawCircle(HudColor, 20f, Offset(cx, cy), style = Stroke(2f))

        // 2. Искусственный горизонт (вращается против наклона телефона)
        rotate(-roll, pivot = Offset(cx, cy)) {
            val pitchOffset = pitch * (h / 90f) // Смещение вверх/вниз

            // Главная линия горизонта
            drawLine(
                color = HudColor.copy(alpha = 0.8f),
                start = Offset(0f, cy + pitchOffset),
                end = Offset(w, cy + pitchOffset),
                strokeWidth = 3f
            )

            // Линии градусов (лестница)
            for (i in -60..60 step 10) {
                if (i == 0) continue
                val y = cy + pitchOffset + (i * (h / 90f))

                if (y > 0 && y < h) {
                    val width = if (i % 20 == 0) 200f else 100f
                    drawLine(
                        HudColor.copy(alpha = 0.5f),
                        Offset(cx - width / 2, y),
                        Offset(cx + width / 2, y),
                        2f
                    )
                    drawText(
                        textMeasurer, "${-i}",
                        topLeft = Offset(cx + width / 2 + 10, y - 20),
                        style = TextStyle(color = HudColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    )
                }
            }
        }

        // 3. Компас сверху
        val tapeTop = 120f
        // Треугольник-указатель
        drawPath(
            path = Path().apply {
                moveTo(cx, tapeTop + 25f)
                lineTo(cx - 15f, tapeTop)
                lineTo(cx + 15f, tapeTop)
                close()
            },
            color = HudWarning
        )

        // Шкала компаса
        val pxPerDeg = w / 90f // Ширина экрана = 90 градусов обзора
        for (deg in 0..360 step 5) {
            var diff = deg - azimuth
            if (diff < -180) diff += 360
            if (diff > 180) diff -= 360

            // Рисуем, если метка попадает в экран
            if (abs(diff * pxPerDeg) < w / 2) {
                val x = cx + (diff * pxPerDeg)
                val isCardinal = deg % 90 == 0 // Север, Юг, Запад, Восток
                val hLine = if (isCardinal) 40f else 20f

                drawLine(
                    if (isCardinal) HudWarning else HudColor,
                    Offset(x, tapeTop - hLine),
                    Offset(x, tapeTop),
                    if (isCardinal) 4f else 2f
                )

                if (deg % 45 == 0) {
                    val label = when(deg) {
                        0, 360 -> "N"
                        90 -> "E"
                        180 -> "S"
                        270 -> "W"
                        else -> "$deg"
                    }
                    drawText(
                        textMeasurer, label,
                        topLeft = Offset(x - 15, tapeTop - hLine - 50),
                        style = TextStyle(color = if (isCardinal) HudWarning else HudColor, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    )
                }
            }
        }

        // 4. Текстовая информация (Низ экрана)
        val infoStyle = TextStyle(color = HudColor, fontSize = 14.sp, fontFamily = FontFamily.Monospace)

        drawText(textMeasurer, "GPS COORD:\n$locationInfo", Offset(40f, h - 250f), infoStyle)
        drawText(textMeasurer, "ALTITUDE: $altitude\nPITCH: ${pitch.toInt()}°\nROLL: ${roll.toInt()}°", Offset(w - 300f, h - 250f), infoStyle)
    }
}