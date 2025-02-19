package com.example.moneta.utils

import android.content.Context
import androidx.core.app.NotificationCompat.MessagingStyle.Message
import com.google.firebase.messaging.FirebaseMessaging

object FCMHelper {
    fun sendReminderNotification(context: Context,title: String, message: String){
        FirebaseMessaging.getInstance().subscribeToTopic("reminders")
    }
}