package com.universalwallet.loyalty.domain.validation

/**
 * A single-purpose validation rule. Concrete validators (card number, label,
 * etc.) are added alongside the domain models in a later phase; the functional
 * interface is defined here as part of the validation foundation.
 */
fun interface Validator<in T> {
    fun validate(value: T): ValidationResult
}
