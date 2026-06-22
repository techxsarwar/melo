/*
 * Melo - by ParallelogramFoundation
 * Licensed Under GPL-3.0
 */

package com.nikhil.yt.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.nikhil.yt.MainActivity
import com.nikhil.yt.R
import com.nikhil.yt.BroadcastNotification

object BroadcastNotificationManager {
    private const val CHANNEL_ID = "broadcast_notification_channel"
    private const val BASE_NOTIFICATION_ID = 20000

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Announcements",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "System updates and foundation announcements"
            }
            val notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager?.createNotificationChannel(channel)
        }
    }

    fun showNotification(context: Context, notification: BroadcastNotification) {
        createNotificationChannel(context)

        val intent = if (!notification.url.isNullOrBlank()) {
            Intent(Intent.ACTION_VIEW, Uri.parse(notification.url))
        } else {
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            notification.id ?: 0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_velune_concept)
            .setContentTitle(notification.title)
            .setContentText(notification.message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        try {
            NotificationManagerCompat.from(context).notify(
                BASE_NOTIFICATION_ID + (notification.id ?: 0),
                builder.build()
            )
        } catch (e: SecurityException) {
            // Missing POST_NOTIFICATIONS permission
        }
    }
}
