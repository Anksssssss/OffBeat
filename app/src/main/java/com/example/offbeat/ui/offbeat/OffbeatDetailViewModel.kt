package com.example.offbeat.ui.offbeat

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.offbeat.models.OffbeatDetail
import com.example.offbeat.repo.OffbeatLocationRepo
import com.example.offbeat.repo.UserRepository
import com.example.offbeat.utils.Result
import kotlinx.coroutines.launch

class OffbeatDetailViewModel: ViewModel() {
    private val _uploadStatus = MutableLiveData<Result<String>>()
    val uploadStatus: LiveData<Result<String>> get() = _uploadStatus

    private val _uploadImageStatus = MutableLiveData<Result<String>>()
    val uploadImageStatus: LiveData<Result<String>> get() = _uploadImageStatus


    fun uploadImageToFirebaseStorage(imageUri: Uri, getFileExtension: (Uri) -> String?){
        _uploadImageStatus.value = Result.Loading
        viewModelScope.launch {
            val result = OffbeatLocationRepo.uploadImageToFirebaseStorage(imageUri, getFileExtension)
            _uploadImageStatus.value = result
        }
    }

    fun addOffbeatLocation(
        userId: String,
        offbeatDetail: OffbeatDetail
    ) {
        _uploadStatus.value = Result.Loading
        viewModelScope.launch {
            val result = OffbeatLocationRepo.addOffbeatLocation(offbeatDetail)
            if (result is Result.Success) {
                val result2 = UserRepository.addOffbeaLocationUploadedByUser(userId, offbeatDetail)
                if (result2 is Result.Success) {
                    _uploadStatus.value = Result.Success("Uploaded Successfully")
                }
            }
        }
    }
}