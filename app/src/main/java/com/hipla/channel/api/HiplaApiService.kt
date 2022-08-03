package com.hipla.channel.api

import com.hipla.channel.entity.response.*
import retrofit2.Response
import retrofit2.http.*

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
    suspend fun createApplication(@Body createApplicationRequestMap: Map<String, String>): Response<ApplicationCreateResponse>

    @PATCH("business/v1/extra/transactionInfo/{applicationId}")
    suspend fun updateApplication(
        @Path("applicationId") applicationId: Int,
        @Body createApplicationRequestMap: Map<String, String>): Response<ApplicationUpdateResponse>

}