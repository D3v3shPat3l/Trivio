package com.devesh.trivio_app

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

// Activity for user login, registration, and password reset functionality
class LoginActivity : AppCompatActivity() {

    private lateinit var mAuth: FirebaseAuth

    // Called when the activity is created
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_login)

        mAuth = FirebaseAuth.getInstance()

        val currentUser = mAuth.currentUser
        if (currentUser != null) {
            val username = currentUser.email?.substringBefore("@")
            saveUsernameToPreferences(username)
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("username", username)
            startActivity(intent)
            finish()
            return
        }

        val emailField = findViewById<EditText>(R.id.et_email)
        val passwordField = findViewById<EditText>(R.id.et_password)
        val loginButton = findViewById<Button>(R.id.btn_login)
        val signUpButton = findViewById<Button>(R.id.btn_signup)
        val forgotPasswordText = findViewById<TextView>(R.id.tv_forgot_password)

        loginButton.setOnClickListener {
            val email = emailField.text.toString().trim()
            val password = passwordField.text.toString().trim()
            loginUser(email, password)
        }

        signUpButton.setOnClickListener {
            val email = emailField.text.toString().trim()
            val password = passwordField.text.toString().trim()
            registerUser(email, password)
        }

        forgotPasswordText.setOnClickListener {
            val email = emailField.text.toString().trim()
            resetPassword(email)
        }
    }

    // Function to log in a user
    private fun loginUser(email: String, password: String) {
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }

        mAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val username = mAuth.currentUser?.email?.substringBefore("@")
                    saveUsernameToPreferences(username)
                    val intent = Intent(this, MainActivity::class.java)
                    intent.putExtra("username", username)
                    Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show()
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this, "Authentication failed. Check your email and password.", Toast.LENGTH_SHORT).show()
                }
            }
    }

    // Function to register a new user
    private fun registerUser(email: String, password: String) {
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }

        mAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val username = mAuth.currentUser?.email?.substringBefore("@")
                    saveUsernameToPreferences(username)
                    val intent = Intent(this, MainActivity::class.java)
                    intent.putExtra("username", username)
                    Toast.makeText(this, "Registration successful", Toast.LENGTH_SHORT).show()
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this, "Registration failed. Try again later.", Toast.LENGTH_SHORT).show()
                }
            }
    }

    // Function to send a password reset email
    private fun resetPassword(email: String) {
        if (email.isEmpty()) {
            Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show()
            return
        }

        mAuth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Password reset email sent.", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Failed to send password reset email.", Toast.LENGTH_SHORT).show()
                }
            }
    }

    // Function to save the username to shared preferences
    private fun saveUsernameToPreferences(username: String?) {
        val sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("USERNAME", username)
        editor.apply()
    }
}
