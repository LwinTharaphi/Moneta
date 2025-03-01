package com.example.moneta.repository

import android.util.Log
import com.example.moneta.model.Notification
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

object NotificationRepository {
    private val db = FirebaseFirestore.getInstance()
    private val notificationCollection = db.collection("notifications")

    suspend fun getNotifications(userId: String): List<Notification> {
        return try {
            Log.d("NotificationRepo", "Fetching notifications for userId: $userId")
            val snapshot = notificationCollection.whereEqualTo("userId", userId).get().await()
            snapshot.documents.mapNotNull { it.toObject(Notification::class.java) }
        } catch (e: Exception) {
            Log.e("NotificationRepo", "Error fetching notifications: ${e.message}")
            emptyList()
        }
    }
}
