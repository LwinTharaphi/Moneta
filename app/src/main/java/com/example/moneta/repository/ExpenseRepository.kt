package com.example.moneta.repository

import android.util.Log
import com.example.moneta.model.Expense
import com.example.moneta.model.FirestoreExpense
import com.example.moneta.model.toFirestoreExpense
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.toObject
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*
import java.util.UUID

class ExpenseRepository {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val userId: String?
        get() = auth.currentUser?.uid

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    /**
     * 🔹 Fetch expenses for a specific date from Firestore (Real-time updates)
     */
    fun getExpensesByDate(date: Date): Flow<List<Expense>> = callbackFlow {
        if (userId == null) {
            close(Exception("User not logged in"))
            return@callbackFlow
        }

        val formattedDate = dateFormat.format(date)

        try {
            val listener = db.collection("users")
                .document(userId!!)
                .collection("expenses")
                .whereEqualTo("date", formattedDate)
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING) // 🔹 Ensure Firestore allows this query
                .addSnapshotListener { snapshot, e ->
                    if (e != null) {
                        Log.e("FirestoreError", "Firestore query failed", e) // Debug Log
                        close(e)
                        return@addSnapshotListener
                    }

                    if (snapshot != null) {
                        val expenses = snapshot.documents.mapNotNull { it.toObject(FirestoreExpense::class.java)?.toExpense() }
                        trySend(expenses)
                    }
                }

            awaitClose { listener.remove() }
        } catch (e: Exception) {
            Log.e("FirestoreError", "Index required for sorting. Check Firestore console.", e)
            close(e)
        }
    }

    /**
     * 🔹 Add a new expense to Firestore
     */
    suspend fun addExpense(expense: Expense) {
        if (userId == null) throw Exception("User not logged in")

        val expenseRef = db.collection("users")
            .document(userId!!)
            .collection("expenses")
            .document() // Generate a unique Firestore document ID

        val expenseToStore = expense.toFirestoreExpense().copy(
            id = expenseRef.id
        ) // Assign Firestore ID

        expenseRef.set(expenseToStore).await() // Save to Firestore
    }

    /**
     * 🔹 Update an existing expense in Firestore
     */
    suspend fun updateExpense(expense: Expense) {
        if (userId == null) throw Exception("User not logged in")

        val expenseRef = db.collection("users")
            .document(userId!!)
            .collection("expenses")
            .document(expense.id) // Reference to the expense document

        val updatedExpense = expense.toFirestoreExpense() // Convert to Firestore-friendly format

        expenseRef.set(updatedExpense).await() // Overwrite the existing document
    }

    /**
     * 🔹 Delete an expense from Firestore
     */
    suspend fun deleteExpense(expenseId: String) {
        if (userId == null) throw Exception("User not logged in")

        db.collection("users")
            .document(userId!!)
            .collection("expenses")
            .document(expenseId)
            .delete()
            .await() // Delete the document from Firestore
    }

    companion object {
        @Volatile
        private var INSTANCE: ExpenseRepository? = null

        fun getInstance(): ExpenseRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: ExpenseRepository().also { INSTANCE = it }
            }
        }
    }
}
