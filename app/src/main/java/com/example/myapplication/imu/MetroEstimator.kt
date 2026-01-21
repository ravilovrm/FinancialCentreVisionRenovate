package com.example.myapplication.imu

import kotlin.math.abs
import kotlin.math.sqrt

class MetroEstimator {
    private val samples = ArrayDeque<Pair<Double, Double>>()
    private val minForAxis = 40

    var axis = Pair(1.0, 0.0)
        private set
    var hasAxis = false
        private set

    var v: Double = 0.0
        private set
    var s: Double = 0.0
        private set

    private var moving = false
    private var segStartT = 0.0

    fun reset() {
        samples.clear()
        axis = Pair(1.0, 0.0)
        hasAxis = false
        v = 0.0
        s = 0.0
        moving = false
        segStartT = 0.0
    }

    fun update(aNav: Vec3, dt: Double, timeSec: Double, isRest: Boolean) {
        val ax = aNav.x
        val ay = aNav.y
        val ah = sqrt(ax*ax + ay*ay)

        if (!isRest && ah > 0.20 && ah < 6.0) {
            if (samples.size > 200) samples.removeFirst()
            samples.addLast(ax to ay)
            if (!hasAxis && samples.size >= minForAxis) {
                axis = pcaAxis2D(samples)
                hasAxis = true
            }
        }

        val (ux, uy) = axis
        val a1d = ux * ax + uy * ay

        if (!isRest) {
            if (!moving) {
                moving = true
                segStartT = timeSec
            }
            v += a1d * dt
            s += v * dt + 0.5 * a1d * dt * dt
        } else {
            if (moving) {
                val T = (timeSec - segStartT).coerceAtLeast(1e-6)
                val vEnd = v
                s -= 0.5 * T * vEnd
                v = 0.0
                moving = false
            } else v = 0.0
        }
    }

    private fun pcaAxis2D(pts: Iterable<Pair<Double, Double>>): Pair<Double, Double> {
        var mx = 0.0
        var my = 0.0
        var n = 0
        for ((x, y) in pts) { mx += x; my += y; n++ }
        mx /= n.toDouble()
        my /= n.toDouble()

        var sxx = 0.0
        var syy = 0.0
        var sxy = 0.0
        for ((x, y) in pts) {
            val dx = x - mx
            val dy = y - my
            sxx += dx * dx
            syy += dy * dy
            sxy += dx * dy
        }
        sxx /= n.toDouble()
        syy /= n.toDouble()
        sxy /= n.toDouble()

        val tr = sxx + syy
        val det = sxx * syy - sxy * sxy
        val disc = (tr*tr - 4.0*det).coerceAtLeast(0.0)
        val l1 = 0.5 * (tr + sqrt(disc))

        val vx = if (abs(sxy) > 1e-9) (l1 - syy) else 1.0
        val vy = if (abs(sxy) > 1e-9) sxy else 0.0

        val norm = sqrt(vx*vx + vy*vy).coerceAtLeast(1e-12)
        return Pair(vx / norm, vy / norm)
    }
}