package com.example.moneta.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.google.firebase.messaging.FirebaseMessaging

@Composable
fun SignInScreen(navController: NavController) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    val auth = FirebaseAuth.getInstance()

    // This is a placeholder for the login process (should be replaced with real authentication)
    fun handleSignIn() {
        if (email.isEmpty() || password.isEmpty()) {
            errorMessage = "Please fill in both fields"
        } else {
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val userId = auth.currentUser?.uid
                        FirebaseMessaging.getInstance().token
                            .addOnCompleteListener { tokenTask ->
                                if (tokenTask.isSuccessful){
                                    val token = tokenTask.result
                                    Log.d("FCM token","User Token: $token")
                                    // Store FCM token in Firestore
                                    userId?.let { uid ->
                                        val userRef = Firebase.firestore.collection("users").document(uid)
                                        // Check if document exists before updating
                                        userRef.get()
                                            .addOnSuccessListener { document ->
                                                if (document.exists()) {
                                                    // Document exists, update the FCM token
                                                    userRef.update("fcmToken", token)
                                                        .addOnSuccessListener {
                                                            Log.d("FCM", "FCM token updated successfully")
                                                        }
                                                        .addOnFailureListener { e ->
                                                            Log.e("FCM", "Error updating FCM token", e)
                                                        }
                                                } else {
                                                    // Document does not exist, create a new one
                                                    val newUser = hashMapOf("fcmToken" to token)
                                                    userRef.set(newUser)
                                                        .addOnSuccessListener {
                                                            Log.d("FCM", "FCM token added to new user document")
                                                        }
                                                        .addOnFailureListener { e ->
                                                            Log.e("FCM", "Error creating new user document", e)
                                                        }
                                                }
                                            }
                                            .addOnFailureListener { e ->
                                                Log.e("FCM", "Error updating FCM token", e)
                                            }
                                    }
                                } else {
                                    Log.e("FCM token", "Failed to get FCM token", tokenTask.exception)
                                }
                            }
                        navController.navigate("expense_screen")
                    } else {
                        errorMessage = task.exception?.message ?: "Sign in failed"
                    }
                }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)  // cool gray background
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center // Align elements at the top
    ) {
        // "Moneta" Box with pink background and rounded corners
        Box(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)  // cool gray background
                .padding(vertical = 16.dp, horizontal = 32.dp)
        )
        {
            Text(
                text = "Moneta",
                style = TextStyle(
                    fontSize = 56.sp, // Font size for "Moneta"
                    color = Color(0xFF10B981), // Emerald Green
                    letterSpacing = 2.sp
                )
            )
        }

        Spacer(modifier = Modifier.height(32.dp)) // Spacer to separate from the input fields

        Text("Sign In", style = MaterialTheme.typography.headlineSmall)

        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { handleSignIn() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Sign In")
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (errorMessage.isNotEmpty()) {
            Text(text = errorMessage, color = Color(0xFFEF4444))
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(
            onClick = { navController.navigate("sign_up_screen") }
        ) {
            Text(
                text = "Don't have an account? Sign Up",
                style = TextStyle(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary,
                    textDecoration = TextDecoration.Underline
                )
            )
        }
    }
}
