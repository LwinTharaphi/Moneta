package com.example.moneta.screens

import android.annotation.SuppressLint
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.cos
import kotlin.math.sin
import com.example.moneta.model.Expense
import com.example.moneta.model.ExpenseCategory

@SuppressLint("MutableCollectionMutableState")
@Composable
fun ExpenseScreen(navController: NavController) {
    var selectedDate by remember { mutableStateOf(Date()) }
    var expensesByDate by remember { mutableStateOf(mutableMapOf<Date, MutableList<Expense>>()) }
    var showDialog by remember { mutableStateOf(false) }

    var showBottomSheet by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var selectedExpense by remember { mutableStateOf<Expense?>(null) }

    val dateFormat = SimpleDateFormat("MMMM dd", Locale.getDefault())
    val expenses = expensesByDate[selectedDate] ?: mutableListOf()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
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
                            selectedExpense = expense
                            showBottomSheet = true
                        }) // ✅ No extra UI duplication here
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
            onDismiss = { showDialog = false },
            onAddExpense = { description, amount, category, imageUri -> // ✅ Updated to include imageUri
                expensesByDate = expensesByDate.toMutableMap().apply {
                    val newExpenses = this[selectedDate]?.toMutableList() ?: mutableListOf()
                    newExpenses.add(
                        Expense(
                            description = description,
                            amount = amount,
                            date = selectedDate,
                            category = category,
                            imageUri = imageUri // ✅ Store the image URI
                        )
                    )
                    this[selectedDate] = newExpenses
                }
                showDialog = false
            }
        )
    }




    // Expense Bottom Sheet
    if (showBottomSheet && selectedExpense != null) {
        ExpenseBottomSheet(
            expense = selectedExpense!!,
            onDismiss = { showBottomSheet = false },
            onEdit = {
                showBottomSheet = false
                showEditDialog = true // Open edit dialog instead of modifying directly
            },
            onDelete = {
                expensesByDate = expensesByDate.toMutableMap().apply {
                    this[selectedDate]?.remove(selectedExpense)
                }
                showBottomSheet = false
            }
        )
    }

    // Edit Dialog
    if (showEditDialog && selectedExpense != null) {
        ExpenseEditDialog(
            expense = selectedExpense!!,
            onDismiss = { showEditDialog = false },
            onConfirm = { updatedDescription, updatedAmount, updatedCategory, updatedImageUri -> // ✅ Now includes imageUri
                expensesByDate = expensesByDate.toMutableMap().apply {
                    this[selectedDate] = this[selectedDate]?.map {
                        if (it == selectedExpense) {
                            Expense(
                                description = updatedDescription,
                                amount = updatedAmount, // ✅ Updated Amount
                                date = selectedDate,
                                category = updatedCategory, // ✅ Updated Category
                                imageUri = updatedImageUri // ✅ Store updated image URI
                            )
                        } else it
                    }?.toMutableList() ?: mutableListOf()
                }
                showEditDialog = false
            }
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
        // ✅ Category (Bigger font, onSurface color)
        Text(
            text = expense.category.name,
            style = MaterialTheme.typography.bodyLarge.copy(
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )
        )

        // ✅ Row for Description & Amount
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = expense.description, // ✅ Expense description
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurface
                ),
                modifier = Modifier.weight(1f) // Use remaining space
            )
            Text(
                text = "฿${String.format(Locale.getDefault(), "%.2f", expense.amount)}", // ✅ Amount aligned right
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
            )
        }

        // ✅ Show Image Below (If Available)
        expense.imageUri?.let {
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = rememberAsyncImagePainter(it),
                    contentDescription = "Expense Image",
                    modifier = Modifier
                        .size(80.dp) // ✅ Keeps it small, aligned left
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.LightGray)
                )
            }
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp)) // ✅ Separator
    }
}


