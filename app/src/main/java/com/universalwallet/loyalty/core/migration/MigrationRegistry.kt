package com.universalwallet.loyalty.core.migration

import androidx.room.migration.Migration
import com.universalwallet.loyalty.data.database.MIGRATION_1_2
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Single source of truth for all migrations. Room schema migrations live in the
 * data layer and are surfaced here for visibility/validation; app *data*
 * migrations (settings/plugin/template) are collected as ordered
 * [DataMigrationStep]s. No data migrations are needed yet, so the list is empty
 * — the engine is in place for future versions.
 */
@Singleton
class MigrationRegistry @Inject constructor() {

    val schemaVersion: Int = 2
    val dataVersion: Int = 1

    val roomMigrations: Array<Migration> = arrayOf(MIGRATION_1_2)

    val dataMigrations: List<DataMigrationStep> = emptyList()

    /** Room schema steps expressed as (from, to) pairs for the validator. */
    fun roomSteps(): List<Pair<Int, Int>> = roomMigrations.map { it.startVersion to it.endVersion }
}
