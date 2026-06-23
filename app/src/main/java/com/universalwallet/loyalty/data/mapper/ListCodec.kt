package com.universalwallet.loyalty.data.mapper

/**
 * Encodes/decodes a list of strings to a single delimited string for storage in
 * a Room column. The delimiter is the ASCII Unit Separator (0x1F), which never
 * appears in store names, keywords, or enum names — so no escaping is required.
 */
internal object ListCodec {
    private const val DELIMITER = "\u001F"

    fun encode(values: List<String>): String = values.joinToString(DELIMITER)

    fun decode(value: String): List<String> =
        if (value.isEmpty()) emptyList() else value.split(DELIMITER)
}
