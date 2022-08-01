package com.hipla.channel.entity.api

import com.hipla.channel.common.ApiErrorCode
import com.squareup.moshi.JsonClass

class ApiException(val code: Int, val msg: ApiErrorMessage? = null) :
    Exception(msg?.message ?: "$code")

@JsonClass(generateAdapter = true)
class ApiErrorMessage(
    val message: String,
    @ApiErrorCode val code: String,
    val payload: List<String>? = null
)
