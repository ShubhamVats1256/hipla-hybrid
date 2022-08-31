package com.hipla.channel.api

import com.hipla.channel.entity.response.*
import retrofit2.Response
import retrofit2.http.*

interface HiplaApiService {

    @GET("business/v1/user")
    suspend fun fetchSalesUserList(
        @Query("currentPage") currentPage: Int,
        @Query("pageSize") pageSize: Int,
        @Query("app_code") appCode: String,
        @Query("page_name") pageName: String
    ): Response<SalesPageResponse>

    @POST("business/v1/user/generateOtp")
    suspend fun generateOTP(
        @Body otpRequestMap: Map<String, String>,
        @Query("app_code") appCode: String,
        @Query("page_name") pageName: String,
    ): Response<GenerateOTPResponse>

    @POST("business/v1/user/generateOtp")
    suspend fun generateOTPForRole(
        @Body otpRequestMap: Map<String, String>,
        @Query("app_code") appCode: String,
        @Query("page_name") pageName: String,
        @Query("role") role: String
    ): Response<GenerateOTPResponse>

    @POST("notification/v1/verifyAnonymousOTP")
    suspend fun verifyOtp(
        @Body otpRequestMap: Map<String, String>,
        @Query("app_code") appCode: String,
        @Query("page_name") pageName: String
    ): Response<VerifyOTPResponse>

    @POST("business/v1/extra/transactionInfo")
    suspend fun createApplication(
        @Body createApplicationRequestMap: Map<String, String>,
        @Query("app_code") appCode: String,
        @Query("page_name") pageName: String,
    ): Response<ApplicationCreateResponse>

    @PATCH("business/v1/extra/transactionInfo/{applicationId}")
    suspend fun updateApplication(
        @Path("applicationId") applicationId: Int,
        @Body createApplicationRequestMap: Map<String, String>,
        @Query("app_code") appCode: String,
        @Query("page_name") pageName: String,
    ): Response<ApplicationUpdateResponse>


    @POST("business/v1/extra/bookUnit")
    suspend fun createInventory(
        @Body createApplicationRequestMap: Map<String, String>,
        @Query("app_code") appCode: String,
        @Query("page_name") pageName: String,
    ): Response<ApplicationCreateResponse>

    @PATCH("business/v1/extra/bookUnit/{applicationId}")
    suspend fun updateInventory(
        @Path("applicationId") applicationId: Int,
        @Body createApplicationRequestMap: Map<String, String>,
        @Query("app_code") appCode: String,
        @Query("page_name") pageName: String,
    ): Response<ApplicationUpdateResponse>

    @GET("business/v1/user/{userId}")
    suspend fun fetchUserDetails(
        @Path("userId") userId: Int,
        @Query("app_code") appCode: String,
        @Query("page_name") pageName: String,
    ): Response<UserDetailsResponse>

    @GET
    suspend fun fetchUnits(
        @Url url : String,
        @Query("currentPage") currentPage: Int,
        @Query("pageSize") pageSize: Int,
        @Query("app_code") appCode: String,
        @Header("page_name") pageName: String
    ): Response<UnitPageResponse>

    @GET("business/v1/building?filters=[{\"siteId\": 0}]")
    suspend fun fetchFloors(
        @Query("currentPage") currentPage: Int,
        @Query("pageSize") pageSize: Int,
        @Query("app_code") appCode: String,
        @Header("page_name") pageName: String
    ): Response<FloorPageResponse>

}