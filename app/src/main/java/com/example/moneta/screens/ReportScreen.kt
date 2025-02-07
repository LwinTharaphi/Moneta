package com.example.moneta.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Notifications
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun ReportScreen(navController: NavController) {
    var showDialog by remember { mutableStateOf(false) }
    var selectedDate by remember { mutableStateOf(getCurrentMonthYear()) }

    Column(
        modifier = Modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Month-Year Picker Row (Without Notification Icon)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.clickable { showDialog = true },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = selectedDate,
                    style = MaterialTheme.typography.headlineSmall,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(imageVector = Icons.Filled.ArrowDropDown, contentDescription = "Expand Date Picker")
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Title
        Text(
            text = "Monthly Report",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Mock Data for Categories and Expenses (Replace with real data)
        val categories = listOf(
            Category("Groceries", 180.0),
            Category("Transport", 100.0),
            Category("Dining", 250.0),
            Category("Others", 80.0)
        )

        val totalExpense = categories.sumOf { it.amount }
        val sortedCategories = categories.sortedByDescending { it.amount }

        Column(modifier = Modifier.padding(16.dp)) {
            // Pie Chart
            PieChart(expenses = categories, modifier = Modifier.fillMaxWidth().height(200.dp))

            Spacer(modifier = Modifier.height(16.dp))

            // Category Rows (Sorted)
            sortedCategories.forEach { category ->
                CategoryRow(category = category, totalExpense = totalExpense)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Total Expense
            Text(
                text = "Total Expense: $${"%.2f".format(totalExpense)}",
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }

    // Month-Year Picker Dialog
    if (showDialog) {
        MonthYearPickerDialog(
            currentSelection = selectedDate,
            onDismiss = { showDialog = false },
            onMonthSelected = { selectedDate = it }
        )
    }
}

// Updated Pie Chart for Category-wise Expenses
@Composable
fun PieChart(expenses: List<Category>, modifier: Modifier = Modifier) {
    val total = expenses.sumOf { it.amount }
    val softColors = listOf(
        Color(0xFFB3E5FC), // Light Blue
        Color(0xFFFFCCBC), // Soft Orange
        Color(0xFFC8E6C9), // Pale Green
        Color(0xFFF8BBD0), // Light Pink
        Color(0xFFD1C4E9)  // Soft Purple
    )

    Canvas(modifier = modifier.size(200.dp)) {
        val diameter = size.minDimension * 0.8f
        val radius = diameter / 2

        if (expenses.isNotEmpty()) {
            var startAngle = -90f
            val center = Offset(size.width / 2, size.height / 2)

            expenses.forEachIndexed { index, category ->
                val sweepAngle = ((category.amount.toFloat() / total.toFloat()) * 360f)

                // Draw the arc slice
                drawArc(
                    color = softColors[index % softColors.size],
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = true,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = Size(diameter, diameter)
                )

                // Calculate label position
                val angle = startAngle + sweepAngle / 2
                val labelRadius = radius * 0.6f
                val pi = 3.14
                val x = center.x + labelRadius * cos(angle * pi / 180).toFloat()
                val y = center.y + labelRadius * sin(angle * pi / 180).toFloat()

                // Draw category name text
                drawContext.canvas.nativeCanvas.drawText(
                    category.name,
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
                    "No Data for Pie Chart",
                    size.width / 2,
                    size.height / 2,
                    paint
                )
            }
        }
    }
}

@Composable
fun CategoryRow(category: Category, totalExpense: Double) {
    val percentage = (category.amount / totalExpense) * 100.0
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Category Name
        Text(
            text = category.name,
            style = MaterialTheme.typography.bodyMedium
        )

        // Percentage Bar
        Box(
            modifier = Modifier
                .fillMaxWidth(0.5f) // Bar size relative to available space
                .height(8.dp)
                .background(color = category.color, shape = RoundedCornerShape(4.dp))
        )

        // Amount
        Text(
            text = "$${"%.2f".format(category.amount)}",
            style = MaterialTheme.typography.bodyMedium
        )
    }

    Spacer(modifier = Modifier.height(8.dp))
}

// Category Data Class with Color Assignments
data class Category(val name: String, val amount: Double) {
    val color: Color
        get() = when (name) {
            "Groceries" -> Color(0xFF8BC34A) // Green
            "Transport" -> Color(0xFF03A9F4) // Blue
            "Dining" -> Color(0xFFFFC107) // Amber
            "Others" -> Color(0xFFF8BBD0) // Grey
            else -> Color.Gray
        }
}


@Preview(showBackground = true)
@Composable
fun ReportScreenPreview() {
    val navController = rememberNavController() // Create a NavController for preview
    ReportScreen(navController = navController)
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
    var selectedYear by remember { mutableIntStateOf(Calendar.getInstance().get(Calendar.YEAR)) }
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
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous Year")
                }
                Text(text = "$selectedYear", fontSize = 20.sp)
                IconButton(onClick = { selectedYear++ }) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next Year")
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