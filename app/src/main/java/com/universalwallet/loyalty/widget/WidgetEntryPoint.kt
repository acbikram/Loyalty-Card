package com.universalwallet.loyalty.widget

import com.universalwallet.loyalty.domain.repository.LoyaltyCardRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Hilt entry point so [AppWidgetProvider]s (which Hilt can't inject directly)
 * can reach the repository to render live data.
 */
@EntryPoint
@InstallIn(SingletonComponent::class)
interface WidgetEntryPoint {
    fun cardRepository(): LoyaltyCardRepository
}
