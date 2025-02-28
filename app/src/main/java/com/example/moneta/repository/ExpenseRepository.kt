package com.example.moneta.repository

import android.util.Log
import android.content.Context
import com.example.moneta.database.ExpenseDao
import com.example.moneta.model.*
import com.example.moneta.database.ExpenseDatabase
import com.example.moneta.model.FirestoreExpense
import com.example.moneta.model.toFirestoreExpense
import com.example.moneta.screens.BudgetData
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
        // Step 2: Extract month and year from the expense date
        val calendar = Calendar.getInstance()
        calendar.time = expense.date
        val expenseYear = calendar.get(Calendar.YEAR)

        val monthNames = arrayOf(
            "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"
        )
        val monthIndex = calendar.get(Calendar.MONTH)
        val expenseMonth = monthNames[monthIndex] // Get the month name

        // Get the year
        val year = calendar.get(Calendar.YEAR)



        // Step 3: Update the budget's used amount
        val budgetRef = db.collection("users")
            .document(userId!!)
            .collection("budgets")
            .whereEqualTo("month", expenseMonth.toString()) // Find the budget for the specific month
            .whereEqualTo("year", expenseYear) // Find the budget for the specific year
            .limit(1) // Assuming there’s only one budget for each month/year

        try {
            val budgetSnapshot = budgetRef.get().await()

            if (budgetSnapshot.isEmpty) {
                // No budget for this month and year, so create a new one
                val newBudget = BudgetData(
                    month = expenseMonth,
                    year = expenseYear,
                    used = expense.amount, // Start with the current expense as the used amount
                    total = 1000.0f // Set an initial total budget amount (replace this with actual logic)
                )
                // Create a new budget document
                db.collection("users")
                    .document(userId!!)
                    .collection("budgets")
                    .add(newBudget)
                    .await()
            } else {
                val currentBudget = budgetSnapshot.documents[0].toObject(BudgetData::class.java)

                if (currentBudget != null) {
                    // Add the expense amount to the current "used" budget amount
                    val updatedUsedAmount = currentBudget.used + expense.amount
                    val updatedBudget = currentBudget.copy(used = updatedUsedAmount)

                    // Save the updated budget back to Firestore
                    budgetSnapshot.documents[0].reference.set(updatedBudget).await()
                }
            }
        } catch (e: Exception) {
            Log.e("FirestoreError", "Error updating budget used amount", e)
        }
    }

    /**
     * 🔹 Update an existing expense in Firestore
     */
    suspend fun updateExpense(expense: Expense) {
        if (userId == null) throw Exception("User not logged in")

        val expenseRef = FirebaseFirestore.getInstance()
            .collection("users")
            .document(userId!!)
            .collection("expenses")
            .document(expense.id)

        val existingExpense = expenseRef.get().await().toObject(FirestoreExpense::class.java)
        val previousAmount = existingExpense?.amount ?: 0f // Get previous amount
        // Convert Expense to FirestoreExpense for Firestore storage
        val updatedExpense = expense.toFirestoreExpense()

        try {
            // Update the expense in Firestore
            expenseRef.set(updatedExpense).await() // This will overwrite the existing document
            println("Expense updated successfully")
            updateBudget(expense, previousAmount)
        } catch (e: Exception) {
            println("Error updating expense: ${e.message}")
            throw e
        }
    }

    suspend fun updateBudget(expense: Expense, previousAmount: Float) {
        if (userId == null) throw Exception("User not logged in")

        val calendar = Calendar.getInstance().apply { time = expense.date }
        val expenseMonth = SimpleDateFormat("MMMM", Locale.getDefault()).format(expense.date)
        val expenseYear = calendar.get(Calendar.YEAR)

        val budgetRef = db.collection("users")
            .document(userId!!)
            .collection("budgets")
            .whereEqualTo("month", expenseMonth)
            .whereEqualTo("year", expenseYear)
            .limit(1)

        try {
            val budgetSnapshot = budgetRef.get().await()
            if (!budgetSnapshot.isEmpty) {
                val budgetDoc = budgetSnapshot.documents[0]
                val currentBudget = budgetDoc.toObject(BudgetData::class.java)

                if (currentBudget != null) {
                    // Adjust used amount by subtracting previous expense amount and adding new amount
                    val newUsedAmount = currentBudget.used - previousAmount + expense.amount
                    val updatedBudget = currentBudget.copy(used = newUsedAmount)

                    // Update Firestore
                    budgetDoc.reference.set(updatedBudget).await()
                    println("Budget updated successfully")
                }
            } else {
                println("No budget found for $expenseMonth $expenseYear")
            }
        } catch (e: Exception) {
            println("Error updating budget: ${e.message}")
            throw e
        }
    }



    /**
     * 🔹 Delete an expense from Firestore
     */
    suspend fun deleteExpense(expenseId: String) {
        if (userId == null) throw Exception("User not logged in")

        try {
            val expenseRef = db.collection("users")
                .document(userId!!)
                .collection("expenses")
                .document(expenseId)

            val expenseSnapshot = expenseRef.get().await()

            if (!expenseSnapshot.exists()) {
                Log.e("FirestoreError", "Expense not found")
                return
            }
            updateBudgetAfterDeletion(expenseId)

            // Delete the expense
            expenseRef.delete().await()
            Log.d("FirestoreSuccess", "Expense deleted successfully")

        } catch (e: Exception) {
            Log.e("FirestoreError", "Error deleting expense: ${e.message}", e)
            throw e
        }
    }


    suspend fun updateBudgetAfterDeletion(expenseId: String) {
        if (userId == null) throw Exception("User not logged in")

        try {
            val expenseRef = db.collection("users")
                .document(userId!!)
                .collection("expenses")
                .document(expenseId)

            val expenseSnapshot = expenseRef.get().await()

            if (!expenseSnapshot.exists()) {
                Log.e("FirestoreError", "Expense not found")
                return
            }

            // Get the expense amount
            val expenseAmount = expenseSnapshot.getDouble("amount") ?: 0.0

            val dateString = expenseSnapshot.getString("date") ?: throw Exception("Expense has no date")
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val expenseDate = dateFormat.parse(dateString) ?: throw Exception("Invalid date format")

            val calendar = Calendar.getInstance().apply { time = expenseDate }
            val expenseYear = calendar.get(Calendar.YEAR)
            val monthNames = arrayOf(
                "January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"
            )
            val expenseMonth = monthNames[calendar.get(Calendar.MONTH)] // Get month name

            // Query the budget for the corresponding month and year
            val budgetQuery = db.collection("users")
                .document(userId!!)
                .collection("budgets")
                .whereEqualTo("month", expenseMonth)
                .whereEqualTo("year", expenseYear)
                .limit(1)

            val budgetSnapshot = budgetQuery.get().await()

            if (!budgetSnapshot.isEmpty) {
                val budgetDoc = budgetSnapshot.documents[0]
                val currentUsedAmount = budgetDoc.getDouble("used") ?: 0.0

                // Update the budget's used amount
                val updatedUsedAmount = (currentUsedAmount - expenseAmount).coerceAtLeast(0.0)

                budgetDoc.reference.update("used", updatedUsedAmount).await()
                Log.d("BudgetUpdate", "Budget updated successfully after deleting expense")
            } else {
                Log.w("BudgetUpdate", "No budget document found for this month/year")
            }

            // Finally, delete the expense
            expenseRef.delete().await()
            Log.d("FirestoreSuccess", "Expense deleted successfully")

        } catch (e: Exception) {
            Log.e("FirestoreError", "Error updating budget after deletion", e)
        }
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
