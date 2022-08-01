package com.hipla.channel.entity

class ApplicationRequest(
    val tag: String = "APPLICATION_TRANSACTION_DETAILS",
    val type: String = "TransactionDetailsDto",
    val unitId: Int = -1,
    val customerName: String,
    val customerPhoneNumber: String,
    val panNumber: String,
    val channelPartnerId: String,
    val identifier: String,
    val paymentType: PaymentType,
    val paymentDetails: String,
    val paymentProofImageUrl: String,
    val ownerId: Int,
    val createdBy: Int,
)

