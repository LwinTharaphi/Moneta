package com.example.moneta.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moneta.model.Expense
import com.example.moneta.repository.ExpenseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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
     * 🔹 Fetch expenses for the selected date
     */
    fun fetchExpenses(date: Date) {
        viewModelScope.launch {
            repository.getExpensesByDate(date).collect { expensesList ->
                _expenses.value = expensesList
            }
        }
    }

    /**
     * 🔹 Add a new expense
     */
    fun addExpense(expense: Expense) {
        viewModelScope.launch {
            repository.addExpense(expense)
        }
    }

    /**
     * 🔹 Update selected date
     */
    fun updateSelectedDate(newDate: Date) {
        _selectedDate.value = newDate
        fetchExpenses(newDate) // Fetch expenses for the new date
    }

    /**
     * 🔹 Update an expense
     */
    fun updateExpense(expense: Expense) {
        viewModelScope.launch {
            repository.updateExpense(expense)
            fetchExpenses(selectedDate.value) // Refresh the expense list
        }
    }

    /**
     * 🔹 Delete an expense
     */
    fun deleteExpense(expenseId: String) {
        viewModelScope.launch {
            repository.deleteExpense(expenseId)
            fetchExpenses(selectedDate.value) // Refresh the expense list
        }
    }
}
