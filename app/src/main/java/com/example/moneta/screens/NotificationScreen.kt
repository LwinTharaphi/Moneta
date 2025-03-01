package com.example.moneta.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.moneta.model.Notification
import com.example.moneta.repository.NotificationRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(navController: NavController) {
    val auth = FirebaseAuth.getInstance()
    val userId = auth.currentUser?.uid
    val notifications = remember { mutableStateOf<List<Notification>>(emptyList()) }
    val coroutineScope = rememberCoroutineScope()

    // Redirect user if not logged in
    if (userId == null) {
        navController.navigate("sign_in_screen") {
            popUpTo(0)
        }
    } else {
        LaunchedEffect(userId) {
            coroutineScope.launch {
                notifications.value = NotificationRepository.getNotifications(userId)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notifications") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (notifications.value.isEmpty()) {
                // Show loading indicator if notifications are not yet loaded
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                // Show the most recent notification at the top
                NotificationCard(notification = notifications.value.first())

                Spacer(modifier = Modifier.height(16.dp)) // Space between the top card and the list

                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(notifications.value.drop(1)) { notification -> // Drop the first item as it's already displayed
                        NotificationCard(notification)
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationCard(notification: Notification) {
    Card(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = notification.title,
                fontSize = MaterialTheme.typography.headlineMedium.fontSize,
                fontWeight = MaterialTheme.typography.headlineMedium.fontWeight
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = notification.body, fontSize = MaterialTheme.typography.bodyLarge.fontSize)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Time: ${notification.timestamp}",
                fontSize = MaterialTheme.typography.bodySmall.fontSize,
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}

@Preview
@Composable
fun NotificationScreenPreview() {
    val navController = rememberNavController()
    NotificationScreen(navController = navController)
}
