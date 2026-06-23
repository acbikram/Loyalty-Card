package com.universalwallet.loyalty.core.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.universalwallet.loyalty.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Concrete notification engine. Creates channels (idempotently), honours the
 * per-type [NotificationSettings] and the OS-level notification permission, and
 * raises notifications that deep-link back into the app. Never posts a type the
 * user disabled, and never includes sensitive card data in the text.
 */
@Singleton
class NotificationEngine @Inject constructor(
    @ApplicationContext private val context: Context,
    private val settings: NotificationSettings,
) {
    /** Registers all channels. Safe to call repeatedly; called on app start. */
    fun ensureChannels() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        NotificationChannelDef.entries.forEach { def ->
            val channel = NotificationChannel(def.id, def.title, NotificationManager.IMPORTANCE_DEFAULT).apply {
                description = def.description
            }
            manager.createNotificationChannel(channel)
        }
    }

    /** Shows [content] if allowed by settings and OS permission. */
    suspend fun notify(content: NotificationContent) {
        if (!settings.current().isAllowed(content.type)) return
        if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) return

        val launch = context.packageManager.getLaunchIntentForPackage(context.packageName)?.apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingFlags = android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        val pendingIntent = launch?.let {
            android.app.PendingIntent.getActivity(context, content.type.notificationId, it, pendingFlags)
        }

        val notification = NotificationCompat.Builder(context, content.type.channel.id)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(content.title)
            .setContentText(content.message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(content.message))
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        runCatching {
            NotificationManagerCompat.from(context).notify(content.type.notificationId, notification)
        }
    }

    fun cancel(type: NotificationType) =
        NotificationManagerCompat.from(context).cancel(type.notificationId)
}
