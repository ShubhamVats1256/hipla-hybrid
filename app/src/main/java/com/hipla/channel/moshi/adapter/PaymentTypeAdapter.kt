package com.hipla.channel.moshi.adapter

import com.hipla.channel.entity.PaymentType
import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson

object PaymentTypeAdapter {
    @ToJson
    fun PaymentTypeToString(paymentType: PaymentType): String {
        return paymentType.typeId.toString()
    }

    @FromJson
    fun stringToPaymentType(paymentTypeId: String): PaymentType {
        return when (paymentTypeId) {
            PaymentType.Cash().typeId.toString() -> PaymentType.Cash()
            PaymentType.Cheque().typeId.toString() -> PaymentType.Cheque()
            PaymentType.Rtgs().typeId.toString() -> PaymentType.Rtgs()
            else -> PaymentType.Unknown()
        }
    }
}