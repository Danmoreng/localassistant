package com.example.localassistant

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import androidx.exifinterface.media.ExifInterface
import com.example.localassistant.ui.theme.LocalAssistantTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LocalAssistantTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MainScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }

    @Composable
    fun MainScreen(modifier: Modifier = Modifier) {
        var capturedImage by remember { mutableStateOf<android.graphics.Bitmap?>(null) }
        var imageRotation by remember { mutableStateOf<Int?>(null) }

        // Define a launcher that starts CameraActivity for a result.
        val cameraLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                // Retrieve the image path from the returned Intent.
                val imagePath = result.data?.getStringExtra("imagePath")
                imagePath?.let { path ->
                    // Decode the image file to a Bitmap.
                    val bitmap = BitmapFactory.decodeFile(path)

                    // Read EXIF data to determine the rotation.
                    val exif = ExifInterface(path)
                    val orientation = exif.getAttributeInt(
                        ExifInterface.TAG_ORIENTATION,
                        ExifInterface.ORIENTATION_NORMAL
                    )
                    val rotation = when (orientation) {
                        ExifInterface.ORIENTATION_ROTATE_90 -> 90
                        ExifInterface.ORIENTATION_ROTATE_180 -> 180
                        ExifInterface.ORIENTATION_ROTATE_270 -> 270
                        else -> 0
                    }
                    val matrix = Matrix().apply {
                        postRotate(rotation.toFloat())
                    }
                    val rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)


                    capturedImage = rotatedBitmap
                    imageRotation = rotation
                }
            }
        }

        Column(
            modifier = modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(onClick = {
                // Launch the CameraActivity.
                val intent = Intent(this@MainActivity, CameraActivity::class.java)
                cameraLauncher.launch(intent)
            }) {
                Text(text = "Open Camera")
            }
            Spacer(modifier = Modifier.height(16.dp))
            if (capturedImage != null) {
                Image(
                    bitmap = capturedImage!!.asImageBitmap(),
                    contentDescription = "Captured Image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )
                // Display the rotation info under the image.
                Text(
                    text = "Rotation: ${imageRotation ?: "N/A"}°",
                    modifier = Modifier.padding(top = 8.dp)
                )
            } else {
                Text(
                    text = "No image captured yet",
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}
