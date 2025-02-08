package com.example.moneta.screens

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
