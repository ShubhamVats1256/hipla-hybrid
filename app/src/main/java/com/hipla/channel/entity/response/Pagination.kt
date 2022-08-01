package com.hipla.channel.entity.response

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class Pagination(
    val currentPage: Int,
    val pageSize: Int,
    val total: Int,
    val totalPage: Int
)