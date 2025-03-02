package com.example.localassistant

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.OrientationEventListener
import android.view.Surface
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.exifinterface.media.ExifInterface
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraActivity : ComponentActivity() {

    private lateinit var cameraExecutor: ExecutorService
    private var imageCapture: ImageCapture? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Initialize a single-thread executor for camera operations.
        cameraExecutor = Executors.newSingleThreadExecutor()
        setContent {
            CameraScreen()
        }
    }

    @Composable
    fun CameraScreen() {
        val context = LocalContext.current
        val lifecycleOwner = LocalLifecycleOwner.current

        // Use a mutable state to hold the current rotation in degrees.
        var currentRotationDegrees by remember { mutableIntStateOf(0) }

        // OrientationEventListener to update the rotation state.
        DisposableEffect(context) {
            val orientationListener = object : OrientationEventListener(context) {
                override fun onOrientationChanged(orientation: Int) {
                    // Map the raw orientation (0-359) to one of 0, 90, 180, or 270.
                    val newRotation = when {
                        orientation in 315..360 || orientation in 0..45 -> 90
                        orientation in 46..135 -> 180
                        orientation in 136..225 -> 270
                        orientation in 226..314 -> 0
                        else -> 0
                    }
                    if (newRotation != currentRotationDegrees) {
                        currentRotationDegrees = newRotation
                    }
                }
            }
            orientationListener.enable()
            onDispose { orientationListener.disable() }
        }

        // Get the camera provider.
        val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }

        // Wrap content in a Column that accounts for system bars
        Column(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding() // Adds bottom padding equal to the navigation bar height.
                .padding(16.dp), // Additional internal padding.
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Display the current rotation live.
            Text(text = "Current Rotation: $currentRotationDegrees°")
            Spacer(modifier = Modifier.height(16.dp))

            // Camera preview using AndroidView.
            AndroidView(
                factory = { ctx ->
                    val previewView = PreviewView(ctx)
                    val preview = Preview.Builder().build()
                    val cameraSelector = CameraSelector.Builder()
                        .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                        .build()

                    imageCapture = ImageCapture.Builder()
                        // Use the live rotation value.
                        .setTargetRotation(
                            when (currentRotationDegrees) {
                                0 -> Surface.ROTATION_0
                                90 -> Surface.ROTATION_90
                                180 -> Surface.ROTATION_180
                                270 -> Surface.ROTATION_270
                                else -> Surface.ROTATION_0
                            }
                        )
                        .build()


                    cameraProviderFuture.addListener({
                        try {
                            val cameraProvider = cameraProviderFuture.get()
                            // Unbind any previously bound use cases.
                            cameraProvider.unbindAll()
                            cameraProvider.bindToLifecycle(
                                this@CameraActivity,
                                cameraSelector,
                                preview,
                                imageCapture
                            )
                            preview.surfaceProvider = previewView.surfaceProvider
                        } catch (exc: Exception) {
                            Log.e("CameraActivity", "Error binding camera use cases", exc)
                        }
                    }, ContextCompat.getMainExecutor(ctx))
                    previewView
                },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Button to capture an image.
            Button(onClick = { takePhoto(currentRotationDegrees) }) {
                Text("Capture Image")
            }

        }
    }

    private fun takePhoto(currentRotationDegrees: Int) {
        // Convert currentRotationDegrees to a Surface rotation constant.
        val targetRotation = when (currentRotationDegrees) {
            0 -> Surface.ROTATION_0
            90 -> Surface.ROTATION_90
            180 -> Surface.ROTATION_180
            270 -> Surface.ROTATION_270
            else -> Surface.ROTATION_0
        }
        imageCapture?.targetRotation = targetRotation

        // Create a unique file name.
        val name = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.US)
            .format(System.currentTimeMillis()) + ".jpg"
        val photoFile = File(externalMediaDirs.first(), name)
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        // Capture the image.
        imageCapture?.takePicture(
            outputOptions,
            cameraExecutor,
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    // Map the device rotation enum to degrees.
                    val deviceRotationDegrees = when (targetRotation) {
                        Surface.ROTATION_0 -> 0
                        Surface.ROTATION_90 -> 90
                        Surface.ROTATION_180 -> 180
                        Surface.ROTATION_270 -> 270
                        else -> 0
                    }

                    // Map the final rotation to the corresponding EXIF constant.
                    val exifOrientation = when (deviceRotationDegrees) {
                        0 -> ExifInterface.ORIENTATION_NORMAL
                        90 -> ExifInterface.ORIENTATION_ROTATE_90
                        180 -> ExifInterface.ORIENTATION_ROTATE_180
                        270 -> ExifInterface.ORIENTATION_ROTATE_270
                        else -> ExifInterface.ORIENTATION_NORMAL
                    }

                    // Update the EXIF orientation.
                    val exif = ExifInterface(photoFile.absolutePath)
                    exif.setAttribute(ExifInterface.TAG_ORIENTATION, exifOrientation.toString())
                    exif.saveAttributes()

                    // Retrieve the saved URI (or create one from the file).
                    val savedUri = outputFileResults.savedUri ?: Uri.fromFile(photoFile)
                    Log.d("CameraActivity", "Photo capture succeeded: $savedUri")
                    runOnUiThread {
                        // Prepare the result intent with the image path.
                        val intent = Intent().apply {
                            putExtra("imagePath", savedUri.path)
                        }
                        setResult(RESULT_OK, intent)
                        finish()
                    }
                }

                override fun onError(exception: ImageCaptureException) {
                    Log.e("CameraActivity", "Photo capture failed: ${exception.message}", exception)
                }
            }
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}
