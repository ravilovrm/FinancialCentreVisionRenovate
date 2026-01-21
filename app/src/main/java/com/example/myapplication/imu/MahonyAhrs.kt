package com.example.myapplication.imu

class MahonyAhrs(
    private val kp: Double = 2.0,
    private val ki: Double = 0.05
) {
    var q: Quaternion = Quaternion.IDENTITY
        private set

    private var iErr = Vec3(0.0, 0.0, 0.0)

    fun reset() {
        q = Quaternion.IDENTITY
        iErr = Vec3(0.0, 0.0, 0.0)
    }

    fun update(gyroRad: Vec3, accel: Vec3, dt: Double): Quaternion {
        if (dt <= 0.0) return q

        val aN = accel.norm()
        if (aN > 1e-6) {
            val aHat = accel * (1.0 / aN)

            val gDown_b = q.conjugate().rotate(Vec3(0.0, 0.0, 1.0)).normalized()
            val fHat = gDown_b * (-1.0)

            val e = aHat.cross(fHat)
            iErr = iErr + e * dt

            val omega = gyroRad + e * kp + iErr * ki
            val dq = Quaternion.fromDeltaTheta(omega * dt)
            q = (q * dq).normalized()
        } else {
            val dq = Quaternion.fromDeltaTheta(gyroRad * dt)
            q = (q * dq).normalized()
        }
        return q
    }
}