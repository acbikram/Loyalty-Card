package com.universalwallet.loyalty

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.fragment.app.FragmentActivity
import com.universalwallet.loyalty.core.navigation.WalletApp
import com.universalwallet.loyalty.core.security.AuthMethod
import com.universalwallet.loyalty.core.security.BiometricAuthenticator
import com.universalwallet.loyalty.core.security.SecurityConfig
import com.universalwallet.loyalty.core.security.SecurityManager
import com.universalwallet.loyalty.core.theme.AppTheme
import com.universalwallet.loyalty.di.ThemeStateHolder
import com.universalwallet.loyalty.feature.lock.LockScreen
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * The single host activity. A [FragmentActivity] (required by BiometricPrompt),
 * it wires edge-to-edge rendering, the splash screen, the top-level [AppTheme],
 * screenshot protection, the security lock gate, and session lifecycle hooks.
 * It contains no screen logic.
 */
@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    @Inject lateinit var themeStateHolder: ThemeStateHolder
    @Inject lateinit var securityManager: SecurityManager
    @Inject lateinit var biometricAuthenticator: BiometricAuthenticator

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        securityManager.initializeOnLaunch()

        setContent {
            val themeMode by themeStateHolder.themeMode.collectAsState()
            val dynamicColor by themeStateHolder.dynamicColor.collectAsState()
            val securityConfig by securityManager.config.collectAsState()
            val isLocked by securityManager.isLocked.collectAsState()

            LaunchedEffect(securityConfig.screenshotProtection) {
                applyScreenshotProtection(securityConfig.screenshotProtection)
            }

            AppTheme(themeMode = themeMode, dynamicColor = dynamicColor) {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    Box(Modifier.fillMaxSize()) {
                        WalletApp()
                        if (isLocked) {
                            LockScreen(onRequestBiometric = { promptBiometric(securityConfig) })
                        }
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        securityManager.onAppForegrounded()
    }

    override fun onStop() {
        securityManager.onAppBackgrounded()
        super.onStop()
    }

    private fun applyScreenshotProtection(enabled: Boolean) {
        if (enabled) {
            window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }
    }

    private fun promptBiometric(config: SecurityConfig) {
        biometricAuthenticator.authenticate(
            activity = this,
            title = "Unlock wallet",
            subtitle = "Authenticate to access your cards",
            allowDeviceCredential = config.authMethod == AuthMethod.DEVICE_CREDENTIAL,
        ) { result ->
            if (result is com.universalwallet.loyalty.core.security.AuthResult.Success) {
                securityManager.onUnlocked()
            }
        }
    }
}
