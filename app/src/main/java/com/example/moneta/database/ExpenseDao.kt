package com.example.moneta.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.moneta.model.ExpenseEntity

@Dao
interface ExpenseDao {

    // 🔹 Fetch all expenses for a specific date
    @Query("SELECT * FROM expenses WHERE date BETWEEN :startOfDay AND :endOfDay ORDER BY date DESC")
    suspend fun getExpensesByDate(startOfDay: Long, endOfDay: Long): List<ExpenseEntity>

    // 🔹 Fetch all expenses for a specific month
    @Query("SELECT * FROM expenses WHERE date BETWEEN :startOfMonth AND :endOfMonth ORDER BY date DESC")
    suspend fun getExpensesByMonth(startOfMonth: Long, endOfMonth: Long): List<ExpenseEntity>

    // 🔹 Insert or update expenses
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpenses(expenses: List<ExpenseEntity>)

    // 🔹 Clear all stored expenses (useful for syncing Firestore updates)
    @Query("DELETE FROM expenses")
    suspend fun clearAllExpenses()
}
