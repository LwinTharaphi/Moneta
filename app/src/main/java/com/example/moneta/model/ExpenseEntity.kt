package com.example.moneta.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "expenses")
data class ExpenseEntity(
    @PrimaryKey val id: String, // 🔹 Matches Firestore document ID
    val description: String,
    val amount: Float,
    val date: Long, // 🔹 Store date as timestamp (Room does not support Date directly)
    val category: String // 🔹 Store category as a string
)

// 🔹 Convert Room's `ExpenseEntity` to App's `Expense`
fun ExpenseEntity.toExpense(): Expense {
    return Expense(
        id = id,
        description = description,
        amount = amount,
        date = Date(date), // 🔹 Convert timestamp back to Date
        category = ExpenseCategory.valueOf(category) // 🔹 Convert String to Enum
    )
}

// 🔹 Convert App's `Expense` to Room's `ExpenseEntity`
fun Expense.toExpenseEntity(): ExpenseEntity {
    return ExpenseEntity(
        id = id,
        description = description,
        amount = amount,
        date = date.time, // 🔹 Convert Date to timestamp
        category = category.name // 🔹 Convert Enum to String
    )
}
