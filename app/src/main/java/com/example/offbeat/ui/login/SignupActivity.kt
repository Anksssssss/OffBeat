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
import com.example.offbeat.R
import com.example.offbeat.databinding.ActivitySignupBinding
import com.example.offbeat.models.User
import com.example.offbeat.utils.SharedPrefManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SignupActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignupBinding
    private lateinit var auth: FirebaseAuth
    private val viewModel: UserViewModel by viewModels()

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
        setListeners()
        setObservers()
    }

    private fun setListeners() {
        binding.btnSignUp.setOnClickListener {
            val email = binding.emailEdt.text.toString()
            val password = binding.passwordEdt.text.toString()
            val confirmPassword = binding.confirmPasswordEdt.text.toString()
            val name = binding.nameEdt.text.toString()
            if (isInputValid(email, password, confirmPassword, name)) {
                viewModel.signUp(email,password,name)
            }
        }
    }

    private fun setObservers(){
        viewModel.signupResult.observe(this){result->
            when(result){
                is Result.Loading->{
                    showLoading(true)
                }
                is Result.Success->{
                    showLoading(false)
                    Log.d("SignUp", "createUserWithEmail:success")
                    Toast.makeText(baseContext, "Sign Up Successful.", Toast.LENGTH_SHORT)
                        .show()
                    auth.signOut()
                    navigateToLogin()
                }
                is Result.Error -> {
                    showLoading(false)
                    Log.d("SignUp", result.message.toString())
                    Toast.makeText(baseContext, result.message, Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }

    private fun isInputValid(email: String, password: String, cnfPassword: String, name: String): Boolean {
        return when {
            email.isEmpty() -> {
                showToast("Email is required.")
                false
            }
            password.isEmpty() -> {
                showToast("Password is required.")
                false
            }

            password.length < 6 -> {
                showToast("Password must be at least 6 characters.")
                false
            }
            password != cnfPassword -> {
                showToast("Passwords do not match.")
                false
            }
            name.isEmpty() -> {
                showToast("Name is required.")
                false
            }
            else -> true
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnSignUp.visibility =  if (isLoading) View.GONE else View.VISIBLE
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun showToast(message: String) {
        Toast.makeText(baseContext, message, Toast.LENGTH_SHORT).show()
    }

}