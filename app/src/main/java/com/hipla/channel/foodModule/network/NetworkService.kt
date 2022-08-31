package com.hipla.channel.foodModule.network

import UnitAvailabiltyBase
import com.hipla.channel.foodModule.network.request.LoginRequest
import com.hipla.channel.foodModule.network.response.*
import com.hipla.sentinelvms.sentinelKt.foodModule.network.QuickSettingResponse
import com.hipla.sentinelvms.sentinelKt.foodModule.network.request.*
import com.hipla.sentinelvms.sentinelKt.foodModule.network.response.*
import retrofit2.Call
import retrofit2.http.*

interface NetworkService {


    @POST(EndPoints.LOGIN)
    fun login(@Body loginRequest: LoginRequest): Call<LoginResponse>

    @GET
    fun checkUnitAvailabilty(@Url url : String): Call<UnitAvailabiltyBase>


    @POST(EndPoints.GET_ALL_PANTRY_DATA)
    fun getPantryData(@Header("apikey") key : String, @Body pantryRequest: AllPantryRequest, @HeaderMap headerMap: HashMap<String,String>): Call<AllPantryResponse>

    @POST(EndPoints.GET_PANTRY_DATA)
    fun getPantryDetail(@Header("apikey") key : String, @Body pantryRequest: PantryRequest, @HeaderMap headerMap: HashMap<String,String>): Call<PantryResponse>

    @POST(EndPoints.GET_DEFAULT_PANTRY_DATA)
    fun getDefaultPantry(@Header("apikey") key : String, @Header("isMobile") isMobile : String, @Body pantryRequest: EmployeeId, @HeaderMap headerMap: HashMap<String,String>): Call<DefaultAllPantryResponse>

    @GET
    fun fetchVipDetails(@Url url :String, @Header("apikey")  key :String) : Call<VipDataBase>


    @POST(EndPoints.GET_DEFAULT_PANTRY_DATA)
    fun getDefaultPantryMeetingRoom(@Header("apikey") key : String, @Header("isMobile") isMobile : String, @Body pantryRequest: RoomId, @HeaderMap headerMap: HashMap<String,String>): Call<DefaultAllPantryResponse>

    @GET(EndPoints.GET_QUICK_SETTING)
    fun getQuickSettings(@Header("apikey") tokenizer : String): Call<QuickSettingResponse>



    @POST(EndPoints.SEND_ORDER_DETAIL)
    fun sendOrderDetail(@Header("apikey") key : String, @Body orderRequest: OrderRequest): Call<OrderPlaceResponse>

    @POST(EndPoints.SEND_ORDER_DETAIL)
    fun sendOrderMeetingDetail(@Header("apikey") key : String, @Body orderRequest: OrderRequestMeetingRoom): Call<OrderPlaceResponse>

    @POST(EndPoints.GET_HISTORY)
    fun getHistory(@Header("apikey") key : String, @Body pantryRequest: PantryRequest, @HeaderMap headerMap: HashMap<String,String>): Call<OrderHistoryResponse>
}