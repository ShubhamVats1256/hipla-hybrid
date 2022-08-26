package com.hipla.channel.foodModule.network.request

import com.google.gson.annotations.SerializedName

class LoginRequest(
    @SerializedName("macAddress" ) var macAddress : String? = null,
    @SerializedName("deviceName" ) var deviceName : String? = null,
    @SerializedName("passcode" ) var passcode : String? = null,
    @SerializedName("deviceType" ) var deviceType : String? = null
) {
}