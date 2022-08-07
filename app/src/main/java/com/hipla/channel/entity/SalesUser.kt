package com.hipla.channel.entity
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SalesUser(
    val createdAt: String?,
    val emailId: String?,
    @Json(name = "extra") val personalInfo: PersonalInfo?,
    val gender: String?,
    val id: Int,
    val isActive: Boolean?,
    val name: String,
    val phoneNumber: String?,
    val profilePic: String?,
    val updatedAt: String?
)

@JsonClass(generateAdapter = true)
data class PersonalInfo(
    val address: String?,
    val companyName: String?,
    val designation: String?,
    val occupation: String?,
    val pincode: String?,
    val placeOfVisit: String?,
    val state: String?,
    val toMeet: Int?
)