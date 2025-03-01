package com.example.moneta.screens

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.auth.FirebaseAuth

@Composable
fun ProfileScreen(navController: NavController, isDarkTheme: Boolean, onThemeToggle: (Boolean) -> Unit) {
    val auth = FirebaseAuth.getInstance()
    var currentUser by remember { mutableStateOf(auth.currentUser) }
    var showDialog by remember { mutableStateOf(false) }
    var selectedFileUri by remember { mutableStateOf<Uri?>(null) }

    val context = LocalContext.current

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(context, "Permission needed to access profile picture", Toast.LENGTH_LONG).show()
        }
    }

    // ✅ File Picker Launcher (Only activates inside the dialog)
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            if (uri != null) {
                selectedFileUri = uri
                Log.d("FilePicker", "Selected File: $uri")
            } else {
                Log.d("FilePicker", "No file selected")
            }
        }
    )

    // Reload user info when the screen is recomposed
    LaunchedEffect(Unit) {
        auth.currentUser?.reload()?.addOnCompleteListener {
            currentUser = auth.currentUser
        }
    }

    val userName by remember { mutableStateOf(currentUser?.displayName ?: "User") }
    val userEmail by remember { mutableStateOf(currentUser?.email ?: "No email available") }
    val userPhotoUrl by remember { mutableStateOf(currentUser?.photoUrl?.toString()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),  // Make it scrollable
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Profile", fontSize = 24.sp, fontWeight = FontWeight.Bold)

        Spacer(modifier = Modifier.height(20.dp))

        // Profile Picture
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(Color.Gray),
            contentAlignment = Alignment.Center
        ) {
            if (!userPhotoUrl.isNullOrEmpty()) {
                Image(
                    painter = rememberAsyncImagePainter(userPhotoUrl),
                    contentDescription = "Profile Picture",
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Text(userName.firstOrNull()?.toString() ?: "U", fontSize = 32.sp, color = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // User Details
        Text(userName, fontSize = 20.sp, fontWeight = FontWeight.Medium)
        Text(userEmail, fontSize = 16.sp, color = Color.Gray)

        Spacer(modifier = Modifier.height(20.dp))

        // Edit Profile Button
        OutlinedButton(onClick = { navController.navigate("edit_profile_screen") }) {
            Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit Profile")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Edit Profile")
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Dark Mode Toggle
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Dark Mode", fontSize = 16.sp)
            Switch(
                checked = isDarkTheme,
                onCheckedChange = { onThemeToggle(!isDarkTheme) }
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Reminders Option
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { navController.navigate("reminder_screen") }
                .padding(vertical = 8.dp)
        ) {
            Text("Reminders", fontSize = 16.sp)
            Icon(imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Go to Reminders")
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Import Expenses from CSV Button
        Button(
            onClick = { showDialog = true }, // ✅ Opens the instructional dialog
            colors = ButtonDefaults.buttonColors(Color(0xFF10B981)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Icon(imageVector = Icons.Default.UploadFile, contentDescription = "Upload CSV")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Import Expenses from CSV")
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Sign Out Button
        Button(
            onClick = {
                auth.signOut()
                navController.navigate("sign_in_screen")
            },
            colors = ButtonDefaults.buttonColors(Color(0xFFEF4444)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(imageVector = Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Sign Out")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Sign Out")
        }

        Spacer(modifier = Modifier.height(20.dp)) // Extra space at the bottom
    }

    // ✅ Show the dialog when triggered
    if (showDialog) {
        ImportCsvDialog(
            onDismiss = { showDialog = false },
            onSelectCsv = {
                showDialog = false
                filePickerLauncher.launch("*/*") // Opens all file types
            }
        )
    }
}

@Composable
fun ImportCsvDialog(
    onDismiss: () -> Unit,
    onSelectCsv: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "How to Import Expenses from CSV",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(8.dp)
            ) {
                Text("📌 The CSV file must have these columns:")
                Text(
                    text = "Date, Description, Amount, Category",
                    fontWeight = FontWeight.Bold
                )
                Text("📅 Example:")
                Text(
                    text = """
                        2025-02-27, Lunch, 10.5, Dining
                        2025-02-26, Taxi, 5.0, Transport
                    """.trimIndent(),
                    fontSize = 14.sp,
                    fontFamily = FontFamily.Monospace,
                    color = Color.Gray
                )
                Text("✔ Once you select a file, expenses will be added automatically.")
                Text("⚠ This action cannot be undone.")
            }
        },
        confirmButton = {
            Button(
                onClick = onSelectCsv, // ✅ Only opens file picker when this is clicked
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
            ) {
                Icon(imageVector = Icons.Default.UploadFile, contentDescription = "Upload CSV", tint = MaterialTheme.colorScheme.background)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Select CSV", color = MaterialTheme.colorScheme.background)
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
        shape = RoundedCornerShape(12.dp) // ✅ Keeps modern rounded look
    )
}

@Preview
@Composable
fun ProfileScreenPreview() {
    val navController = rememberNavController()
    ProfileScreen(navController = navController, isDarkTheme = false, onThemeToggle = {})
}
