package com.universalwallet.loyalty.core.notifications

/** Notification channels grouped by purpose (Android 8+ requirement). */
enum class NotificationChannelDef(val id: String, val title: String, val description: String) {
    REMINDERS("uw_reminders", "Reminders", "Backup and unused-card reminders"),
    SECURITY("uw_security", "Security", "Security and privacy alerts"),
    UPDATES("uw_updates", "Updates", "New store plugins and import/export results"),
    OFFERS("uw_offers", "Offers", "Store offers and points expiry (future)"),
}

/** Every notification the engine can raise, mapped to a channel + stable id. */
enum class NotificationType(
    val channel: NotificationChannelDef,
    val notificationId: Int,
) {
    BACKUP_REMINDER(NotificationChannelDef.REMINDERS, 1001),
    UNUSED_CARDS(NotificationChannelDef.REMINDERS, 1002),
    NEW_STORE_PLUGIN(NotificationChannelDef.UPDATES, 1003),
    IMPORT_COMPLETE(NotificationChannelDef.UPDATES, 1004),
    EXPORT_COMPLETE(NotificationChannelDef.UPDATES, 1005),
    SECURITY_REMINDER(NotificationChannelDef.SECURITY, 1006),
    OFFER(NotificationChannelDef.OFFERS, 1007),
    POINTS_EXPIRY(NotificationChannelDef.OFFERS, 1008),
}

/** Content for a single notification. */
data class NotificationContent(
    val type: NotificationType,
    val title: String,
    val message: String,
)

/**
 * Schedules deferred/periodic notifications. Interface only for now — a future
 * implementation backs this with WorkManager. The immediate-display path lives
 * in [NotificationEngine].
 */
interface NotificationScheduler {
    fun scheduleBackupReminder(intervalDays: Int)
    fun scheduleUnusedCardsCheck(intervalDays: Int)
    fun cancel(type: NotificationType)
    fun cancelAll()
}
