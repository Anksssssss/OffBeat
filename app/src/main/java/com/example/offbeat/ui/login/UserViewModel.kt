package com.example.offbeat.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.offbeat.models.User
import com.example.offbeat.repo.UserRepository
import kotlinx.coroutines.launch

class UserViewModel:ViewModel() {
    private val _signupStatus = MutableLiveData<Result>()
    val signupResult: LiveData<Result> = _signupStatus

    private val _loginStatus = MutableLiveData<com.example.offbeat.utils.Result<User>>()
    val loginResult: LiveData<com.example.offbeat.utils.Result<User>> = _loginStatus

    fun signUp(email: String, password: String, name: String) {
        viewModelScope.launch {
            _signupStatus.value = Result.Loading
            val result = UserRepository.signUp(email, password, name)
            _signupStatus.value = when (result){
                is com.example.offbeat.utils.Result.Success-> {
                    Result.Success
                }
                is com.example.offbeat.utils.Result.Error -> {
                    Result.Error(result.exception.message)
                }
                else-> Result.Error("Unknown Error")
            }
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _loginStatus.value = com.example.offbeat.utils.Result.Loading
            val result = UserRepository.login(email, password)
            _loginStatus.value = result
        }
    }
}

sealed class Result {
    object Loading : Result()
    object Success : Result()
    data class Error(val message: String?) : Result()
}