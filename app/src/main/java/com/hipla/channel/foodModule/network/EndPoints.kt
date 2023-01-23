package com.hipla.channel.foodModule.network

object EndPoints {

    const val GET_ALL_PANTRY_DATA ="v1/pantry/find"
    const val LOGIN = "v1/auth/mobile/validatePasscode"
    const val GET_PANTRY_DATA ="v1/pantry/stock/find"
    const val SEND_ORDER_DETAIL = "v1/pantry/order"
    const val GET_HISTORY = "v1/pantry/order/find"
    const val GET_DEFAULT_PANTRY_DATA ="v1/pantry/findDefault"
    const val GET_QUICK_SETTING = "v1/quickSettings/sentinel"

    const val SUBMIT_FEEDBACK_QUESTIONS = "business/v1/feedback"
    const val GENERATE_OTP ="business/v1/user/generateOtp"
    const val VERIFY_OTP = "notification/v1/verifyAnonymousOTP"
}