package com.example.myapplication.imu

import com.example.myapplication.model.MotionMode
import kotlin.math.abs

class MotionEstimator(
    private val g: Double = 9.81913
) {
    var mode: MotionMode = MotionMode.TAPE

    private var bg = Vec3(0.0, 0.0, 0.0)
    private var ba = Vec3(0.0, 0.0, 0.0)

    private val ahrs = MahonyAhrs(kp = 2.0, ki = 0.05)
    private val zupt = ZuptDetector(g = g)

    private val steps = StepDetector(g = g)
    private val metro = MetroEstimator()

    private var v = Vec3(0.0, 0.0, 0.0)
    private var p = Vec3(0.0, 0.0, 0.0)

    private var timeSec = 0.0

    private var segMoving = false
    private var segStartT = 0.0
    private var segStartP = Vec3(0.0, 0.0, 0.0)
    private var doneDistance = 0.0

    fun setStepK(k: Double) { steps.kWeinberg = k }
    fun getStepK(): Double = steps.kWeinberg

    fun reset() {
        bg = Vec3(0.0, 0.0, 0.0)
        ba = Vec3(0.0, 0.0, 0.0)
        ahrs.reset()
        zupt.reset()
        v = Vec3(0.0, 0.0, 0.0)
        p = Vec3(0.0, 0.0, 0.0)
        timeSec = 0.0
        segMoving = false
        segStartT = 0.0
        segStartP = Vec3(0.0, 0.0, 0.0)
        doneDistance = 0.0
        steps.reset()
        metro.reset()
    }

    data class Out(
        val isRest: Boolean,
        val quality: String,
        val speedMps: Double,
        val distanceM: Double,
        val steps: Int,
        val stepLenM: Double
    )

    fun update(accel: Vec3, gyro: Vec3, dt: Double): Out {
        if (dt <= 0.0) {
            return Out(true, "dt=0", 0.0, doneDistance, steps.steps, steps.lastStepLengthM)
        }
        timeSec += dt

        val isRest = zupt.update(accel, gyro)

        if (isRest) {
            val alpha = 0.03
            bg = bg * (1.0 - alpha) + gyro * alpha

            val gDown_b = ahrs.q.conjugate().rotate(Vec3(0.0, 0.0, 1.0)).normalized()
            val baEst = accel + gDown_b * g
            ba = ba * (1.0 - alpha) + baEst * alpha
        }

        val q = ahrs.update(gyroRad = (gyro - bg), accel = (accel - ba), dt = dt)

        val f_b = accel - ba
        val a_n = q.rotate(f_b) + Vec3(0.0, 0.0, g)

        val quality = when (mode) {
            MotionMode.WALK -> "WALK (PDR)"
            MotionMode.METRO -> if (metro.hasAxis) "METRO 1D (axis OK)" else "METRO 1D (axis…)"
            MotionMode.TAPE -> if (isRest) "TAPE (rest)" else "TAPE (moving)"
        }

        return when (mode) {
            MotionMode.WALK -> {
                steps.update(accel = accel, timeSec = timeSec)
                val dist = steps.steps * steps.lastStepLengthM
                Out(isRest, quality, steps.speedMps, dist, steps.steps, steps.lastStepLengthM)
            }

            MotionMode.METRO -> {
                metro.update(aNav = a_n, dt = dt, timeSec = timeSec, isRest = isRest)
                Out(isRest, quality, abs(metro.v), abs(metro.s), 0, 0.0)
            }

            MotionMode.TAPE -> {
                if (!isRest) {
                    if (!segMoving) {
                        segMoving = true
                        segStartT = timeSec
                        segStartP = p
                    }
                    v = v + a_n * dt
                    p = p + v * dt + a_n * (0.5 * dt * dt)
                } else {
                    if (segMoving) {
                        val T = (timeSec - segStartT).coerceAtLeast(1e-6)
                        val vEnd = v
                        p = p - vEnd * (0.5 * T)

                        val disp = p - segStartP
                        doneDistance += disp.xyNorm()
                        segMoving = false
                    }
                    v = Vec3(0.0, 0.0, 0.0)
                }

                val liveSeg = if (segMoving) (p - segStartP).xyNorm() else 0.0
                Out(isRest, quality, v.xyNorm(), doneDistance + liveSeg, 0, 0.0)
            }
        }
    }
}