package com.hipla.channel.entity.response

import com.hipla.channel.entity.SalesUser
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class SalesPageResponse(
    val pagination: Pagination,
    @Json(name = "records")  val salesUserList: List<SalesUser>?
)