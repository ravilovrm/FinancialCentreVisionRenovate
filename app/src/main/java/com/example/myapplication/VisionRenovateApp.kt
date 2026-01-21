package com.example.myapplication

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Build
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.PhotoCamera
import androidx.compose.material.icons.rounded.Straighten
import androidx.compose.material.icons.rounded.Summarize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.core.AppViewModel
import com.example.myapplication.navigation.AppNavGraph
import com.example.myapplication.navigation.Destinations

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VisionRenovateApp() {
    val navController = rememberNavController()
    val vm: AppViewModel = viewModel()

    val backStack by navController.currentBackStackEntryAsState()
    val currentDest = backStack?.destination

    val items = listOf(
        Destinations.Home,
        Destinations.Scan,
        Destinations.Measure,
        Destinations.Materials,
        Destinations.Report,
    )

    val title = when (currentDest?.route) {
        Destinations.Home.route -> "VisionRenovate"
        Destinations.Scan.route -> "План по фото"
        Destinations.Measure.route -> "Уровень (сенсоры)"
        Destinations.Materials.route -> "Смета материалов"
        Destinations.Report.route -> "PDF отчёт"
        Destinations.Camera.route -> "Камера"
        else -> "VisionRenovate"
    }

    val showBottomBar = currentDest?.route != Destinations.Camera.route

    Scaffold(
        modifier = androidx.compose.ui.Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(title) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)
                )
            )
        },
        bottomBar = {
            AnimatedVisibility(visible = showBottomBar) {
                NavigationBar {
                    items.forEach { item ->
                        val selected = currentDest?.hierarchy?.any { it.route == item.route } == true
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(Destinations.Home.route) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = when (item) {
                                        Destinations.Home -> Icons.Rounded.Home
                                        Destinations.Scan -> Icons.Rounded.PhotoCamera
                                        Destinations.Measure -> Icons.Rounded.Straighten
                                        Destinations.Materials -> Icons.Rounded.Build
                                        Destinations.Report -> Icons.Rounded.Summarize
                                        else -> Icons.Rounded.Home
                                    },
                                    contentDescription = null
                                )
                            },
                            label = { Text(item.label) }
                        )
                    }
                }
            }
        }
    ) { padding ->
        AppNavGraph(navController = navController, vm = vm, padding = padding)
    }
}