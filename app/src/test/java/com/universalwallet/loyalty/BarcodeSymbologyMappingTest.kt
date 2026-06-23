package com.universalwallet.loyalty

import com.google.common.truth.Truth.assertThat
import com.universalwallet.loyalty.core.barcode.BarcodeSymbology
import com.universalwallet.loyalty.data.mapper.BarcodeTypeMapper
import com.universalwallet.loyalty.domain.model.BarcodeType
import org.junit.Test

/** Verifies the domain<->contract symbology bridge is consistent. */
class BarcodeSymbologyMappingTest {

    @Test
    fun everyBarcodeType_mapsToSymbologyAndBack() {
        // 1D + 2D types round-trip exactly (UPC maps to UPC_A and back to UPC).
        BarcodeType.entries.forEach { type ->
            val symbology = BarcodeTypeMapper.toSymbology(type)
            val back = BarcodeTypeMapper.fromSymbology(symbology)
            assertThat(back).isEqualTo(type)
        }
    }

    @Test
    fun upcEAndDataMatrix_collapseToClosestDomainType() {
        assertThat(BarcodeTypeMapper.fromSymbology(BarcodeSymbology.UPC_E)).isEqualTo(BarcodeType.UPC)
        assertThat(BarcodeTypeMapper.fromSymbology(BarcodeSymbology.DATA_MATRIX)).isEqualTo(BarcodeType.QR)
    }
}
