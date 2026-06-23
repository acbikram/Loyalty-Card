package com.universalwallet.loyalty.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.widget.RemoteViews
import com.universalwallet.loyalty.R
import com.universalwallet.loyalty.core.cards.maskCardNumber
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Shows the user's favourite (or most-used) card on the home screen. Reads data
 * through a Hilt [WidgetEntryPoint] inside [goAsync] so the broadcast stays
 * alive while the repository is queried off the main thread.
 */
class FavoriteCardWidget : AppWidgetProvider() {

    override fun onUpdate(context: Context, manager: AppWidgetManager, appWidgetIds: IntArray) {
        val pending = goAsync()
        val repo = EntryPointAccessors
            .fromApplication(context.applicationContext, WidgetEntryPoint::class.java)
            .cardRepository()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val card = repo.observeFavorites().first().firstOrNull()
                    ?: repo.observeMostUsed(1).first().firstOrNull()
                appWidgetIds.forEach { id ->
                    val views = RemoteViews(context.packageName, R.layout.widget_favorite_card)
                    if (card != null) {
                        views.setTextViewText(R.id.widget_store_name, card.nickname.ifBlank { card.storeName })
                        views.setTextViewText(R.id.widget_card_number, maskCardNumber(card.cardNumber))
                    } else {
                        views.setTextViewText(R.id.widget_store_name, "No card yet")
                        views.setTextViewText(R.id.widget_card_number, "Tap to add a card")
                    }
                    views.setOnClickPendingIntent(R.id.widget_root, launchIntent(context))
                    manager.updateAppWidget(id, views)
                }
            } finally {
                pending.finish()
            }
        }
    }

    private fun launchIntent(context: Context): PendingIntent {
        val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        return PendingIntent.getActivity(context, 1, intent, flags)
    }
}
