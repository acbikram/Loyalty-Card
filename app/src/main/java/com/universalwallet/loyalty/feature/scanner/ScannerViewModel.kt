package com.universalwallet.loyalty.feature.scanner

import androidx.lifecycle.ViewModel
import com.universalwallet.loyalty.core.barcode.BarcodeAnalyzer
import com.universalwallet.loyalty.core.barcode.BarcodeScanResult
import com.google.mlkit.vision.barcode.BarcodeScanner
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

/** Single-shot vs keep-scanning behaviour. */
enum class ScanMode { SINGLE, CONTINUOUS }

/** Immutable scanner UI state. */
data class ScannerUiState(
    val torchEnabled: Boolean = false,
    val zoom: Float = 0f,
    val mode: ScanMode = ScanMode.SINGLE,
    val isPaused: Boolean = false,
)

/** One-time scanner effects. */
sealed interface ScannerEvent {
    data class Scanned(val result: BarcodeScanResult) : ScannerEvent
}

/**
 * Scanner ViewModel. Owns the camera-independent scan state (torch, zoom, mode,
 * pause) and de-duplicates rapid repeat detections of the same value. The
 * injected ML Kit [BarcodeScanner] is an app singleton, so it is reused (and
 * intentionally never closed here) across the live analyzer and still-image
 * decoding. The analyzer factory keeps all CameraX wiring in the screen.
 */
@HiltViewModel
class ScannerViewModel @Inject constructor(
    private val scanner: BarcodeScanner,
) : ViewModel() {

    private val _state = MutableStateFlow(ScannerUiState())
    val state = _state.asStateFlow()

    private val _events = Channel<ScannerEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    private var lastValue: String? = null

    /** Builds an analyzer bound to this ViewModel's detection handler. */
    fun createAnalyzer(): BarcodeAnalyzer = BarcodeAnalyzer(scanner, ::onBarcodeDetected)

    /** Called (off the main thread) by the analyzer for each recognised code. */
    fun onBarcodeDetected(result: BarcodeScanResult) {
        val current = _state.value
        if (current.isPaused) return
        if (result.rawValue == lastValue) return
        lastValue = result.rawValue
        _events.trySend(ScannerEvent.Scanned(result))
        if (current.mode == ScanMode.SINGLE) {
            _state.update { it.copy(isPaused = true) }
        }
    }

    fun toggleTorch() = _state.update { it.copy(torchEnabled = !it.torchEnabled) }
    fun setZoom(value: Float) = _state.update { it.copy(zoom = value.coerceIn(0f, 1f)) }
    fun setMode(mode: ScanMode) = _state.update { it.copy(mode = mode, isPaused = false) }
    fun pause() = _state.update { it.copy(isPaused = true) }
    fun resume() {
        lastValue = null
        _state.update { it.copy(isPaused = false) }
    }
}
