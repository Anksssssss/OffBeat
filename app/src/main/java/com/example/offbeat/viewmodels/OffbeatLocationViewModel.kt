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
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

class OffbeatLocationViewModel : ViewModel() {
    private val _offBeatLocations = MutableLiveData<Result<List<OffbeatDetail>>>()
    val offBeatLocations: LiveData<Result<List<OffbeatDetail>>> get() = _offBeatLocations

    private val _signOutStatus = MutableLiveData<Boolean>()
    val signOutStatus: LiveData<Boolean> get() = _signOutStatus


    private var lastDocument: DocumentSnapshot? = null
    private val pageSize: Long = 4
    private var noMoreData = false
    private var isFetching = false

    fun fetchOffbeatLocations(reset: Boolean = false) {
        if (reset) {
            lastDocument = null
            noMoreData = false
        }
        if (noMoreData || isFetching) return
        isFetching = true
        _offBeatLocations.value = Result.Loading
        viewModelScope.launch {
            val result = OffbeatLocationRepo.fetchOffbeatLocations(lastDocument, pageSize)
            if (result is Result.Success) {
                lastDocument = result.data.second
                _offBeatLocations.value = Result.Success(result.data.first)
                if (result.data.first.size < pageSize) {
                    noMoreData = true
                    Log.d("OffbeatLocationViewModel", "No more data")
                }
                Log.d(
                    "OffbeatLocationViewModel",
                    "fetchOffbeatLocations: ${result.data.first}"
                )
            } else {
                _offBeatLocations.value = result as Result.Error
            }
            isFetching = false
        }


    }

    fun fetchNextPage() {
        if (noMoreData || isFetching) return
        isFetching = true
        val currentList =
            (_offBeatLocations.value as? Result.Success)?.data ?: emptyList()
        _offBeatLocations.value = Result.Loading
        viewModelScope.launch {
            val result = OffbeatLocationRepo.fetchOffbeatLocations(lastDocument, pageSize)
            if (result is Result.Success) {
                Log.d("OffbeatLocationViewModel", result.data.first.size.toString())
                lastDocument = result.data.second
                if (result.data.first.size < pageSize) {
                    noMoreData = true
                    Log.d("OffbeatLocationViewModel", "No more data")
                }
                _offBeatLocations.value = Result.Success(currentList + result.data.first)
                Log.d("OffbeatLocationViewModel", "Fetched next page")
            } else {
                _offBeatLocations.value = result as Result.Error
            }
            isFetching = false
        }
    }

    fun signOut() {
        viewModelScope.launch {
            FirebaseAuth.getInstance().signOut()
            _signOutStatus.value = true
        }
    }
}