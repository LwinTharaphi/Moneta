package com.example.moneta

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
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
import com.example.moneta.screens.AddReminderScreen
import com.example.moneta.screens.HomeScreen
import com.example.moneta.screens.ExpenseScreen
import com.example.moneta.screens.BudgetScreen
import com.example.moneta.screens.NotificationScreen
import com.example.moneta.screens.ProfileScreen
import com.example.moneta.screens.ReminderScreen
import com.example.moneta.ui.theme.MonetaTheme

sealed class Screen(val route:String, val title:String, val icon: ImageVector) {
    object Home: Screen("home_screen","Home", Icons.Filled.Home)
    object Expense: Screen("expense_screen","Expenses", Icons.Filled.ShoppingCart)
    object Budget: Screen("budget_screen","Budget", Icons.Filled.Add)
    object Notification: Screen("notification_screen","Notification", Icons.Filled.Notifications)
    object Profile: Screen("profile_screen","Profile",Icons.Filled.Person)
    object Reminder: Screen("reminder_screen","Reminder",Icons.Default.KeyboardArrowRight)
    object AddReminder: Screen("add_reminder_screen","AddReminder",Icons.Default.Add)
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
    val screens = listOf(Screen.Home, Screen.Expense, Screen.Budget, Screen.Profile)
    var selectedScreen by remember { mutableStateOf(Screen.Home.route) }
    val reminders = remember { mutableStateOf<List<Triple<String, String, String>>>(emptyList()) }

    Scaffold (
        bottomBar = {
            if (selectedScreen != Screen.Notification.route){
                BottomNavigationBar(navController,screens,selectedScreen) { newRoute ->
                    selectedScreen = newRoute

                }
            }
        }
    ){ paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.fillMaxSize().padding(paddingValues)
        ) {
            composable(Screen.Home.route) {
                selectedScreen = Screen.Home.route
                HomeScreen(navController)
            }
            composable(Screen.Expense.route) {
                selectedScreen = Screen.Expense.route
                ExpenseScreen(navController)
            }
            composable(Screen.Budget.route) {
                selectedScreen = Screen.Budget.route
                BudgetScreen(navController)
            }
            composable(Screen.Notification.route) {
                selectedScreen = Screen.Notification.route
                NotificationScreen(navController)
            }
            composable(Screen.Profile.route) {
                selectedScreen = Screen.Profile.route
                ProfileScreen(navController, isDarkTheme = true, onThemeToggle = {})
            }
            composable(Screen.Reminder.route) {
                selectedScreen = Screen.Reminder.route
                ReminderScreen(navController, reminders)
            }
            composable(Screen.AddReminder.route){
                selectedScreen = Screen.AddReminder.route
                AddReminderScreen(navController,reminders)
            }
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