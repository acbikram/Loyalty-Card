package com.universalwallet.loyalty.core.permission

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

/** Snapshot of camera-permission status plus a request trigger. */
class CameraPermissionState internal constructor(
    val hasPermission: Boolean,
    val requestPermission: () -> Unit,
)

/**
 * Lifecycle-aware camera-permission state, backed by the Activity Result API
 * (no third-party permission library). Recomposes when the grant changes.
 */
@Composable
fun rememberCameraPermissionState(): CameraPermissionState {
    val context = LocalContext.current
    var granted by remember { mutableStateOf(context.hasCameraPermission()) }
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { isGranted -> granted = isGranted }

    return remember(granted, launcher) {
        CameraPermissionState(hasPermission = granted) {
            launcher.launch(Manifest.permission.CAMERA)
        }
    }
}

private fun Context.hasCameraPermission(): Boolean =
    ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
