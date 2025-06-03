/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Apache 2.0 license with Commons Clause
 * (see https://www.apache.org/licenses/LICENSE-2.0 and
 * https://commonsclause.com/).
 */
package io.redlink.more.app.android.activities.qrScanner

import android.content.pm.PackageManager
import androidx.activity.ComponentActivity
import androidx.camera.view.PreviewView
import com.google.mlkit.vision.barcode.BarcodeScanning
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import androidx.activity.compose.setContent
import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.mlkit.vision.common.InputImage
import io.redlink.more.app.android.R
import io.redlink.more.app.android.extensions.getStringResource
import io.redlink.more.app.android.ui.theme.MoreColors
import io.redlink.more.app.android.ui.theme.morePrimary

// Infos to Barcodes mit ML Kit: https://developers.google.com/ml-kit/vision/barcode-scanning/android?hl=de

class QRScannerActivity: ComponentActivity() {
    // preview view for the camera qr code scanner
    private lateinit var previewView: PreviewView
    private val scanner = BarcodeScanning.getClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestCameraPermissionIfNeeded()

        setContent {
            QrScannerScreen(
                previewViewProvider = {
                    previewView = PreviewView(it)
                    previewView
                },
                onClose = { finish() }
            )
        }
    }

    private fun requestCameraPermissionIfNeeded() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 123)
        } else {
            // Start camera only when permission granted and view is ready
            // (delay to let Compose inflate first)
            Handler(Looper.getMainLooper()).postDelayed({ startCamera() }, 200)
        }
    }

    @Deprecated("This method has been deprecated in favor of using the Activity Result API\n      which brings increased type safety via an {@link ActivityResultContract} and the prebuilt\n      contracts for common intents available in\n      {@link androidx.activity.result.contract.ActivityResultContracts}, provides hooks for\n      testing, and allow receiving results in separate, testable classes independent from your\n      activity. Use\n      {@link #registerForActivityResult(ActivityResultContract, ActivityResultCallback)} passing\n      in a {@link RequestMultiplePermissions} object for the {@link ActivityResultContract} and\n      handling the result in the {@link ActivityResultCallback#onActivityResult(Object) callback}.")
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 123 && grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED) {
            startCamera()
        } else {
            finish()
        }
    }

    // function converts camera image into an inputimage
    @OptIn(ExperimentalGetImage::class)
    private fun processImageProxy(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image ?: return
        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

        scanner.process(image)
            .addOnSuccessListener { barcodes ->
                for (barcode in barcodes) {
                    barcode.rawValue?.let { result ->
                        // returns scanned result and closes the camera
                        val intent = Intent().putExtra("qrResult", result)
                        setResult(Activity.RESULT_OK, intent)
                        finish()
                    }
                }
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            val analysis = ImageAnalysis.Builder().build().also {
                it.setAnalyzer(ContextCompat.getMainExecutor(this)) { imageProxy ->
                    processImageProxy(imageProxy)
                }
            }

            val selector = CameraSelector.DEFAULT_BACK_CAMERA
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(this, selector, preview, analysis)
        }, ContextCompat.getMainExecutor(this))
    }
}

@Composable
fun QrScannerScreen(
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    previewViewProvider: (Context) -> PreviewView,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MoreColors.PrimaryLight200)
    ) {
        // Camera Preview (full screen)
        AndroidView(
            factory = previewViewProvider,
            modifier = Modifier.fillMaxSize()
        )

        // Overlay with center cutout (placed directly after camera view so it doesn’t cover text/buttons)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawWithContent {
                    drawContent()

                    val cutoutSize = Size(300.dp.toPx(), 300.dp.toPx())
                    val canvasWidth = size.width
                    val canvasHeight = size.height
                    val left = (canvasWidth - cutoutSize.width) / 2
                    val top = (canvasHeight - cutoutSize.height) / 2

                    // Dim everything
                    drawRect(color = Color(0xAA000000))

                    drawRect(
                        color = Color.Transparent,
                        topLeft = Offset(left, top),
                        size = cutoutSize,
                        blendMode = BlendMode.Clear
                    )
                }
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 48.dp)
                .align(Alignment.TopCenter),
            contentAlignment = Alignment.TopCenter
        ) {
            Text(
                text = getStringResource(id = R.string.more_qr_code_button),
                color = MoreColors.PrimaryLight200,
                fontSize = 20.sp
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 48.dp)
                .align(Alignment.BottomCenter),
            contentAlignment = Alignment.BottomCenter
        ) {
            OutlinedButton(
                onClick = onClose,
                modifier = Modifier
                    .padding(vertical = 8.dp)
                    .height(60.dp),
                colors = ButtonDefaults.morePrimary(),
                border = MoreColors.borderPrimary(true)
            ) {
                Text(getStringResource(id = R.string.more_close))
            }
        }
    }
}
