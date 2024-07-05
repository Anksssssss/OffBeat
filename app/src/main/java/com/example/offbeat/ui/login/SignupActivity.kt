package com.example.offbeat.ui.login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.offbeat.R
import com.example.offbeat.databinding.ActivitySignupBinding
import com.example.offbeat.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SignupActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignupBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        auth = FirebaseAuth.getInstance()


        binding.btnSignUp.setOnClickListener {
            val email = binding.emailEdt.text.toString()
            val password = binding.passwordEdt.text.toString()
            val name = binding.nameEdt.text.toString()
            if (verified(email, password, name)) {
                signUp(email, password, name)
            }

        }
    }

    private fun verified(email: String, password: String, name: String): Boolean {
        if (email.isEmpty()) {
            Toast.makeText(baseContext, "Email is required.", Toast.LENGTH_SHORT).show()
            return false
        }
        if (password.isEmpty()) {
            Toast.makeText(baseContext, "Password is required.", Toast.LENGTH_SHORT).show()
            return false
        }
        if (password.length < 6) {
            Toast.makeText(
                baseContext,
                "Password must be at least 6 characters.",
                Toast.LENGTH_SHORT
            ).show()
            return false
        }
        if (binding.nameEdt.text.toString().isEmpty()) {
            Toast.makeText(baseContext, "Name is required.", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun signUp(email: String, password: String, name: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign up success
                    Log.d("SignUp", "createUserWithEmail:success")
                    val user = auth.currentUser
                    val uid = user!!.uid
                    val newUser = User(uid, name, email)

                    val db = FirebaseFirestore.getInstance()
                    db.collection("users").document(uid ?: "")
                        .set(newUser)
                        .addOnSuccessListener {
                            Log.d("SignUp", "User data added to Firestore")
                            Toast.makeText(baseContext, "Sign Up Successful.", Toast.LENGTH_SHORT)
                                .show()
                            auth.signOut()
                            val intent = Intent(this, LoginActivity::class.java)
                            startActivity(intent)
                            finish()
                        }.addOnFailureListener { e ->
                            Log.w("SignUp", "Error adding user data to Firestore", e)
                            Toast.makeText(
                                baseContext,
                                "Sign Up Failed: ${e.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                } else {
                    // If sign up fails, display a message to the user.
                    Log.w("SignUp", "createUserWithEmail:failure", task.exception)
                    Toast.makeText(
                        baseContext,
                        "Sign Up Failed: ${task.exception?.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }
}