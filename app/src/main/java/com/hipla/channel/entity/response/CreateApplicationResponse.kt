package com.hipla.channel.entity.response
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class CreateApplicationResponse(val id : Int)