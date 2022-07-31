package com.hipla.channel.repo

import com.hipla.channel.api.HiplaApiService
import com.hipla.channel.api.Resource
import com.hipla.channel.api.ResourceError
import com.hipla.channel.api.asResource

class HiplaRepo(private val hiplaApiService: HiplaApiService) {

    suspend fun fetchTest(
    ): Resource<String> {
        return try {
            return hiplaApiService.fetchTest().asResource()
        } catch (e: Exception) {
            ResourceError(e)
        }
    }


}