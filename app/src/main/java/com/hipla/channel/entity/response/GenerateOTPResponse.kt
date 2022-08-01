package com.hipla.channel.entity.response

import com.squareup.moshi.Json

class GenerateOTPResponse(
    val referenceId: String,
    @Json(name = "record") val salesUserReference: SalesUserReference
)

class SalesUserReference(val id: String)