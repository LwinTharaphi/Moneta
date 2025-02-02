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
fun BudgetScreen(navController: NavController) {
    var budgetAmount = ""

    Column(modifier = Modifier.padding(16.dp)) {
        Text(text = "Set Your Budget", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(20.dp))

        // Budget Amount
        TextField(
            value = budgetAmount,
            onValueChange = { budgetAmount = it },
            label = { Text("Budget Limit") }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Save Button (just a placeholder)
        Button(onClick = {
            // Placeholder action
            Toast.makeText(navController.context, "Budget Set", Toast.LENGTH_SHORT).show()
            navController.popBackStack() // Navigate back to HomeScreen
        }) {
            Text("Save Budget")
        }
    }
}


@Preview(showBackground = true)
@Composable
fun BudgetScreenPreview() {
    val navController = rememberNavController() // Create a NavController for preview
    BudgetScreen(navController = navController)
}
