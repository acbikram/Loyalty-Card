package com.universalwallet.loyalty.core.barcode

import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.common.InputImage

/**
 * CameraX [ImageAnalysis.Analyzer] that runs ML Kit barcode detection on each
 * frame and reports the first recognised barcode via [onDetected]. Frame
 * back-pressure is handled by CameraX (keep-only-latest); each [ImageProxy] is
 * always closed so the pipeline never stalls.
 */
class BarcodeAnalyzer(
    private val scanner: BarcodeScanner,
    private val onDetected: (BarcodeScanResult) -> Unit,
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
                for (barcode in barcodes) {
                    val symbology = BarcodeFormats.fromMlKit(barcode.format)
                    val raw = barcode.rawValue
                    if (symbology != null && !raw.isNullOrBlank()) {
                        onDetected(BarcodeScanResult(raw, symbology))
                        break
                    }
                }
            }
            .addOnCompleteListener { imageProxy.close() }
    }
}
