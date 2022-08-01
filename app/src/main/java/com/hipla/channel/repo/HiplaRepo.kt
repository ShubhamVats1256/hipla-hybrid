package com.hipla.channel.repo

import com.hipla.channel.api.HiplaApiService
import com.hipla.channel.entity.api.Resource
import com.hipla.channel.entity.api.ResourceError
import com.hipla.channel.api.asResource
import com.hipla.channel.entity.response.SalesPageResponse

class HiplaRepo(private val hiplaApiService: HiplaApiService) {

    suspend fun generateOtp(phoneNo: String): Resource<Unit> {
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

    suspend fun verifyOtp(otp: String, userId : String, referenceId : String): Resource<Unit> {
        return try {
            return hiplaApiService.generateOTP(
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

    suspend fun createApplication(otp: String, userId : String, referenceId : String): Resource<Unit> {
        return try {
            return hiplaApiService.generateOTP(
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

    suspend fun fetchSalesUserList(currentPage: Int, pageSize: Int): Resource<SalesPageResponse> {
        return try {
            return hiplaApiService.fetchSalesUserList(currentPage, pageSize).asResource()
        } catch (e: Exception) {
            ResourceError(e)
        }
    }


}