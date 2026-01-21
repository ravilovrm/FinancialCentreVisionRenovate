package com.example.myapplication.imu

class ZuptDetector(
    private val g: Double,
    private val windowN: Int = 25,
    private val sigmaA: Double = 0.15,
    private val sigmaW: Double = 0.05,
    private val gamma: Double = 3.0
) {
    private val accMag = ArrayDeque<Double>(windowN)
    private val gyrMag = ArrayDeque<Double>(windowN)

    fun reset() {
        accMag.clear()
        gyrMag.clear()
    }

    fun update(accel: Vec3, gyro: Vec3): Boolean {
        val am = accel.norm()
        val wm = gyro.norm()

        if (accMag.size == windowN) accMag.removeFirst()
        if (gyrMag.size == windowN) gyrMag.removeFirst()
        accMag.addLast(am)
        gyrMag.addLast(wm)

        if (accMag.size < windowN) return false

        var s1 = 0.0
        for (v in accMag) {
            val z = (v - g) / sigmaA
            s1 += z * z
        }
        var s2 = 0.0
        for (v in gyrMag) {
            val z = v / sigmaW
            s2 += z * z
        }

        val t = (s1 + s2) / windowN.toDouble()
        return t < gamma
    }
}