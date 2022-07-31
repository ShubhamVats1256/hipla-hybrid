package com.hipla.channel.entity

import com.hipla.channel.common.ApiErrorCode
import com.squareup.moshi.JsonClass
@JsonClass(generateAdapter = true)
data class ApiErrorMessage(
    val message: String,
    @ApiErrorCode val code: String,
    val payload: List<String>? = null
)
