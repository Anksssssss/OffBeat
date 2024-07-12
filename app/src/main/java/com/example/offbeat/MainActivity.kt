package com.example.offbeat

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.offbeat.adapter.PostsAdapter
import com.example.offbeat.databinding.ActivityMainBinding
import com.example.offbeat.models.OffbeatDetail
import com.example.offbeat.ui.login.LoginActivity
import com.example.offbeat.ui.offbeat.OffbeatDetailsActivity
import com.example.offbeat.ui.offbeat.OffbeatInfoActivity
import com.example.offbeat.utils.DataHolder
import com.example.offbeat.utils.OnItemClickListener
import com.example.offbeat.utils.Result
import com.example.offbeat.utils.SharedPrefManager
import com.example.offbeat.viewmodels.OffbeatLocationViewModel
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity(), OnItemClickListener {
    private lateinit var binding: ActivityMainBinding
    private lateinit var postsAdapter: PostsAdapter
    private val viewModel: OffbeatLocationViewModel by viewModels()
    private var searchByStateorCity = ""
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

    override fun onResume() {
        super.onResume()
        // Reset data when returning to the activity
        viewModel.fetchOffbeatLocations(reset = true)
    }

    private fun setObservers() {
        viewModel.offBeatLocations.observe(this) { result ->
            when (result) {
                is Result.Loading -> {
                    binding.apply {
                        if(postsAdapter.currentList.isEmpty()) {
                            recyclerViewMain.visibility = View.GONE
                            progressBar.visibility = View.VISIBLE
                            ErrorTv.visibility = View.GONE
                        }else{
                            nextProgressBar.visibility = View.VISIBLE
                        }
                        Log.d("MainActivity", "Loading")
                    }
                }

                is Result.Success -> {
                    binding.apply {
                        if(result.data.isEmpty()){
                            recyclerViewMain.visibility = View.GONE
                            progressBar.visibility = View.GONE
                            ErrorTv.visibility = View.VISIBLE
                            nextProgressBar.visibility = View.GONE
                        }else {
                            recyclerViewMain.visibility = View.VISIBLE
                            progressBar.visibility = View.GONE
                            ErrorTv.visibility = View.GONE
                            nextProgressBar.visibility = View.GONE
                        }
                    }
                    Log.d("MainActivity", "data size ${result.data}")
                    postsAdapter.submitList(result.data)
                    Log.d("MainActivity", "Success")
                }

                is Result.Error -> {
                    binding.apply {
                        recyclerViewMain.visibility = View.GONE
                        progressBar.visibility = View.GONE
                        ErrorTv.visibility = View.VISIBLE
                        nextProgressBar.visibility = View.GONE
                        ErrorTv.text = result.exception.localizedMessage
                    }
                    Log.d("MainActivity", "Error")
                }
            }
        }
        viewModel.signOutStatus.observe(this){
            if(it){
                val sharedPrefManager = SharedPrefManager(this)
                sharedPrefManager.clearuser()
                val intent = Intent(this, LoginActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                startActivity(intent)
                finish()
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

            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                    val totalItemCount = layoutManager.itemCount
                    val lastVisibleItem = layoutManager.findLastVisibleItemPosition()

                    if (lastVisibleItem + 1 >= totalItemCount) {
                        viewModel.fetchNextPage(searchByStateorCity)
                    }
                }
            })
        }
    }

    private fun setListeners() {
        binding.btnSighOut.setOnClickListener {
            viewModel.signOut()
        }
        binding.fab.setOnClickListener {
            val intent = Intent(this, OffbeatDetailsActivity::class.java)
            startActivity(intent)
        }
        binding.btnSearch.setOnClickListener {
            val searchText = binding.searchEdt.text.toString()
            if(searchText.isNotEmpty()) {
                searchByStateorCity = searchText.toString()
                viewModel.fetchOffbeatLocations(true,searchText.toString())
                binding.clearFilterTv.visibility = View.VISIBLE
            }
        }

        binding.clearFilterTv.setOnClickListener {
            binding.apply {
                viewModel.fetchOffbeatLocations(true)
                searchEdt.setText("")
                searchByStateorCity = ""
                clearFilterTv.visibility = View.GONE
            }
        }
    }

    override fun onItemClick(location: OffbeatDetail) {
        val intent = Intent(this, OffbeatInfoActivity::class.java)
        DataHolder.offbeatDetail = location
        startActivity(intent)
    }
}