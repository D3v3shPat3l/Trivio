package com.devesh.trivio_app

// Data class to represent an achievement with a name, its unlocked status, and a description
data class Achievement(
    val name: String = "",
    val unlocked: Boolean = false,
    val description: String = ""
)