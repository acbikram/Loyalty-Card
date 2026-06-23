package com.universalwallet.loyalty.data.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * v1 → v2: adds the organization & usage columns introduced in Part 4B
 * (pinning, archiving, hiding, usage frequency, and manual sort order). All
 * columns are non-null with safe defaults, so existing rows upgrade cleanly.
 */
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE loyalty_cards ADD COLUMN isPinned INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE loyalty_cards ADD COLUMN isArchived INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE loyalty_cards ADD COLUMN isHidden INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE loyalty_cards ADD COLUMN usageCount INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE loyalty_cards ADD COLUMN sortIndex INTEGER NOT NULL DEFAULT 0")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_loyalty_cards_isPinned ON loyalty_cards(isPinned)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_loyalty_cards_isArchived ON loyalty_cards(isArchived)")
    }
}
