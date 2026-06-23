package com.universalwallet.loyalty.feature.lock

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.universalwallet.loyalty.core.components.WalletButton
import com.universalwallet.loyalty.core.components.WalletButtonStyle
import com.universalwallet.loyalty.core.security.AuthMethod
import com.universalwallet.loyalty.core.theme.Spacing
import com.universalwallet.loyalty.core.theme.WalletIcons

/**
 * Full-screen lock overlay shown while the session is locked. Offers biometric
 * /device-credential unlock (triggered through [onRequestBiometric], which the
 * activity fulfils) and/or a PIN entry, depending on the configured method.
 * Fully labelled for TalkBack with large touch targets.
 */
@Composable
fun LockScreen(
    onRequestBiometric: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LockViewModel = hiltViewModel(),
) {
    val config by viewModel.config.collectAsStateWithLifecycle()
    val state by viewModel.state.collectAsStateWithLifecycle()
    var pin by remember { mutableStateOf("") }

    val showPin = config.authMethod == AuthMethod.PIN || config.hasPin
    val showBiometric = config.authMethod != AuthMethod.PIN

    Surface(modifier = modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier.fillMaxSize().padding(Spacing.xl),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                imageVector = WalletIcons.Lock,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(72.dp),
            )
            Text(
                text = "Wallet locked",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(top = Spacing.lg),
            )
            Text(
                text = "Authenticate to continue",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = Spacing.xs, bottom = Spacing.xl),
            )

            if (showPin) {
                OutlinedTextField(
                    value = pin,
                    onValueChange = { if (it.length <= 12 && it.all(Char::isDigit)) pin = it },
                    label = { Text("PIN") },
                    singleLine = true,
                    isError = state.error != null,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics { contentDescription = "Enter your unlock PIN" },
                )
                WalletButton(
                    text = "Unlock",
                    onClick = { viewModel.submitPin(pin); pin = "" },
                    enabled = pin.length >= 4,
                    modifier = Modifier.fillMaxWidth().padding(top = Spacing.md),
                )
            }

            if (showBiometric) {
                WalletButton(
                    text = "Use biometrics",
                    onClick = onRequestBiometric,
                    style = if (showPin) WalletButtonStyle.SECONDARY else WalletButtonStyle.PRIMARY,
                    leadingIcon = WalletIcons.Lock,
                    modifier = Modifier.fillMaxWidth().padding(top = Spacing.md),
                )
            }

            state.error?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = Spacing.md),
                )
            }
        }
    }
}
