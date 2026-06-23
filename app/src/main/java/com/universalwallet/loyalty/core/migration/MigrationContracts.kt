package com.universalwallet.loyalty.core.migration

/** Category of a non-database migration. */
enum class MigrationKind { SETTINGS, PLUGIN, TEMPLATE }

/**
 * A single forward migration step for app *data* (not the Room schema, which
 * Room handles). Steps are ordered by [fromVersion] → [toVersion] and run once
 * when the stored data version is older than the target.
 */
interface DataMigrationStep {
    val kind: MigrationKind
    val fromVersion: Int
    val toVersion: Int
    suspend fun migrate()
}

/** Settings-shape migration (e.g. renamed/relocated preference keys). */
interface SettingsMigration : DataMigrationStep {
    override val kind: MigrationKind get() = MigrationKind.SETTINGS
}

/** Store-plugin migration (e.g. plugin id/format changes). */
interface PluginMigration : DataMigrationStep {
    override val kind: MigrationKind get() = MigrationKind.PLUGIN
}

/** Card-template migration (e.g. template schema changes). */
interface TemplateMigration : DataMigrationStep {
    override val kind: MigrationKind get() = MigrationKind.TEMPLATE
}

/** Outcome of validating the migration chains. */
data class MigrationValidationReport(
    val isValid: Boolean,
    val issues: List<String>,
)
