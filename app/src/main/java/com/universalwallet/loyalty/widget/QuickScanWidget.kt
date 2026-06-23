package com.universalwallet.loyalty.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.widget.RemoteViews
import com.universalwallet.loyalty.R

/**
 * A one-tap "scan a card" widget. Stateless: it just launches the app, so it
 * needs no data access and updates instantly.
 */
class QuickScanWidget : AppWidgetProvider() {
    override fun onUpdate(context: Context, manager: AppWidgetManager, appWidgetIds: IntArray) {
        appWidgetIds.forEach { id ->
            val views = RemoteViews(context.packageName, R.layout.widget_quick_scan)
            views.setOnClickPendingIntent(R.id.widget_root, launchIntent(context))
            views.setOnClickPendingIntent(R.id.widget_scan_button, launchIntent(context))
            manager.updateAppWidget(id, views)
        }
    }

    private fun launchIntent(context: Context): PendingIntent {
        val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        return PendingIntent.getActivity(context, 0, intent, flags)
    }
}
