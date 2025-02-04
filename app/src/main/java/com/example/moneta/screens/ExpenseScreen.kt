package com.example.moneta.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.cos
import kotlin.math.sin

@SuppressLint("MutableCollectionMutableState")
@Composable
fun ExpenseScreen(navController: NavController) {
    var selectedDate by remember { mutableStateOf(Date()) }
    var expensesByDate by remember { mutableStateOf(mutableMapOf<Date, MutableList<Expense>>()) }
    var showDialog by remember { mutableStateOf(false) }

    val dateFormat = SimpleDateFormat("MMMM dd", Locale.getDefault())
    val expenses = expensesByDate[selectedDate] ?: mutableListOf()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Title
        Text(
            text = "Your Daily Expense",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Pie Chart
        PieChart(expenses = expenses, modifier = Modifier.fillMaxWidth().height(200.dp))

        Spacer(modifier = Modifier.height(16.dp))

        // Date & Add Button Row
        Column(modifier = Modifier.fillMaxWidth()) {
            // Line above the date
            Divider(modifier = Modifier.padding(vertical = 8.dp))

            // Date Navigation with smaller font
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center, // Center the date
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "<",
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable {
                        selectedDate = Calendar.getInstance().apply {
                            time = selectedDate
                            add(Calendar.DAY_OF_MONTH, -1)
                        }.time
                    }
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    dateFormat.format(selectedDate),
                    style = MaterialTheme.typography.bodyMedium, // Smaller font size
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    ">",
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable {
                        selectedDate = Calendar.getInstance().apply {
                            time = selectedDate
                            add(Calendar.DAY_OF_MONTH, 1)
                        }.time
                    }
                )
            }

            // Line below the date
            Divider(modifier = Modifier.padding(vertical = 8.dp))

            // Add Expense Button at the right side
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End, // Align button to the right
                verticalAlignment = Alignment.CenterVertically
            ) {
                FloatingActionButton(
                    onClick = { showDialog = true },
                    modifier = Modifier.size(36.dp), // Adjust the size to make it smaller
                    shape = CircleShape
                ) {
                    Text("+", fontSize = 18.sp) // Adjust font size for smaller button
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Expenses List with Lines
        if (expenses.isEmpty()) {
            Text(
                text = "No expense for today",
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 12.sp), // Adjust font size
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        } else {
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(expenses) { expense ->
                    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                        // Row for description and amount
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = expense.description, // Expense description
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.weight(1f) // Make description take up available space
                            )
                            Text(
                                text = "${expense.amount}", // Expense amount
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                modifier = Modifier.align(Alignment.CenterVertically) // Align vertically
                            )
                        }

                        // Add a line separator
                        Divider(modifier = Modifier.padding(vertical = 8.dp))
                    }
                }
            }
        }

        // Total Expense at the bottom right (placed below LazyColumn)
        val totalExpense = expenses.sumOf { it.amount }
        Text(
            text = "Total: $totalExpense",
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier
                .align(Alignment.CenterHorizontally) // Align to the bottom right
                .padding(16.dp) // Add padding around the total text
        )
    }



    // Expense Dialog
    if (showDialog) {
        ExpenseDialog(
            onDismiss = { showDialog = false },
            onAddExpense = { description, amount ->
                expensesByDate = expensesByDate.toMutableMap().apply {
                    val newExpenses = this[selectedDate]?.toMutableList() ?: mutableListOf()
                    newExpenses.add(Expense(description, amount.toDouble(), selectedDate))
                    this[selectedDate] = newExpenses
                }
                showDialog = false
            }
        )
    }
}

// Expense Item
@Composable
fun ExpenseItem(expense: Expense) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Text(text = expense.description, fontWeight = FontWeight.Bold)
        Text(text = "$${expense.amount}")
        Divider(color = Color.Gray, thickness = 1.dp)
    }
}

// Expense Dialog
@Composable
fun ExpenseDialog(onDismiss: () -> Unit, onAddExpense: (String, String) -> Unit) {
    var description by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(onClick = {
                if (description.isNotEmpty() && amount.isNotEmpty()) {
                    onAddExpense(description, amount)
                }
            }) {
                Text("Add")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        title = { Text("Add Expense") },
        text = {
            Column {
                TextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") }
                )
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Amount") }
                )
            }
        }
    )
}

// Pie Chart
@Composable
fun PieChart(expenses: List<Expense>, modifier: Modifier = Modifier) {
    val total = expenses.sumOf { it.amount }
    val colors = listOf(Color.Red, Color.Blue, Color.Green, Color.Yellow, Color.Magenta, Color.Cyan)

    Canvas(modifier = modifier.size(200.dp)) {  // Ensure fixed size
        val diameter = size.minDimension * 0.8f // Keep circle shape & make smaller
        val radius = diameter / 2

        if (expenses.isNotEmpty()) {
            var startAngle = -90f
            val center = Offset(size.width / 2, size.height / 2)

            expenses.forEachIndexed { index, expense ->
                val sweepAngle = (expense.amount / expenses.sumOf { it.amount } * 360).toFloat()
                val softColors = listOf(
                    Color(0xFFB3E5FC), // Light Blue
                    Color(0xFFFFCCBC), // Soft Orange
                    Color(0xFFC8E6C9), // Pale Green
                    Color(0xFFF8BBD0), // Light Pink
                    Color(0xFFD1C4E9)  // Soft Purple
                )

                // Draw the arc slices
                drawArc(
                    color = softColors[index % softColors.size],  // Use softer colors
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = true,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = Size(diameter, diameter)
                )

                // Convert the angle to degrees
                val angle = startAngle + sweepAngle / 2
                val labelRadius = radius * 0.6f  // Offset to place the label outside the circle
                val pi = 3.14
                val x = center.x + labelRadius * cos(angle * pi / 180).toFloat()  // Convert to radians here
                val y = center.y + labelRadius * sin(angle * pi / 180).toFloat()  // Convert to radians here

                // Draw the label text for each slice
                drawContext.canvas.nativeCanvas.drawText(
                    expense.description,
                    x,
                    y,
                    android.graphics.Paint().apply {
                        color = android.graphics.Color.BLACK
                        textSize = 30f
                        textAlign = android.graphics.Paint.Align.CENTER
                    }
                )

                startAngle += sweepAngle
            }
        } else {
            drawIntoCanvas { canvas ->
                val paint = android.graphics.Paint().apply {
                    color = android.graphics.Color.GRAY
                    textSize = 35f
                    textAlign = android.graphics.Paint.Align.CENTER
                }
                canvas.nativeCanvas.drawText(
                    "No Data for pie chart",
                    size.width / 2,
                    size.height / 2,
                    paint
                )
            }
        }
    }
}

// Expense Data Class
data class Expense(val description: String, val amount: Double, val date: Date)

// Preview
@Preview(showBackground = true)
@Composable
fun ExpenseScreenPreview() {
    val navController = rememberNavController()
    ExpenseScreen(navController)
}
