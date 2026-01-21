package com.example.myapplication.core

import android.net.Uri
import androidx.lifecycle.ViewModel
import com.example.myapplication.model.Geometry
import com.example.myapplication.model.MaterialsInput
import com.example.myapplication.model.MaterialsResult
import com.example.myapplication.model.NPoint
// 1. ИМПОРТИРУЕМ PlanResult ВМЕСТО RoomPlan
import com.example.myapplication.model.PlanResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

// 2. В State используем PlanResult
data class AppUiState(
    val projectName: String = "Мой проект",
    val imageUri: String? = null,
    val imageW: Int = 0,
    val imageH: Int = 0,
    val corners: List<NPoint> = emptyList(),
    val refA: NPoint? = null,
    val refB: NPoint? = null,
    val referenceLengthM: Double = 1.0,

    // ЗДЕСЬ БЫЛА ОШИБКА. ТЕПЕРЬ ТУТ PlanResult
    val plan: PlanResult? = null,

    val materialsInput: MaterialsInput = MaterialsInput(),
    val materials: MaterialsResult? = null,
    val totalPrice: Double = 0.0,
    val paintLiters: Int = 0,
    val laminatePacks: Int = 0
)

class AppViewModel : ViewModel() {
    private val _ui = MutableStateFlow(AppUiState())
    val ui: StateFlow<AppUiState> = _ui

    fun setProjectName(name: String) = _ui.update { it.copy(projectName = name) }

    fun setImage(uri: Uri?) = _ui.update {
        it.copy(
            imageUri = uri?.toString(),
            corners = emptyList(),
            refA = null,
            refB = null,
            plan = null,
            materials = null,
            imageW = 0,
            imageH = 0,
            totalPrice = 0.0,
            paintLiters = 0,
            laminatePacks = 0
        )
    }

    fun setImageSize(w: Int, h: Int) = _ui.update { s ->
        updateAllCalculations(s.copy(imageW = w, imageH = h))
    }

    fun setCorners(corners: List<NPoint>) = _ui.update { s ->
        updateAllCalculations(s.copy(corners = corners.map { it.clamp01() }.take(4)))
    }

    fun setRefA(p: NPoint?) = _ui.update { s ->
        updateAllCalculations(s.copy(refA = p?.clamp01()))
    }

    fun setRefB(p: NPoint?) = _ui.update { s ->
        updateAllCalculations(s.copy(refB = p?.clamp01()))
    }

    fun setReferenceLengthMeters(m: Double) = _ui.update { s ->
        updateAllCalculations(s.copy(referenceLengthM = m.coerceAtLeast(0.01)))
    }

    fun setMaterialsInput(input: MaterialsInput) = _ui.update { s ->
        updateAllCalculations(s.copy(materialsInput = input))
    }

    private fun updateAllCalculations(s: AppUiState): AppUiState {
        // 3. Geometry возвращает PlanResult, и теперь newPlan тоже PlanResult
        val newPlan = Geometry.estimateRectangleMeters(
            corners = s.corners,
            imgW = s.imageW,
            imgH = s.imageH,
            refA = s.refA,
            refB = s.refB,
            refLenM = s.referenceLengthM
        )

        if (newPlan == null) {
            return s.copy(
                plan = null,
                materials = null,
                totalPrice = 0.0,
                paintLiters = 0,
                laminatePacks = 0
            )
        }

        val inp = s.materialsInput
        val waste = Geometry.wasteFactor(inp.wastePercent)

        // ВАЖНО: Если тут загорится красным areaM2, значит в PlanResult поле называется просто area
        // Попробуйте newPlan.area и newPlan.perimeter, если .areaM2 не сработает
        val wallArea = newPlan.perimeterM * inp.roomHeightM

        val paintLitDouble = (wallArea / inp.paintCoverageM2PerL) * inp.paintLayers * waste
        val packs = Geometry.ceilInt((newPlan.areaM2 * waste) / inp.laminatePackM2)
        val paintLitInt = Geometry.ceilInt(paintLitDouble)

        val summary = buildString {
            appendLine("Пол: ${Geometry.r2(newPlan.areaM2)} м²")
            appendLine("Стены: ${Geometry.r2(wallArea)} м²")
            appendLine("Краска: ${Geometry.r2(paintLitDouble)} л")
            appendLine("Ламинат: $packs уп.")
        }

        val matResult = MaterialsResult(
            wallAreaM2 = wallArea,
            paintLiters = paintLitDouble,
            laminatePacks = packs,
            summary = summary
        )

        val price = (paintLitInt * 800.0) + (packs * 1200.0)

        // 4. Теперь типы совпадают (PlanResult = PlanResult)
        return s.copy(
            plan = newPlan,
            materials = matResult,
            totalPrice = price,
            paintLiters = paintLitInt,
            laminatePacks = packs
        )
    }
}