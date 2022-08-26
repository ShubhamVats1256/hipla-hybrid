package com.hipla.sentinelvms.sentinelKt.foodModule.network.request

import com.google.gson.annotations.SerializedName

class AllPantryRequest(
    @SerializedName("search") var search : List<String>,
    @SerializedName("sort") var sort : Sort,
    @SerializedName("skip") var skip : Int,
    @SerializedName("take") var take : Int
) {
}

data class Sort (
    @SerializedName("key") var key : String,
    @SerializedName("order") var order : String
)


data class EmployeeId (
    @SerializedName("employeeId") var employeeId : String
)

data class RoomId (
    @SerializedName("roomId") var roomId : String
)