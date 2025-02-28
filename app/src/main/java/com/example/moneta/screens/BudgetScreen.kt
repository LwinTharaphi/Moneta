package com.example.moneta.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlin.random.Random

// Data class for Budget
data class BudgetData(
    val id: String = "", // Add this field to store the document ID
    val month: String = "",
    val year: Int = 0,
    val total: Float = 0f,
    val used: Float = 0f
) {
    constructor() : this("", "", 0, 0f, 0f) // Required for Firestore
}

@Composable
fun BudgetScreen(navController: NavController) {
    val db = Firebase.firestore
    val auth = FirebaseAuth.getInstance()
    val userId = auth.currentUser?.uid // Get the current user ID from Firebase Authentication

    if (userId == null) {
        navController.navigate("login_screen")
        return
    }
    val usedAmount = 6000f
    var budgets by remember { mutableStateOf(listOf<BudgetData>()) }
    var showDialog by remember { mutableStateOf(false) }
    var editDialog by remember { mutableStateOf(false) }
    var selectedBudget by remember { mutableStateOf<BudgetData?>(null) }
    var newMonth by remember { mutableStateOf("") }
    var newAmount by remember { mutableStateOf("") }
    var showYearDialog by remember { mutableStateOf(false) }
    var selectedYear by remember { mutableIntStateOf(2025) }

    // Fetch budgets for the selected year
    LaunchedEffect(selectedYear) {
        db.collection("users").document(userId).collection("budgets")
            .whereEqualTo("year", selectedYear)
            .get()
            .addOnSuccessListener { result ->
                budgets = result.documents.map { document ->
                    document.toObject(BudgetData::class.java)?.copy(id = document.id) ?: BudgetData()
                }
            }
            .addOnFailureListener { e ->
                // Handle the error
            }
    }

    // Add vertical scroll modifier to make the content scrollable
    Column(modifier = Modifier
        .padding(16.dp)
        .verticalScroll(rememberScrollState())) {

        // Top Row with "Budget" on the left and Year on the right
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Budget", style = MaterialTheme.typography.headlineSmall)

            // Year selection on the right
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { showYearDialog = true }
            ) {
                Text(
                    text = "$selectedYear",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Icon(imageVector = Icons.Filled.ArrowDropDown, contentDescription = "Expand Year Picker")
            }
        }
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        Spacer(modifier = Modifier.height(10.dp))

        val filteredBudgets = budgets.filter { it.year == selectedYear }

        if (filteredBudgets.isEmpty()) {
            Text("No budget set for $selectedYear", color = Color.Gray)
        } else {
            filteredBudgets.forEach { budget ->
                BudgetRow(
                    budget = budget,
                    onEdit = {
                        selectedBudget = budget
                        newMonth = budget.month
                        newAmount = budget.total.toString()
                        editDialog = true
                    },
                    onDelete = {
                        db.collection("users").document(userId).collection("budgets")
                            .document(budget.id) // Use the document ID
                            .delete()
                            .addOnSuccessListener {
                                budgets = budgets.filterNot { it == budget }
                            }
                            .addOnFailureListener { e ->
                                // Handle the error
                            }
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        FloatingActionButton(
            onClick = { showDialog = true },
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.End),
            containerColor = MaterialTheme.colorScheme.secondary
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add",
                tint = MaterialTheme.colorScheme.background
            )
        }
    }

    if (showDialog) {
        BudgetDialog(
            title = "Set Up Budget",
            newMonth = newMonth,
            newAmount = newAmount,
            onMonthChange = { newMonth = it },
            onAmountChange = { newAmount = it },
            onConfirm = {
                val budget = BudgetData(
                    month = newMonth,
                    year = selectedYear,
                    total = newAmount.toFloatOrNull() ?: 0f,
                    used = 0.0f
                )
                db.collection("users").document(userId).collection("budgets")
                    .add(budget)
                    .addOnSuccessListener {
                        budgets = budgets + budget
                        newMonth = ""
                        newAmount = ""
                        showDialog = false
                    }
                    .addOnFailureListener { e ->
                        // Handle the error
                    }
            },
            onDismiss = { showDialog = false }
        )
    }

    if (editDialog) {
        BudgetDialog(
            title = "Edit Budget",
            newMonth = newMonth,
            newAmount = newAmount,
            onMonthChange = { newMonth = it },
            onAmountChange = { newAmount = it },
            onConfirm = {
                val updatedBudget = selectedBudget?.copy(
                    month = newMonth,
                    total = newAmount.toFloatOrNull() ?: 0f
                )
                if (updatedBudget != null) {
                    db.collection("users").document(userId).collection("budgets")
                        .document(updatedBudget.id) // Use the document ID
                        .set(updatedBudget)
                        .addOnSuccessListener {
                            budgets = budgets.map { budget ->
                                if (budget == selectedBudget) updatedBudget else budget
                            }
                            newMonth = ""
                            newAmount = ""
                            editDialog = false
                        }
                        .addOnFailureListener { e ->
                            // Handle the error
                        }
                }
            },
            onDismiss = { editDialog = false }
        )
    }

    // Year Picker Dialog
    if (showYearDialog) {
        YearPickerDialog(
            currentYear = selectedYear,
            onDismiss = { showYearDialog = false },
            onYearSelected = { newYear ->
                selectedYear = newYear
                showYearDialog = false
            }
        )
    }
}

@Composable
fun BudgetRow(budget: BudgetData, onEdit: () -> Unit, onDelete: () -> Unit) {
    val backgroundColor = remember { getRandomPaleColor() }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor, RoundedCornerShape(10.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        BudgetDonutChart(used = budget.used, total = budget.total)
        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(budget.month, fontWeight = FontWeight.Bold, color = Color.Black)
                Row(
                    horizontalArrangement = Arrangement.spacedBy((-24).dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Filled.Edit, contentDescription = "Edit", tint = Color.Black)
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = Color.Red)
                    }
                }
            }
            Text("Total: ฿${budget.total}", color = Color.Black)
            Text("Used: ฿${budget.used}", color = Color.Black)
        }
    }
}

