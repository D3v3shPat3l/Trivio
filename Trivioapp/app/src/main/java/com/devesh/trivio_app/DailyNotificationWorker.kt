package com.devesh.trivio_app

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

class DailyNotificationWorker(appContext: Context, workerParams: WorkerParameters) : Worker(appContext, workerParams) {

    override fun doWork(): Result {
        if (ContextCompat.checkSelfPermission(applicationContext, android.Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(applicationContext, "Permission to post notifications is not granted!", Toast.LENGTH_SHORT).show()
            return Result.failure() // Optionally handle failure in a more graceful way
        }

        val notificationTime = inputData.getLong("notification_time", 0L)

        if (notificationTime == 0L) return Result.failure()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "daily_reminder_channel",
                "Daily Quiz Reminder",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(applicationContext, "daily_reminder_channel")
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Daily Quiz Reminder")
            .setContentText("Here's your daily quiz reminder!")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        val notificationManager = NotificationManagerCompat.from(applicationContext)
        notificationManager.notify(1, notification)

        return Result.success()
    }
}

