package com.hipla.channel.foodModule.network.response

import com.google.gson.annotations.SerializedName

class AllPantryResponse(
    @SerializedName("status") var status: String,
    @SerializedName("message") var message: String,
    @SerializedName("data") var data: List<AllPantryResponseData>

)

data class AllPantryResponseData(
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
    @SerializedName("forwardOrderDelay") var forwardOrderDelay: String? = null,
    @SerializedName("organization") var organization: Organization? = null,
    @SerializedName("building") var building: Building? = null

)

data class Organization(

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

data class Building(

    @SerializedName("id") var id: String? = null,
    @SerializedName("name") var name: String? = null,
    @SerializedName("number") var number: String? = null,
    @SerializedName("locationId") var locationId: String? = null,
    @SerializedName("isActive") var isActive: Boolean? = null,
    @SerializedName("createdAt") var createdAt: String? = null,
    @SerializedName("updatedAt") var updatedAt: String? = null,
    @SerializedName("businessId") var businessId: String? = null

)

class DefaultAllPantryResponse(
    @SerializedName("status") var status: String,
    @SerializedName("message") var message: String,
    @SerializedName("data") var data: AllPantryResponseData

)