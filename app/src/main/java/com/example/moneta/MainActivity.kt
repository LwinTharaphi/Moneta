package com.example.moneta

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.NavHost
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.moneta.screens.HomeScreen
import com.example.moneta.screens.ExpenseScreen
import com.example.moneta.screens.BudgetScreen
import com.example.moneta.ui.theme.MonetaTheme

sealed class Screen(val route:String, val title:String, val icon: ImageVector) {
    object Home: Screen("home_screen","Home", Icons.Filled.Home)
    object Expense: Screen("expense_screen","Expenses", Icons.Filled.ShoppingCart)
    object Budget: Screen("budget_screen","Budget", Icons.Filled.Add)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MonetaTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    // Create a NavController to manage navigation
                    val navController = rememberNavController()
                    MainScreen(navController)
                }
            }
        }
    }
}

@Composable
fun MainScreen(navController: NavHostController) {
    val screens = listOf(Screen.Home, Screen.Expense, Screen.Budget)
    var selectedScreen by remember { mutableStateOf(Screen.Home.route) }

    Scaffold (
        bottomBar = {
            BottomNavigationBar(navController,screens,selectedScreen) { newRoute ->
                selectedScreen = newRoute

            }
        }
    ){ paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.fillMaxSize().padding(paddingValues)
        ) {
            composable(Screen.Home.route) { HomeScreen(navController) }
            composable(Screen.Expense.route) { ExpenseScreen(navController) }
            composable(Screen.Budget.route) { BudgetScreen(navController) }
        }

    }
}

@Composable
fun BottomNavigationBar(
    navController: NavHostController,
    screens: List<Screen>,
    selectedScreen: String,
    onItemSelected: (String) -> Unit
) {
    NavigationBar {
        screens.forEach{ screen ->
            NavigationBarItem(
                label = { Text(screen.title) },
                icon = { Icon(screen.icon, contentDescription = screen.title) },
                selected = selectedScreen == screen.route,
                onClick = {
                    navController.navigate(screen.route) {
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                    onItemSelected(screen.route)
                }
            )
        }
    }
}