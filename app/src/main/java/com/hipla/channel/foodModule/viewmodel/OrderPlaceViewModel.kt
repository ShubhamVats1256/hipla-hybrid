package com.hipla.channel.foodModule.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.hipla.sentinelvms.sentinelKt.foodModule.network.request.OrderRequest
import com.hipla.sentinelvms.sentinelKt.foodModule.network.response.OrderPlaceResponse
import com.hipla.channel.foodModule.repository.CommonRepository
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class OrderPlaceViewModel constructor(private val repository: CommonRepository) : ViewModel() {

    companion object {
        const val TAG = "OrderPlaceViewModel"
    }

    val orderPlaceData = MutableLiveData<OrderPlaceResponse>()
    val errorMessage = MutableLiveData<String>()
    val loading = MutableLiveData<Boolean>()

     fun sendOrderDetail(
         apiKey: String,
         orderRequest: OrderRequest
     ) {
         loading.value = true
         val response = repository.sendOrderDetail(apiKey, orderRequest)
         response.enqueue(object : Callback<OrderPlaceResponse> {
             override fun onResponse(
                 call: Call<OrderPlaceResponse>,
                 response: Response<OrderPlaceResponse>
             ) {
                 when {
                     response.code() == 200 -> {
                         orderPlaceData.postValue(response.body())
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

             override fun onFailure(call: Call<OrderPlaceResponse>, t: Throwable) {
                 errorMessage.postValue(t.message)
                 loading.value = false
             }
         })
     }
}