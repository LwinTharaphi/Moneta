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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.example.moneta.model.Reminder
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Calendar
import androidx.compose.ui.graphics.Color.Companion as Color

@Composable
fun EditReminderScreen(navController: NavHostController, reminderId: String) {
    val db = FirebaseFirestore.getInstance()
    var reminder by remember { mutableStateOf<Reminder?>(null) }
    val coroutineScope = rememberCoroutineScope()

    // Fetch reminder details from Firebase
    LaunchedEffect(reminderId) {
        coroutineScope.launch {
            val snapshot = db.collection("reminders").document(reminderId).get().await()
            if (snapshot.exists()) {
                reminder = snapshot.toObject(Reminder::class.java)
            }
        }
    }

    var name by remember { mutableStateOf("") }
    var selectedTime by remember { mutableStateOf("") }
    var repeatOption by remember { mutableStateOf("") }

    // Update state when reminder is loaded
    LaunchedEffect(reminder) {
        reminder?.let {
            name = it.name
            selectedTime = it.time
            repeatOption = it.repeat
        }
    }

    var showDialog by remember { mutableStateOf(false) }
    var showRepeatDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val calendar = Calendar.getInstance()

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

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Reminder") },
            text = { Text("Are you sure you want to delete this reminder? This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            db.collection("reminders").document(reminderId).delete().await()
                            navController.popBackStack()
                        }
                        showDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete", color = Color.White)
                }
            },
            dismissButton = {
                Button(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header Row
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "Back")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text("Edit Reminder", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = {
                    val updatedReminder = reminder?.copy(name = name, time = selectedTime, repeat = repeatOption)
                    updatedReminder?.let {
                        coroutineScope.launch {
                            db.collection("reminders").document(reminderId).set(it).await()
                        }
                    }
                    navController.popBackStack()
                }
            ) {
                Icon(imageVector = Icons.Default.Check, contentDescription = "Save Reminder")
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Reminder Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                // Reminder Name
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Name", fontSize = 16.sp)
                    Spacer(modifier = Modifier.weight(1f))
                    TextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Enter Name") },
                        modifier = Modifier.width(180.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Reminder Time
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Reminder Time", fontSize = 16.sp)
                    Spacer(modifier = Modifier.weight(1f))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(selectedTime, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
                        IconButton(onClick = { timePickerDialog.show() }) {
                            Icon(imageVector = Icons.Default.KeyboardArrowRight, contentDescription = "Choose")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Repeat Option
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Repeat", fontSize = 16.sp)
                    Spacer(modifier = Modifier.weight(1f))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(repeatOption, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
                        IconButton(onClick = {showRepeatDialog = true}) {
                            Icon(imageVector = Icons.Default.KeyboardArrowRight, contentDescription = "Choose")
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = { showDeleteDialog = true },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Delete Reminder", color = Color.White)
        }
    }
}
