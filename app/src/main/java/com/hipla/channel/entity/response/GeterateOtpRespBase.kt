package com.hipla.channel.entity.response

data class GeterateOtpRespBase (

//    val pagination : GeterateOtpRespPagination,
    val referenceId : String,
    val record : GeterateOtpRespRecord,
    val status : GenerateOtpStatus,
)
