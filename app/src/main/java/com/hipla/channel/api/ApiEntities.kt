package com.hipla.channel.api

import com.hipla.channel.common.ApiErrorCode
import com.squareup.moshi.JsonClass

data class ApiException(val code: Int, val msg: ApiErrorMessage? = null) :
    Exception(msg?.message ?: "$code")

@JsonClass(generateAdapter = true)
data class ApiErrorMessage(
    val message: String,
    @ApiErrorCode val code: String,
    val payload: List<String>? = null
)
