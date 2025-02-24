package com.example.moneta.model

import com.google.firebase.firestore.ServerTimestamp
import java.text.SimpleDateFormat
import java.util.*

enum class ExpenseCategory {
    Dining, Transport, Beverages, Groceries, Entertainment, Shopping, Other
}

// 🔹 The original Expense class (Used inside the app)
data class Expense(
    val id: String = "",  // Firestore auto-generated document ID
    val description: String,
    val amount: Float,
    val date: Date,  // 🔹 Keep Date type for internal use
    val category: ExpenseCategory,  // 🔹 Keep Enum type for internal use
)

// 🔹 FirestoreExpense (Used ONLY for Firestore storage)
data class FirestoreExpense(
    val id: String = "",  // Firestore document ID
    val description: String = "",
    val amount: Float = 0f,
    val date: String = "",  // 🔹 Store as "yyyy-MM-dd" for Firestore
    val category: String = "",  // 🔹 Store as String (Enum name)
    @ServerTimestamp val timestamp: Date? = null
) {
    // Convert FirestoreExpense to Expense (For use in the app)
    fun toExpense(): Expense {
        return Expense(
            id = id,
            description = description,
            amount = amount,
            date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(date) ?: Date(),
            category = ExpenseCategory.valueOf(category), // Convert String to Enum
        )
    }
}

// 🔹 Extension function to convert Expense to FirestoreExpense
fun Expense.toFirestoreExpense(): FirestoreExpense {
    return FirestoreExpense(
        id = id,
        description = description,
        amount = amount,
        date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date), // Convert Date to String
        category = category.name, // Convert Enum to String
        timestamp = Date()
    )
}