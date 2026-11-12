package com.cutmap.geoattendance

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.CustomCredential
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var emailInput: EditText
    private lateinit var passwordInput: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()

        // Maintain login session: if already logged in, go directly to Dashboard
        if (auth.currentUser != null) {
            openDashboard()
            return
        }

        emailInput = findViewById(R.id.email)
        passwordInput = findViewById(R.id.password)

        findViewById<Button>(R.id.loginBtn).setOnClickListener {
            login()
        }

        findViewById<Button>(R.id.registerBtn).setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        findViewById<Button>(R.id.googleBtn).setOnClickListener {
            googleSignIn()
        }
    }

    private fun login() {
        val e = emailInput.text.toString().trim()
        val p = passwordInput.text.toString()

        if (e.isEmpty() || p.isEmpty()) {
            Toast.makeText(this, "Enter email and password", Toast.LENGTH_SHORT).show()
            return
        }

        auth.signInWithEmailAndPassword(e, p)
            .addOnSuccessListener {
                Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show()
                openDashboard()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Login failed: ${it.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun googleSignIn() {
        lifecycleScope.launch {
            try {
                val manager = CredentialManager.create(this@MainActivity)

                val option = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(getString(R.string.default_web_client_id))
                    .setAutoSelectEnabled(false)
                    .build()

                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(option)
                    .build()

                val result = manager.getCredential(this@MainActivity, request)
                val credential = result.credential

                if (credential is CustomCredential &&
                    credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {

                    val googleCredential = GoogleIdTokenCredential.createFrom(credential.data)
                    val firebaseCredential = GoogleAuthProvider.getCredential(googleCredential.idToken, null)

                    auth.signInWithCredential(firebaseCredential)
                        .addOnSuccessListener {
                            Toast.makeText(this@MainActivity, "Google Sign-In Successful", Toast.LENGTH_SHORT).show()
                            openDashboard()
                        }
                        .addOnFailureListener {
                            Toast.makeText(this@MainActivity, "Firebase Auth failed: ${it.message}", Toast.LENGTH_LONG).show()
                        }
                }

            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "Google Sign-In: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun openDashboard() {
        val intent = Intent(this, DashboardActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
