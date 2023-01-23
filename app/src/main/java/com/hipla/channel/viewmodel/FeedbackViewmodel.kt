package com.hipla.channel.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.hipla.channel.entity.response.QuestionRequest
import com.hipla.channel.entity.response.QuestionSubmitResponse
import com.hipla.channel.foodModule.repository.CommonRepository
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class FeedbackViewmodel constructor(private val repository: CommonRepository) : ViewModel() {

    companion object {
        const val TAG = ""
    }

    val covidHelpSubmitData = MutableLiveData<QuestionSubmitResponse>()
    val errorMessage = MutableLiveData<String>()
    val loading = MutableLiveData<Boolean>()

    fun submitQuestions(
        token: String,questionRequest: QuestionRequest
    ) {
        loading.value = true
        val response = repository.submitFeedbackQuestions(token,questionRequest)
        response.enqueue(object : Callback<QuestionSubmitResponse> {
            override fun onResponse(
                call: Call<QuestionSubmitResponse>,
                response: Response<QuestionSubmitResponse>
            ) {
                when {
                    response.code() == 200 -> {
                        covidHelpSubmitData.postValue(response.body())
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

            override fun onFailure(call: Call<QuestionSubmitResponse>, t: Throwable) {
                errorMessage.postValue(t.message)
                loading.value = false
            }
        })
    }
}