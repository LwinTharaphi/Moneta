package com.example.moneta.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController

@Composable
fun ProfileScreen(navController: NavController, isDarkTheme: Boolean, onThemeToggle: (Boolean) -> Unit) {
    val userEmail = "johndoe@example.com"
    var userName = "Joh Doe"
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Profile", fontSize = 24.sp, style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(20.dp))
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(Color.Gray),
            contentAlignment = Alignment.Center
        ){
            Text(userName.first().toString(), fontSize = 32.sp)
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text(userName, fontSize = 18.sp, style = MaterialTheme.typography.bodyLarge)
        Text(userEmail, fontSize = 16.sp)

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedButton(onClick = {navController.navigate("edit_profile_screen")}) {
            Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit Profile")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Edit Profile")
        }

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Dark Mode", fontSize = 16.sp)
            Switch(
                checked = isDarkTheme,
                onCheckedChange = { onThemeToggle(!isDarkTheme) } // Update theme state
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { navController.navigate("reminder_screen") }
                .padding(vertical = 8.dp)
        ) {
            Text("Reminders", fontSize = 16.sp)
            Icon(imageVector = Icons.Default.KeyboardArrowRight, contentDescription = "Go to Reminders")
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = { navController.navigate("sign_in_screen")},
            colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
            modifier = Modifier.fillMaxWidth()
        ){
            Icon(imageVector = Icons.Default.ExitToApp, contentDescription = "sign Out")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Sign Out")
        }
    }
}

@Preview
@Composable
fun ProfileScreenPreview() {
    val navController = rememberNavController()
    ProfileScreen(navController = navController, isDarkTheme = true, onThemeToggle = {})
}
