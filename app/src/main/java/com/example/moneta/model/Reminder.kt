package com.example.moneta.model

data class Reminder(
    val id: String = "",  // Default value for Firestore deserialization
    val name: String = "",
    val time: String = "",
    val repeat: String = "",
    val userId: String = ""
) {
    // No-argument constructor
    constructor() : this("", "", "", "", "")
}
