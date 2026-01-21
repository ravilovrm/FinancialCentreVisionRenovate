package com.example.myapplication.model

data class MotionUiState(
    val isRunning: Boolean = false,
    val mode: MotionMode = MotionMode.TAPE,

    val isRest: Boolean = true,
    val quality: String = "—",

    val speedMps: Double = 0.0,
    val distanceM: Double = 0.0,

    val steps: Int = 0,
    val stepLengthM: Double = 0.70,
    val stepK: Double = 0.40
)