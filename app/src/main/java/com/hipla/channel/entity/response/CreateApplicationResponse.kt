package com.hipla.channel.entity.response
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class CreateApplicationResponse(@Json(name = "record")val userReference : RecordReference)

