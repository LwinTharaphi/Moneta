package com.example.moneta.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.moneta.repository.ExpenseRepository

class ExpenseViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ExpenseViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ExpenseViewModel(ExpenseRepository.getInstance(context)) as T // 🔹 Pass context
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

