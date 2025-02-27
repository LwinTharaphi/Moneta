package com.example.moneta.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.moneta.repository.ExpenseRepository
import com.example.moneta.viewmodel.ReportViewModel
import com.example.moneta.viewmodel.ReportViewModelFactory

@Composable
fun ReportScreen(navController: NavController) {
    val context = LocalContext.current // 🔹 Get Context
    val expenseRepository = ExpenseRepository.getInstance(context) // 🔹 Pass context
    val viewModel: ReportViewModel = viewModel(factory = ReportViewModelFactory(expenseRepository))
    var showDialog by remember { mutableStateOf(false) }
    val selectedMonth by viewModel.selectedMonth.collectAsState()
    val selectedYear by viewModel.selectedYear.collectAsState()

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
                    text = "${getMonthName(selectedMonth)} $selectedYear",
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

        val monthlyExpenses by viewModel.monthlyExpenses.collectAsState() // 🔹 Fetch from ViewModel

        val categories = viewModel.processCategoryData(monthlyExpenses) // 🔹 Group by category

        val totalExpense = categories.sumOf { it.amount }.takeIf { it > 0 } ?: 0.0
        val sortedCategories = categories.sortedByDescending { it.amount }

        Column(modifier = Modifier.padding(16.dp)) {
            // Pie Chart
            PieChart(expenses = categories, modifier = Modifier.fillMaxWidth().height(200.dp))

            Spacer(modifier = Modifier.height(16.dp))

            // Category Rows (Sorted)
            sortedCategories.forEach { category ->
                CategoryRow(category = category)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Centered Total Expense Text
            Text(
                text = if (totalExpense > 0)
                    "Total Expense: ฿ ${"%.2f".format(totalExpense)}"
                else
                    "No expenses recorded for this month.",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary
                ),
                modifier = Modifier
                    .fillMaxWidth() // 🔹 Makes sure text takes full width
                    .wrapContentWidth(Alignment.CenterHorizontally) // 🔹 Centers the text
            )
        }
    }

    // Month-Year Picker Dialog
    if (showDialog) {
        MonthYearPickerDialog(
            currentMonth = selectedMonth,
            currentYear = selectedYear,
            onDismiss = { showDialog = false },
            onMonthSelected = { newMonth, newYear ->
                viewModel.updateSelectedMonth(newMonth, newYear)
                showDialog = false
            }
        )
    }
}

fun getMonthName(month: Int): String {
    val months = listOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    )
    return months[month - 1] // 🔹 Convert number to month name
}


// Updated Pie Chart for Category-wise Expenses
@Composable
fun PieChart(expenses: List<Category>, modifier: Modifier = Modifier) {
    if (expenses.isEmpty()) {
        Text(
            text = "No data available",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.fillMaxWidth().wrapContentWidth(Alignment.CenterHorizontally)
        )
        return
    }

    val total = expenses.sumOf { it.amount }

    // Ensure we only create animation states if expenses is not empty
    val animatedAngles = remember(expenses) {
        expenses.map { mutableStateOf(0f) }
    }

    // Trigger animation when expenses are updated
    LaunchedEffect(expenses) {
        expenses.forEachIndexed { index, category ->
            animatedAngles[index].value = ((category.amount.toFloat() / total.toFloat()) * 360f)
        }
    }

    val animatedSweepAngles = expenses.mapIndexed { index, category ->
        animateFloatAsState(
            targetValue = animatedAngles[index].value,
            animationSpec = tween(durationMillis = 1200, delayMillis = index * 300) // 🔹 Delays each slice
        )
    }

    Canvas(modifier = modifier.size(200.dp)) {
        val diameter = size.minDimension * 0.8f
        val radius = diameter / 2
        var startAngle = -90f
        val center = Offset(size.width / 2, size.height / 2)

        expenses.forEachIndexed { index, category ->
            val animatedSweepAngle = animatedSweepAngles.getOrNull(index)?.value ?: 0f // 🔹 Prevents crash

            // Draw the animated arc slice
            drawArc(
                color = getCategoryColor(category.name),
                startAngle = startAngle,
                sweepAngle = animatedSweepAngle,
                useCenter = true,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(diameter, diameter)
            )

            startAngle += animatedSweepAngle
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        expenses.forEach { category ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .background(getCategoryColor(category.name), shape = RoundedCornerShape(4.dp))
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${category.name}: ${"%.1f".format((category.amount / total) * 100)}%",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
fun CategoryRow(category: Category) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Category Name with Percentage
        Text(
            text = category.name,
            style = MaterialTheme.typography.bodyMedium
        )

        // Percentage Bar
        Box(
            modifier = Modifier
                .fillMaxWidth(0.5f)
                .height(8.dp)
                .background(color = getCategoryColor(category.name), shape = RoundedCornerShape(4.dp))
        )
        // Amount
        Text(
            text = "฿ ${"%.2f".format(category.amount)}",
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary
            )
        )
    }

    Spacer(modifier = Modifier.height(8.dp))
}

data class Category(val name: String, val amount: Double)

// Fun to Get Category Color Assignments
fun getCategoryColor(categoryName: String): Color {
    return when (categoryName) {
        "Groceries" -> Color(0xFF4CAF50) // Green
        "Transport" -> Color(0xFF2196F3) // Blue
        "Dining" -> Color(0xFFFF9800) // Orange
        "Beverages" -> Color(0xFF9C27B0) // Purple
        "Entertainment" -> Color(0xFFE91E63) // Pink
        "Shopping" -> Color(0xFFFFEB3B) // Yellow
        "Other" -> Color(0xFF9E9E9E) // Gray
        else -> Color.Gray
    }
}

@Composable
fun MonthYearPickerDialog(
    currentMonth: Int,
    currentYear: Int,
    onDismiss: () -> Unit,
    onMonthSelected: (Int, Int) -> Unit
) {
    var selectedMonth by remember { mutableIntStateOf(currentMonth - 1) } // 🔹 Store selected month
    var selectedYear by remember { mutableIntStateOf(currentYear) }
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
            ) {
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
                for (i in months.indices step 6) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        for (j in i until (i + 6).coerceAtMost(months.size)) {
                            Text(
                                text = months[j],
                                modifier = Modifier
                                    .clickable {
                                        selectedMonth = j // 🔹 Update selectedMonth when clicked
                                        onMonthSelected(j + 1, selectedYear)
                                        onDismiss()
                                    }
                                    .padding(8.dp)
                                    .background(
                                        if (j == selectedMonth) Color.LightGray else Color.Transparent, // 🔹 Highlight selected month
                                        shape = RoundedCornerShape(4.dp)
                                    ),
                                fontSize = 16.sp,
                                fontWeight = if (j == selectedMonth) FontWeight.Bold else FontWeight.Normal, // 🔹 Bold selected month
                                color = if (j == selectedMonth) Color.Black else Color.Gray
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