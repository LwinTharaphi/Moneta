package com.example.moneta.screens

import android.annotation.SuppressLint
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
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
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.cos
import kotlin.math.sin
import com.example.moneta.model.Expense
import com.example.moneta.model.ExpenseCategory
import com.example.moneta.viewmodel.ExpenseViewModel
import com.example.moneta.viewmodel.ExpenseViewModelFactory

@SuppressLint("MutableCollectionMutableState")
@Composable
fun ExpenseScreen(
    navController: NavController
){
    val context = LocalContext.current // 🔹 Get context
    val viewModel: ExpenseViewModel = viewModel(factory = ExpenseViewModelFactory(context))

    val selectedDate by viewModel.selectedDate.collectAsState()
    val expenses by viewModel.expenses.collectAsState()
    var showDialog by remember { mutableStateOf(false) }

    var showBottomSheet by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    val selectedExpense by viewModel.selectedExpense.collectAsState()

    val dateFormat = SimpleDateFormat("MMMM dd", Locale.getDefault())
//    val expenses = expensesByDate[selectedDate] ?: mutableListOf()

    LaunchedEffect(selectedDate) {
        viewModel.fetchExpenses(selectedDate)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Title with Notification Icon
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween, // Space between title and icon
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Your Daily Expense",
                style = MaterialTheme.typography.headlineSmall.copy(
                    color = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier.weight(1f) // Make title take available space
            )

            IconButton(onClick = { navController.navigate("notification_screen") }) {
                Icon(
                    imageVector = Icons.Filled.Notifications,
                    contentDescription = "Notifications"
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Pie Chart
        PieChart(expenses = expenses, modifier = Modifier.fillMaxWidth().height(200.dp))

        Spacer(modifier = Modifier.height(16.dp))

        // Date & Add Button Row
        Column(modifier = Modifier.fillMaxWidth()) {
            // Line above the date
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

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
                        viewModel.updateSelectedDate(
                            Calendar.getInstance().apply {
                                time = selectedDate
                                add(Calendar.DAY_OF_MONTH, -1)
                            }.time
                        )
                    }
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    dateFormat.format(selectedDate),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    ">",
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable {
                        viewModel.updateSelectedDate(
                            Calendar.getInstance().apply {
                                time = selectedDate
                                add(Calendar.DAY_OF_MONTH, 1)
                            }.time
                        )
                    }
                )
            }

            // Line below the date
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // Add Expense Button at the right side
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End, // Align button to the right
                verticalAlignment = Alignment.CenterVertically
            ) {
                FloatingActionButton(
                    onClick = { showDialog = true },
                    modifier = Modifier.size(56.dp),
                    containerColor = MaterialTheme.colorScheme.secondary // Uses Emerald Green dynamically
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add",
                        tint = MaterialTheme.colorScheme.background // Ensures contrast
                    )
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
                    ExpenseItem(expense = expense, onClick = {
                        viewModel.selectExpense(expense)
                        showBottomSheet = true
                    })
                }
            }
        }

        // Total Expense at the bottom right (placed below LazyColumn)
        val totalExpense = expenses.sumOf { it.amount.toDouble() }
        Text(
            text = "Total: ฿$totalExpense",
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary // Emerald Green
            ),
            modifier = Modifier
                .align(Alignment.CenterHorizontally) // Align to the bottom right
                .padding(16.dp) // Add padding around the total text
        )
    }

    // Expense Dialog
    if (showDialog) {
        ExpenseDialog(
            viewModel = viewModel, // 🔹 Pass ViewModel directly
            onDismiss = { showDialog = false }
        )
    }

    // Expense Bottom Sheet
    if (showBottomSheet && selectedExpense != null) {
        ExpenseBottomSheet(
            viewModel = viewModel,
            expense = selectedExpense!!,
            onDismiss = { showBottomSheet = false },
            onEdit = {
                showBottomSheet = false
                showEditDialog = true
            }
        )
    }

    // Edit Dialog
    if (showEditDialog && selectedExpense != null) {
        ExpenseEditDialog(
            viewModel = viewModel,
            expense = selectedExpense!!,
            onDismiss = { showEditDialog = false }
        )
    }
}

