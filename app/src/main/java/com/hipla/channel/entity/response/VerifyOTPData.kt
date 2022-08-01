package com.hipla.channel.entity.response

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class VerifyOTPData(
    val referenceId: String,
    val isVerified: Boolean,
)