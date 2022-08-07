package com.hipla.channel.entity.response

import com.hipla.channel.entity.UnitInfo
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class UnitPageResponse(
    val pagination: Pagination,
    @Json(name = "records")  val unitList: List<UnitInfo>?
)