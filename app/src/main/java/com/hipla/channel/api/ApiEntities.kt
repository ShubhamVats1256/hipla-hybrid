package com.hipla.channel.api

import com.hipla.channel.common.ApiErrorCode
import com.hipla.channel.entity.PaymentType
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

class ApiException(val code: Int, val msg: ApiErrorMessage? = null) :
    Exception(msg?.message ?: "$code")

@JsonClass(generateAdapter = true)
class ApiErrorMessage(
    val message: String,
    @ApiErrorCode val code: String,
    val payload: List<String>? = null
)

@JsonClass(generateAdapter = true)
class VerifyOTPResponse(
    @Json(name = "data") val verifyOTPData: VerifyOTPData,
    val message: String,
    val status: String,
)

@JsonClass(generateAdapter = true)
class VerifyOTPData(
    val referenceId: String,
    val isVerified: Boolean,
)

class ApplicationRequest(
    val tag: String = "APPLICATION_TRANSACTION_DETAILS",
    val type: String = "TransactionDetailsDto",
    val unitId: Int = -1,
    val customerName: String,
    val customerPhoneNumber: String,
    val panNumber: String,
    val channelPartnerId: String,
    val identifier: String,
    val paymentType: PaymentType,
    val paymentDetails: String,
    val paymentProofImageUrl: String,
    val ownerId: Int,
    val createdBy: Int,
)