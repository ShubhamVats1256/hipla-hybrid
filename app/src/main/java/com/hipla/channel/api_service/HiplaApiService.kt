package com.hipla.channel.api_service

import retrofit2.Response
import retrofit2.http.GET

interface HiplaApiService {

    @GET("v1/bpi/currentprice.json")
    suspend fun fetchTest(): Response<String>

}