package com.example.moneta.screens

import android.content.res.Resources.Theme
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController

@Composable
fun ProfileScreen(navController: NavController, isDarkTheme: Boolean, onThemeToggle: (Boolean)-> Unit){
    Column (
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        Text("Profile", fontSize = 24.sp, style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(20.dp))
        Text("UserName: ", fontSize = 18.sp, style = MaterialTheme.typography.bodyLarge)

        Spacer(modifier = Modifier.height(20.dp))

        Row (
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ){
            Text("Dark Mode", fontSize = 16.sp)
            Switch(checked = isDarkTheme, onCheckedChange = onThemeToggle)
        }

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .clickable {navController.navigate("reminder_screen")}
                .padding(vertical = 8.dp)
        ) {
            Text("Reminders", fontSize = 16.sp)
            Icon(imageVector = Icons.Default.KeyboardArrowRight, contentDescription = "Go to Reminders")
        }
    }
}

@Preview
@Composable
fun ProfileScreenPreview(){
    val navController = rememberNavController()
    ProfileScreen(navController = navController, isDarkTheme = true, onThemeToggle = {})
}