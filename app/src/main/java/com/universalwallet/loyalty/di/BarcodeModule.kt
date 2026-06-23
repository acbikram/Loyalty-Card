package com.universalwallet.loyalty.di

import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.universalwallet.loyalty.core.barcode.BarcodeEncoder
import com.universalwallet.loyalty.core.barcode.ZxingBarcodeEncoder
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Provides the barcode subsystem: the ZXing-backed [BarcodeEncoder] for
 * generation, and a shared ML Kit [BarcodeScanner] (restricted to the formats
 * the app supports) for live and still-image scanning.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class BarcodeModule {

    @Binds
    @Singleton
    abstract fun bindBarcodeEncoder(impl: ZxingBarcodeEncoder): BarcodeEncoder

    companion object {
        @Provides
        @Singleton
        fun provideBarcodeScannerOptions(): BarcodeScannerOptions =
            BarcodeScannerOptions.Builder()
                .setBarcodeFormats(
                    Barcode.FORMAT_QR_CODE,
                    Barcode.FORMAT_CODE_128,
                    Barcode.FORMAT_CODE_39,
                    Barcode.FORMAT_CODE_93,
                    Barcode.FORMAT_EAN_13,
                    Barcode.FORMAT_EAN_8,
                    Barcode.FORMAT_UPC_A,
                    Barcode.FORMAT_UPC_E,
                    Barcode.FORMAT_PDF417,
                    Barcode.FORMAT_AZTEC,
                    Barcode.FORMAT_DATA_MATRIX,
                    Barcode.FORMAT_ITF,
                )
                .build()

        @Provides
        @Singleton
        fun provideBarcodeScanner(options: BarcodeScannerOptions): BarcodeScanner =
            BarcodeScanning.getClient(options)
    }
}
