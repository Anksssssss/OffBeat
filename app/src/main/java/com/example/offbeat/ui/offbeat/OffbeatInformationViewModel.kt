package com.example.offbeat.ui.offbeat

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.offbeat.models.OffbeatDetail
import com.example.offbeat.models.Review
import com.example.offbeat.repo.OffbeatLocationRepo
import com.example.offbeat.repo.UserRepository
import com.example.offbeat.utils.Result
import com.example.offbeat.utils.SharedPrefManager
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

class OffbeatInformationViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()

    private val _addReviewResult = MutableLiveData<Result<Unit>>()
    val addReviewResult: LiveData<Result<Unit>> get() = _addReviewResult

    private val _reviews = MutableLiveData<Result<List<Review>>>()
    val reviews: LiveData<Result<List<Review>>> get() = _reviews

    fun addReview(review: Review, docId: String) {
        _addReviewResult.value = Result.Loading
        viewModelScope.launch {
            val result = OffbeatLocationRepo.addReview(review,docId)
            _addReviewResult.value = result
        }
    }

    fun fetchReviews(docId: String) {
        _reviews.value = Result.Loading

        viewModelScope.launch {
            try {
                val offbeatDetailRef = db.collection("OffBeatLocations").document(docId)
                offbeatDetailRef.get().addOnSuccessListener {
                    if (it.exists()) {
                        val offBeatDetail = it.toObject(OffbeatDetail::class.java)
                        _reviews.value = Result.Success(offBeatDetail!!.reviewList ?: emptyList())
                    } else {
                        _reviews.value = Result.Error(Exception("Document does not exist"))
                    }
                }
            } catch (e: Exception) {
                _reviews.value = Result.Error(e)
                Log.w("OffbeatInformationViewModel", "Error fetching Reviews", e)
            }
        }
    }
}