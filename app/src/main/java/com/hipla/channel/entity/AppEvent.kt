package com.hipla.channel.entity

open class AppEvent(open val id: Int, open val message: String? = null)

class AppEventWithData<T>(
    override val id: Int,
    override val message: String? = null,
    val extras: T? = null
) : AppEvent(id, message)

const val APP_EVENT_ERROR = 0

const val APP_EVENT_APPLICATION_CREATED = 100
const val APP_EVENT_APPLICATION_FAILED = 101

const val APP_EVENT_CUSTOMER_OTP_VERIFICATION_COMPLETE = 200
const val APP_EVENT_CUSTOMER_OTP_GENERATE_SUCCESS = 201
const val APP_EVENT_CUSTOMER_OTP_GENERATE_FAILED= 202
const val APP_EVENT_CUSTOMER_OTP_VERIFICATION_FAILED = 203
const val APP_EVENT_CUSTOMER_OTP_VERIFICATION_SUCCESS = 204
