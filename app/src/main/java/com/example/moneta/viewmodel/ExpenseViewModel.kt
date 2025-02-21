package com.example.moneta.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moneta.model.Expense
import com.example.moneta.repository.ExpenseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ExpenseViewModel(private val repository: ExpenseRepository) : ViewModel() {

    // ✅ StateFlow to store list of expenses
    private val _expenses = MutableStateFlow<List<Expense>>(emptyList())
    val expenses: StateFlow<List<Expense>> = _expenses

    // ✅ Loading & Error States
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    // ✅ Fetch Expenses (Real-time updates)
    fun fetchExpenses() {
        _isLoading.value = true
        viewModelScope.launch {
            repository.getExpenses { result ->
                result.onSuccess { expenseList ->
                    _expenses.value = expenseList
                    _isLoading.value = false
                }.onFailure { exception ->
                    _errorMessage.value = exception.localizedMessage
                    _isLoading.value = false
                }
            }
        }
    }

    // ✅ Add Expense
    fun addExpense(expense: Expense) {
        _isLoading.value = true
        viewModelScope.launch {
            val result = repository.addExpense(expense)
            result.onSuccess {
                fetchExpenses() // ✅ Refresh expenses after adding
                _isLoading.value = false
            }.onFailure { exception ->
                _errorMessage.value = exception.localizedMessage
                _isLoading.value = false
            }
        }
    }

    // ✅ Updated Delete Expense (Passes `imageUris` too)
    fun deleteExpense(expense: Expense) {
        _isLoading.value = true
        viewModelScope.launch {
            val result = repository.deleteExpense(expense.id, expense.imageUris) // ✅ Now deletes images too
            result.onSuccess {
                fetchExpenses() // ✅ Refresh after delete
                _isLoading.value = false
            }.onFailure { exception ->
                _errorMessage.value = exception.localizedMessage
                _isLoading.value = false
            }
        }
    }
}