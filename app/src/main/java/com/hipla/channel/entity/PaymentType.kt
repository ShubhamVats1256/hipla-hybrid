package com.hipla.channel.entity

sealed class PaymentType {
    abstract val typeId: Int
    class DD(override val typeId: Int = 0) : PaymentType()
    class Cheque(override val typeId: Int = 1) : PaymentType()
    class Rtgs(override val typeId: Int = 2) : PaymentType()
    class Unknown(override val typeId: Int = -1) : PaymentType()
}