package com.example.moneta.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

@Composable
fun ProfileScreen(navController: NavController, isDarkTheme: Boolean, onThemeToggle: (Boolean) -> Unit) {
    val auth = FirebaseAuth.getInstance()
    var currentUser by remember { mutableStateOf<FirebaseUser?>(auth.currentUser) }

    // Reload user info when the screen is recomposed
    LaunchedEffect(Unit) {
        auth.currentUser?.reload()?.addOnCompleteListener {
            currentUser = auth.currentUser
        }
    }

    var userName by remember { mutableStateOf(currentUser?.displayName ?: "User") }
    var userEmail by remember { mutableStateOf(currentUser?.email ?: "No email available") }
    var userPhotoUrl by remember { mutableStateOf(currentUser?.photoUrl?.toString()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Profile", fontSize = 24.sp, fontWeight = FontWeight.Bold)

        Spacer(modifier = Modifier.height(20.dp))

        // Profile Picture
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(Color.Gray),
            contentAlignment = Alignment.Center
        ) {
            if (!userPhotoUrl.isNullOrEmpty()) {
                Image(
                    painter = rememberAsyncImagePainter(userPhotoUrl),
                    contentDescription = "Profile Picture",
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Text(userName.firstOrNull()?.toString() ?: "U", fontSize = 32.sp, color = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // User Details
        Text(userName, fontSize = 20.sp, fontWeight = FontWeight.Medium)
        Text(userEmail, fontSize = 16.sp, color = Color.Gray)

        Spacer(modifier = Modifier.height(20.dp))

        // Edit Profile Button
        OutlinedButton(onClick = { navController.navigate("edit_profile_screen") }) {
            Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit Profile")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Edit Profile")
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Dark Mode Toggle
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Dark Mode", fontSize = 16.sp)
            Switch(
                checked = isDarkTheme,
                onCheckedChange = { onThemeToggle(!isDarkTheme) }
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Reminders Option
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { navController.navigate("reminder_screen") }
                .padding(vertical = 8.dp)
        ) {
            Text("Reminders", fontSize = 16.sp)
            Icon(imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Go to Reminders")
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Sign Out Button
        Button(
            onClick = {
                auth.signOut()
                navController.navigate("sign_in_screen")
            },
            colors = ButtonDefaults.buttonColors(Color(0xFFEF4444)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(imageVector = Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Sign Out")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Sign Out")
        }
    }
}

@Preview
@Composable
fun ProfileScreenPreview() {
    val navController = rememberNavController()
    ProfileScreen(navController = navController, isDarkTheme = false, onThemeToggle = {})
}
