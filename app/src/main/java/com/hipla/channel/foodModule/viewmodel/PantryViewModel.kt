package com.hipla.channel.foodModule.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.hipla.sentinelvms.sentinelKt.foodModule.network.request.PantryRequest
import com.hipla.channel.foodModule.network.response.PantryResponse
import com.hipla.channel.foodModule.repository.CommonRepository
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PantryViewModel constructor(private val repository: CommonRepository) : ViewModel() {

    companion object {
        const val TAG = "PantryViewModel"
    }

    val pantryDetail = MutableLiveData<PantryResponse>()
    val pantryDetailPagination = MutableLiveData<PantryResponse>()
    val errorMessage = MutableLiveData<String>()
    val errorMessagePantryDetail = MutableLiveData<String>()

    val loading = MutableLiveData<Boolean>()

    fun getPantryDetail(
        apiKey: String,
        pantryRequest: PantryRequest,
        hashMap: HashMap<String, String>
    ) {
        loading.value = true
        val response = repository.getPantryDetail(apiKey, pantryRequest, hashMap)
        response.enqueue(object : Callback<PantryResponse> {
            override fun onResponse(
                call: Call<PantryResponse>,
                response: Response<PantryResponse>
            ) {
                when {
                    response.code() == 200 -> {
                        pantryDetail.postValue(response.body())
                    }

                    response.code() == 201 -> {
                        pantryDetail.postValue(response.body())
                    }

                    else -> {
                        errorMessagePantryDetail.postValue("Something Went Wrong")
                    }
                }
                loading.value = false
            }

            override fun onFailure(call: Call<PantryResponse>, t: Throwable) {
                errorMessage.postValue(t.message)
                loading.value = false
            }
        })
    }

    fun getPantryDetailPagination(
        apiKey: String,
        pantryRequest: PantryRequest,
        hashMap: HashMap<String, String>
    ) {
        val response = repository.getPantryDetail(apiKey, pantryRequest, hashMap)
        response.enqueue(object : Callback<PantryResponse> {
            override fun onResponse(
                call: Call<PantryResponse>,
                response: Response<PantryResponse>
            ) {
                when {
                    response.code() == 200 -> {
                        pantryDetailPagination.postValue(response.body())
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
            }

            override fun onFailure(call: Call<PantryResponse>, t: Throwable) {
                errorMessage.postValue(t.message)
            }
        })
    }
}