package com.universalwallet.loyalty.core.cards

/** Groups a card number into space-separated blocks of four for readability. */
fun formatCardNumber(number: String): String =
    number.trim().chunked(4).joinToString(" ")

/** Masks all but the last four characters, e.g. "•••• 1234". */
fun maskCardNumber(number: String): String {
    val trimmed = number.trim()
    if (trimmed.length <= 4) return trimmed
    return "•••• " + trimmed.takeLast(4)
}
