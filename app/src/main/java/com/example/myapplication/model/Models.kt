package com.example.myapplication.model

import kotlin.math.ceil
import kotlin.math.hypot
import kotlin.math.max

data class NPoint(val x: Float, val y: Float) {
    fun clamp01(): NPoint = NPoint(x.coerceIn(0f, 1f), y.coerceIn(0f, 1f))
}

data class PlanResult(
    val widthM: Double,
    val lengthM: Double,
    val perimeterM: Double,
    val areaM2: Double
)

data class MaterialsInput(
    val roomHeightM: Double = 2.7,
    val paintLayers: Int = 2,
    val paintCoverageM2PerL: Double = 10.0,
    val wastePercent: Int = 10,
    val laminatePackM2: Double = 2.2,
)

data class MaterialsResult(
    val wallAreaM2: Double,
    val paintLiters: Double,
    val laminatePacks: Int,
    val summary: String
)

object Geometry {
    private fun distPx(a: NPoint, b: NPoint, w: Int, h: Int): Double {
        val dx = (a.x - b.x) * w
        val dy = (a.y - b.y) * h
        return hypot(dx.toDouble(), dy.toDouble())
    }

    fun estimateRectangleMeters(
        corners: List<NPoint>,
        imgW: Int,
        imgH: Int,
        refA: NPoint?,
        refB: NPoint?,
        refLenM: Double
    ): PlanResult? {
        if (corners.size != 4) return null
        if (imgW <= 0 || imgH <= 0) return null
        if (refA == null || refB == null || refLenM <= 0.0) return null

        val refPx = distPx(refA, refB, imgW, imgH)
        if (refPx <= 1.0) return null

        val mPerPx = refLenM / refPx

        fun edge(i: Int, j: Int): Double = distPx(corners[i], corners[j], imgW, imgH) * mPerPx

        val e01 = edge(0, 1)
        val e12 = edge(1, 2)
        val e23 = edge(2, 3)
        val e30 = edge(3, 0)

        val width = (e01 + e23) / 2.0
        val length = (e12 + e30) / 2.0
        val perimeter = e01 + e12 + e23 + e30
        val area = width * length

        if (width.isNaN() || length.isNaN()) return null
        if (width <= 0.3 || length <= 0.3) return null

        return PlanResult(width, length, perimeter, area)
    }

    fun wasteFactor(wastePercent: Int): Double =
        1.0 + (wastePercent.coerceIn(0, 40) / 100.0)

    fun ceilInt(x: Double): Int = max(0, ceil(x).toInt())
    fun r2(x: Double): Double = kotlin.math.round(x * 100.0) / 100.0
}