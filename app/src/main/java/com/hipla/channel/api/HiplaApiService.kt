package com.hipla.channel.api

import com.hipla.channel.entity.Test
import org.json.JSONObject
import retrofit2.Response
import retrofit2.http.GET

interface HiplaApiService {

    @GET("fact")
    suspend fun fetchTest(): Response<Test>

}