package com.hipla.channel.foodModule.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.hipla.channel.foodModule.network.request.LoginRequest
import com.hipla.channel.foodModule.network.response.LoginResponse
import com.hipla.channel.foodModule.repository.CommonRepository
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginViewModel constructor(private val repository: CommonRepository) : ViewModel() {

    companion object {
        const val TAG = "LoginViewModel"
    }

    val loginData = MutableLiveData<LoginResponse>()
    val errorMessage = MutableLiveData<String>()
    val loading = MutableLiveData<Boolean>()

    fun getLoginData(loginRequest: LoginRequest) {
        loading.value = true
        val response = repository.getLoginData(loginRequest)
        response.enqueue(object : Callback<LoginResponse> {
            override fun onResponse(
                call: Call<LoginResponse>,
                response: Response<LoginResponse>
            ) {
                when {
                    response.code() == 200 -> {
                        loginData.postValue(response.body())
                    }
                    response.code() == 406 -> {
                        errorMessage.postValue(response.message())
                    }
                    response.code() == 500 -> {
                        errorMessage.postValue(response.message())
                    }
                    else -> {
                        errorMessage.postValue(response.body()?.message)
                    }
                }
                loading.value = false
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                errorMessage.postValue(t.message)
                loading.value = false
            }
        })
    }
}