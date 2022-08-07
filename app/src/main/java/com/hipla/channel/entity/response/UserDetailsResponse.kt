package com.hipla.channel.entity.response

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class UserDetailsResponse(
    @Json(name = "record") val userInfo: UserDetails?,
    val imageUploadUrl: String?,
    val imageReadUrl: String?
)

@JsonClass(generateAdapter = true)
class UserDetails(
    val id: Int,
    val name: String? = null,
    val emailId: String? = null,
    val phoneNumber: String? = null,
    val gender: String? = null,
    val profilePic: String? = null,
    val isActive: Boolean? = null,
)