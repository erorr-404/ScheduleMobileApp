package com.erorr404.timetable

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log

class AndroidAlarmScheduler(
    private val context: Context
): AlarmScheduler {

    private val alarmManager = context.getSystemService(AlarmManager::class.java)

    override fun schedule(item: AlarmItem) {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra(LESSON_NAME, item.title)
            putExtra(LESSON_LINK, item.intentLink)
            putExtra(NOTIFICATION_TEXT, item.content)
            putExtra(NOTIFICATION_ID, item.hashCode())
        }

        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            item.time.timeInMillis,
            AlarmManager.INTERVAL_DAY * 7,
            PendingIntent.getBroadcast(
                context,
                item.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
            )
        )

        Log.d("AndroidAlarmScheduler", "Set repeating: ${item.title}")
    }

    override fun cancel(item: AlarmItem) {
        alarmManager.cancel(
            PendingIntent.getBroadcast(
                context,
                item.hashCode(),
                Intent(context, AlarmReceiver::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
            )
        )
    }

    fun cancelAll() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            alarmManager.cancelAll()
        } else {
            Log.w("AndroidAlarmScheduler", "Cant cancel all Alarms. Reason: SDK version is lower than 34")
        }
    }

    companion object {
        const val LESSON_NAME = "lessonName"
        const val LESSON_LINK = "lessonLink"
        const val NOTIFICATION_TEXT = "notificationText"
        const val NOTIFICATION_ID = "notificationID"
    }
}