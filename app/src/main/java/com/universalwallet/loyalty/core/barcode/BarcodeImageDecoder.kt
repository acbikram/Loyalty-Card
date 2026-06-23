package com.universalwallet.loyalty.core.barcode

import android.content.Context
import android.net.Uri
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.common.InputImage
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import kotlin.coroutines.resume

/**
 * Detects a barcode inside a still image (an imported screenshot or photo) using
 * ML Kit. Returns the first recognised barcode, or null if none is found — the
 * basis of the "import a screenshot, auto-detect the code" flow.
 */
class BarcodeImageDecoder @Inject constructor(
    @ApplicationContext private val context: Context,
    private val scanner: BarcodeScanner,
) {

    suspend fun decodeFromUri(uri: Uri): BarcodeScanResult? = suspendCancellableCoroutine { cont ->
        val input = try {
            InputImage.fromFilePath(context, uri)
        } catch (e: Exception) {
            cont.resume(null)
            return@suspendCancellableCoroutine
        }
        scanner.process(input)
            .addOnSuccessListener { barcodes ->
                val match = barcodes.firstOrNull {
                    BarcodeFormats.fromMlKit(it.format) != null && !it.rawValue.isNullOrBlank()
                }
                val result = match?.let {
                    BarcodeScanResult(it.rawValue!!, BarcodeFormats.fromMlKit(it.format)!!)
                }
                cont.resume(result)
            }
            .addOnFailureListener { cont.resume(null) }
    }
}
