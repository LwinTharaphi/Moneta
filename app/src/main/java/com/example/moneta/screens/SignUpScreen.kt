package com.example.authapp

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun SignUpScreen(navController: NavController) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    // Placeholder for real sign up logic
    fun handleSignUp() {
        if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            errorMessage = "Please fill in all fields"
        } else if (password != confirmPassword) {
            errorMessage = "Passwords do not match"
        } else {
            // Proceed with the sign-up process, for now, navigate to the home screen
            navController.navigate("home_screen")
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Decorated Moneta text box at the top
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp),  // Adds space below the Moneta box
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .background(Color(0xFFFFC0CB))  // Pink background (light pink color)
                    .padding(vertical = 16.dp, horizontal = 32.dp)
            ) {
                Text(
                    text = "Moneta",
                    style = TextStyle(
                        fontSize = 32.sp, // Font size
                        color = Color.White, // White text color
                        letterSpacing = 2.sp
                    )
                )
        }
        }

        Text("Sign Up", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(16.dp))

        // Username input field
        TextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Username") },
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
            Text(text = errorMessage, color = Color.Red)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Link to sign-in screen
        TextButton(onClick = {
            // Navigate to SignIn Screen
            navController.navigate("sign_in_screen")
        }) {
            Text("Already have an account? Sign In")
        }
    }
}
