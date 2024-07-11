package com.example.offbeat.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.offbeat.models.OffbeatDetail
import com.example.offbeat.repo.OffbeatLocationRepo
import com.example.offbeat.utils.Result
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

class OffbeatLocationViewModel: ViewModel() {
    private val _offBeatLocations = MutableLiveData<Result<List<OffbeatDetail>>>()
    val offBeatLocations: LiveData<Result<List<OffbeatDetail>>> get() = _offBeatLocations

    private val _signOutStatus = MutableLiveData<Boolean>()
    val signOutStatus: LiveData<Boolean> get() = _signOutStatus


    var offbeatDetail: OffbeatDetail? = null

    private val db = FirebaseFirestore.getInstance()
    fun fetchOffbeatLocations(){
        _offBeatLocations.value = Result.Loading
        viewModelScope.launch {
            val result = OffbeatLocationRepo.fetchOffbeatLocations()
            _offBeatLocations.value = result
        }
//        viewModelScope.launch {
//            try {
//                db.collection("OffBeatLocations")
//                    .get()
//                    .addOnSuccessListener { result ->
//                        val locations = mutableListOf<OffbeatDetail>()
//                        for(document in result){
//                            val offBeatDetail = document.toObject(OffbeatDetail::class.java)
//                            offBeatDetail.offBeatId = document.id
//                            locations.add(offBeatDetail)
//                        }
//                        _offBeatLocations.value =Result.Success (locations)
//                    }.addOnFailureListener { e->
//                        _offBeatLocations.value =Result.Error(e)
//                        Log.w("OffbeatLocationViewModel", "Error fetching Offbeat locations", e)
//                    }
//            }catch (e: Exception) {
//                _offBeatLocations.value = Result.Error(e)
//                Log.w("OffbeatLocationViewModel", "Error fetching Offbeat locations", e)
//            }
//        }

    }

    fun signOut() {
        viewModelScope.launch {
            FirebaseAuth.getInstance().signOut()
            _signOutStatus.value = true
        }
    }
}