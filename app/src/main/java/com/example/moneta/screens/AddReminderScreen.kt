package com.example.moneta.screens

import android.app.TimePickerDialog
import android.widget.TimePicker
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.*
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.moneta.database.ReminderDatabase
import com.example.moneta.model.Reminder
import com.example.moneta.utils.FCMHelper
import com.example.moneta.utils.NotificationScheduler
import com.example.moneta.utils.NotificationScheduler.calculateTimeInMillis
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.UUID
import kotlin.coroutines.coroutineContext

@Composable
fun AddReminderScreen(navController: NavController){
    var reminderName by remember { mutableStateOf("") }
    var selectedTime by remember { mutableStateOf("8:00 PM") }
    var repeatOption by remember { mutableStateOf("Daily") }
    var showDialog by remember { mutableStateOf(false) }
    var showRepeatDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val calendar = Calendar.getInstance()
    val coroutineScope = rememberCoroutineScope()
    val userId = FirebaseAuth.getInstance().currentUser?.uid

    val timePickerDialog = TimePickerDialog(
        context,
        { _: TimePicker, hour: Int, minute: Int ->
            val amPm = if (hour<12) "AM" else "PM"
            selectedTime = String.format("%02d:%02d %s", hour % 12, minute, amPm)

        },
        calendar.get(Calendar.HOUR_OF_DAY),
        calendar.get(Calendar.MINUTE),
        false
    )

    if (showDialog){
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Error") },
            text = { Text("Reminder name cannot be empty") },
            confirmButton = {
                TextButton(onClick = { showDialog = false}) {
                    Text("Ok")
                }
            }
        )
    }

    if (showRepeatDialog){
        AlertDialog(
            onDismissRequest = { showRepeatDialog = false },
            title = { Text("Select Repeat Option") },
            text = {
                Column {
                    listOf("Daily","Weekly","Monthly","Yearly").forEach { option ->
                        Text(
                            text = option,
                            fontSize = 16.sp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                                .clickable {
                                    repeatOption = option
                                    showRepeatDialog = false
                                }
                            )
                    }
                }
            },
            confirmButton = {}
        )
    }

    Column (
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        Row (
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ){
            IconButton(onClick = { navController.popBackStack()}) {
                Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "Back")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text("Add Reminder", fontSize = 24.sp, style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = {
                    if (reminderName.isEmpty()) {
                        showDialog = true
                    } else {
                        val newReminder = Reminder(UUID.randomUUID().toString(),reminderName,selectedTime,repeatOption)
                        coroutineScope.launch {
                            ReminderDatabase.addReminder(newReminder,userId.toString())
                        }
                        FCMHelper.sendReminderNotification(context,reminderName,"Time to record your accounts")
                        val timeInMillis = calculateTimeInMillis(selectedTime)
                        NotificationScheduler.scheduleReminder(
                            context,
                            timeInMillis,
                            reminderName,
                            "Time to record your accounts"
                        )

                        navController.popBackStack()
                    }
                }
            ) {
                Icon(imageVector = Icons.Default.Check, contentDescription = "Add reminder")
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row (
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ){
                    Text("Name", fontSize = 16.sp)
                    Spacer(modifier = Modifier.weight(1f))
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextField(
                            value = reminderName,
                            onValueChange = { reminderName = it},
                            label = { Text("Enter Name") },
                            modifier = Modifier.width(180.dp)
                        )
                        Icon(imageVector = Icons.Default.Edit, contentDescription = "Choose")

                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row (
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ){
                    Text("Reminder Time ", fontSize = 16.sp)
                    Spacer(modifier = Modifier.weight(1f))
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ){
                        Text(selectedTime, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
                        IconButton(onClick = { timePickerDialog.show() }) {
                            Icon(imageVector = Icons.Default.KeyboardArrowRight, contentDescription = "Chosse")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Repeat", fontSize = 16.sp)
                    Spacer(modifier = Modifier.weight(1f))
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(repeatOption, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
                        IconButton(onClick = {showRepeatDialog = true}) {
                            Icon(imageVector = Icons.Default.KeyboardArrowRight, contentDescription = "Chosse")
                        }
                    }
                }
            }
        }
//        Spacer(modifier = Modifier.height(20.dp))
//
//        Card(
//            modifier = Modifier.fillMaxWidth(),
//            shape = RoundedCornerShape(12.dp),
//            elevation = CardDefaults.cardElevation(4.dp)
//        ) {
//            Column(
//                modifier = Modifier.padding(16.dp)
//            ) {
//                Text("Notification Content", fontSize = 18.sp, style = MaterialTheme.typography.bodyLarge)
//                Spacer(modifier = Modifier.height(8.dp))
//                Text("Time to record your accounts", fontSize = 16.sp)
//                Spacer(modifier = Modifier.height(8.dp))
//                Text(
//                    "Record one now",
//                    fontSize = 12.sp,
//                )
//            }
//        }

    }
}