package com.hipla.channel.entity.response

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class VerifyOTPResponse(
    @Json(name = "data") val verifyOTPData: VerifyOTPData,
    val message: String,
    val status: String,
)