// Expense Dialog
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseDialog(
    onDismiss: () -> Unit,
    onAddExpense: (String, Float, ExpenseCategory, String?) -> Unit // ✅ Add imageUri parameter
) {
    var description by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(ExpenseCategory.Dining) }
    var isDropdownExpanded by remember { mutableStateOf(false) }
    var imageUri by remember { mutableStateOf<String?>(null) } // ✅ Store selected image

    val categories = ExpenseCategory.entries

    // ✅ Image Picker Launcher
    val imagePickerLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            imageUri = uri?.toString()
        }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    val amountFloat = amount.toFloatOrNull() ?: 0f
                    if (description.isNotEmpty() && amountFloat > 0f) {
                        onAddExpense(description, amountFloat, selectedCategory, imageUri)
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

                // ✅ Image Picker Section
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    imageUri?.let {
                        Image(
                            painter = rememberAsyncImagePainter(it),
                            contentDescription = "Selected Image",
                            modifier = Modifier
                                .size(100.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.LightGray)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    Button(onClick = { imagePickerLauncher.launch("image/*") }) {
                        Icon(imageVector = Icons.Default.AddAPhoto, contentDescription = "Add Image")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Pick Image")
                    }
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseBottomSheet(
    expense: Expense,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
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

            // ✅ Show Image if Available
            expense.imageUri?.let { uri ->
                Image(
                    painter = rememberAsyncImagePainter(uri),
                    contentDescription = "Expense Image",
                    modifier = Modifier
                        .size(120.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.LightGray)
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

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
                Button(onClick = onEdit, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)) {
                    Text("Edit")
                }
                Button(onClick = onDelete, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)) {
                    Text("Delete", color = MaterialTheme.colorScheme.onPrimary)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseEditDialog(
    expense: Expense,
    onDismiss: () -> Unit,
    onConfirm: (String, Float, ExpenseCategory, String?) -> Unit // ✅ Now includes imageUri
) {
    var description by remember { mutableStateOf(expense.description) }
    var amount by remember { mutableStateOf(expense.amount.toString()) }
    var selectedCategory by remember { mutableStateOf(expense.category) }
    var isDropdownExpanded by remember { mutableStateOf(false) }
    var imageUri by remember { mutableStateOf(expense.imageUri) } // ✅ Store the selected image

    val categories = ExpenseCategory.entries

    // ✅ Image Picker Launcher
    val imagePickerLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            imageUri = uri?.toString()
        }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    val updatedAmount = amount.toFloatOrNull() ?: 0f
                    if (description.isNotEmpty() && updatedAmount > 0f) {
                        onConfirm(description, updatedAmount, selectedCategory, imageUri)
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

                // ✅ Image Picker Section
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    imageUri?.let {
                        Image(
                            painter = rememberAsyncImagePainter(it),
                            contentDescription = "Selected Image",
                            modifier = Modifier
                                .size(100.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.LightGray)
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        // ✅ Remove Image Button
                        Button(
                            onClick = { imageUri = null },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                        ) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Remove Image")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Remove Image")
                        }
                    }

                    Button(onClick = { imagePickerLauncher.launch("image/*") }) {
                        Icon(imageVector = Icons.Default.AddAPhoto, contentDescription = "Change Image")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (imageUri != null) "Change Image" else "Pick Image")
                    }
                }
            }
        }
    )
}


// Pie Chart
@Composable
fun PieChart(expenses: List<Expense>, modifier: Modifier = Modifier) {
    val total = expenses.sumOf { it.amount.toDouble() } // Ensure double precision
    val colors = listOf(
        Color(0xFF90CAF9), // Soft Blue
        Color(0xFFFFAB91), // Light Orange
        Color(0xFFA5D6A7), // Soft Green
        Color(0xFFF48FB1), // Light Pink
        Color(0xFFCE93D8), // Lavender
        Color(0xFFFFF59D)  // Pale Yellow
    )

    val textColor = MaterialTheme.colorScheme.onSurface.toArgb()

    Canvas(modifier = modifier.size(200.dp)) {
        val diameter = size.minDimension * 0.8f
        val radius = diameter / 2
        val center = Offset(size.width / 2, size.height / 2)

        if (total > 0) { // ✅ Prevents division by zero
            var startAngle = -90f
            expenses.forEachIndexed { index, expense ->
                val sweepAngle = (expense.amount / total * 360).toFloat()

                drawArc(
                    color = colors[index % colors.size], // ✅ Use main color list
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = true,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = Size(diameter, diameter)
                )

                // Position text labels
                val angle = Math.toRadians((startAngle + sweepAngle / 2).toDouble()).toFloat()
                val labelRadius = radius * 0.7f // ✅ Adjusted label radius
                val x = center.x + labelRadius * cos(angle)
                val y = center.y + labelRadius * sin(angle)

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
                    color = textColor
                    textSize = 35f
                    textAlign = android.graphics.Paint.Align.CENTER
                }
                canvas.nativeCanvas.drawText(
                    "Start Adding Expenses",
                    size.width / 2,
                    size.height / 2,
                    paint
                )
            }
        }
    }
}

// Preview
@Preview(showBackground = true)
@Composable
fun ExpenseScreenPreview() {
    val navController = rememberNavController()
    ExpenseScreen(navController)
}
