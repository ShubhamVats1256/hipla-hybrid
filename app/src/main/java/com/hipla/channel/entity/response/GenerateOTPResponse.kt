package com.hipla.channel.entity.response

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class GenerateOTPResponse(
    val referenceId: String,
    @Json(name = "record") val recordReference: RecordReference
)

@JsonClass(generateAdapter = true)
class RecordReference(
    val id: Int,
    val name: String? = null,
)
