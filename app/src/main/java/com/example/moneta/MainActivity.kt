package com.example.moneta

import EditReminderScreen
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.moneta.screens.SignInScreen
import com.example.moneta.screens.SignUpScreen
import com.example.moneta.screens.*
import com.example.moneta.ui.theme.MonetaTheme
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings

sealed class Screen(val route: String, val title: String, val icon: ImageVector?) {
    object SignIn: Screen("sign_in_screen", "Sign In", null)
    object SignUp: Screen("sign_up_screen", "Sign Up", null)
    object Report : Screen("report_screen", "Report", Icons.Filled.Assessment)
    object Expense: Screen("expense_screen", "Expenses", Icons.Filled.ShoppingCart)
    object Budget: Screen("budget_screen", "Budget", Icons.Filled.AttachMoney)
    object Notification: Screen("notification_screen", "Notification", Icons.Filled.Notifications)
    object Profile: Screen("profile_screen", "Profile", Icons.Filled.Person)
    object Reminder: Screen("reminder_screen", "Reminder",
        Icons.AutoMirrored.Filled.KeyboardArrowRight
    )
    object AddReminder: Screen("add_reminder_screen", "Add Reminder", Icons.Default.Add)
    object EditProfile: Screen("edit_profile_screen","Edit Profile", Icons.Default.Edit)
    object FinancialNews: Screen("financial_news_screen", "Financial News", Icons.Filled.Public) // Added
    object EditReminderScreen: Screen("edit_reminder_screen/{reminderId}", "Edit Reminder", Icons.Default.Edit)
}

class MainActivity : ComponentActivity() {
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                Log.d("FCM","Notification permission granted")
            } else {
                Log.d("FCM", "Notification permission denied")
            }
        }
        requestNotificationPermission()
        requestStoragePermission()



        // Initialize Firebase
        FirebaseApp.initializeApp(this)

        // Enable Firebase Authentication offline persistence
        FirebaseAuth.getInstance().useAppLanguage()

        // Enable Firestore offline persistence if you're using Firestore
        FirebaseFirestore.getInstance().firestoreSettings = FirebaseFirestoreSettings.Builder()
            .build()

        setContent {
            val auth = FirebaseAuth.getInstance()
            val navController = rememberNavController()
            var isDarkTheme by remember { mutableStateOf(false) }

            LaunchedEffect(auth.currentUser) {
                if (auth.currentUser != null) {
                    navController.navigate(Screen.Expense.route) {
                        popUpTo(0) // Clear back stack
                    }
                } else {
                    navController.navigate(Screen.SignIn.route) {
                        popUpTo(0)
                    }
                }
            }

            MonetaTheme(darkTheme = isDarkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen(navController, isDarkTheme, onThemeToggle = { isDarkTheme = it })
                }
            }
        }
    }
    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permission = Manifest.permission.POST_NOTIFICATIONS
            if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
                Log.d("FCM", "Notification permission already granted")
            } else {
                requestPermissionLauncher.launch(permission)
            }
        }
    }
    private fun requestStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // Android 13+
            val permission = Manifest.permission.READ_MEDIA_IMAGES
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(permission)
            }
        }
    }
}


@Composable
fun MainScreen(
    navController: NavHostController,
    isDarkTheme: Boolean,
    onThemeToggle: (Boolean) -> Unit // Callback to toggle theme
) {
    val screens = listOf(Screen.Expense, Screen.Report, Screen.Budget, Screen.Profile, Screen.FinancialNews)
    var selectedScreen by remember { mutableStateOf(Screen.Expense.route) }
    val reminders = remember { mutableStateOf<List<Triple<String, String, String>>>(emptyList()) }

    Scaffold(
        bottomBar = {
            // Hide the bottom navigation on SignIn or SignUp screens
            if (selectedScreen != Screen.Notification.route && selectedScreen != Screen.SignIn.route && selectedScreen != Screen.SignUp.route) {
                BottomNavigationBar(navController, screens, selectedScreen) { newRoute ->
                    selectedScreen = newRoute
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Screen.SignIn.route, // Start with SignIn screen
            modifier = Modifier.fillMaxSize().padding(paddingValues)
        ) {
            composable(Screen.SignIn.route) {
                selectedScreen = Screen.SignIn.route
                SignInScreen(navController) // SignIn Screen
            }
            composable(Screen.SignUp.route) {
                selectedScreen = Screen.SignUp.route
                SignUpScreen(navController) // SignUp Screen
            }
            composable(Screen.Expense.route) {
                selectedScreen = Screen.Expense.route
                ExpenseScreen(navController) // Expense Screen
            }
            composable(Screen.Report.route) {
                selectedScreen = Screen.Report.route
                ReportScreen(navController) // Expense Screen
            }
            composable(Screen.Budget.route) {
                selectedScreen = Screen.Budget.route
                BudgetScreen(navController) // Budget Screen
            }
            composable(Screen.Notification.route) {
                selectedScreen = Screen.Notification.route
                NotificationScreen(navController) // Notification Screen
            }
            composable(Screen.Profile.route) {
                selectedScreen = Screen.Profile.route
                ProfileScreen(
                    navController = navController,
                    isDarkTheme = isDarkTheme, // Pass the correct theme state
                    onThemeToggle = onThemeToggle // Pass the callback to toggle theme
                ) // Profile Screen
            }
            composable(Screen.Reminder.route) {
                selectedScreen = Screen.Reminder.route
                ReminderScreen(navController) // Reminder Screen
            }
            composable(Screen.AddReminder.route) {
                selectedScreen = Screen.AddReminder.route
                AddReminderScreen(navController) // Add Reminder Screen
            }
            composable(Screen.EditProfile.route){
                selectedScreen = Screen.EditProfile.route
                EditProfileScreen(navController)
            }
            composable(Screen.FinancialNews.route) {
                selectedScreen = Screen.FinancialNews.route
                FinancialNewsScreen(navController) // Financial News Screen
            }
            composable(
                route = "edit_reminder_screen/{reminderId}",
                arguments = listOf(navArgument("reminderId") { type = NavType.StringType })
            ) { backStackEntry ->
                selectedScreen = Screen.EditReminderScreen.route
                val reminderId = backStackEntry.arguments?.getString("reminderId") ?: ""
                EditReminderScreen(navController, reminderId)
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
        screens.forEach { screen ->
            // Check if the screen should be displayed (don't display on SignIn/SignUp)
            if (screen.route != Screen.SignIn.route && screen.route != Screen.SignUp.route) {
                NavigationBarItem(
                    label = { Text(screen.title) },
                    icon = { screen.icon?.let { Icon(it, contentDescription = screen.title) } },
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
}
