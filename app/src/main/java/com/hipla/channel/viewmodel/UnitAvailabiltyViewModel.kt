package com.hipla.channel.viewmodel

import UnitAvailabiltyBase
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.hipla.channel.foodModule.network.request.LoginRequest
import com.hipla.channel.foodModule.network.response.LoginResponse
import com.hipla.channel.foodModule.repository.CommonRepository
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class UnitAvailabiltyViewModel constructor(private val repository: CommonRepository) : ViewModel() {


    val unitAvailabiltyData = MutableLiveData<UnitAvailabiltyBase>()
    val errorMessage = MutableLiveData<String>()
    val loading = MutableLiveData<Boolean>()

    fun checkUnitAvailabilty(url : String) {
        loading.value = true
        val response = repository.checkUnitAvailabilty(url)
        response.enqueue(object : Callback<UnitAvailabiltyBase> {
            override fun onResponse(
                call: Call<UnitAvailabiltyBase>,
                response: Response<UnitAvailabiltyBase>
            ) {
                when {
                    response.code() == 200 -> {
                        unitAvailabiltyData.postValue(response.body())
                    }

                    else -> {
                        errorMessage.postValue("")
                    }
                }
                loading.value = false
            }

            override fun onFailure(call: Call<UnitAvailabiltyBase>, t: Throwable) {
                errorMessage.postValue(t.message)
                loading.value = false
            }
        })
    }
}