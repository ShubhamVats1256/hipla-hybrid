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

    suspend fun generateOtp(
        phoneNo: String,
        pageName: String,
        appCode: String
    ): Resource<GenerateOTPResponse> {
        return try {
            return hiplaApiService.generateOTP(
                otpRequestMap = mutableMapOf<String, String>().apply {
                    put("id", phoneNo)
                },
                appCode = appCode,
                pageName = pageName,
            ).asResource()
        } catch (e: Exception) {
            ResourceError(e)
        }
    }


    suspend fun generateOTPForRole(
        phoneNo: String,
        pageName: String,
        appCode: String,
        role: String,
        ): Resource<GenerateOTPResponse> {
        return try {
            return hiplaApiService.generateOTPForRole(
                otpRequestMap = mutableMapOf<String, String>().apply {
                    put("id", phoneNo)
                },
                appCode = appCode,
                pageName = pageName,
                role = role,
                ).asResource()
        } catch (e: Exception) {
            ResourceError(e)
        }
    }

    suspend fun verifyOtp(
        otp: String,
        userId: String,
        referenceId: String,
        pageName: String,
        appCode: String
    ): Resource<VerifyOTPResponse> {
        return try {
            return hiplaApiService.verifyOtp(
                otpRequestMap = mutableMapOf<String, String>().apply {
                    put("otp", otp)
                    put("userId", userId)
                    put("referenceId", referenceId)
                },
                appCode = appCode,
                pageName = pageName,
            ).asResource()
        } catch (e: Exception) {
            ResourceError(e)
        }
    }

    suspend fun createApplication(
        applicationRequest: ApplicationRequest,
        pageName: String,
        appCode: String
    ): Resource<ApplicationCreateResponse> {
        return try {
            return hiplaApiService.createApplication(
                applicationRequest.toCreateApplicationRequestMap(),
                appCode = appCode,
                pageName = pageName
            ).asResource()
        } catch (e: Exception) {
            ResourceError(e)
        }
    }

    suspend fun updateInventory(
        applicationRequest: ApplicationRequest,
        pageName: String,
        appCode: String
    ): Resource<ApplicationUpdateResponse> {
        return try {
            return hiplaApiService.updateInventory(
                applicationRequest.id,
                applicationRequest.toUpdateApplicationRequestMap(),
                appCode = appCode,
                pageName = pageName,
            ).asResource()
        } catch (e: Exception) {
            ResourceError(e)
        }
    }


    suspend fun createInventory(
        applicationRequest: ApplicationRequest,
        pageName: String,
        appCode: String
    ): Resource<ApplicationCreateResponse> {
        return try {
            return hiplaApiService.createInventory(
                applicationRequest.toCreateApplicationRequestMap(),
                appCode = appCode,
                pageName = pageName
            ).asResource()
        } catch (e: Exception) {
            ResourceError(e)
        }
    }

    suspend fun updateApplication(
        applicationRequest: ApplicationRequest,
        pageName: String,
        appCode: String
    ): Resource<ApplicationUpdateResponse> {
        return try {
            return hiplaApiService.updateApplication(
                applicationRequest.id,
                applicationRequest.toUpdateApplicationRequestMap(),
                appCode = appCode,
                pageName = pageName,
            ).asResource()
        } catch (e: Exception) {
            ResourceError(e)
        }
    }

    suspend fun fetchSalesUserList(
        currentPage: Int,
        pageSize: Int,
        pageName: String,
        appCode: String
    ): Resource<SalesPageResponse> {
        return try {
            return hiplaApiService.fetchSalesUserList(
                currentPage = currentPage,
                pageSize = pageSize,
                appCode = appCode,
                pageName = pageName
            ).asResource()
        } catch (e: Exception) {
            ResourceError(e)
        }
    }

    suspend fun fetchUnits(
        url : String,
        currentPage: Int,
        pageSize: Int,
        pageName: String,
        appCode: String
    ): Resource<UnitPageResponse> {
        return try {
            return hiplaApiService.fetchUnits(
                url = url,
                currentPage = currentPage,
                pageSize = pageSize,
                appCode = appCode,
                pageName = pageName
            ).asResource()
        } catch (e: Exception) {
            ResourceError(e)
        }
    }

    suspend fun fetchFloors(
        currentPage: Int,
        pageSize: Int,
        pageName: String,
        appCode: String
    ): Resource<FloorPageResponse> {
        return try {
            return hiplaApiService.fetchFloors(
                currentPage = currentPage,
                pageSize = pageSize,
                appCode = appCode,
                pageName = pageName
            ).asResource()
        } catch (e: Exception) {
            ResourceError(e)
        }
    }

    suspend fun fetchUserDetails(
        userId: Int,
        pageName: String,
        appCode: String
    ): Resource<UserDetailsResponse> {
        return try {
            return hiplaApiService.fetchUserDetails(
                userId = userId,
                appCode = appCode,
                pageName = pageName
            ).asResource()
        } catch (e: Exception) {
            ResourceError(e)
        }
    }


}