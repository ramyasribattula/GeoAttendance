package com.cutmap.geoattendance

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var emailInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var confirmPasswordInput: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()

        emailInput = findViewById(R.id.registerEmail)
        passwordInput = findViewById(R.id.registerPassword)
        confirmPasswordInput = findViewById(R.id.registerConfirmPassword)

        findViewById<Button>(R.id.btnRegisterSubmit).setOnClickListener {
            registerUser()
        }

        findViewById<TextView>(R.id.tvLoginRedirect).setOnClickListener {
            finish()
        }
    }

    private fun registerUser() {
        val email = emailInput.text.toString().trim()
        val password = passwordInput.text.toString()
        val confirmPassword = confirmPasswordInput.text.toString()

        if (email.isEmpty()) {
            emailInput.error = "Email is required"
            return
        }

        if (password.length < 6) {
            passwordInput.error = "Password must be at least 6 characters"
            return
        }

        if (password != confirmPassword) {
            confirmPasswordInput.error = "Passwords do not match"
            return
        }

        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                Toast.makeText(this, "Registration successful!", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Registration failed: ${it.message}", Toast.LENGTH_LONG).show()
            }
    }
}
