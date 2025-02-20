package com.example.moneta.model

import java.util.Date

enum class ExpenseCategory {
    Dining, Transport, Beverages, Groceries, Entertainment, Shopping, Other
}

data class Expense(
    val id: String = "",  // Firestore auto-generated document ID
    val description: String,
    val amount: Float,
    val date: Date,
    val category: ExpenseCategory, // Now using ENUM instead of String
    val imageUris: List<String> = emptyList()
//    val imageUri: String? = null // ✅ New field for storing the image URI
)
