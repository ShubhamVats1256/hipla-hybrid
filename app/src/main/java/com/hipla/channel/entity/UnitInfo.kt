package com.hipla.channel.entity

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class UnitInfo(
    val id: Int,
    val name: String,
    val floorId: Int?,
    val status: UnitStatus?
)

@JsonClass(generateAdapter = true)
data class UnitStatus(
    val currentStatus: String,
    val color: String
)