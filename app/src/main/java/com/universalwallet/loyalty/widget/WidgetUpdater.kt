package com.universalwallet.loyalty.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context

/** Triggers a refresh of all placed widgets (e.g. after a favourite changes). */
object WidgetUpdater {
    fun refreshAll(context: Context) {
        val manager = AppWidgetManager.getInstance(context)
        listOf(FavoriteCardWidget::class.java, QuickScanWidget::class.java).forEach { cls ->
            val ids = manager.getAppWidgetIds(ComponentName(context, cls))
            if (ids.isNotEmpty()) {
                val intent = android.content.Intent(context, cls).apply {
                    action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
                }
                context.sendBroadcast(intent)
            }
        }
    }
}
