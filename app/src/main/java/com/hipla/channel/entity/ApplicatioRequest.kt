package com.hipla.channel.entity

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ApplicationRequest(
    var id: Int = -1,
    val tag: String = "APPLICATION_TRANSACTION_DETAILS",
    val type: String = "TransactionDetailsDto",
    var unitId: Int = -1,
    var customerName: String? = null,
    var customerLastName: String? = null,
    var customerPhoneNumber: String? = null,
    var panNumber: String? = null,
    var channelPartnerId: String? = null,
    var amountPayable: String? = null,
    var identifier: String? = null,
    var paymentType: PaymentType = PaymentType.Unknown(),
    var paymentDetails: String? = null,
    var paymentProofImageUrl: String? = null,
    var floorPreferenceId: Int = -1,
    var ownerId: Int? = null,
    val createdBy: Int? = null,
)

