package com.hipla.channel.repo

import com.hipla.channel.api.HiplaApiService
import com.hipla.channel.api.Resource
import com.hipla.channel.api.ResourceError
import com.hipla.channel.api.asResource

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


}