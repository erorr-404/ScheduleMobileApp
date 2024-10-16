package com.erorr404.timetable

import android.Manifest
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class AlarmReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val name = intent?.getStringExtra(AndroidAlarmScheduler.LESSON_NAME)
        val content = intent?.getStringExtra(AndroidAlarmScheduler.NOTIFICATION_TEXT)
        val link = intent?.getStringExtra(AndroidAlarmScheduler.LESSON_LINK)

        Log.d("AlarmReceiver", "Received: $name, $content, $link. Launching notification...")

        val notificationIntent = Intent(Intent.ACTION_VIEW, Uri.parse(link))
        val pendingIntent = PendingIntent.getActivity(
            context,
            1,
            notificationIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, "lesson_new_channel")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(name)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .build()

        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            NotificationManagerCompat.from(context).notify(1001, notification)
            Log.d("AlarmReceiver", "Notification launched.")
        } else {
            Log.e("AlarmReceiver", "POST_NOTIFICATIONS permission is bot granted. Cant show notification")
        }

    }
}