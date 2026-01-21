package com.example.myapplication.core

import android.app.Application
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.lifecycle.AndroidViewModel
import com.example.myapplication.imu.MotionEstimator
import com.example.myapplication.imu.Vec3
import com.example.myapplication.model.MotionMode
import com.example.myapplication.model.MotionUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

class MotionViewModel(app: Application) : AndroidViewModel(app), SensorEventListener {

    private val sm = app.getSystemService(SensorManager::class.java)

    private val accelS = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val gyroS = sm.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

    private val est = MotionEstimator()

    private val _ui = MutableStateFlow(MotionUiState())
    val ui: StateFlow<MotionUiState> = _ui

    private var lastAccel: Vec3? = null
    private var lastGyroTs: Long = 0L

    fun start() {
        if (_ui.value.isRunning) return
        est.reset()
        lastAccel = null
        lastGyroTs = 0L

        sm.registerListener(this, accelS, SensorManager.SENSOR_DELAY_GAME)
        sm.registerListener(this, gyroS, SensorManager.SENSOR_DELAY_GAME)

        _ui.update { it.copy(isRunning = true) }
    }

    fun stop() {
        if (!_ui.value.isRunning) return
        sm.unregisterListener(this)
        _ui.update { it.copy(isRunning = false, speedMps = 0.0, quality = "—") }
    }

    fun setMode(mode: MotionMode) {
        est.mode = mode
        est.reset()
        _ui.update { it.copy(mode = mode, distanceM = 0.0, steps = 0, speedMps = 0.0, quality = "—") }
    }

    fun setStepK(k: Double) {
        est.setStepK(k)
        _ui.update { it.copy(stepK = k) }
    }

    override fun onCleared() {
        sm.unregisterListener(this)
        super.onCleared()
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit

    override fun onSensorChanged(event: SensorEvent) {
        if (!_ui.value.isRunning) return

        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> {
                lastAccel = Vec3(
                    event.values[0].toDouble(),
                    event.values[1].toDouble(),
                    event.values[2].toDouble()
                )
            }
            Sensor.TYPE_GYROSCOPE -> {
                val a = lastAccel ?: return

                val ts = event.timestamp
                if (lastGyroTs == 0L) {
                    lastGyroTs = ts
                    return
                }
                val dt = (ts - lastGyroTs) * 1e-9
                lastGyroTs = ts

                val g = Vec3(
                    event.values[0].toDouble(),
                    event.values[1].toDouble(),
                    event.values[2].toDouble()
                )

                val out = est.update(accel = a, gyro = g, dt = dt)

                _ui.update {
                    it.copy(
                        isRest = out.isRest,
                        quality = out.quality,
                        speedMps = out.speedMps,
                        distanceM = out.distanceM,
                        steps = out.steps,
                        stepLengthM = if (out.stepLenM > 0) out.stepLenM else it.stepLengthM,
                        stepK = est.getStepK()
                    )
                }
            }
        }
    }
}