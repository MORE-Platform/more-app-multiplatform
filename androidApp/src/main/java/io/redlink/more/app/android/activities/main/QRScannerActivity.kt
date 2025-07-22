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
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.mlkit.vision.common.InputImage
import io.redlink.more.app.android.R
import io.redlink.more.app.android.extensions.Image
import io.redlink.more.app.android.extensions.getStringResource
import io.redlink.more.app.android.shared_composables.IconInline
import io.redlink.more.app.android.ui.theme.MoreColors
import io.redlink.more.app.android.ui.theme.moreSecondary


// Infos to Barcodes mit ML Kit: https://developers.google.com/ml-kit/vision/barcode-scanning/android?hl=de

class QRScannerActivity: ComponentActivity() {
    // preview view for the camera qr code scanner
    private lateinit var previewView: PreviewView
    private val scanner = BarcodeScanning.getClient()

    private val permissionGiven: MutableState<Boolean> = mutableStateOf(false)


    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            permissionGiven.value = isGranted
            if (isGranted) {
                startCamera()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestCameraPermissionIfNeeded()

        setContent {
            QrScannerScreen(
                previewViewProvider = {
                    previewView = PreviewView(it)
                    previewView
                },
                permissionGiven = permissionGiven,
                onClose = { finish() }
            )
        }
    }

    private fun requestCameraPermissionIfNeeded() {
        when {
            ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED -> {
                startCamera()
            }
            ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA) -> {
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
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
    permissionGiven: MutableState<Boolean>
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MoreColors.PrimaryLight200)
    ) {
        // Camera Preview (full screen)
        AndroidView(
            factory = previewViewProvider,
            Modifier.fillMaxSize(0.95f)
        )

        // Overlay with center cutout (placed directly after camera view so it doesn’t cover text/buttons)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawWithContent {
                    drawContent()

                    val cutoutSize = Size(size.width - 140, size.height / 2)
                    val canvasWidth = size.width
                    val canvasHeight = size.height
                    val left = (canvasWidth - cutoutSize.width) / 2
                    val top = ((canvasHeight - cutoutSize.height) / 3) * 2

                    // background color
                    drawRect(color = MoreColors.PrimaryLight)

                    // camera area
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
                .padding(horizontal = 8.dp),
            contentAlignment = Alignment.TopEnd
        ) {
            Button(
                onClick = onClose,
                modifier = Modifier
                    .height(60.dp),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = Color.Transparent,
                    contentColor = MoreColors.Secondary
                ),
                elevation = null
            ) {
                IconInline(
                    icon = Icons.Rounded.Close,
                    color = MoreColors.Secondary,
                    contentDescription = getStringResource(id = R.string.more_close_icon)
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(top = 64.dp)
                .padding(horizontal = 24.dp)
                .align(Alignment.TopCenter),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // Welcome Image
            Image(
                id = R.drawable.welcome_to_more,
                contentDescription = getStringResource(id = R.string.more_welcome_title)
            )

            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.TopCenter
            ) {
                Text(
                    text = getStringResource(id = R.string.more_qr_code_button),
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = MoreColors.Primary,
                    textAlign = TextAlign.Center
                )
            }

            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.TopCenter
            ) {
                Text(
                    text = getStringResource(id = R.string.more_qr_code_scan_automatically),
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp,
                    color = MoreColors.Secondary,
                    textAlign = TextAlign.Center
                )
            }

            if (!permissionGiven.value) {
                Box(
                    modifier = Modifier.fillMaxWidth()
                        .padding(vertical = 30.dp)
                        .padding(horizontal = 30.dp),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Text(
                        text = getStringResource(id = R.string.more_qr_camera_needed),
                        fontWeight = FontWeight.Medium,
                        fontSize = 16.sp,
                        color = MoreColors.PrimaryLight200,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 48.dp)
                .padding(horizontal = 24.dp)
                .align(Alignment.BottomCenter),
            contentAlignment = Alignment.BottomCenter
        ) {
            OutlinedButton(
                onClick = onClose,
                modifier = Modifier
                    .padding(vertical = 8.dp)
                    .height(60.dp)
                    .fillMaxWidth(),
                colors = ButtonDefaults.moreSecondary(),
                border = MoreColors.borderPrimary(true)
            ) {
                Text(getStringResource(id = R.string.more_close))
            }
        }
    }
}
