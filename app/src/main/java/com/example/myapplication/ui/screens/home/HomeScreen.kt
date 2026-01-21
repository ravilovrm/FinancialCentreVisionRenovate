package com.example.myapplication.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.myapplication.core.AppViewModel
import com.example.myapplication.core.MotionViewModel
import com.example.myapplication.navigation.Destinations
// ВАЖНО: Импортируем правильный класс данных из model
import com.example.myapplication.model.MotionUiState 
import com.example.myapplication.ui.components.HeroCard
import com.example.myapplication.ui.components.MotionSummaryCard
import com.example.myapplication.ui.components.PrimaryButton
import com.example.myapplication.ui.components.SecondaryButton

@Composable
fun HomeScreen(padding: PaddingValues, vm: AppViewModel, nav: NavHostController) {
    val ui by vm.ui.collectAsState()

    val motionVm: MotionViewModel = viewModel()
    // Теперь motionState будет правильного типа (из model)
    val motionState by motionVm.ui.collectAsState()

    Column(
        modifier = Modifier
            .padding(padding)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        HeroCard(
            title = "VisionRenovate",
            subtitle = "Фото → план → смета → PDF"
        ) {
            PrimaryButton(
                text = "Начать (план по фото)",
                onClick = { nav.navigate(Destinations.Scan.route) },
                modifier = Modifier.fillMaxWidth()
            )
            SecondaryButton(
                text = "Уровень (сенсоры)",
                onClick = { nav.navigate(Destinations.Measure.route) },
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Карточка IMU
        MotionSummaryCard(
            state = motionState,
            onOpen = { nav.navigate(Destinations.Motion.route) }
        )

        OutlinedTextField(
            value = ui.projectName,
            onValueChange = vm::setProjectName,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Название проекта") }
        )

        SecondaryButton(
            text = "Заметки + Яндекс.Диск",
            onClick = { nav.navigate(Destinations.Notes.route) },
            modifier = Modifier.fillMaxWidth()
        )

        SecondaryButton(
            text = "PDF отчёт",
            onClick = { nav.navigate(Destinations.Report.route) },
            modifier = Modifier.fillMaxWidth()
        )
    }
}