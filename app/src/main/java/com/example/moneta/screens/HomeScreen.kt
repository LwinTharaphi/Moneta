package com.example.moneta.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController

@Composable
fun HomeScreen(navController: NavController) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(text = "Welcome to your Finance Tracker!", style = MaterialTheme.typography.headlineLarge)

        Spacer(modifier = Modifier.height(20.dp))

        // Button to navigate to Expense Screen
        Button(onClick = { navController.navigate("expense_screen") }) {
            Text("Add Expense")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Button to navigate to Budget Screen
        Button(onClick = { navController.navigate("budget_screen") }) {
            Text("Set Budget")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    val navController = rememberNavController() // Create a NavController for preview
    HomeScreen(navController = navController)
}


