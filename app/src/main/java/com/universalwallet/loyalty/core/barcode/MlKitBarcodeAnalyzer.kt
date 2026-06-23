package com.universalwallet.loyalty.core.barcode

import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.common.InputImage

/**
 * CameraX [ImageAnalysis.Analyzer] that runs ML Kit barcode detection on each
 * frame and reports the first recognised, supported barcode via [onDetected].
 * Throttling / duplicate-suppression is left to the caller (the ViewModel), so
 * this stays a thin, reusable bridge. The [ImageProxy] is always closed.
 */
class MlKitBarcodeAnalyzer(
    private val scanner: BarcodeScanner,
    private val onDetected: (BarcodeScanResult) -> Unit,
    private val onError: (Throwable) -> Unit = {},
) : ImageAnalysis.Analyzer {

    @ExperimentalGetImage
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage == null) {
            imageProxy.close()
            return
        }
        val input = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
        scanner.process(input)
            .addOnSuccessListener { barcodes ->
                val match = barcodes.firstOrNull { it.rawValue != null }
                val value = match?.rawValue
                if (match != null && value != null) {
                    val symbology = mlKitFormatToSymbology(match.format) ?: BarcodeSymbology.QR_CODE
                    onDetected(BarcodeScanResult(value, symbology))
                }
            }
            .addOnFailureListener(onError)
            .addOnCompleteListener { imageProxy.close() }
    }
}
