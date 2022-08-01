package com.hipla.channel.common

import androidx.annotation.StringDef

object Constant {
    const val EMPTY_STRING = ""
    const val API_ERROR = "api_error"
}

const val CLIENT_ERROR_NO_INTERNET = "CLIENT_NO_INTERNET"
const val CLIENT_GENERIC = "CLIENT_GENERIC_ERROR"

@Retention(AnnotationRetention.SOURCE)
@StringDef(
    CLIENT_ERROR_NO_INTERNET,
    CLIENT_GENERIC,
)
internal annotation class ApiErrorCode

const val KEY_SALES_USER_ID = "customer_name"
const val KEY_CUSTOMER_NAME = "customer_name"
const val KEY_CUSTOMER_PHONE_NO = "customer_phone"
const val KEY_CUSTOMER_PAN = "customer_pan"
const val KEY_PAYMENT_VALUE = "payment_value"
const val KEY_PAYMENT_REFERENCE_NO = "payment_ref"
const val KEY_PAYMENT_DATE = "payment_date"
const val KEY_PARTNER_NAME  = "partner_name"

