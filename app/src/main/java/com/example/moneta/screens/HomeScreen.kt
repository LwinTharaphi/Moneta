package com.example.moneta.screens

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun HomeScreen(navController: NavController) {
    var showDialog by remember { mutableStateOf(false) }
    var selectedDate by remember { mutableStateOf(getCurrentMonthYear()) }

    Column(
        modifier = Modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
        ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row (
                modifier = Modifier.clickable { showDialog = true },
                verticalAlignment = Alignment.CenterVertically
            ){
                Text(
                    text = selectedDate,
                    style = MaterialTheme.typography.headlineMedium,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(imageVector = Icons.Filled.ArrowDropDown, contentDescription = "Expand Date Picker")
            }

            Spacer(modifier = Modifier.width(16.dp))

            IconButton(onClick = { navController.navigate("notification_screen")}) {
                Icon(imageVector = Icons.Filled.Notifications, contentDescription = "Notifications")
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(text = "Welcome to your Finance Tracker!", style = MaterialTheme.typography.headlineLarge)

        Spacer(modifier = Modifier.height(20.dp))

        // Button to navigate to Expense Screen
        Button(onClick = { navController.navigate("expense_screen") }) {
            Text("Add Expense")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Button to navigate to Budget Screen
        Button(onClick = { navController.navigate("budget_screen") }) {
            Text("Set Budget")
        }
    }
    if (showDialog) {
        MonthYearPickerDialog(
            currentSelection = selectedDate,
            onDismiss = { showDialog = false},
            onMonthSelected = { selectedDate = it }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    val navController = rememberNavController() // Create a NavController for preview
    HomeScreen(navController = navController)
}

fun getCurrentMonthYear(): String {
    val calendar = Calendar.getInstance()
    val formatter = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
    return formatter.format(calendar.time)
}

@Composable
fun MonthYearPickerDialog(
    currentSelection: String,
    onDismiss: ()-> Unit,
    onMonthSelected: (String)->Unit
){
    var selectedYear by remember { mutableStateOf(Calendar.getInstance().get(Calendar.YEAR)) }
    val months = listOf(
        "Jan", "Feb", "Mar", "Apr", "May", "Jun",
        "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ){
                IconButton(onClick = { selectedYear-- }) {
                    Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "Previous Year")
                }
                Text(text = "$selectedYear", fontSize = 20.sp)
                IconButton(onClick = { selectedYear++ }) {
                    Icon(imageVector = Icons.Filled.ArrowForward, contentDescription = "Next Year")
                }
            }
        },
        text = {
            Column {
               for (i in months.indices step 6){
                   Row (
                       modifier = Modifier.fillMaxWidth(),
                       horizontalArrangement = Arrangement.SpaceEvenly
                   ) {
                       for (j in i until (i+6).coerceAtMost(months.size)){
                           Text(
                               text = months[j],
                               modifier = Modifier
                                   .clickable{
                                       onMonthSelected("${months[j]} $selectedYear")
                                       onDismiss()
                                   }
                                   .padding(8.dp),
                               fontSize = 16.sp
                           )
                       }
                   }
               }
            }
        },
        confirmButton = {},
        shape = RoundedCornerShape(12.dp)
    )
}


