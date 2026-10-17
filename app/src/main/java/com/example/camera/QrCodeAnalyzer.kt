package com.example.camera

import android.util.Log
import androidx.annotation.OptIn
import androidx.camera.core.Camera
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.ZoomSuggestionOptions
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.atomic.AtomicBoolean

@OptIn(ExperimentalGetImage::class)
class QrCodeAnalyzer(
    private val getCamera: () -> Camera?,
    private val onQrCodeScanned: (String) -> Unit
) : ImageAnalysis.Analyzer {

    private val isScanned = AtomicBoolean(false)

    // ML Kit Auto-Zoom Suggestion (يكتشف الكود البعيد ويكبر العدسة آلياً)
    private val zoomSuggestionOptions = ZoomSuggestionOptions.Builder { suggestedZoomRatio ->
        val camera = getCamera() ?: return@Builder false
        val maxZoom = camera.cameraInfo.zoomState.value?.maxZoomRatio ?: 1f
        val clampedZoom = suggestedZoomRatio.coerceIn(1f, minOf(maxZoom, 6.0f))
        camera.cameraControl.setZoomRatio(clampedZoom)
        true
    }.setMaxSupportedZoomRatio(6.0f).build()

    private val scanner = BarcodeScanning.getClient(
        BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
            .enableAllPotentialBarcodes() // التقاط الأكواد المائلة وذات التباين الضعيف
            .setZoomSuggestionOptions(zoomSuggestionOptions)
            .build()
    )

    override fun analyze(imageProxy: ImageProxy) {
        if (isScanned.get()) {
            imageProxy.close()
            return
        }

        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            scanner.process(image)
                .addOnSuccessListener { barcodes ->
                    if (isScanned.get()) return@addOnSuccessListener
                    for (barcode in barcodes) {
                        val rawValue = barcode.rawValue
                        if (!rawValue.isNullOrBlank()) {
                            // تفعيل القفل الذري لمنع التكرار
                            if (isScanned.compareAndSet(false, true)) {
                                onQrCodeScanned(rawValue)
                                break
                            }
                        }
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("QrCodeAnalyzer", "Barcode scan failed", e)
                }
                .addOnCompleteListener {
                    // تحرير الإطار فوراً لمنع تجميد الكاميرا
                    imageProxy.close()
                }
        } else {
            imageProxy.close()
        }
    }

    fun close() {
        try {
            scanner.close()
        } catch (e: Exception) {
            Log.e("QrCodeAnalyzer", "Failed to close scanner", e)
        }
    }
}
