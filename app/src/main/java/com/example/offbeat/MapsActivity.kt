package com.example.offbeat

import android.app.Activity
import android.content.Intent
import android.location.Geocoder
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.offbeat.databinding.ActivityMapsBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import java.util.Locale

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.mapFrag) as SupportMapFragment
        mapFragment.getMapAsync(this)

        setListeners()

    }

    private fun setListeners() {

        binding.btnConfirm.setOnClickListener {
            val selectedLocation = mMap.cameraPosition.target
            val resultIntent = Intent().apply {
                putExtra("lat", selectedLocation.latitude)
                putExtra("lon", selectedLocation.longitude)
            }
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }

        binding.searchBtn.setOnClickListener {
            val locName = binding.serchEdt.text.toString()
            if (locName.isNotEmpty()) {
                searchLoaction(locName)
            } else {
                Toast.makeText(this, "Please enter a location name", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun searchLoaction(location: String) {
        val geocoder = Geocoder(this, Locale.getDefault())
        try {
            val addresses = geocoder.getFromLocationName(location, 2)
            if (addresses!!.size > 0) {
                val address = addresses[0]
                val latLng = LatLng(address.latitude, address.longitude)
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
                //mMap.addMarker(MarkerOptions().position(latLng).title(location).draggable(true))
            } else {
                Toast.makeText(this, "Location not found", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error finding location", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onMapReady(p0: GoogleMap) {
        mMap = p0
        val location = LatLng(28.7041, 77.1025)
        mMap.moveCamera(CameraUpdateFactory.newLatLng(location))
    }
}