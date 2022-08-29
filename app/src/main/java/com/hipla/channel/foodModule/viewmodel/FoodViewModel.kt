package com.hipla.channel.foodModule.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.hipla.sentinelvms.sentinelKt.foodModule.network.response.FoodListResponse
import com.hipla.channel.foodModule.repository.CommonRepository

class FoodViewModel constructor(private val repository: CommonRepository) : ViewModel() {

    companion object {
        const val TAG = "FoodViewModel"
    }

    val dailyRosterData = MutableLiveData<FoodListResponse>()
    val errorMessage = MutableLiveData<String>()
    val loading = MutableLiveData<Boolean>()

   /* fun getDailyRoster(
        token: String,
        dateRequest: String
    ) {
        loading.value = true
        val response = repository.getDailyRoster(token, dateRequest)
        response.enqueue(object : Callback<FoodListResponse> {
            override fun onResponse(
                call: Call<FoodListResponse>,
                response: Response<FoodListResponse>
            ) {
                when {
                    response.code() == 200 -> {
                        dailyRosterData.postValue(response.body())
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

            override fun onFailure(call: Call<FoodListResponse>, t: Throwable) {
                errorMessage.postValue(t.message)
                loading.value = false
            }
        })
    }*/
}