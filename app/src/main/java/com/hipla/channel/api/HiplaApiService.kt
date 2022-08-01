package com.hipla.channel.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface HiplaApiService {

    @POST("business/v1/user/generateOtp")
    suspend fun generateOTP(@Body otpRequestMap: Map<String, String>): Response<Unit>

}