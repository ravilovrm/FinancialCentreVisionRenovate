package com.example.myapplication.ui.screens.camera

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavHostController
import com.example.myapplication.core.AppViewModel
import com.example.myapplication.ui.components.PrimaryButton
import com.example.myapplication.ui.components.SecondaryButton
import java.io.File
import java.util.concurrent.Executor

@Composable
fun CameraCaptureScreen(vm: AppViewModel, nav: NavHostController) {
    val ctx = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val hasPermission = remember { mutableStateOf(false) }

    val permLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted -> hasPermission.value = granted }
    )

    androidx.compose.runtime.LaunchedEffect(Unit) {
        permLauncher.launch(android.Manifest.permission.CAMERA)
    }

    val previewView = remember { PreviewView(ctx) }
    val imageCapture = remember { ImageCapture.Builder().build() }
    val executor: Executor = ContextCompat.getMainExecutor(ctx)

    DisposableEffect(hasPermission.value) {
        if (!hasPermission.value) return@DisposableEffect onDispose { }

        val providerFuture = ProcessCameraProvider.getInstance(ctx)
        val listener = Runnable {
            val provider = providerFuture.get()
            provider.unbindAll()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            provider.bindToLifecycle(
                lifecycleOwner,
                CameraSelector.DEFAULT_BACK_CAMERA,
                preview,
                imageCapture
            )
        }
        providerFuture.addListener(listener, executor)

        onDispose {
            try { providerFuture.get().unbindAll() } catch (_: Throwable) {}
        }
    }

    androidx.compose.foundation.layout.Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Камера", style = MaterialTheme.typography.titleLarge)

        Card(modifier = Modifier.weight(1f)) {
            AndroidView(factory = { previewView }, modifier = Modifier.fillMaxSize())
        }

        PrimaryButton(
            text = "Сделать снимок",
            onClick = {
                if (!hasPermission.value) return@PrimaryButton
                takePhoto(ctx, imageCapture, executor) { uri ->
                    vm.setImage(uri)
                    nav.popBackStack()
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        SecondaryButton("Назад", onClick = { nav.popBackStack() }, modifier = Modifier.fillMaxWidth())
    }
}

private fun takePhoto(
    context: Context,
    imageCapture: ImageCapture,
    executor: Executor,
    onSaved: (Uri) -> Unit
) {
    val outFile = File(context.cacheDir, "scan_${System.currentTimeMillis()}.jpg")
    val opts = ImageCapture.OutputFileOptions.Builder(outFile).build()

    imageCapture.takePicture(
        opts,
        executor,
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                onSaved(output.savedUri ?: Uri.fromFile(outFile))
            }
            override fun onError(exception: ImageCaptureException) {}
        }
    )
}