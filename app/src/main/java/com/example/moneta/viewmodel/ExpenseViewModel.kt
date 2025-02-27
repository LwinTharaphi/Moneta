package com.example.moneta.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moneta.model.Expense
import com.example.moneta.repository.ExpenseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.*

class ExpenseViewModel(private val repository: ExpenseRepository) : ViewModel() {

    private val _expenses = MutableStateFlow<List<Expense>>(emptyList())
    val expenses: StateFlow<List<Expense>> = _expenses

    private val _selectedDate = MutableStateFlow(Date())
    val selectedDate: StateFlow<Date> = _selectedDate

    private val _selectedExpense = MutableStateFlow<Expense?>(null)
    val selectedExpense: StateFlow<Expense?> = _selectedExpense

    fun selectExpense(expense: Expense?) {
        _selectedExpense.value = expense
    }

    /**
     * 🔹 Fetch expenses for the selected date (Uses Room First, Then Firestore)
     */
    fun fetchExpenses(date: Date) {
        viewModelScope.launch {
            repository.getExpensesByDate(date).collectLatest { expensesList ->
                _expenses.value = expensesList
            }
        }
    }

    /**
     * 🔹 Fetch expenses for the selected month (Uses Room First, Then Firestore)
     */
    fun fetchMonthlyExpenses(startDate: String, endDate: String) {
        viewModelScope.launch {
            repository.getMonthlyExpenses(startDate, endDate).collectLatest { expensesList ->
                _expenses.value = expensesList
            }
        }
    }

    /**
     * 🔹 Add a new expense (Firestore only)
     */
    fun addExpense(expense: Expense) {
        viewModelScope.launch {
            repository.addExpense(expense)
            fetchExpenses(selectedDate.value) // Refresh list after adding
        }
    }

    /**
     * 🔹 Update selected date
     */
    fun updateSelectedDate(newDate: Date) {
        _selectedDate.value = newDate
        fetchExpenses(newDate) // 🔹 Fetch expenses for the new date
    }

    /**
     * 🔹 Update an expense (Firestore only)
     */
    fun updateExpense(expense: Expense) {
        viewModelScope.launch {
            repository.updateExpense(expense)
            fetchExpenses(selectedDate.value) // Refresh the expense list
        }
    }

    /**
     * 🔹 Delete an expense (Firestore only)
     */
    fun deleteExpense(expenseId: String) {
        viewModelScope.launch {
            repository.deleteExpense(expenseId)
            fetchExpenses(selectedDate.value) // Refresh the expense list
        }
    }
}

