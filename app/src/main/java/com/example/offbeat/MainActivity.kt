package com.example.offbeat

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.offbeat.adapter.PostsAdapter
import com.example.offbeat.databinding.ActivityMainBinding
import com.example.offbeat.models.OffbeatDetail
import com.example.offbeat.ui.login.LoginActivity
import com.example.offbeat.ui.offbeat.OffbeatDetailsActivity
import com.example.offbeat.ui.offbeat.OffbeatInfoActivity
import com.example.offbeat.utils.DataHolder
import com.example.offbeat.utils.OnItemClickListener
import com.example.offbeat.utils.Result
import com.example.offbeat.viewmodels.OffbeatLocationViewModel
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity(), OnItemClickListener {
    private lateinit var binding: ActivityMainBinding
    private lateinit var postsAdapter: PostsAdapter
    private val viewModel: OffbeatLocationViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        initView()
        setObservers()
        setListeners()

    }

    private fun setObservers() {
        viewModel.offBeatLocations.observe(this) { result ->
            when (result) {
                is Result.Loading -> {
                    binding.apply {
                        recyclerViewMain.visibility = View.GONE
                        progressBar.visibility = View.VISIBLE
                        ErrorTv.visibility = View.GONE
                    }
                }

                is Result.Success -> {
                    binding.apply {
                        recyclerViewMain.visibility = View.VISIBLE
                        progressBar.visibility = View.GONE
                        ErrorTv.visibility = View.GONE
                    }
                    postsAdapter.submitList(result.data)
                }

                is Result.Error -> {
                    binding.apply {
                        recyclerViewMain.visibility = View.GONE
                        progressBar.visibility = View.GONE
                        ErrorTv.visibility = View.VISIBLE
                    }
                }
            }
        }
    }

    private fun initView() {
        viewModel.fetchOffbeatLocations()
        postsAdapter = PostsAdapter(this)
        binding.apply {
            recyclerViewMain.visibility = View.GONE
            progressBar.visibility = View.VISIBLE
            ErrorTv.visibility = View.GONE
        }
        binding.recyclerViewMain.apply {
            layoutManager = LinearLayoutManager(
                this@MainActivity,
                LinearLayoutManager.VERTICAL,
                false
            )
            adapter = postsAdapter
        }
    }

    private fun setListeners() {
        binding.btnSighOut.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
        binding.fab.setOnClickListener {
            val intent = Intent(this, OffbeatDetailsActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onItemClick(location: OffbeatDetail) {
        val intent = Intent(this, OffbeatInfoActivity::class.java)
        DataHolder.offbeatDetail = location
        startActivity(intent)
    }
}