package com.example.myapplication.util

import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import com.example.myapplication.core.AppUiState // ВАЖНЫЙ ИМПОРТ 1
import com.example.myapplication.model.PlanResult // ВАЖНЫЙ ИМПОРТ 2
import com.example.myapplication.model.Geometry

object PdfReport {

    fun buildPdfBytes(ui: AppUiState): ByteArray {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // А4
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas
        val paint = Paint()

        // 1. Заголовок
        paint.color = Color.BLACK
        paint.textSize = 24f
        paint.isFakeBoldText = true
        canvas.drawText("Отчет: ${ui.projectName}", 40f, 50f, paint)

        // 2. Основная информация
        paint.textSize = 14f
        paint.isFakeBoldText = false
        var y = 90f

        // Проверяем, есть ли план
        val plan = ui.plan
        if (plan != null) {
            canvas.drawText("Размеры помещения:", 40f, y, paint)
            y += 20f
            // Используем Geometry.r2 для красивого округления
            canvas.drawText("• Ширина: ~${Geometry.r2(plan.widthM)} м", 50f, y, paint)
            y += 20f
            canvas.drawText("• Длина: ~${Geometry.r2(plan.lengthM)} м", 50f, y, paint)
            y += 20f
            canvas.drawText("• Площадь: ${Geometry.r2(plan.areaM2)} м²", 50f, y, paint)
            y += 20f
            canvas.drawText("• Периметр: ${Geometry.r2(plan.perimeterM)} м", 50f, y, paint)
            y += 40f
        } else {
            canvas.drawText("План помещения: Нет данных", 40f, y, paint)
            y += 40f
        }

        // 3. Материалы и Смета
        canvas.drawText("Смета материалов:", 40f, y, paint)
        y += 20f

        // Берем текст из summary, если он есть
        val summaryText = ui.materials?.summary ?: "Нет данных о материалах"
        val lines = summaryText.split("\n")

        for (line in lines) {
            if (line.isNotBlank()) {
                canvas.drawText(line, 50f, y, paint)
                y += 20f
            }
        }

        // 4. Итоговая цена (если есть)
        y += 20f
        if (ui.totalPrice > 0) {
            paint.textSize = 18f
            paint.isFakeBoldText = true
            canvas.drawText("ИТОГО: ${ui.totalPrice} руб.", 40f, y, paint)
        }

        pdfDocument.finishPage(page)

        // Сохраняем в поток байтов
        val stream = java.io.ByteArrayOutputStream()
        pdfDocument.writeTo(stream)
        pdfDocument.close()

        return stream.toByteArray()
    }
}