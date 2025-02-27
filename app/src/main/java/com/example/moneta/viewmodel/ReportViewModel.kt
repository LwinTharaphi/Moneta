package com.example.moneta.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moneta.model.Expense
import com.example.moneta.repository.ExpenseRepository
import com.example.moneta.screens.Category
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class ReportViewModel(private val repository: ExpenseRepository) : ViewModel() {

    // 🔹 Store the selected month (1-12) and year (e.g., 2025)
    private val _selectedMonth = MutableStateFlow(Calendar.getInstance().get(Calendar.MONTH) + 1) // 1-12
    private val _selectedYear = MutableStateFlow(Calendar.getInstance().get(Calendar.YEAR)) // 2025
    val selectedMonth: StateFlow<Int> = _selectedMonth
    val selectedYear: StateFlow<Int> = _selectedYear

    // 🔹 Store fetched expenses for the selected month
    private val _monthlyExpenses = MutableStateFlow<List<Expense>>(emptyList())
    val monthlyExpenses: StateFlow<List<Expense>> = _monthlyExpenses

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    init {
        fetchMonthlyExpenses() // Fetch expenses when ViewModel is created
    }

    /**
     * 🔹 Update the selected month & year when user selects a new month.
     */
    fun updateSelectedMonth(month: Int, year: Int) {
        _selectedMonth.value = month
        _selectedYear.value = year
        fetchMonthlyExpenses() // 🔹 Fetch expenses when month/year changes
    }

    fun processCategoryData(expenses: List<Expense>): List<Category> {
        val categoryTotals = mutableMapOf<String, Double>()

        // 🔹 Group expenses by category
        for (expense in expenses) {
            categoryTotals[expense.category.name] =
                categoryTotals.getOrDefault(expense.category.name, 0.0) + expense.amount
        }

        // 🔹 Convert map into list of Category objects
        return categoryTotals.map { (name, amount) -> Category(name, amount) }
    }


    /**
     * 🔹 Fetch expenses for the selected month from Firestore
     */
    private fun fetchMonthlyExpenses() {
        viewModelScope.launch {
            val month = _selectedMonth.value
            val year = _selectedYear.value

            // Format first and last date of the month for Firestore query
            val startDate = "%04d-%02d-01".format(year, month)  // e.g., "2025-02-01"
            val endDate = "%04d-%02d-31".format(year, month)  // e.g., "2025-02-31"

            repository.getMonthlyExpenses(startDate, endDate).collect { expenses ->
                _monthlyExpenses.value = expenses // 🔹 Store fetched expenses
            }
        }
    }
}
