package com.hipla.sentinelvms.sentinelKt.foodModule.network.response

import com.google.gson.annotations.SerializedName

class OrderPlaceResponse(
@SerializedName("status") var status: String,
@SerializedName("message") var message: String,
@SerializedName("data") var data: OrderPlaceResponseData
)

data class OrderPlaceResponseData(
    @SerializedName("id") var id: String? = null,
    @SerializedName("pantryId") var pantryId: String? = null,
    @SerializedName("employeeId") var employeeId: String? = null,
    @SerializedName("instruction") var instruction: String? = null,
    @SerializedName("status") var status: String? = null,
    @SerializedName("organizationId") var organizationId: String? = null,
    @SerializedName("businessId") var businessId: String? = null,
    @SerializedName("isActive") var isActive: Boolean? = null,
    @SerializedName("createdAt") var createdAt: Long? = null,
    @SerializedName("updatedAt") var updatedAt: Long? = null,
    @SerializedName("deliverAt") var deliverAt: String? = null,
    @SerializedName("forwaredOrder") var forwaredOrder: String? = null,
    @SerializedName("items") var items: List<Items> = arrayListOf()
){

}
data class Items (

    @SerializedName("pantryItemId" ) var pantryItemId : String? = null,
    @SerializedName("quantity"     ) var quantity     : Int?    = null

)