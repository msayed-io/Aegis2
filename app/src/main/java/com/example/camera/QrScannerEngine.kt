package com.example.camera

import android.util.Log
import android.util.Size
import androidx.camera.core.*
import androidx.camera.core.resolutionselector.AspectRatioStrategy
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.core.resolutionselector.ResolutionStrategy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.lifecycle.LifecycleOwner
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class QrScannerEngine(
    private val lifecycleOwner: LifecycleOwner,
    private val previewView: PreviewView,
    private val onQrScanned: (String) -> Unit
) {
    private val cameraExecutor: ExecutorService = Executors.newSingleThreadExecutor()
    private var activeCamera: Camera? = null
    private var analyzer: QrCodeAnalyzer? = null
    private var cameraProvider: ProcessCameraProvider? = null
    private var isBound = false

    fun startCamera() {
        if (isBound) {
            Log.d("QrScannerEngine", "Camera already bound, skipping rebind")
            return
        }

        val cameraProviderFuture = ProcessCameraProvider.getInstance(previewView.context)
        cameraProviderFuture.addListener({
            try {
                cameraProvider = cameraProviderFuture.get()

                val resolutionSelector = ResolutionSelector.Builder()
                    .setAspectRatioStrategy(AspectRatioStrategy.RATIO_16_9_FALLBACK_AUTO_STRATEGY)
                    .setResolutionStrategy(
                        ResolutionStrategy(
                            Size(1920, 1080),
                            ResolutionStrategy.FALLBACK_RULE_CLOSEST_HIGHER_THEN_LOWER
                        )
                    )
                    .build()

                val preview = Preview.Builder()
                    .setResolutionSelector(resolutionSelector)
                    .build()
                    .also { it.surfaceProvider = previewView.surfaceProvider }

                analyzer = QrCodeAnalyzer(
                    getCamera = { activeCamera },
                    onQrCodeScanned = onQrScanned
                )

                val imageAnalysis = ImageAnalysis.Builder()
                    .setResolutionSelector(resolutionSelector)
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_YUV_420_888)
                    .build()
                    .also { it.setAnalyzer(cameraExecutor, analyzer!!) }

                cameraProvider?.unbindAll()
                activeCamera = cameraProvider?.bindToLifecycle(
                    lifecycleOwner,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    imageAnalysis
                )
                isBound = true

                // Center Continuous Auto-Focus and Auto-Exposure
                try {
                    val factory = SurfaceOrientedMeteringPointFactory(1f, 1f)
                    val centerPoint = factory.createPoint(0.5f, 0.5f)
                    val focusAction = FocusMeteringAction.Builder(
                        centerPoint,
                        FocusMeteringAction.FLAG_AF or FocusMeteringAction.FLAG_AE
                    ).setAutoCancelDuration(2, TimeUnit.SECONDS).build()
                    activeCamera?.cameraControl?.startFocusAndMetering(focusAction)
                } catch (e: Exception) {
                    Log.w("QrScannerEngine", "Focus metering failed", e)
                }

                Log.d("QrScannerEngine", "Camera bound successfully")
            } catch (e: Exception) {
                Log.e("QrScannerEngine", "Binding failed", e)
                isBound = false
            }
        }, androidx.core.content.ContextCompat.getMainExecutor(previewView.context))
    }

    fun shutdown() {
        try {
            cameraProvider?.unbindAll()
        } catch (e: Exception) {
            Log.w("QrScannerEngine", "Unbind failed", e)
        }
        try {
            analyzer?.close()
        } catch (e: Exception) {
            Log.e("QrScannerEngine", "Failed to close scanner", e)
        }
        try {
            cameraExecutor.shutdown()
            if (!cameraExecutor.awaitTermination(1, TimeUnit.SECONDS)) {
                cameraExecutor.shutdownNow()
            }
        } catch (e: Exception) {
            Log.w("QrScannerEngine", "Executor shutdown interrupted", e)
        }
        isBound = false
        activeCamera = null
        Log.d("QrScannerEngine", "Shutdown complete")
    }
}
