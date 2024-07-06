package com.example.offbeat.ui.offbeat

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.denzcoskun.imageslider.constants.ScaleTypes
import com.denzcoskun.imageslider.models.SlideModel
import com.example.offbeat.R
import com.example.offbeat.databinding.ActivityOffbeatInfoBinding
import com.example.offbeat.models.OffbeatDetail
import com.example.offbeat.utils.DataHolder
import com.example.offbeat.viewmodels.OffbeatLocationViewModel

class OffbeatInfoActivity : AppCompatActivity() {
    private lateinit var binding: ActivityOffbeatInfoBinding
   // private val viewModel: OffbeatLocationViewModel by viewModels()
    private lateinit var offBeatLocation: OffbeatDetail
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
        initView()
        setListeners()
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
    }

    private fun initView() {
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
    }
}