package com.universalwallet.loyalty.core.locale

/**
 * Languages the app ships translations for. [tag] is a BCP-47 tag ("" means
 * follow the system language). Keep this in sync with `res/xml/locales_config.xml`
 * and the `values-<lang>` resource folders.
 */
enum class AppLanguage(val tag: String, val displayName: String, val isRtl: Boolean) {
    SYSTEM("", "System default", false),
    ENGLISH("en", "English", false),
    ARABIC("ar", "العربية", true),
    ;

    companion object {
        fun fromTag(tag: String): AppLanguage = entries.firstOrNull { it.tag == tag } ?: SYSTEM
    }
}
