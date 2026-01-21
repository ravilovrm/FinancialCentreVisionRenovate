package com.example.myapplication.imu

import kotlin.math.sqrt

data class Vec3(val x: Double, val y: Double, val z: Double) {
    operator fun plus(o: Vec3) = Vec3(x + o.x, y + o.y, z + o.z)
    operator fun minus(o: Vec3) = Vec3(x - o.x, y - o.y, z - o.z)
    operator fun times(k: Double) = Vec3(x * k, y * k, z * k)

    fun dot(o: Vec3) = x * o.x + y * o.y + z * o.z
    fun cross(o: Vec3) = Vec3(
        y * o.z - z * o.y,
        z * o.x - x * o.z,
        x * o.y - y * o.x
    )

    fun norm() = sqrt(x * x + y * y + z * z)
    fun normalized(): Vec3 {
        val n = norm()
        return if (n < 1e-12) Vec3(0.0, 0.0, 0.0) else this * (1.0 / n)
    }

    fun xyNorm() = sqrt(x * x + y * y)
}