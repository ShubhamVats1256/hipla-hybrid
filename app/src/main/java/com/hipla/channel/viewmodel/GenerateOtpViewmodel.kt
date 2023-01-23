package com.hipla.channel.viewmodel


import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.hipla.channel.entity.PushOtpRequest
import com.hipla.channel.entity.response.GenerateOtpRequestBase
import com.hipla.channel.entity.response.GeterateOtpRespBase
import com.hipla.channel.entity.response.VerifyOtpRespBase
import com.hipla.channel.foodModule.repository.CommonRepository
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class GenerateOtpViewmodel constructor(private val repository: CommonRepository) : ViewModel() {

    val gson = Gson()
    val userResptype = object : TypeToken<GeterateOtpRespBase>() {}.type
    val generateOtpData = MutableLiveData<GeterateOtpRespBase>()
    val errorMessage = MutableLiveData<String>()
    val loading = MutableLiveData<Boolean>()

    fun generateOtp(generateOtpRequestBase : GenerateOtpRequestBase) {
        loading.value = true
        val response = repository.generateOtp(generateOtpRequestBase)
        response.enqueue(object : Callback<GeterateOtpRespBase> {
            override fun onResponse(
                call: Call<GeterateOtpRespBase>,
                response: Response<GeterateOtpRespBase>
            ) {
                when {
                    response.code() == 200 -> {
                        generateOtpData.postValue(response.body())
                    }
                    response.code() == 201 -> {
                        generateOtpData.postValue(response.body())
                    }


                    else -> {



                        try {
                            val errorResponse: GeterateOtpRespBase? = gson.fromJson(response.errorBody()!!.charStream(), userResptype)
                            errorMessage.postValue(errorResponse!!.status.error)
                        }
                        catch (e : Exception){
                            errorMessage.postValue("ERROR : "+response.code())

                        }

                    }
                }
                loading.value = false
            }

            override fun onFailure(call: Call<GeterateOtpRespBase>, t: Throwable) {
                errorMessage.postValue(t.message)
                loading.value = false
            }
        })
    }

    val submiteOtpData = MutableLiveData<VerifyOtpRespBase>()
    val  submiteOtperrorMessage = MutableLiveData<String>()
    val  submiteOtploading = MutableLiveData<Boolean>()


    fun submitOtp(pushOtpRequest : PushOtpRequest) {
        submiteOtploading.value = true
        val response = repository.pushOtp(pushOtpRequest)
        response.enqueue(object : Callback<VerifyOtpRespBase> {
            override fun onResponse(
                call: Call<VerifyOtpRespBase>,
                response: Response<VerifyOtpRespBase>
            ) {
                when {
                    response.code() == 200 -> {
                        submiteOtpData.postValue(response.body())
                    }
                    response.code() == 201 -> {
                        submiteOtpData.postValue(response.body())
                    }


                    response.code() == 400 -> {
                        submiteOtperrorMessage.postValue(response.message())
                    }

                    response.code() == 406 -> {
                        submiteOtperrorMessage.postValue(response.message())
                    }
                    response.code() == 500 -> {
                        submiteOtperrorMessage.postValue(response.message())
                    }
                    else -> {
                        submiteOtperrorMessage.postValue("Server Error!")
                    }
                }
                submiteOtploading.value = false
            }

            override fun onFailure(call: Call<VerifyOtpRespBase>, t: Throwable) {
                submiteOtperrorMessage.postValue(t.message)
                submiteOtploading.value = false
            }
        })
    }

}