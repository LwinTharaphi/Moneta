package com.example.moneta.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import kotlin.random.Random

data class BudgetData(val month: String, val year: Int, val total: Float, val used: Float)

@Composable
fun BudgetScreen(navController: NavController) {
    val usedAmount = 7000f
    var budgets by remember { mutableStateOf(listOf<BudgetData>()) }
    var showDialog by remember { mutableStateOf(false) }
    var editDialog by remember { mutableStateOf(false) }
    var selectedBudget by remember { mutableStateOf<BudgetData?>(null) }
    var newMonth by remember { mutableStateOf("") }
    var newAmount by remember { mutableStateOf("") }
    var selectedYear by remember { mutableIntStateOf(2025) }

    Column(modifier = Modifier.padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Budget List", style = MaterialTheme.typography.headlineSmall)
            YearSelection(selectedYear = selectedYear, onYearSelected = { selectedYear = it })
        }
        Divider(modifier = Modifier.padding(vertical = 8.dp))

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
                        budgets = budgets.filterNot { it == budget }
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
            shape = CircleShape
        ) {
            Text("+")
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
                budgets = budgets + BudgetData(
                    newMonth,
                    selectedYear,
                    newAmount.toFloatOrNull() ?: 0f,
                    usedAmount
                )
                newMonth = ""
                newAmount = ""
                showDialog = false
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
                budgets = budgets.map { budget ->
                    if (budget == selectedBudget) budget.copy(month = newMonth, total = newAmount.toFloatOrNull() ?: 0f)
                    else budget
                }
                newMonth = ""
                newAmount = ""
                editDialog = false
            },
            onDismiss = { editDialog = false }
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

        // Column for month, total, and used
        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween, // Align text and icons to the ends
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(budget.month, fontWeight = FontWeight.Bold, color = Color.Black)

                // Row for icons aligned to the right
                Row(
                    horizontalArrangement = Arrangement.spacedBy(-24.dp),
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
            Text("Total: $${budget.total}", color = Color.Black)
            Text("Used: $${budget.used}", color = Color.Black)
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
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                OutlinedTextField(
                    value = newMonth,
                    onValueChange = onMonthChange,
                    label = { Text("Month") }
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = newAmount,
                    onValueChange = onAmountChange,
                    label = { Text("Amount") }
                )
            }
        },
        confirmButton = {
            Button(onClick = onConfirm) { Text("Save") }
        },
        dismissButton = {
            Button(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun YearSelection(selectedYear: Int, onYearSelected: (Int) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = "<", // Display '<' symbol
            fontWeight = FontWeight.Bold,
            modifier = Modifier.clickable {
                onYearSelected(selectedYear - 1) // Go one year back
            }
        )
        Spacer(modifier = Modifier.width(8.dp)) // Add some space between the arrows and the year
        Text(
            text = "$selectedYear", // Display the selected year
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.width(8.dp)) // Add space after the year
        Text(
            text = ">", // Display '>' symbol
            fontWeight = FontWeight.Bold,
            modifier = Modifier.clickable {
                onYearSelected(selectedYear + 1) // Go one year forward
            }
        )
    }
}


@Composable
fun BudgetDonutChart(used: Float, total: Float) {
    val usedPercentage = if (total > 0) (used / total) * 100 else 0f

    Canvas(modifier = Modifier.size(50.dp)) {
        drawArc(
            color = Color.Green,
            startAngle = -90f,
            sweepAngle = 360f * (usedPercentage / 100f),
            useCenter = false,
            style = Stroke(width = 8.dp.toPx())
        )
        drawArc(
            color = Color.LightGray,
            startAngle = -90f + (360f * (usedPercentage / 100f)),
            sweepAngle = 360f * ((100f - usedPercentage) / 100f),
            useCenter = false,
            style = Stroke(width = 8.dp.toPx())
        )
    }
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
