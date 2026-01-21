package com.example.myapplication.navigation

sealed class Destinations(val route: String, val label: String) {
    data object Home : Destinations("home", "Домой")
    data object Scan : Destinations("scan", "Фото")
    data object Measure : Destinations("measure", "Уровень")
    data object Materials : Destinations("materials", "Смета")
    data object Report : Destinations("report", "PDF")
    data object Camera : Destinations("camera", "Камера")
    data object Motion : Destinations("motion", "IMU")
    data object Notes : Destinations("notes", "Заметки")
}