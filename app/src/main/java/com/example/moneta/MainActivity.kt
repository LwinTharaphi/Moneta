package com.example.moneta

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHost
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.moneta.screens.HomeScreen
import com.example.moneta.screens.ExpenseScreen
import com.example.moneta.screens.BudgetScreen
import com.example.moneta.ui.theme.MonetaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MonetaTheme {
                Surface(modifier = androidx.compose.ui.Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    // Create a NavController to manage navigation
                    val navController = rememberNavController()
                    NavHost(navController = navController, startDestination = "home_screen") {
                        // Define your composable routes
                        composable("home_screen") { HomeScreen(navController) }
                        composable("expense_screen") { ExpenseScreen(navController) }
                        composable("budget_screen") { BudgetScreen(navController) }
                    }
                }
            }
        }
    }
}
