package com.example.moneta.screens

import android.graphics.drawable.Icon
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Face
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.moneta.database.ReminderDatabase
import com.example.moneta.model.Reminder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

@Composable
fun ReminderScreen(navController: NavController){
    val auth = FirebaseAuth.getInstance()
    val userId = auth.currentUser?.uid
    val reminders = remember { mutableStateOf<List<Reminder>>(emptyList()) }
    val coroutineScope = rememberCoroutineScope()
    val db = FirebaseFirestore.getInstance()
    if (userId == null){
        navController.navigate("sign_in_screen") {
            popUpTo(0)
        }
    } else {
        LaunchedEffect(userId) {
            coroutineScope.launch {
                val fetchedReminders = ReminderDatabase.getReminder(userId)
                reminders.value = fetchedReminders
            }
        }
    }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row (
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ){
                IconButton(onClick = { navController.popBackStack()}) {
                    Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "Back")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text("Reminders", fontSize = 24.sp, style = MaterialTheme.typography.headlineMedium)
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Reminder",
                    modifier = Modifier
                        .clickable{ navController.navigate("add_reminder_screen")}
                        .padding(8.dp)
                    )
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (reminders.value.isEmpty()){
                Text("No reminders yet", fontSize = 16.sp, color = Color.Gray)
            } else {
                LazyColumn {
                    items(reminders.value) { reminder ->
                        ReminderCard(reminder,navController)
                    }
                }
            }

        }
    }
}

@Preview
@Composable
fun ReminderScreenPreview(){
    val navController = rememberNavController()
    ReminderScreen(navController)
}

@Composable
fun ReminderCard(reminder: Reminder, navController: NavController){
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable {
                navController.navigate("edit_reminder_screen/${reminder.id}")
            },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ){
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start,
                modifier = Modifier.fillMaxWidth()
            ){
                Icon(imageVector = Icons.Filled.Face, contentDescription = "Icon")
                Text(reminder.name, fontSize = 16.sp, fontWeight =  FontWeight.Bold)
            }
            Text("Time to record your accounts", fontSize = 12.sp)
            Text("Record one now", fontSize = 12.sp)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ){
                Text(reminder.time, fontSize = 12.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(reminder.repeat, fontSize = 12.sp)
            }
        }
    }
}