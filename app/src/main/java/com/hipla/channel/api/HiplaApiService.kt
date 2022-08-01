package com.hipla.channel.api

import com.hipla.channel.entity.response.SalesPageResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface HiplaApiService {

    @GET("business/v1/user")
    suspend fun fetchSalesUserList(
        @Query("currentPage") currentPage: Int,
        @Query("pageSize") pageSize: Int
    ): Response<SalesPageResponse>

    @POST("business/v1/user/generateOtp")
    suspend fun generateOTP(@Body otpRequestMap: Map<String, String>): Response<Unit>

    @POST("notification/v1/verifyAnonymousOTP")
    suspend fun verifyOtp(@Body otpRequestMap: Map<String, String>): Response<Unit>

}