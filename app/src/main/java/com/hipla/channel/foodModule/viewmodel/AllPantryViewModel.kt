package com.hipla.channel.foodModule.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.hipla.sentinelvms.sentinelKt.foodModule.network.request.AllPantryRequest
import com.hipla.sentinelvms.sentinelKt.foodModule.network.request.EmployeeId
import com.hipla.sentinelvms.sentinelKt.foodModule.network.request.RoomId
import com.hipla.channel.foodModule.network.response.AllPantryResponse
import com.hipla.channel.foodModule.network.response.DefaultAllPantryResponse
import com.hipla.channel.foodModule.repository.CommonRepository
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AllPantryViewModel constructor(private val repository: CommonRepository) : ViewModel() {

    companion object {
        const val TAG = "AllPantryViewModel"
    }

    val allPantryData = MutableLiveData<AllPantryResponse>()
    val allPantryDataPagination = MutableLiveData<AllPantryResponse>()
    val errorMessage = MutableLiveData<String>()
    val loading = MutableLiveData<Boolean>()
    val defaultPantryData = MutableLiveData<DefaultAllPantryResponse>()

    fun getDefaultPantryMeetingRoom(
        apiKey: String,
        pantryRequest: RoomId,
        hashMap: HashMap<String, String>
    ) {
        loading.value = true
        val response = repository.getDefaultPantryMeetingRoom(apiKey, pantryRequest, hashMap)
        response.enqueue(object : Callback<DefaultAllPantryResponse> {
            override fun onResponse(
                call: Call<DefaultAllPantryResponse>,
                response: Response<DefaultAllPantryResponse>
            ) {
                when {
                    response.code() == 200 -> {
                        defaultPantryData.postValue(response.body())
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

            override fun onFailure(call: Call<DefaultAllPantryResponse>, t: Throwable) {
                TODO("Not yet implemented")
            }


        })
    }



    fun getDefaultPantry(
        apiKey: String,
        pantryRequest: EmployeeId,
        hashMap: HashMap<String, String>
    ) {
        loading.value = true
        val response = repository.getDefaultPantry(apiKey, pantryRequest, hashMap)
        response.enqueue(object : Callback<DefaultAllPantryResponse> {
            override fun onResponse(
                call: Call<DefaultAllPantryResponse>,
                response: Response<DefaultAllPantryResponse>
            ) {
                when {
                    response.code() == 200 -> {
                        defaultPantryData.postValue(response.body())
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

            override fun onFailure(call: Call<DefaultAllPantryResponse>, t: Throwable) {
               errorMessage.postValue(t.message)
            }


        })
    }



    fun getAllPantry(
        apiKey: String,
        pantryRequest: AllPantryRequest,
        hashMap: HashMap<String, String>
    ) {
        loading.value = true
        val response = repository.getPantryData(apiKey, pantryRequest, hashMap)
        response.enqueue(object : Callback<AllPantryResponse> {
            override fun onResponse(
                call: Call<AllPantryResponse>,
                response: Response<AllPantryResponse>
            ) {
                when {
                    response.code() == 200 -> {
                        allPantryData.postValue(response.body())
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

            override fun onFailure(call: Call<AllPantryResponse>, t: Throwable) {
                errorMessage.postValue(t.message)
                loading.value = false
            }
        })
    }

    fun getAllPantryDataPagination(
        apiKey: String,
        pantryRequest: AllPantryRequest,
        hashMap: HashMap<String, String>
    ) {
        val response = repository.getPantryData(apiKey, pantryRequest, hashMap)
        response.enqueue(object : Callback<AllPantryResponse> {
            override fun onResponse(
                call: Call<AllPantryResponse>,
                response: Response<AllPantryResponse>
            ) {
                when {
                    response.code() == 200 -> {
                        allPantryDataPagination.postValue(response.body())
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

            override fun onFailure(call: Call<AllPantryResponse>, t: Throwable) {
                errorMessage.postValue(t.message)
            }
        })
    }
}