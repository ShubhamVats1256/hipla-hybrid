package com.hipla.channel.entity.response

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class ApplicationResponse(
    @Json(name = "record") val userReference: ApplicationRecordReference
)

@JsonClass(generateAdapter = true)
class ApplicationRecordReference(
    val id: Int,
    val name: String? = null,
    @Json(name = "extraInfo") val applicationRecordExtraInfo: ApplicationRecordExtraInfo,
    val imageUploadUrl: String?,
    val imageReaddUrl: String?
)

@JsonClass(generateAdapter = true)
class ApplicationRecordExtraInfo(
    val paymentProofImageUrl: String?
)