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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging

@Composable
fun SignUpScreen(navController: NavController) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    val auth = FirebaseAuth.getInstance()

    // Placeholder for real sign up logic
    fun handleSignUp() {
        if (email.isEmpty() || password.isEmpty()) {
            errorMessage = "Please fill in all fields"
        } else {
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        navController.navigate("sign_in_screen")
                        FirebaseMessaging.getInstance().token
                            .addOnCompleteListener { tokenTask ->
                                if (tokenTask.isSuccessful){
                                    val userId = auth.currentUser?.uid
                                    val token = tokenTask.result
                                    Log.d("FCM Token","User Token:$token")
                                    // Store FCM token in Firestore
                                    userId?.let { uid ->
                                        val userRef = FirebaseFirestore.getInstance().collection("users").document(uid)
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
                                    Log.e("FCM Token", "Failed to get FCM token", tokenTask.exception)
                                }
                            }
                    } else {
                        errorMessage = task.exception?.message ?: "Sign up failed"
                    }
                }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Decorated Moneta text box at the top
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
                .padding(bottom = 32.dp),  // Adds space below the Moneta box
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.background)  // cool gray background
                    .padding(vertical = 16.dp, horizontal = 32.dp)
            ) {
                Text(
                    text = "Moneta",
                    style = TextStyle(
                        fontSize = 56.sp, // Font size
                        color = Color(0xFF10B981), // Emerald Green
                        letterSpacing = 2.sp
                    )
                )
            }
        }

        Text("Sign Up", style = MaterialTheme.typography.headlineSmall)

        Spacer(modifier = Modifier.height(16.dp))

        // Username input field
        TextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Password input field
        TextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Confirm password input field
        TextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text("Confirm Password") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Sign-up button
        Button(
            onClick = { handleSignUp() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Sign Up")
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Show error message if there is any
        if (errorMessage.isNotEmpty()) {
            Text(text = errorMessage, color = Color(0xFFEF4444))
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Link to sign-in screen
        TextButton(
            onClick = { navController.navigate("sign_in_screen") }
        ) {
            Text(
                text = "Already have an account? Sign In",
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
