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

const val KEY_SALES_USER_ID = "key_sales_user_id"
const val KEY_FLOW_CONFIG = "key_flow_config"

const val KEY_APP_REQ = "app_req"
const val KEY_APPLICATION_SERVER_INF0 = "app_server_info"
const val KEY_PARTNER = "partner_mobile_no"

const val SUCCESS = "success"
const val FAILURE = "failure"

const val UNIT_AVAILABLE = "AVAILABLE"
const val UNIT_HOLD = "HOLD"
const val UNIT_BOOKED = "BOOKED"
