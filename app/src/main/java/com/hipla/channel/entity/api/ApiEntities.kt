package com.hipla.channel.entity.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass


@JsonClass(generateAdapter = true)
class ApiError(
    val statusCode: String,
    @Json(name = "errors") val errorList: List<ErrorInfo>?
) : Exception("Server error")

@JsonClass(generateAdapter = true)
class ErrorInfo(
    val msg: String?,
)

