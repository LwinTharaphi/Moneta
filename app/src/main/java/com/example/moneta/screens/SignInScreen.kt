package com.example.authapp

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
fun SignInScreen(navController: NavController) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    // This is a placeholder for the login process (should be replaced with real authentication)
    fun handleSignIn() {
        if (username.isEmpty() || password.isEmpty()) {
            errorMessage = "Please fill in both fields"
        } else {
            // Navigate to the home screen
            navController.navigate("home_screen")
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center // Align elements at the top
    ) {
        // "Moneta" Box with pink background and rounded corners
        Box(
            modifier = Modifier
                .background(Color(0xFFFFC0CB))  // Pale pink background
                .padding(vertical = 16.dp, horizontal = 32.dp)
        )
        {
            Text(
                text = "Moneta",
                style = TextStyle(
                    fontSize = 32.sp, // Font size for "Moneta"
                    color = Color.White, // White text color
                    letterSpacing = 2.sp
                )
            )
        }

        Spacer(modifier = Modifier.height(32.dp)) // Spacer to separate from the input fields

        Text("Sign In", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Username") },
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
            Text(text = errorMessage, color = Color.Red)
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = {
            // Navigate to SignUp Screen
            navController.navigate("sign_up_screen")
        }) {
            Text("Don't have an account? Sign Up")
        }
    }
}
