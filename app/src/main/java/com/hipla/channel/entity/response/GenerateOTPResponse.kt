package com.hipla.channel.entity.response

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class GenerateOTPResponse(
    val referenceId: String,
    @Json(name = "record") val userReference: UserReference
)

class UserReference(
    val id: Int,
    val name: String? = null,
)