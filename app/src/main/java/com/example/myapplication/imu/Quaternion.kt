package com.example.myapplication.imu

import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

data class Quaternion(val w: Double, val x: Double, val y: Double, val z: Double) {

    fun normalized(): Quaternion {
        val n = sqrt(w*w + x*x + y*y + z*z)
        return if (n < 1e-12) IDENTITY else Quaternion(w/n, x/n, y/n, z/n)
    }

    fun conjugate() = Quaternion(w, -x, -y, -z)

    operator fun times(o: Quaternion) = Quaternion(
        w*o.w - x*o.x - y*o.y - z*o.z,
        w*o.x + x*o.w + y*o.z - z*o.y,
        w*o.y - x*o.z + y*o.w + z*o.x,
        w*o.z + x*o.y - y*o.x + z*o.w
    )

    fun rotate(v: Vec3): Vec3 {
        val qv = Quaternion(0.0, v.x, v.y, v.z)
        val r = this * qv * this.conjugate()
        return Vec3(r.x, r.y, r.z)
    }

    companion object {
        val IDENTITY = Quaternion(1.0, 0.0, 0.0, 0.0)

        fun fromDeltaTheta(dtheta: Vec3): Quaternion {
            val th = dtheta.norm()
            if (th < 1e-12) {
                return Quaternion(1.0, dtheta.x * 0.5, dtheta.y * 0.5, dtheta.z * 0.5).normalized()
            }
            val half = th * 0.5
            val s = sin(half) / th
            return Quaternion(
                cos(half),
                dtheta.x * s,
                dtheta.y * s,
                dtheta.z * s
            ).normalized()
        }
    }
}