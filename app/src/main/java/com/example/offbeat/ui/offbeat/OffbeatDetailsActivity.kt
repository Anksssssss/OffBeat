package com.example.offbeat.ui.offbeat

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.offbeat.MainActivity
import com.example.offbeat.MapsActivity
import com.example.offbeat.R
import com.example.offbeat.adapter.PhotosAdapter
import com.example.offbeat.databinding.ActivityOffbeatDetailsBinding
import com.example.offbeat.models.OffbeatDetail
import com.example.offbeat.ui.login.LoginActivity
import com.example.offbeat.ui.login.Result
import com.example.offbeat.utils.SharedPrefManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class OffbeatDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOffbeatDetailsBinding
    private val photoUrls = mutableListOf<String>()
    private lateinit var photosAdapter: PhotosAdapter
    private lateinit var mapActivityResultLauncher: ActivityResultLauncher<Intent>
    private lateinit var imageActivityResultLauncher: ActivityResultLauncher<Intent>
    private val viewModel: OffbeatDetailViewModel by viewModels()
    private var latitude = ""
    private var longitude = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityOffbeatDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        mapActivityResultLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                val data = result.data
                latitude = data?.getDoubleExtra("lat", 0.0).toString()
                longitude = data?.getDoubleExtra("lon", 0.0).toString()
                binding.latitudeEdt.setText(latitude.toString())
                binding.longitudeEdt.setText(longitude.toString())
            }
        }

        imageActivityResultLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                handleImageSelectionResult(result.data!!)
            } else {
                Toast.makeText(baseContext, "No Image Selected", Toast.LENGTH_SHORT).show()
            }
        }

        initView()
        setListeners()
        setObservers()
    }

    private fun setObservers() {
        viewModel.uploadImageStatus.observe(this){result->
            when(result){
                is com.example.offbeat.utils.Result.Loading->{
                    binding.apply {
                        placeholderImg.visibility = View.GONE
                        selectorTv1.visibility = View.GONE
                        selectorTv2.visibility = View.GONE
                        progressBar.visibility = View.VISIBLE
                    }
                }
                is com.example.offbeat.utils.Result.Success->{
                    storeImageUrlInFirestore(result.data)
                }
                is com.example.offbeat.utils.Result.Error -> {
                    Log.e("Error", result.exception.toString())
                }
            }
        }

        viewModel.uploadStatus.observe(this){result->
            when(result){
                is com.example.offbeat.utils.Result.Loading->{
                   binding.apply {
                       btnUpload.visibility = View.GONE
                       progressBarUpload.visibility = View.VISIBLE
                   }
                }
                is com.example.offbeat.utils.Result.Success->{
                    binding.progressBarUpload.visibility = View.GONE
                    Log.d("Upload", "Offbeat location data added to Firestore")
                    Toast.makeText(baseContext, "Upload Successful", Toast.LENGTH_SHORT)
                        .show()
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
                is com.example.offbeat.utils.Result.Error -> {
                    binding.apply {
                        btnUpload.visibility = View.VISIBLE
                        progressBarUpload.visibility = View.GONE
                    }
                    Log.e("Error", result.exception.toString())
                    Toast.makeText(
                        baseContext,
                        "Failed to upload data",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun handleImageSelectionResult(data: Intent) {
        if (data.clipData != null) {
            // Multiple images selected
            val count = data.clipData!!.itemCount
            for (i in 0 until count) {
                val imageUri = data.clipData!!.getItemAt(i).uri
                viewModel.uploadImageToFirebaseStorage(imageUri, ::getFileExtension)
               // uploadImageToFirebaseStorage(imageUri)
            }
        } else if (data.data != null) {
            // Single image selected
            val imageUri = data.data!!
            viewModel.uploadImageToFirebaseStorage(imageUri, ::getFileExtension)
            //uploadImageToFirebaseStorage(imageUri)

        }
    }

    private fun initView() {
        photosAdapter = PhotosAdapter(photoUrls)
        binding.selectPicsRv.apply {
            layoutManager = GridLayoutManager(this@OffbeatDetailsActivity, 3)
            adapter = photosAdapter
        }
    }

    private fun setListeners() {
        binding.imageSelector.setOnClickListener {
            openFileChooser()
        }

        binding.btnMarkLocation.setOnClickListener {
            val intent = Intent(this, MapsActivity::class.java)
            mapActivityResultLauncher.launch(intent)
        }

        binding.btnUpload.setOnClickListener {
            val locationName = binding.locationNameEdt.text.toString()
            val description = binding.descriptionEdt.text.toString()
            val stayDuration = binding.durationEdt.text.toString()
            val bestTime = binding.bestTimeEdt.text.toString()
            val address = binding.directionNotesEdt.text.toString()
            val id = FirebaseAuth.getInstance().currentUser!!.uid
            if (locationName.isNotEmpty() && description.isNotEmpty() && address.isNotEmpty()) {
                val sharedPrefManager = SharedPrefManager(this)
                val userName = sharedPrefManager.getUserName()?:""
                val offbeatDetails = OffbeatDetail(
                    id, userName, locationName,  description, stayDuration, bestTime, address, photoUrls,
                    latitude,
                    longitude
                )
                viewModel.addOffbeatLocation(id,offbeatDetails)
               // addOffbeatLocationDetails(offbeatDetails)
            } else if (photoUrls.isEmpty()) {
                Toast.makeText(baseContext, "Please select atleast one image", Toast.LENGTH_SHORT)
                    .show()
            } else {
                Toast.makeText(baseContext, "Please fill all the fields", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun addOffbeatLocationDetails(
        locationName: String,
        description: String,
        stayDuration: String,
        bestTime: String,
        address: String,
        id: String
    ) {
        val sharedPrefManager = SharedPrefManager(this)
        val name = sharedPrefManager.getUserName()?:""
        val offbeatDetails = OffbeatDetail(
            id, name, locationName,  description, stayDuration, bestTime, address, photoUrls,
            latitude,
            longitude
        )
        val db = FirebaseFirestore.getInstance()


        db.collection("users").document(id)
            .collection("OffBeatLocations")
            .add(offbeatDetails).addOnSuccessListener {
                db.collection("OffBeatLocations")
                    .add(offbeatDetails)
                    .addOnSuccessListener {
                        Log.d("Upload", "Offbeat location data added to Firestore")
                        Toast.makeText(baseContext, "Upload Successful", Toast.LENGTH_SHORT)
                            .show()
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()

                    }.addOnFailureListener { e ->
                        Log.w("Upload", "Error adding offbeat location to Firestore", e)
                        Toast.makeText(
                            baseContext,
                            "Failed to upload ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            }.addOnFailureListener { e->
                Log.w("Upload", "Error adding offbeat location to Firestore", e)
            }
    }

    private fun openFileChooser() {
        val intent = Intent()
        intent.type = "image/*"
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        intent.action = Intent.ACTION_GET_CONTENT
        imageActivityResultLauncher.launch(Intent.createChooser(intent, "Select Picture"))
        // startActivityForResult(Intent.createChooser(intent, "Select Picture"), 100)
    }

    private fun uploadImageToFirebaseStorage(imageUri: Uri) {
        val storageReference = FirebaseStorage.getInstance().reference
        val fileReference = storageReference.child(
            "uploads/${System.currentTimeMillis()}.${
                getFileExtension(imageUri)
            }"
        )

        fileReference.putFile(imageUri)
            .addOnSuccessListener { taskSnapshot ->
                fileReference.downloadUrl.addOnSuccessListener { uri ->
                    val downloadUrl = uri.toString()
                    // Store the download URL in Firestore
                    storeImageUrlInFirestore(downloadUrl)
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to upload image: ${e.message}", Toast.LENGTH_SHORT)
                    .show()
            }
    }

    private fun getFileExtension(uri: Uri): String? {
        val contentResolver = contentResolver
        val mime = MimeTypeMap.getSingleton()
        return mime.getExtensionFromMimeType(contentResolver.getType(uri))
    }

    private fun storeImageUrlInFirestore(downloadUrl: String) {
        photoUrls.add(downloadUrl)
        photosAdapter.notifyDataSetChanged()
        // You can update the UI or proceed with storing the location details once all images are uploaded
        binding.apply {
            progressBar.visibility = View.GONE
            selectPicsRv.visibility = View.VISIBLE
        }
    }
}