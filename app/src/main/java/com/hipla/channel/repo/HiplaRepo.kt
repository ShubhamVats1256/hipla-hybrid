package com.hipla.channel.repo

import com.hipla.channel.api.HiplaApiService
import com.hipla.channel.api.Resource
import com.hipla.channel.api.ResourceError
import com.hipla.channel.api.asResource
import com.hipla.channel.entity.Test
import org.json.JSONObject

class HiplaRepo(private val hiplaApiService: HiplaApiService) {

    suspend fun fetchTest(): Resource<Test> {
        return try {
            return hiplaApiService.fetchTest().asResource()
        } catch (e: Exception) {
            ResourceError(e)
        }
    }


}