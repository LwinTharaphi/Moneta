package com.example.moneta.utils

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters

class NotificationHelper (context: Context, params: WorkerParameters): Worker(context, params){
    override fun doWork(): Result {
        val title = inputData.getString("title") ?: "Reminder"
        val message = inputData.getString("message") ?: "Time to record your accounts"

        val firebaseService = MyFirebaseMessagingService()
        firebaseService.sendNotification(title,message)

        return Result.success()
    }
}