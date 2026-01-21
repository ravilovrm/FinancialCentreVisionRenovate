package com.example.myapplication.util

import android.graphics.Bitmap
import com.example.myapplication.model.NPoint
import kotlin.math.abs

object AutoPlan {
    fun guessRectangle(bitmap: Bitmap): List<NPoint> {
        val maxSide = 320
        val b = downscale(bitmap, maxSide)

        val w = b.width
        val h = b.height
        val px = IntArray(w * h)
        b.getPixels(px, 0, w, 0, 0, w, h)

        fun lum(c: Int): Int {
            val r = (c shr 16) and 0xFF
            val g = (c shr 8) and 0xFF
            val bl = c and 0xFF
            return (r * 30 + g * 59 + bl * 11) / 100
        }

        val col = DoubleArray(w)
        val row = DoubleArray(h)

        for (y in 0 until h - 1) {
            for (x in 0 until w - 1) {
                val i = y * w + x
                val a = lum(px[i])
                val dx = abs(lum(px[i + 1]) - a)
                val dy = abs(lum(px[i + w]) - a)
                val g = (dx + dy).toDouble()
                col[x] += g
                row[y] += g
            }
        }

        val left = edge(col, 0.05)
        val right = w - 1 - edge(col.reversedArray(), 0.05)
        val top = edge(row, 0.05)
        val bottom = h - 1 - edge(row.reversedArray(), 0.05)

        val l = (left.toFloat() / w).coerceIn(0f, 1f)
        val r = (right.toFloat() / w).coerceIn(0f, 1f)
        val t = (top.toFloat() / h).coerceIn(0f, 1f)
        val bo = (bottom.toFloat() / h).coerceIn(0f, 1f)

        return listOf(NPoint(l, t), NPoint(r, t), NPoint(r, bo), NPoint(l, bo))
    }

    private fun edge(energy: DoubleArray, p: Double): Int {
        val total = energy.sum().coerceAtLeast(1.0)
        val target = total * p
        var s = 0.0
        for (i in energy.indices) {
            s += energy[i]
            if (s >= target) return i
        }
        return energy.lastIndex
    }

    private fun downscale(src: Bitmap, maxSide: Int): Bitmap {
        val w = src.width
        val h = src.height
        val m = maxOf(w, h)
        if (m <= maxSide) return src
        val scale = maxSide.toFloat() / m.toFloat()
        val nw = (w * scale).toInt().coerceAtLeast(1)
        val nh = (h * scale).toInt().coerceAtLeast(1)
        return Bitmap.createScaledBitmap(src, nw, nh, true)
    }
}