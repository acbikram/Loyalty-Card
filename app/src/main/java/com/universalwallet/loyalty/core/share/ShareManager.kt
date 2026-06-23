package com.universalwallet.loyalty.core.share

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.universalwallet.loyalty.core.barcode.ZxingBarcodeEncoder
import com.universalwallet.loyalty.core.utils.IoDispatcher
import com.universalwallet.loyalty.data.mapper.BarcodeTypeMapper
import com.universalwallet.loyalty.domain.model.LoyaltyCard
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Builds and launches share sheets for a card. Callers must obtain explicit
 * user confirmation first (the UI shows a chooser of what to share); this class
 * only constructs the intent. Sharing never includes anything beyond what the
 * user selected — number, details text, or a rendered barcode/QR image.
 */
@Singleton
class ShareManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val encoder: ZxingBarcodeEncoder,
    @IoDispatcher private val io: CoroutineDispatcher,
) {

    /** Shares just the membership number as plain text. */
    fun shareCardNumber(card: LoyaltyCard) {
        startTextShare("${card.storeName} card number: ${card.cardNumber}")
    }

    /** Shares a human-readable summary of the card (no hidden fields). */
    fun shareDetails(card: LoyaltyCard) {
        val text = buildString {
            appendLine(card.storeName)
            if (card.nickname.isNotBlank()) appendLine(card.nickname)
            appendLine("Number: ${card.cardNumber}")
            appendLine("Category: ${card.category.name.lowercase()}")
        }.trim()
        startTextShare(text)
    }

    /** Renders the card's barcode to a PNG and shares it as an image. */
    suspend fun shareBarcodeImage(card: LoyaltyCard) {
        val symbology = BarcodeTypeMapper.toSymbology(card.barcodeType)
        val is2d = card.barcodeType.isTwoDimensional
        val width = 800
        val height = if (is2d) 800 else 300
        val uri = withContext(io) {
            val bitmap = encoder.encodeOrNull(card.barcodeValue, symbology, width, height)
                ?: return@withContext null
            val dir = File(context.cacheDir, "images").apply { mkdirs() }
            val file = File(dir, "share_${card.id}.png")
            FileOutputStream(file).use { bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, it) }
            FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        } ?: return
        startImageShare(uri)
    }

    private fun startTextShare(text: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        launch(intent)
    }

    private fun startImageShare(uri: Uri) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        launch(intent)
    }

    private fun launch(intent: Intent) {
        val chooser = Intent.createChooser(intent, "Share card")
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }
}
