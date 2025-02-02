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
fun ExpenseScreen(navController: NavController) {
    var description = ""
    var amount = ""

    Column(modifier = Modifier.padding(16.dp)) {
        Text(text = "Add New Expense", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(20.dp))

        // Expense Description
        TextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Description") }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Expense Amount
        TextField(
            value = amount,
            onValueChange = { amount = it },
            label = { Text("Amount") }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Save Button (just a placeholder)
        Button(onClick = {
            // Placeholder action
            Toast.makeText(navController.context, "Expense Added", Toast.LENGTH_SHORT).show()
            navController.popBackStack() // Navigate back to HomeScreen
        }) {
            Text("Save Expense")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ExpenseScreenPreview() {
    val navController = rememberNavController() // Create a NavController for preview
    ExpenseScreen(navController = navController)
}
