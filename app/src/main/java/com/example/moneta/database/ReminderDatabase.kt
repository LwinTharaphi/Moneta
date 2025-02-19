package com.example.moneta.database

import android.util.Log
import com.example.moneta.model.Reminder
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.tasks.await
import java.util.UUID

object ReminderDatabase {
    private val db = FirebaseFirestore.getInstance()
    private val reminderCollection = db.collection("reminders")

    suspend fun addReminder(reminder: Reminder, userId: String) {
        try {
            Log.d("Reminder Database", "Adding reminder: $reminder")

            // Include the userId in the reminder document
            val reminderWithUserId = reminder.copy(userId = userId)

            // Add the reminder to Firestore
            reminderCollection.document(reminderWithUserId.id).set(reminderWithUserId).await()
            Log.d("Reminder Database", "Reminder added to Firestore")
        } catch (e: Exception) {
            Log.e("Reminder Database", "Error adding reminder: ${e.message}")
        }
    }


    suspend fun getReminder(userId: String): List<Reminder> {
        try {
            Log.d("Reminder Database", "Fetching reminders for userId: $userId")
            val snapshot = reminderCollection.whereEqualTo("userId", userId).get().await()
            Log.d("Reminder Database", "Fetched ${snapshot.size()} reminders")
            return snapshot.documents.map { doc -> doc.toObject(Reminder::class.java)!! }
        } catch (e: Exception) {
            Log.e("Reminder Database", "Error fetching reminders: ${e.message}")
            return emptyList()
        }
    }

}