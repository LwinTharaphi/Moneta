package com.example.moneta.repository

import android.util.Log
import android.content.Context
import com.example.moneta.database.ExpenseDao
import com.example.moneta.model.*
import com.example.moneta.database.ExpenseDatabase
import com.example.moneta.model.FirestoreExpense
import com.example.moneta.model.toFirestoreExpense
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

class ExpenseRepository (private val database: ExpenseDatabase) {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val userId: String?
        get() = auth.currentUser?.uid

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val expenseDao = database.expenseDao()

    @OptIn(DelicateCoroutinesApi::class)
    fun getExpensesByDate(date: Date): Flow<List<Expense>> = callbackFlow {
        val startOfDay = date.time
        val endOfDay = startOfDay + (24 * 60 * 60 * 1000) - 1

        // Fetch from Room first
        val localExpenses = expenseDao.getExpensesByDate(startOfDay, endOfDay)
        if (localExpenses.isNotEmpty()) {
            trySend(localExpenses.map { it.toExpense() })
        }

        if (userId != null) {
            val formattedDate = dateFormat.format(date)
            try {
                val listener = db.collection("users")
                    .document(userId!!)
                    .collection("expenses")
                    .whereEqualTo("date", formattedDate)
                    .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                    .addSnapshotListener { snapshot, e ->
                        if (e != null) {
                            Log.e("FirestoreError", "Firestore query failed", e)
                            close(e)
                            return@addSnapshotListener
                        }

                        if (snapshot != null) {
                            val expenses = snapshot.documents.mapNotNull { it.toObject(FirestoreExpense::class.java)?.toExpense() }
                            trySend(expenses)

                            // 🔹 Insert into Room inside coroutine
                            GlobalScope.launch {
                                expenseDao.insertExpenses(expenses.map { it.toExpenseEntity() })
                            }
                        }
                    }

                awaitClose { listener.remove() }
            } catch (e: Exception) {
                Log.e("FirestoreError", "Firestore index required for sorting. Check Firestore console.", e)
                close(e)
            }
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

    @OptIn(DelicateCoroutinesApi::class)
    fun getMonthlyExpenses(startDate: String, endDate: String): Flow<List<Expense>> = callbackFlow {
        val startOfMonth = dateFormat.parse(startDate)?.time ?: 0
        val endOfMonth = dateFormat.parse(endDate)?.time ?: 0

        // Fetch from Room first
        val localExpenses = expenseDao.getExpensesByMonth(startOfMonth, endOfMonth)
        if (localExpenses.isNotEmpty()) {
            trySend(localExpenses.map { it.toExpense() })
        }

        if (userId != null) {
            try {
                val listener = db.collection("users")
                    .document(userId!!)
                    .collection("expenses")
                    .whereGreaterThanOrEqualTo("date", startDate)
                    .whereLessThanOrEqualTo("date", endDate)
                    .orderBy("date", com.google.firebase.firestore.Query.Direction.DESCENDING)
                    .addSnapshotListener { snapshot, e ->
                        if (e != null) {
                            Log.e("FirestoreError", "Firestore query failed for monthly report", e)
                            close(e)
                            return@addSnapshotListener
                        }

                        if (snapshot != null) {
                            val expenses = snapshot.documents.mapNotNull { it.toObject(FirestoreExpense::class.java)?.toExpense() }
                            trySend(expenses)

                            // 🔹 Insert into Room inside coroutine
                            GlobalScope.launch {
                                expenseDao.insertExpenses(expenses.map { it.toExpenseEntity() })
                            }
                        }
                    }

                awaitClose { listener.remove() }
            } catch (e: Exception) {
                Log.e("FirestoreError", "Firestore index required for sorting. Check Firestore console.", e)
                close(e)
            }
        }
    }


    companion object {
        @Volatile
        private var INSTANCE: ExpenseRepository? = null

        fun getInstance(context: Context): ExpenseRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: ExpenseRepository(ExpenseDatabase.getDatabase(context)).also { INSTANCE = it }
            }
        }
    }
}
