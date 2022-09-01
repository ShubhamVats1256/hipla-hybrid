package com.hipla.channel.entity

import com.squareup.moshi.JsonClass


@JsonClass(generateAdapter = true)
data class ApplicationRequest(
    var id: Int = -1,
    val tag: String,
    val type: String,
    var unitId: Int = -1,
    var customerName: String? = null,
    var customerLastName: String? = null,
    var customerPhoneNumber: String? = null,
    var remark: String? = null,
    var applicationReferenceId: String? = "",
    var panNumber: String? = null,
    var channelPartnerId: String? = null,
    var amountPayable: String? = null,
    var identifier: String? = null,
    var paymentType: PaymentType = PaymentType.Unknown(),
    var paymentDetails: String? = null,
    var paymentProofImageUrl: String? = null,
    var floorPreferenceId: Int = -1,
    var ownerId: Int? = null,
    var floorId: Int? = 0,

    var createdBy: Int? = null,
    var chequeDate: String? = null,
)