// Expense Item
@Composable
fun ExpenseItem(expense: Expense, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onClick() }
    ) {
        // 🔹 Category (Bigger font, primary color)
        Text(
            text = expense.category.name,
            style = MaterialTheme.typography.bodyLarge.copy(
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        )

        // 🔹 Row for Description & Amount
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = expense.description,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurface
                ),
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "฿${String.format(Locale.getDefault(), "%.2f", expense.amount)}",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary
                )
            )
        }

        // 🔹 Separator Line
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseDialog(
    viewModel: ExpenseViewModel, // 🔹 Inject ViewModel
    onDismiss: () -> Unit
) {
    var description by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(ExpenseCategory.Dining) }
    var isDropdownExpanded by remember { mutableStateOf(false) }

    val selectedDate by viewModel.selectedDate.collectAsState() // 🔹 Use ViewModel's selected date

    val categories = ExpenseCategory.entries

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    val amountFloat = amount.toFloatOrNull() ?: 0f
                    if (description.isNotEmpty() && amountFloat > 0f) {
                        // 🔹 Call ViewModel to add expense to Firestore
                        viewModel.addExpense(
                            Expense(
                                description = description,
                                amount = amountFloat,
                                date = selectedDate,
                                category = selectedCategory
                            )
                        )
                        onDismiss()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
            ) {
                Text("Add", color = MaterialTheme.colorScheme.background)
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
            ) {
                Text("Cancel", color = MaterialTheme.colorScheme.background)
            }
        },
        title = { Text("Add Expense") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Expense Description") }
                )

                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Expense Amount") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )

                ExposedDropdownMenuBox(
                    expanded = isDropdownExpanded,
                    onExpandedChange = { isDropdownExpanded = !isDropdownExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedCategory.name,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category") },
                        trailingIcon = { Icon(Icons.Filled.ArrowDropDown, contentDescription = "Dropdown") },
                        modifier = Modifier
                            .menuAnchor()
                            .clickable { isDropdownExpanded = true }
                    )
                    DropdownMenu(
                        expanded = isDropdownExpanded,
                        onDismissRequest = { isDropdownExpanded = false }
                    ) {
                        categories.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category.name) },
                                onClick = {
                                    selectedCategory = category
                                    isDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseBottomSheet(
    viewModel: ExpenseViewModel,
    expense: Expense,
    onDismiss: () -> Unit,
    onEdit: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = Modifier.heightIn(min = 350.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Expense Details", style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Description:", fontWeight = FontWeight.Bold)
                Text(expense.description, style = MaterialTheme.typography.bodyLarge)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Amount:", fontWeight = FontWeight.Bold)
                Text("฿${String.format(Locale.getDefault(), "%.2f", expense.amount)}", style = MaterialTheme.typography.bodyLarge)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Category:", fontWeight = FontWeight.Bold)
                Text(expense.category.name, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.primary)
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(onClick = onEdit, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)) {
                    Text("Edit")
                }
                Button(
                    onClick = {
                        viewModel.deleteExpense(expense.id) // 🔹 Call deleteExpense
                        onDismiss() // 🔹 Close the bottom sheet
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.onPrimary)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@SuppressLint("MutableCollectionMutableState")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseEditDialog(
    viewModel: ExpenseViewModel,
    expense: Expense,
    onDismiss: () -> Unit
) {
    var description by remember { mutableStateOf(expense.description) }
    var amount by remember { mutableStateOf(expense.amount.toString()) }
    var selectedCategory by remember { mutableStateOf(expense.category) }
    var isDropdownExpanded by remember { mutableStateOf(false) }

    val categories = ExpenseCategory.entries

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    val updatedAmount = amount.toFloatOrNull() ?: 0f
                    if (description.isNotEmpty() && updatedAmount > 0f) {
                        val updatedExpense = expense.copy(
                            description = description,
                            amount = updatedAmount,
                            category = selectedCategory
                        )
                        viewModel.updateExpense(updatedExpense) // 🔹 Call updateExpense
                        onDismiss()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
            ) {
                Text("Update", color = MaterialTheme.colorScheme.background)
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
            ) {
                Text("Cancel", color = MaterialTheme.colorScheme.background)
            }
        },
        title = { Text("Edit Expense") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Update Description") }
                )

                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Update Amount") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )

                ExposedDropdownMenuBox(
                    expanded = isDropdownExpanded,
                    onExpandedChange = { isDropdownExpanded = !isDropdownExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedCategory.name,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category") },
                        trailingIcon = { Icon(Icons.Filled.ArrowDropDown, contentDescription = "Dropdown") },
                        modifier = Modifier
                            .menuAnchor()
                            .clickable { isDropdownExpanded = true }
                    )
                    DropdownMenu(
                        expanded = isDropdownExpanded,
                        onDismissRequest = { isDropdownExpanded = false }
                    ) {
                        categories.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category.name) },
                                onClick = {
                                    selectedCategory = category
                                    isDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        shape = RoundedCornerShape(12.dp) // ✅ Modern rounded look
    )
}

@Composable
fun PieChart(expenses: List<Expense>, modifier: Modifier = Modifier) {
    val total = expenses.sumOf { it.amount.toDouble() }

    if (expenses.isEmpty()) {
        Canvas(modifier = modifier.size(200.dp)) {
            val center = Offset(size.width / 2, size.height / 2)

            drawIntoCanvas { canvas ->
                val paint = android.graphics.Paint().apply {
                    color = android.graphics.Color.GRAY
                    textSize = 35f
                    textAlign = android.graphics.Paint.Align.CENTER
                }
                canvas.nativeCanvas.drawText(
                    "Start Adding Expenses",
                    center.x,
                    center.y,
                    paint
                )
            }
        }
        return
    }

    // 🔹 Ensure animation states match the current list of expenses
    val animatedAngles = remember(expenses) { expenses.map { mutableFloatStateOf(0f) } }.toMutableList()

    // 🔹 Ensure each new expense gets an animation state
    LaunchedEffect(expenses) {
        expenses.forEachIndexed { index, expense ->
            if (index < animatedAngles.size) {
                animatedAngles[index].floatValue = ((expense.amount.toFloat() / total.toFloat()) * 360f)
            } else {
                animatedAngles += mutableFloatStateOf((expense.amount.toFloat() / total.toFloat()) * 360f)
            }
        }
    }

    val animatedSweepAngles = List(expenses.size) { index ->
        animateFloatAsState(
            targetValue = animatedAngles.getOrElse(index) { mutableFloatStateOf(0f) }.floatValue, // 🔹 Prevents out-of-bounds error
            animationSpec = tween(durationMillis = 1200, delayMillis = index * 300)
        )
    }

    Canvas(modifier = modifier.size(200.dp)) {
        val diameter = size.minDimension * 0.8f
        val radius = diameter / 2
        var startAngle = -90f
        val center = Offset(size.width / 2, size.height / 2)

        expenses.forEachIndexed { index, expense ->
            val animatedSweepAngle = animatedSweepAngles.getOrElse(index) { mutableFloatStateOf(0f) }.value // 🔹 Prevents crash

            // Draw the animated arc slice
            drawArc(
                color = getCategoryColor(expense.category.name),
                startAngle = startAngle,
                sweepAngle = animatedSweepAngle,
                useCenter = true,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(diameter, diameter)
            )

            // Position text labels inside slices
            val angle = Math.toRadians((startAngle + animatedSweepAngle / 2).toDouble()).toFloat()
            val labelRadius = radius * 0.6f
            val x = center.x + labelRadius * cos(angle)
            val y = center.y + labelRadius * sin(angle)

            drawContext.canvas.nativeCanvas.drawText(
                expense.description,
                x,
                y,
                android.graphics.Paint().apply {
                    color = android.graphics.Color.WHITE
                    textSize = 30f
                    textAlign = android.graphics.Paint.Align.CENTER
                }
            )

            startAngle += animatedSweepAngle
        }
    }
}
