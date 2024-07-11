package com.example.offbeat.ui.login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.offbeat.MainActivity
import com.example.offbeat.R
import com.example.offbeat.databinding.ActivityLoginBinding
import com.example.offbeat.models.User
import com.example.offbeat.utils.Result
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
    private val viewModel: UserViewModel by viewModels()

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
        setObservers()
    }

    private fun setObservers() {
        viewModel.loginResult.observe(this){result->
            when(result){
                is Result.Loading->{
                    showLoading(true)
                }
                is Result.Success->{
                    showLoading(false)
                    Log.d("Login", "signInWithEmail:success")
                    Toast.makeText(baseContext, "Login Successful.", Toast.LENGTH_SHORT).show()
                    saveUserToSharedPref(result.data)
                    navigateToMain()
                }
                is Result.Error->{
                    showLoading(false)
                    Log.d("Login", result.exception.message.toString())
                    Toast.makeText(baseContext, result.exception.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setListners() {
        binding.registerTv.setOnClickListener {
            val intent = Intent(this, SignupActivity::class.java)
            startActivity(intent)
            finish()
        }
        binding.btnLogin.setOnClickListener {
            val email = binding.emailEdt.text.toString()
            val password = binding.passwordEdt.text.toString()
            if (isInputValid(email, password)) {
                viewModel.login(email, password)
            }
           // val sharedPrefManager = SharedPrefManager(this)

           // signIn(email, password)
        }
    }

    private fun isInputValid(email: String, password: String): Boolean {
        return when {
            email.isEmpty() -> {
                showToast("Email is required.")
                false
            }

            password.isEmpty() -> {
                showToast("Password is required.")
                false
            }

            else -> true
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnLogin.visibility = if (isLoading) View.GONE else View.VISIBLE
        binding.registerTv.visibility = if (isLoading) View.GONE else View.VISIBLE
    }

    private fun saveUserToSharedPref(user: User) {
        val sharedPrefManager = SharedPrefManager(this)
        sharedPrefManager.saveUser(user.name, user.email)
    }

    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun showToast(message: String) {
        Toast.makeText(baseContext, message, Toast.LENGTH_SHORT).show()
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
                        .addOnSuccessListener { document ->
                            val user = document.toObject(User::class.java)
                            sharedPrefManager.saveUser(user!!.name, user!!.email)
                            Toast.makeText(baseContext, "Sign In Successful.", Toast.LENGTH_SHORT)
                                .show()
                            val intent = Intent(this, MainActivity::class.java)
                            startActivity(intent)
                            finish()
                        }.addOnFailureListener {
                            FirebaseAuth.getInstance().signOut()
                            Toast.makeText(
                                baseContext,
                                "Sign In Failed: ${it.message}",
                                Toast.LENGTH_SHORT
                            ).show()
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