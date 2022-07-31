package com.hipla.channel.common

import androidx.annotation.StringDef

object Constant {
   const val EMPTY_STRING = ""
}

const val CLIENT_ERROR_NO_INTERNET = "CLIENT_NO_INTERNET"
const val CLIENT_GENERIC = "CLIENT_GENERIC_ERROR"
@Retention(AnnotationRetention.SOURCE)
@StringDef(
   CLIENT_ERROR_NO_INTERNET,
   CLIENT_GENERIC,
)
internal annotation class ApiErrorCode