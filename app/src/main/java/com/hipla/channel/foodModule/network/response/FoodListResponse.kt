package com.hipla.sentinelvms.sentinelKt.foodModule.network.response

import com.google.gson.annotations.SerializedName

class FoodListResponse(
    @SerializedName("status") var status: String? = null,
    @SerializedName("message") var message: String? = null,
    @SerializedName("data") var data: String? = null
) {
}