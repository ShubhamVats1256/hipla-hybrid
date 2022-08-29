package com.hipla.sentinelvms.sentinelKt.foodModule.network.response

import com.google.gson.annotations.SerializedName

class OrderHistoryResponse(
    @SerializedName("status") var status: String,
    @SerializedName("message") var message: String,
    @SerializedName("data") var data: List<OrderHistoryResponseData>
)

data class OrderHistoryResponseData(
    @SerializedName("id") var id: String? = null,
    @SerializedName("pantryId") var pantryId: String? = null,
    @SerializedName("employeeId") var employeeId: String? = null,
    @SerializedName("instruction") var instruction: String? = null,
    @SerializedName("status") var status: String? = null,
    @SerializedName("organizationId") var organizationId: String? = null,
    @SerializedName("businessId") var businessId: String? = null,
    @SerializedName("isActive") var isActive: Boolean? = null,
    @SerializedName("createdAt") var createdAt: String? = null,
    @SerializedName("updatedAt") var updatedAt: String? = null,
    @SerializedName("deliverAt") var deliverAt: String? = null,
    @SerializedName("forwaredOrder") var forwaredOrder: String? = null,
    @SerializedName("pantry") var pantry: OrderHistoryPantry? = OrderHistoryPantry(),
    @SerializedName("pantryOrderItems") var pantryOrderitems : List<OrderHistoryItems> ?= null,
    @SerializedName("employee") var employee: OrderHistoryEmployee? = OrderHistoryEmployee(),
    @SerializedName("organization") var organization: OrderHistoryOrganization? = OrderHistoryOrganization()
)

data class OrderHistoryItems (
    @SerializedName("id") val id : String? = null,
//    @SerializedName("pantryOrderId") val pantryOrderId : String? = null,
//    @SerializedName("pantryItemId") val pantryItemId : String? = null,
    @SerializedName("quantity") val quantity : Int? = null,
//    @SerializedName("organizationId") val organizationId : String? = null,
//    @SerializedName("businessId") val businessId : String? = null,
//    @SerializedName("isActive") val isActive : Boolean? = null,
//    @SerializedName("createdAt") val createdAt : Int? = null,
//    @SerializedName("updatedAt") val updatedAt : Int? = null,
    @SerializedName("pantryItem") val pantryItem :OrderHistoryPantryItem= OrderHistoryPantryItem()
)


data class OrderHistoryPantryItem (
   // @SerializedName("id") val id : String? = null,
    @SerializedName("name") val name : String? = null,
//    @SerializedName("isActive") val isActive : Boolean? = null,
//    @SerializedName("createdAt") val createdAt : Int? = null,
//    @SerializedName("updatedAt") val updatedAt : Int? = null,
//    @SerializedName("businessId") val businessId : String? = null
)


data class OrderHistoryPantry(
    @SerializedName("id") var id: String? = null,
    @SerializedName("organizationId") var organizationId: String? = null,
    @SerializedName("name") var name: String? = null,
    @SerializedName("isActive") var isActive: Boolean? = null,
    @SerializedName("createdAt") var createdAt: String? = null,
    @SerializedName("updatedAt") var updatedAt: String? = null,
    @SerializedName("buildingId") var buildingId: String? = null,
    @SerializedName("businessId") var businessId: String? = null,
    @SerializedName("forwardOrder") var forwardOrder: String? = null,
    @SerializedName("receiveForwardedOrder") var receiveForwardedOrder: String? = null,
    @SerializedName("forwardOrderDelay") var forwardOrderDelay: String? = null
)

data class OrderHistoryEmployee(
    @SerializedName("id") var id: String? = null,
    @SerializedName("organizationId") var organizationId: String? = null,
    @SerializedName("name") var name: String? = null,
    @SerializedName("email") var email: String? = null,
    @SerializedName("mobile") var mobile: String? = null,
    @SerializedName("workPhone") var workPhone: String? = null,
    @SerializedName("workFax") var workFax: String? = null,
    @SerializedName("photo") var photo: String? = null,
    @SerializedName("countryCode") var countryCode: String? = null,
    @SerializedName("isActive") var isActive: Boolean? = null,
    @SerializedName("role") var role: String? = null,
    @SerializedName("createdAt") var createdAt: String? = null,
    @SerializedName("updatedAt") var updatedAt: String? = null,
    @SerializedName("lastLogin") var lastLogin: String? = null,
    @SerializedName("building") var building: String? = null,
    @SerializedName("department") var department: String? = null,
    @SerializedName("designation") var designation: String? = null,
    @SerializedName("website") var website: String? = null,
    @SerializedName("address") var address: String? = null,
    @SerializedName("rememberToken") var rememberToken: String? = null,
    @SerializedName("businessId") var businessId: String? = null
)

data class OrderHistoryOrganization(
    @SerializedName("id") var id: String? = null,
    @SerializedName("name") var name: String? = null,
    @SerializedName("timezoneName") var timezoneName: String? = null,
    @SerializedName("timezone") var timezone: String? = null,
    @SerializedName("countrycode") var countrycode: String? = null,
    @SerializedName("logo") var logo: String? = null,
    @SerializedName("createdAt") var createdAt: String? = null,
    @SerializedName("isActive") var isActive: Boolean? = null,
    @SerializedName("updatedAt") var updatedAt: String? = null,
    @SerializedName("businessId") var businessId: String? = null

)