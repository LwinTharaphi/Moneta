package com.example.moneta.utils

import android.content.Context
import androidx.work.*
import java.util.Calendar
import java.util.concurrent.TimeUnit

object NotificationScheduler {
    fun scheduleReminder(context: Context, delay: Long, title: String, message: String) {
        val data = workDataOf(
            "title" to title,
            "message" to message
        )

        val workRequest = OneTimeWorkRequestBuilder<NotificationHelper>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(data)
            .build()

        WorkManager.getInstance(context).enqueue(workRequest)
    }
    fun calculateTimeInMillis(time: String): Long {
        val calendar = Calendar.getInstance()
        val parts = time.split(":")
        val hour = parts[0].toInt()
        val minute = parts[1].split(" ")[0].toInt()
        val amPm = parts[1].split(" ")[1]

        val adjustedHour = if (amPm == "PM" && hour != 12) hour + 12 else hour
        calendar.set(Calendar.HOUR_OF_DAY, adjustedHour)
        calendar.set(Calendar.MINUTE, minute)
        calendar.set(Calendar.SECOND, 0)

        val currentTime = System.currentTimeMillis()
        val reminderTime = calendar.timeInMillis

        return if (reminderTime > currentTime) {
            reminderTime - currentTime
        } else {
            reminderTime + 24 * 60 * 60 * 1000 - currentTime
        }
    }

}
