package com.universalwallet.loyalty.core.locale

import android.content.Context
import android.content.res.Configuration
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.universalwallet.loyalty.core.datastore.WalletPreferencesKeys
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Localization framework entry point (Part 6B). Persists the user's chosen
 * language and produces locale-wrapped contexts. On Android 13+ the system
 * language picker (backed by `locales_config.xml`) can drive this with no code;
 * [wrap] supports in-app switching on older versions via `attachBaseContext`.
 *
 * Uses only framework APIs — no extra dependency.
 */
@Singleton
class LocaleManager @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {
    /** Persisted BCP-47 tag; empty string means "follow system". */
    val languageTag: Flow<String> = dataStore.data.map {
        it[WalletPreferencesKeys.APP_LANGUAGE] ?: ""
    }

    val language: Flow<AppLanguage> = languageTag.map { AppLanguage.fromTag(it) }

    suspend fun setLanguage(language: AppLanguage) = setLanguageTag(language.tag)

    suspend fun setLanguageTag(tag: String) {
        dataStore.edit { it[WalletPreferencesKeys.APP_LANGUAGE] = tag }
    }

    fun supported(): List<AppLanguage> = AppLanguage.entries

    fun localeFor(tag: String): Locale =
        if (tag.isBlank()) Locale.getDefault() else Locale.forLanguageTag(tag)

    /**
     * Returns a context configured for [tag]. Call from
     * `Activity.attachBaseContext(base)` as `super.attachBaseContext(wrap(base, tag))`.
     * Returns the original context unchanged when [tag] is blank (system default).
     */
    fun wrap(context: Context, tag: String): Context {
        if (tag.isBlank()) return context
        val locale = Locale.forLanguageTag(tag)
        Locale.setDefault(locale)
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        config.setLayoutDirection(locale)
        return context.createConfigurationContext(config)
    }
}
