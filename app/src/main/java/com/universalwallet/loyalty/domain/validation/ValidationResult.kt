package com.universalwallet.loyalty.domain.validation

import com.universalwallet.loyalty.core.ui.UiText

/**
 * Outcome of validating a single value. [Invalid] carries a presentation-ready
 * [UiText] reason so the UI can show it without re-deriving the message.
 */
sealed interface ValidationResult {
    data object Valid : ValidationResult
    data class Invalid(val reason: UiText) : ValidationResult

    val isValid: Boolean get() = this is Valid
}
