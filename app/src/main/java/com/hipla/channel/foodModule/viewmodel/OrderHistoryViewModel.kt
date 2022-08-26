package com.hipla.channel.foodModule.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.hipla.sentinelvms.sentinelKt.foodModule.network.request.PantryRequest
import com.hipla.sentinelvms.sentinelKt.foodModule.network.response.OrderHistoryResponse
import com.hipla.channel.foodModule.repository.CommonRepository
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class OrderHistoryViewModel constructor(private val repository: CommonRepository) : ViewModel() {

    companion object {
        const val TAG = "OrderHistoryViewModel"
    }

    val historyData = MutableLiveData<OrderHistoryResponse>()
    val historyDataPagination = MutableLiveData<OrderHistoryResponse>()
    val errorMessage = MutableLiveData<String>()
    val loading = MutableLiveData<Boolean>()

    fun getHistory(
        apiKey: String,
        pantryRequest: PantryRequest,
        hashMap: HashMap<String, String>
    ) {
//        loading.value = true

        loading.postValue(true)

        val response = repository.getHistory(apiKey, pantryRequest, hashMap)
        response.enqueue(object : Callback<OrderHistoryResponse> {
            override fun onResponse(
                call: Call<OrderHistoryResponse>,
                response: Response<OrderHistoryResponse>
            ) {
                when {
                    response.code() == 200 -> {
                        historyData.postValue(response.body())
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

            override fun onFailure(call: Call<OrderHistoryResponse>, t: Throwable) {
                errorMessage.postValue(t.message)
                loading.value = false
            }
        })
    }

    fun getHistoryDataPagination(
        apiKey: String,
        pantryRequest: PantryRequest,
        hashMap: HashMap<String, String>
    ) {
        val response = repository.getHistory(apiKey, pantryRequest, hashMap)
        response.enqueue(object : Callback<OrderHistoryResponse> {
            override fun onResponse(
                call: Call<OrderHistoryResponse>,
                response: Response<OrderHistoryResponse>
            ) {
                when {
                    response.code() == 200 -> {
                        historyDataPagination.postValue(response.body())
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

            override fun onFailure(call: Call<OrderHistoryResponse>, t: Throwable) {
                errorMessage.postValue(t.message)
            }
        })
    }
}