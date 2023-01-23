package com.hipla.channel.foodModule.repository

import com.hipla.channel.entity.PushOtpRequest
import com.hipla.channel.entity.response.GenerateOtpRequestBase
import com.hipla.channel.entity.response.QuestionRequest
import com.hipla.channel.foodModule.network.request.LoginRequest
import com.hipla.channel.foodModule.network.NetworkService
import com.hipla.sentinelvms.sentinelKt.foodModule.network.request.*

class CommonRepository constructor(private val networkService: NetworkService?) {

    fun getLoginData(loginRequest: LoginRequest) =
        networkService!!.login(loginRequest)

    fun checkUnitAvailabilty(url: String) =
        networkService!!.checkUnitAvailabilty(url)




    fun getPantryData(apiKey : String, pantryRequest: AllPantryRequest, hashMap: HashMap<String,String>) =
        networkService!!.getPantryData(apiKey,pantryRequest,hashMap)

    fun getDefaultPantry(apiKey : String, pantryRequest: EmployeeId, hashMap: HashMap<String,String>) =
        networkService!!.
        getDefaultPantry(apiKey,"true",pantryRequest,hashMap)



    fun getDefaultPantryMeetingRoom(apiKey : String, pantryRequest: RoomId, hashMap: HashMap<String,String>) =
        networkService!!.
        getDefaultPantryMeetingRoom(apiKey,"true",pantryRequest,hashMap)


    fun getQuickSettings(token : String) =
        networkService!!.getQuickSettings(token)


    fun getPantryDetail(apiKey : String, pantryRequest: PantryRequest, hashMap: HashMap<String,String>) =
        networkService!!.
        getPantryDetail(apiKey,pantryRequest,hashMap)

    fun sendOrderDetail(apiKey : String, orderRequest: OrderRequest) =
        networkService!!.sendOrderDetail(apiKey,orderRequest)

    fun sendOrderMetingDetail(apiKey : String, orderRequest: OrderRequestMeetingRoom) =
        networkService!!.sendOrderMeetingDetail(apiKey,orderRequest)

    fun getHistory(apiKey : String, pantryRequest: PantryRequest, hashMap: HashMap<String,String>) =
        networkService!!.getHistory(apiKey,pantryRequest,hashMap)


    fun submitFeedbackQuestions(token : String,questionRequest: QuestionRequest) =
        networkService!!.submitFeedbackQuestions(token,questionRequest)

    fun generateOtp(generateOtpRequestBase: GenerateOtpRequestBase) =
        networkService!!.
        generateOtp(generateOtpRequestBase)

    fun pushOtp(pushOtpRequest: PushOtpRequest) =
        networkService!!.
        pushOtp(pushOtpRequest)


}