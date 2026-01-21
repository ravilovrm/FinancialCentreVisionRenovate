package com.example.myapplication.imu

import kotlin.math.abs
import kotlin.math.pow

class StepDetector(
    private val g: Double,
    var kWeinberg: Double = 0.40
) {
    var steps: Int = 0
        private set
    var lastStepLengthM: Double = 0.70
        private set
    var speedMps: Double = 0.0
        private set

    private var lastStepTime = 0.0
    private var maxA = -1e9
    private var minA = 1e9
    private var prev = 0.0
    private var rising = false

    private val highThr = 0.60
    private val minStepDt = 0.28

    fun reset() {
        steps = 0
        lastStepLengthM = 0.70
        speedMps = 0.0
        lastStepTime = 0.0
        maxA = -1e9
        minA = 1e9
        prev = 0.0
        rising = false
    }

    fun update(accel: Vec3, timeSec: Double) {
        val a = accel.norm() - g
        maxA = maxOf(maxA, a)
        minA = minOf(minA, a)

        val da = a - prev
        if (da > 0) rising = true

        if (rising && a > highThr && da < 0) {
            val stepDt = timeSec - lastStepTime
            if (stepDt > minStepDt) {
                val amp = abs(maxA - minA).coerceAtLeast(1e-6)
                val L = (kWeinberg * amp.pow(0.25)).coerceIn(0.20, 1.50)

                steps += 1
                lastStepLengthM = L
                speedMps = (L / stepDt).coerceIn(0.0, 4.0)

                lastStepTime = timeSec
                maxA = -1e9
                minA = 1e9
            }
            rising = false
        }

        prev = a
    }
}