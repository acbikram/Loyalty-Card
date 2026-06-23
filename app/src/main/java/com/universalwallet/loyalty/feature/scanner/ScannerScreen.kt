package com.universalwallet.loyalty.feature.scanner

import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.universalwallet.loyalty.core.barcode.BarcodeScanResult
import com.universalwallet.loyalty.core.permission.rememberCameraPermissionState
import com.universalwallet.loyalty.core.components.WalletButton
import com.universalwallet.loyalty.core.theme.Spacing
import com.universalwallet.loyalty.core.theme.WalletIcons
import com.universalwallet.loyalty.core.ui.ObserveAsEvents
import java.util.concurrent.Executors

/**
 * Full-screen barcode scanner: CameraX preview + ML Kit analysis, a framed
 * overlay with an animated scan line, torch, zoom, and single/continuous mode.
 * Reports the first accepted code via [onScanned] and dismisses via [onClose].
 * Works in portrait and landscape (the layout is orientation-agnostic).
 */
@Composable
fun ScannerScreen(
    onScanned: (BarcodeScanResult) -> Unit,
    onClose: () -> Unit,
    viewModel: ScannerViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val permission = rememberCameraPermissionState()

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            is ScannerEvent.Scanned -> {
                onScanned(event.result)
                onClose()
            }
        }
    }

    LaunchedEffect(permission.hasPermission) {
        if (!permission.hasPermission) permission.requestPermission()
    }

    Box(Modifier.fillMaxSize().background(Color.Black)) {
        if (permission.hasPermission) {
            val analyzer = remember(viewModel) { viewModel.createAnalyzer() }
            CameraPreview(
                analyzer = analyzer,
                torchEnabled = state.torchEnabled,
                zoom = state.zoom,
                modifier = Modifier.fillMaxSize(),
            )
            ScannerOverlay()
            ScannerControls(
                state = state,
                onClose = onClose,
                onToggleTorch = viewModel::toggleTorch,
                onZoomChange = viewModel::setZoom,
                onModeChange = viewModel::setMode,
                onResume = viewModel::resume,
            )
        } else {
            PermissionRationale(onRequest = permission.requestPermission, onClose = onClose)
        }
    }
}

@Composable
private fun CameraPreview(
    analyzer: ImageAnalysis.Analyzer,
    torchEnabled: Boolean,
    zoom: Float,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val previewView = remember {
        PreviewView(context).apply { scaleType = PreviewView.ScaleType.FILL_CENTER }
    }
    val executor = remember { Executors.newSingleThreadExecutor() }
    val cameraRef = remember { mutableStateOf<androidx.camera.core.Camera?>(null) }
    val providerRef = remember { mutableStateOf<ProcessCameraProvider?>(null) }

    DisposableEffect(lifecycleOwner) {
        val future = ProcessCameraProvider.getInstance(context)
        future.addListener({
            val provider = future.get()
            providerRef.value = provider
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }
            val analysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also { it.setAnalyzer(executor, analyzer) }
            provider.unbindAll()
            cameraRef.value = provider.bindToLifecycle(
                lifecycleOwner,
                CameraSelector.DEFAULT_BACK_CAMERA,
                preview,
                analysis,
            )
        }, ContextCompat.getMainExecutor(context))

        onDispose {
            providerRef.value?.unbindAll()
            executor.shutdown()
        }
    }

    LaunchedEffect(torchEnabled) { cameraRef.value?.cameraControl?.enableTorch(torchEnabled) }
    LaunchedEffect(zoom) { cameraRef.value?.cameraControl?.setLinearZoom(zoom) }

    AndroidView(factory = { previewView }, modifier = modifier)
}

@Composable
private fun ScannerOverlay() {
    val windowWidth = 280.dp
    val windowHeight = 180.dp
    val transition = rememberInfiniteTransition(label = "scan")
    val linePosition by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1500, easing = LinearEasing), RepeatMode.Reverse),
        label = "scanLine",
    )

    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .width(windowWidth)
                .height(windowHeight)
                .border(2.dp, Color.White, RoundedCornerShape(16.dp)),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = windowHeight * linePosition)
                    .height(2.dp)
                    .background(MaterialTheme.colorScheme.primary),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ScannerControls(
    state: ScannerUiState,
    onClose: () -> Unit,
    onToggleTorch: () -> Unit,
    onZoomChange: (Float) -> Unit,
    onModeChange: (ScanMode) -> Unit,
    onResume: () -> Unit,
) {
    Column(Modifier.fillMaxSize().padding(Spacing.lg)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            IconButton(onClick = onClose) {
                Icon(WalletIcons.Close, contentDescription = "Close scanner", tint = Color.White)
            }
            IconButton(onClick = onToggleTorch) {
                Icon(
                    imageVector = WalletIcons.Scan,
                    contentDescription = if (state.torchEnabled) "Turn torch off" else "Turn torch on",
                    tint = if (state.torchEnabled) MaterialTheme.colorScheme.primary else Color.White,
                )
            }
        }

        Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
            if (state.isPaused) {
                WalletButton(text = "Scan again", onClick = onResume)
            }
        }

        Text(
            text = "Align the barcode within the frame",
            color = Color.White,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(bottom = Spacing.sm),
        )

        SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
            ScanMode.entries.forEachIndexed { index, mode ->
                SegmentedButton(
                    selected = state.mode == mode,
                    onClick = { onModeChange(mode) },
                    shape = SegmentedButtonDefaults.itemShape(index, ScanMode.entries.size),
                ) {
                    Text(if (mode == ScanMode.SINGLE) "Single" else "Continuous")
                }
            }
        }

        Row(Modifier.fillMaxWidth().padding(top = Spacing.sm), verticalAlignment = Alignment.CenterVertically) {
            Text("Zoom", color = Color.White, style = MaterialTheme.typography.labelMedium)
            Slider(
                value = state.zoom,
                onValueChange = onZoomChange,
                modifier = Modifier.weight(1f).padding(start = Spacing.md),
            )
        }
    }
}

@Composable
private fun PermissionRationale(onRequest: () -> Unit, onClose: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(Spacing.xl),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(WalletIcons.Scan, contentDescription = null, tint = Color.White, modifier = Modifier.size(64.dp))
        Text(
            text = "Camera access needed",
            color = Color.White,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(top = Spacing.lg),
        )
        Text(
            text = "Allow camera access to scan a card's barcode. You can also enter the number manually instead.",
            color = Color.White,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(vertical = Spacing.md),
        )
        WalletButton(text = "Grant camera access", onClick = onRequest, modifier = Modifier.fillMaxWidth())
        WalletButton(
            text = "Enter manually instead",
            onClick = onClose,
            style = com.universalwallet.loyalty.core.components.WalletButtonStyle.TEXT,
            modifier = Modifier.fillMaxWidth().padding(top = Spacing.sm),
        )
    }
}