@Composable
fun BudgetDialog(
    title: String,
    newMonth: String,
    newAmount: String,
    onMonthChange: (String) -> Unit,
    onAmountChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val months = listOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    )

    var expanded by remember { mutableStateOf(false) }
    var selectedMonth by remember { mutableStateOf(newMonth) }
    var textFieldSize by remember { mutableStateOf(Size.Zero) }
    val density = LocalDensity.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = selectedMonth.ifEmpty { "Select Month" },
                        onValueChange = {},
                        label = { Text("Month") },
                        readOnly = true,
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Expand month selection",
                                modifier = Modifier.clickable { expanded = !expanded }
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .onGloballyPositioned { coordinates ->
                                textFieldSize = coordinates.size.toSize()
                            }
                            .clickable { expanded = !expanded }
                    )

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.width(with(density) { textFieldSize.width.toDp() })
                    ) {
                        months.forEach { month ->
                            DropdownMenuItem(
                                text = { Text(text = month) },
                                onClick = {
                                    selectedMonth = month
                                    onMonthChange(month)
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = newAmount,
                    onValueChange = onAmountChange,
                    label = { Text("Amount") },
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text("Save")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun BudgetDonutChart(used: Float, total: Float) {
    val usedPercentage = if (total > 0) (used / total) * 100 else 0f

    Canvas(modifier = Modifier.size(50.dp)) {
        drawArc(
            color = Color(0xFF10B981),
            startAngle = -90f,
            sweepAngle = 360f * (usedPercentage / 100f),
            useCenter = false,
            style = Stroke(width = 8.dp.toPx())
        )
        drawArc(
            color = Color(0xFF374151),
            startAngle = -90f + (360f * (usedPercentage / 100f)),
            sweepAngle = 360f * ((100f - usedPercentage) / 100f),
            useCenter = false,
            style = Stroke(width = 8.dp.toPx())
        )
    }
}

@Composable
fun YearPickerDialog(
    currentYear: Int,
    onDismiss: () -> Unit,
    onYearSelected: (Int) -> Unit
) {
    var selectedYear by remember { mutableIntStateOf(currentYear) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Year") },
        text = {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { selectedYear-- }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Previous Year")
                    }
                    Text(text = "$selectedYear", style = MaterialTheme.typography.headlineMedium)
                    IconButton(onClick = { selectedYear++ }) {
                        Icon(Icons.Default.ArrowForward, contentDescription = "Next Year")
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onYearSelected(selectedYear) }) {
                Text("OK")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

fun getRandomPaleColor(): Color {
    val red = Random.nextInt(150, 255)
    val green = Random.nextInt(150, 255)
    val blue = Random.nextInt(150, 255)
    return Color(red, green, blue, 255)
}

@Preview(showBackground = true)
@Composable
fun BudgetScreenPreview() {
    val navController = rememberNavController()
    BudgetScreen(navController)
}