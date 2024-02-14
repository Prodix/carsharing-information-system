package com.syndicate.carsharing.views

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.icu.text.SimpleDateFormat
import android.media.Image
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.syndicate.carsharing.R
import java.io.File
import java.lang.Exception
import java.util.Locale
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@Composable
fun Camera(
    fileName: String,
    navigation: NavHostController,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val cameraProviderFuture = remember(context) { ProcessCameraProvider.getInstance(context) }
    var imageCapture: ImageCapture? by remember { mutableStateOf(null) }
    val lifecycleOwner = LocalLifecycleOwner.current
    val executor = Executors.newSingleThreadExecutor()

    Box (
        modifier = Modifier
            .fillMaxSize()
    ){
        AndroidView(
            factory = {
                val previewView = PreviewView(context)
                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()
                    val preview = androidx.camera.core.Preview.Builder()
                        .build()
                        .also {
                            it.setSurfaceProvider(previewView.surfaceProvider)
                        }


                    val cameraSelector = if (fileName == "selfie") CameraSelector.DEFAULT_FRONT_CAMERA else CameraSelector.DEFAULT_BACK_CAMERA

                    imageCapture = ImageCapture.Builder()
                        .setTargetRotation(previewView.display.rotation)
                        .build()

                    try {
                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            cameraSelector,
                            preview,
                            imageCapture
                        )
                    } catch (ex: Exception) {
                        Log.e("Camera", "Use case not bound", ex)
                    }
                }, ContextCompat.getMainExecutor(context))

                previewView

            }
        )
        Box(
            modifier = Modifier
                .padding(30.dp)
                .align(Alignment.BottomCenter)
        ) {
            Box(
                modifier = Modifier
                    .background(Color(0x80FFFFFF), CircleShape)
                    .size(80.dp),
                contentAlignment = Alignment.Center
            ) {
                Button(
                    onClick = {
                        when (fileName) {
                            "passport" -> imageCapture?.let { takePhoto(navigation, "${fileName}.jpeg", context, it, "documentViewer/passport") }
                            "selfie" -> imageCapture?.let { takePhoto(navigation, "${fileName}.jpeg", context, it, "documentViewer/selfie") }
                            else -> imageCapture?.let { takePhoto(navigation, "${fileName}.jpeg", context, it, "documentViewer/license") }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF6699CC)
                    ),
                    shape = CircleShape,
                    modifier = Modifier
                        .size(60.dp),
                    content = {}
                )
            }
        }
    }
}

private fun takePhoto(navigation: NavHostController, name: String, context: Context, imageCapture: ImageCapture, route: String) {
    val file = File("${Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)}/AutoShare/${name}")
    if (file.exists()) {
        file.delete()
    }

    val contentValues = ContentValues().apply {
        put(MediaStore.MediaColumns.DISPLAY_NAME, name)
        put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/AutoShare")
        }
    }

    val outputOptions = ImageCapture.OutputFileOptions
        .Builder(
            context.contentResolver,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            contentValues
        )
        .build()

    imageCapture.takePicture(
        outputOptions,
        ContextCompat.getMainExecutor(context),
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(
                outputFileResults: ImageCapture.OutputFileResults
            ) {
                val msg = "Photo capture succeeded"
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                Log.d("Camera", msg)
                navigation.navigate(route)

            }

            override fun onError(
                exception: ImageCaptureException
            ) {
                Log.e("Camera", "Photo capture failed", exception)
            }
        }

    )



}



