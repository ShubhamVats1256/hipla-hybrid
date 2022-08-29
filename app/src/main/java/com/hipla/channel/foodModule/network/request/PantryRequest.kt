package com.hipla.sentinelvms.sentinelKt.foodModule.network.request

import com.google.gson.annotations.SerializedName

class PantryRequest(
    @SerializedName("search") var search : List<Search>,
    @SerializedName("sort") var sort : Sort,
    @SerializedName("skip") var skip : Int,
    @SerializedName("take") var take : Int
) {

    data class Search (

        @SerializedName("key") var key : String,
        @SerializedName("value") var value : String,
        @SerializedName("comparison") var comparison : String

    )}
