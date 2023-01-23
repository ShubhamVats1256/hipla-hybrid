package com.hipla.channel.entity

data class PushOtpRequest(
    val otp : String,
    val referenceId : String,
    val userId : Int
)
