package com.example.myapplication.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.myapplication.core.AppViewModel
import com.example.myapplication.core.MotionViewModel
import com.example.myapplication.ui.screens.camera.CameraCaptureScreen
import com.example.myapplication.ui.screens.home.HomeScreen
import com.example.myapplication.ui.screens.materials.MaterialsScreen
import com.example.myapplication.ui.screens.measure.MeasureScreen
import com.example.myapplication.ui.screens.motion.MotionScreen
import com.example.myapplication.ui.screens.notes.NotesScreen
import com.example.myapplication.ui.screens.report.ReportScreen
import com.example.myapplication.ui.screens.scan.ScanScreen
import com.example.myapplication.data.notes.NotesViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
@Composable
fun AppNavGraph(
    navController: NavHostController,
    vm: AppViewModel,
    padding: PaddingValues
) {
    NavHost(
        navController = navController,
        startDestination = Destinations.Home.route
    ) {
        composable(Destinations.Home.route) {
            HomeScreen(padding = padding, vm = vm, nav = navController)
        }
        composable(Destinations.Scan.route) {
            ScanScreen(padding = padding, vm = vm, nav = navController)
        }
        composable(Destinations.Measure.route) {
            MeasureScreen(padding = padding)
        }
        composable(Destinations.Materials.route) {
            MaterialsScreen(padding = padding, vm = vm)
        }
        composable(Destinations.Report.route) {
            ReportScreen(padding = padding, vm = vm)
        }
        composable(Destinations.Camera.route) {
            CameraCaptureScreen(vm = vm, nav = navController)
        }

        // Motion: общий MotionViewModel с Home (чтобы сводка и экран были про одно и то же)
        composable(Destinations.Motion.route) {
            val homeEntry = remember(navController) {
                navController.getBackStackEntry(Destinations.Home.route)
            }
            val motionVm: MotionViewModel = viewModel(homeEntry)
            MotionScreen(padding = padding, motionVm = motionVm)
        }

        composable(Destinations.Notes.route) {
            val notesVm: NotesViewModel = viewModel()
            NotesScreen(padding = padding, vm = notesVm)
        }
    }
}