package com.hipla.channel.entity.response

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class ApplicationCreateResponse(
    @Json(name = "record") val userReference: ApplicationRecordReference,
    val imageUploadUrl: String?,
    val imageReadUrl: String?
)

@JsonClass(generateAdapter = true)
class ApplicationRecordReference(
    val id: Int,
    val name: String? = null,
    @Json(name = "extraInfo") val applicationRecordExtraInfo: ApplicationRecordExtraInfo,
)

@JsonClass(generateAdapter = true)
class ApplicationRecordExtraInfo(
    val paymentProofImageUrl: String?
)

@JsonClass(generateAdapter = true)
class ApplicationUpdateResponse(
    val status: RecordStatus,
    val displayCounter : Int?
)