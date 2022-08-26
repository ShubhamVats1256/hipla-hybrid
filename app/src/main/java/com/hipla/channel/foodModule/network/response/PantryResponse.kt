package com.hipla.channel.foodModule.network.response

import com.google.gson.annotations.SerializedName

class PantryResponse(
    @SerializedName("status") var status : String? = null,
    @SerializedName("message") var message : String? = null,
    @SerializedName("data") var data : List<PantryData>? = null
)

data class PantryData (
    @SerializedName("id") var id : String? = null,
    @SerializedName("pantryId") var pantryId : String? = null,
    @SerializedName("pantryItemId") var pantryItemId : String? = null,
    @SerializedName("businessId") var businessId : String? = null,
    @SerializedName("stock") var stock : String? = null,
    @SerializedName("status") var status : String? = null,
    @SerializedName("isActive") var isActive : Boolean? = null,
    @SerializedName("createdAt") var createdAt : String? = null,
    @SerializedName("updatedAt") var updatedAt : String? = null,
    @SerializedName("pantryItem") var pantryItem : PantryItem? = null
)

data class PantryItem (
    @SerializedName("id") var id : String? = null,
    @SerializedName("name") var name : String? = null,
    @SerializedName("isActive") var isActive : Boolean? = null,
    @SerializedName("createdAt") var createdAt : String? = null,
    @SerializedName("updatedAt") var updatedAt : String? = null,
    @SerializedName("businessId") var businessId : String? = null
)