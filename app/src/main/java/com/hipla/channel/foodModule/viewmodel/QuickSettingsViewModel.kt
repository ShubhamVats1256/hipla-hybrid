package com.hipla.sentinelvms.sentinelKt.foodModule.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.hipla.sentinelvms.sentinelKt.foodModule.network.QuickSettingResponse
import com.hipla.channel.foodModule.repository.CommonRepository

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class QuickSettingsViewModel constructor(private val repository: CommonRepository) : ViewModel() {

    companion object {
        const val TAG = "QuickSettingViewModel"
    }


    val quickSettingViewModel = MutableLiveData<QuickSettingResponse>()
    val errorMessage = MutableLiveData<String>()
    val loading = MutableLiveData<Boolean>()


    fun getQuickSettings(token : String) {
        loading.value = true
        val response = repository.getQuickSettings(token)
        response.enqueue(object : Callback<QuickSettingResponse> {
            override fun onResponse(
                call: Call<QuickSettingResponse>,
                response: Response<QuickSettingResponse>
            ) {
                when {
                    response.code() == 200 -> {
                        quickSettingViewModel.postValue(response.body())
                    }
                    response.code() == 422 -> {
                        errorMessage.postValue(response.message())
                    }
                    response.code() == 500 -> {
                        errorMessage.postValue(response.message())
                    }
                    else -> {
                        errorMessage.postValue("Something Went Wrong")
                    }
                }
                loading.value = false
            }

            override fun onFailure(call: Call<QuickSettingResponse>, t: Throwable) {
                errorMessage.postValue(t.message)
                loading.value = false
            }
        })
    }




}