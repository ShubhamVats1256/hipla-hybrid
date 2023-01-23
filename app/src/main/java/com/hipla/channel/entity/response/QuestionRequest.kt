package com.hipla.channel.entity.response

import com.google.gson.annotations.SerializedName

class QuestionRequest(
        @SerializedName("value") var value : List<Value>,
        @SerializedName("phone") var phone_number : String
)

data class Value (

        @SerializedName("question") var question : String,
        @SerializedName("answer") var answer : String

)