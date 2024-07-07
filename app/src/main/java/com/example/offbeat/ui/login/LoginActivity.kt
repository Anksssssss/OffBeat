package com.example.offbeat.ui.login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.offbeat.MainActivity
import com.example.offbeat.R
import com.example.offbeat.databinding.ActivityLoginBinding
import com.example.offbeat.models.User
import com.example.offbeat.utils.SharedPrefManager
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()
        if (auth.currentUser != null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
        setListners()
    }

    private fun setListners() {
        binding.registerTv.setOnClickListener {
            val intent = Intent(this, SignupActivity::class.java)
            startActivity(intent)
            finish()
        }
        binding.btnSignIn.setOnClickListener {
            val email = binding.emailEdt.text.toString()
            val password = binding.passwordEdt.text.toString()
            val sharedPrefManager = SharedPrefManager(this)

            signIn(email, password)
        }
    }


    private fun signIn(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success
                    Log.d("SignIn", "signInWithEmail:success")
                    val user = auth.currentUser
                    val sharedPrefManager = SharedPrefManager(this)
                    val db = FirebaseFirestore.getInstance()
                    db.collection("users").document(user!!.uid)
                        .get()
                        .addOnSuccessListener { document->
                            val user = document.toObject(User::class.java)
                            sharedPrefManager.saveUser(user!!.name,user!!.email)
                            Toast.makeText(baseContext, "Sign In Successful.", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this, MainActivity::class.java)
                            startActivity(intent)
                            finish()
                        }.addOnFailureListener {
                            FirebaseAuth.getInstance().signOut()
                            Toast.makeText(baseContext, "Sign In Failed: ${it.message}", Toast.LENGTH_SHORT).show()
                        }

                } else {
                    // If sign in fails, display a message to the user.
                    Log.w("SignIn", "signInWithEmail:failure", task.exception)
                    Toast.makeText(
                        baseContext,
                        "Sign In Failed: ${task.exception?.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }
}