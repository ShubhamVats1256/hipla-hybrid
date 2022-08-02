package com.hipla.channel.api

import com.hipla.channel.entity.ApplicationRequest
import com.hipla.channel.entity.response.CreateApplicationResponse
import com.hipla.channel.entity.response.GenerateOTPResponse
import com.hipla.channel.entity.response.SalesPageResponse
import com.hipla.channel.entity.response.VerifyOTPResponse
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
    suspend fun generateOTP(@Body otpRequestMap: Map<String, String>): Response<GenerateOTPResponse>

    @POST("notification/v1/verifyAnonymousOTP")
    suspend fun verifyOtp(@Body otpRequestMap: Map<String, String>): Response<VerifyOTPResponse>

    @POST("business/v1/extra/transactionInfo")
    suspend fun createApplication(applicationRequest: ApplicationRequest): Response<CreateApplicationResponse>

}