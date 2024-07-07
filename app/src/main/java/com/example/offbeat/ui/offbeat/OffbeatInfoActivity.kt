package com.example.offbeat.ui.offbeat

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.denzcoskun.imageslider.constants.ScaleTypes
import com.denzcoskun.imageslider.models.SlideModel
import com.example.offbeat.R
import com.example.offbeat.adapter.ReviewAdapter
import com.example.offbeat.databinding.ActivityOffbeatInfoBinding
import com.example.offbeat.models.OffbeatDetail
import com.example.offbeat.models.Review
import com.example.offbeat.utils.DataHolder
import com.example.offbeat.utils.Result
import com.example.offbeat.utils.SharedPrefManager
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class OffbeatInfoActivity : AppCompatActivity() {
    private lateinit var binding: ActivityOffbeatInfoBinding
    private lateinit var offBeatLocation: OffbeatDetail
    private lateinit var reviewsAdapter: ReviewAdapter
    private val viewModel: OffbeatInformationViewModel by viewModels()
    private lateinit var reviewList: MutableList<Review>
    private lateinit var sharedPrefManager: SharedPrefManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOffbeatInfoBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        offBeatLocation = DataHolder.offbeatDetail!!
        reviewList = offBeatLocation.reviewList
        initView()
        setListeners()
        setObservers()
    }

    private fun setObservers() {
        viewModel.addReviewResult.observe(this){result->
            when(result){
                is Result.Loading->{
                    binding.submitReviewBtn.isEnabled = false
                }
                is Result.Success->{
                    binding.reviewEdt.text.clear()
                    binding.submitReviewBtn.isEnabled = true
                    Toast.makeText(this, "Review Submitted", Toast.LENGTH_SHORT).show()
                }
                is Result.Error->{
                    binding.submitReviewBtn.isEnabled = true
                    Toast.makeText(this, "Error Occurred", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setListeners() {
        binding.viewOnMapBtn.setOnClickListener {
           if(offBeatLocation.latitude != ""){
               val lat = offBeatLocation.latitude
               val lon = offBeatLocation.longitude
               val uri = "geo:$lat,$lon"
               val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
               intent.setPackage("com.google.android.apps.maps")
               if(intent.resolveActivity(packageManager)!=null){
                   startActivity(intent)
               }else{
                   Toast.makeText(this,"Google Maps is not installed",Toast.LENGTH_SHORT).show()
               }

           }else{
               Toast.makeText(this,"Location not available",Toast.LENGTH_SHORT).show()
           }
        }

        binding.submitReviewBtn.setOnClickListener {
            binding.reviewTv.setTextColor(ContextCompat.getColor(this,R.color.text_grey))
            val review = binding.reviewEdt.text.toString()
            val user = sharedPrefManager.getUserName()
            val postId = offBeatLocation.offBeatId
            val calendar = Calendar.getInstance()
            val dateFormat = SimpleDateFormat("d MMM yyyy", Locale.ENGLISH)
            val formattedDate = dateFormat.format(calendar.time)
            val time = formattedDate
            val newReview = Review(postId,user!!,review,time)
            viewModel.addReview(newReview,postId)
            reviewList.add(0,newReview)
            setReviews(reviewList)
        }
    }

    private fun initView() {
        sharedPrefManager = SharedPrefManager(this)
        val imageList = ArrayList<SlideModel>()
        for(i in offBeatLocation.photos){
            imageList.add(SlideModel(i,ScaleTypes.CENTER_CROP))
        }
        binding.apply {
            imageSlider.setImageList(imageList)
            locName.text = offBeatLocation.locationName
            locDescription.text = offBeatLocation.description
            directionsNoteTV.text = offBeatLocation.address

            if(offBeatLocation.stayDuration != ""){
                duration.text = offBeatLocation.stayDuration
            }else{
                duration.text = "NA"
            }

            if(offBeatLocation.bestTime != ""){
                bestTime.text = offBeatLocation.bestTime
            }else{
                bestTime.text = "NA"
            }
        }
        setReviews(reviewList)
    }

    private fun setReviews(reviewList: List<Review>) {
        val intitialReviews = if(reviewList.size>3) reviewList.take(3) else reviewList
        reviewsAdapter = ReviewAdapter(intitialReviews)
        binding.reviewRv.apply {
            layoutManager = LinearLayoutManager(this@OffbeatInfoActivity,LinearLayoutManager.VERTICAL,false)
            adapter = reviewsAdapter
        }

        binding.seeAllReviews.visibility = if(reviewList.size>3) View.VISIBLE else View.GONE
        binding.seeAllReviews.setOnClickListener {
            reviewsAdapter.updateList(reviewList)
            binding.seeAllReviews.visibility = View.GONE
        }
    }
}