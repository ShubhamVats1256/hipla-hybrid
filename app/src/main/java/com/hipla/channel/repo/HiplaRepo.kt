package com.hipla.channel.repo

import com.hipla.channel.api.HiplaApiService
import com.hipla.channel.entity.ApplicationRequest
import com.hipla.channel.entity.api.Resource
import com.hipla.channel.entity.api.ResourceError
import com.hipla.channel.entity.response.*
import com.hipla.channel.extension.asResource
import com.hipla.channel.extension.toCreateApplicationRequestMap
import com.hipla.channel.extension.toUpdateApplicationRequestMap

class HiplaRepo(private val hiplaApiService: HiplaApiService) {

    suspend fun generateOtp(phoneNo: String): Resource<GenerateOTPResponse> {
        return try {
            return hiplaApiService.generateOTP(
                otpRequestMap = mutableMapOf<String, String>().apply {
                    put("id", phoneNo)
                }
            ).asResource()
        } catch (e: Exception) {
            ResourceError(e)
        }
    }

    suspend fun verifyOtp(
        otp: String,
        userId: String,
        referenceId: String
    ): Resource<VerifyOTPResponse> {
        return try {
            return hiplaApiService.verifyOtp(
                otpRequestMap = mutableMapOf<String, String>().apply {
                    put("otp", otp)
                    put("userId", userId)
                    put("referenceId", referenceId)
                }
            ).asResource()
        } catch (e: Exception) {
            ResourceError(e)
        }
    }

    suspend fun createApplication(applicationRequest: ApplicationRequest): Resource<ApplicationCreateResponse> {
        return try {
            return hiplaApiService.createApplication(applicationRequest.toCreateApplicationRequestMap())
                .asResource()
        } catch (e: Exception) {
            ResourceError(e)
        }
    }

    suspend fun updateApplication(applicationRequest: ApplicationRequest): Resource<ApplicationUpdateResponse> {
        return try {
            return hiplaApiService.updateApplication(
                applicationRequest.id,
                applicationRequest.toUpdateApplicationRequestMap()
            ).asResource()
        } catch (e: Exception) {
            ResourceError(e)
        }
    }

    suspend fun fetchSalesUserList(
        currentPage: Int,
        pageSize: Int,
        pageName: String
    ): Resource<SalesPageResponse> {
        return try {
            return hiplaApiService.fetchSalesUserList(currentPage, pageSize, pageName).asResource()
        } catch (e: Exception) {
            ResourceError(e)
        }
    }

    suspend fun fetchUserDetails(userId: Int): Resource<UserDetailsResponse> {
        return try {
            return hiplaApiService.fetchUserDetails(userId).asResource()
        } catch (e: Exception) {
            ResourceError(e)
        }
    }


}