package com.example.moneta.repository

import com.example.moneta.model.Expense
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID

class ExpenseRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance().reference
    private val expensesCollection = firestore.collection("expenses") // ✅ Firestore reference

    private val _expenses = MutableStateFlow<List<Expense>>(emptyList())
    val expenses: StateFlow<List<Expense>> = _expenses // ✅ StateFlow to expose expenses

    // ✅ Function to fetch expenses in real-time
    fun getExpenses(onResult: (Result<List<Expense>>) -> Unit) {
        expensesCollection.addSnapshotListener { snapshot, exception ->
            if (exception != null) {
                onResult(Result.failure(exception))
                return@addSnapshotListener
            }

            val expenseList = snapshot?.documents?.mapNotNull { it.toObject<Expense>() } ?: emptyList()
            _expenses.value = expenseList // ✅ Update StateFlow
            onResult(Result.success(expenseList))
        }
    }

    // ✅ Function to add an expense (Uploads images first)
    suspend fun addExpense(expense: Expense): Result<String> {
        return try {
            // ✅ Upload images to Firebase Storage
            val uploadedImageUrls = expense.imageUris.mapNotNull { uploadImageToStorage(it) }

            // ✅ Create a new expense with image URLs
            val newExpense = expense.copy(
                id = expensesCollection.document().id, // Generate Firestore document ID
                imageUris = uploadedImageUrls // ✅ Store uploaded image URLs
            )

            // ✅ Save the expense to Firestore
            expensesCollection.document(newExpense.id).set(newExpense).await()

            Result.success("Expense added successfully!")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ✅ Function to upload an image to Firebase Storage
    private suspend fun uploadImageToStorage(imageUri: String): String? {
        return try {
            val imageRef = storage.child("expense_images/${UUID.randomUUID()}.jpg") // Unique name for image
            val uploadTask = imageRef.putFile(android.net.Uri.parse(imageUri)).await()
            imageRef.downloadUrl.await().toString() // ✅ Return download URL
        } catch (e: Exception) {
            null // ✅ If upload fails, return null
        }
    }

    // ✅ Function to delete an expense from Firestore & Storage
    suspend fun deleteExpense(expenseId: String, imageUris: List<String>): Result<String> {
        return try {
            // ✅ Delete expense document from Firestore
            expensesCollection.document(expenseId).delete().await()

            // ✅ Delete images from Firebase Storage
            imageUris.forEach { imageUrl ->
                deleteImageFromStorage(imageUrl)
            }

            Result.success("Expense deleted successfully!")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ✅ Function to delete an image from Firebase Storage
    private suspend fun deleteImageFromStorage(imageUrl: String) {
        try {
            val imageRef = FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl)
            imageRef.delete().await()
        } catch (e: Exception) {
            // Ignore if image deletion fails (Maybe already deleted)
        }
    }
}
