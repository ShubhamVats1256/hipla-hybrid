package com.hipla.channel.foodModule.network.response


data class VipDataBase (
    val status : String,
    val message : String,
    val data : VidData
)


data class VidData (

//	val id : String,
    val organizationId : String,
//    val name : String,
//    val email : String,
    //val vip : String,
//	val mobile : Int,
//	val workPhone : String,
//	val workFax : String,
//	val photo : String,
//	val countryCode : Int,
//	val isActive : Boolean,
//	val role : String,
//	val createdAt : Int,
//	val updatedAt : Int,
//	val lastLogin : String,
//	val building : String,
//	val department : String,
//	val designation : String,
//	val website : String,
//	val address : String,
//	val rememberToken : String,
	val businessId : String,
//	val organization : VipOrganization,
//	val business : VipBusiness,
//	val accessDetails : VipAccessDetails,
//	val metadata : List<Metadata>,
  //  val vip : CurrentStatus
)
//
//data class VipAccessDetails (
//
//	val id : String,
//	val organizationId : String,
//	val businessId : String,
//	val userType : String,
//	val employeeId : String,
//	val visitorId : String,
//	val staffId : String,
//	val qrPin : Int,
//	val qrCode : String,
//	val isActive : Boolean,
//	val createdAt : Int,
//	val updatedAt : Int
//)
//
//
//data class VipBusiness (
//
//	val id : String,
//	val code : String,
//	val name : String,
//	val domain : String,
//	val timezoneName : String,
//	val timezone : String,
//	val logo : String,
//	val createdAt : Int,
//	val isActive : Boolean,
//	val updatedAt : Int
//)
//
//data class VipMetadata (
//
//	val id : String,
//	val key : String,
//	val value : String,
//	val employeeId : String
//)

//
//data class VipOrganization (
//
//	val id : String,
//	val name : String,
//	val timezoneName : String,
//	val timezone : String,
//	val countrycode : String,
//	val logo : String,
//	val createdAt : Int,
//	val isActive : Boolean,
//	val updatedAt : Int,
//	val businessId : String
//)

//data class Vip (
//
//	val insideDeviceId : String,
//	val outsideDeviceId : String,
//	val currentStatus : String
//)