package com.hipla.channel.viewmodel

import android.os.Bundle
import com.hipla.channel.common.KEY_SALES_USER_ID
import com.hipla.channel.common.LogConstant
import com.hipla.channel.entity.*
import com.hipla.channel.entity.api.ifError
import com.hipla.channel.entity.api.ifSuccessful
import com.hipla.channel.entity.response.ApplicationCreateResponse
import com.hipla.channel.repo.HiplaRepo
import org.koin.java.KoinJavaComponent
import timber.log.Timber

class ApplicationCustomerInfoViewModel : BaseViewModel() {
    private val hiplaRepo: HiplaRepo by KoinJavaComponent.inject(HiplaRepo::class.java)
    private var salesUserId: String? = null
    private var selectedFloorPreference: FloorDetails? = null
    var floorList: List<FloorDetails> = generateFloors()
    var applicationCreateResponse: ApplicationCreateResponse? = null
    private var applicationRequest: ApplicationRequest? = null

    fun extractArguments(arguments: Bundle?) {
        salesUserId = arguments?.getString(KEY_SALES_USER_ID);
    }

    fun selectedFloorId(selectionFloorDetailsIndex: Int) {
        selectedFloorPreference = floorList[selectionFloorDetailsIndex]
        Timber.tag(LogConstant.CUSTOMER_INFO)
            .d("floor preference ${selectedFloorPreference?.name}")
    }

    fun getCustomerFloorPreferenceId() = selectedFloorPreference?.id

    fun getCustomerFirstName() = applicationRequest?.customerName

    fun getCustomerLastName() = applicationRequest?.customerLastName

    fun getCustomerPanNo() = applicationRequest?.panNumber

    fun getCustomerMobileNo() = applicationRequest?.customerPhoneNumber

    fun isFloorPreferenceSelected() = selectedFloorPreference != null

    private fun createApplicationInServer(applicationRequest: ApplicationRequest?) {
        launchIO {
            appEvent.tryEmit(AppEvent(APP_EVENT_APPLICATION_CREATING))
            with(hiplaRepo.createApplication(applicationRequest!!)) {
                ifSuccessful {
                    applicationCreateResponse = it
                    applicationRequest.id = it.userReference.id
                    applicationRequest.paymentProofImageUrl =
                        it.userReference.applicationRecordExtraInfo.paymentProofImageUrl
                    Timber.tag(LogConstant.CUSTOMER_INFO)
                        .d("application ID : :${applicationRequest.id} created for ${applicationRequest.customerName}")

                    if ((applicationRequest.id ?: 0) > 0) {
                        Timber.tag(LogConstant.CUSTOMER_INFO).d("customer info collection complete")
                        appEvent.tryEmit(
                            AppEventWithData(
                                APP_EVENT_APPLICATION_SUCCESS,
                                extras = applicationRequest
                            )
                        )
                    } else {
                        appEvent.tryEmit(AppEvent(APP_EVENT_APPLICATION_FAILED))
                        Timber.tag(LogConstant.CUSTOMER_INFO)
                            .d("application id is invalid cannot proceed to next flow")
                    }
                }
                ifError {
                    appEvent.tryEmit(AppEvent(APP_EVENT_APPLICATION_FAILED))
                    Timber.tag(LogConstant.CUSTOMER_INFO)
                        .e(it.throwable?.message.toString())
                }
                appEvent.tryEmit(AppEvent(APP_EVENT_APPLICATION_COMPLETE))
            }
        }
    }

    fun createApplicationRequest(
        customerFirstName: String,
        customerLastName: String,
        panNo: String,
        customerPhone: String,
        floorId: Int,
    ) {
        Timber.tag(LogConstant.CUSTOMER_INFO)
            .d("customer first name  : $customerFirstName")
        Timber.tag(LogConstant.CUSTOMER_INFO)
            .d("customer last name  : $customerLastName")
        Timber.tag(LogConstant.CUSTOMER_INFO)
            .d("customer phone no  : $customerPhone")
        Timber.tag(LogConstant.CUSTOMER_INFO)
            .d("customer pan no : $panNo")
        Timber.tag(LogConstant.CUSTOMER_INFO)
            .d("floor preference Id : $floorId")
        Timber.tag(LogConstant.CUSTOMER_INFO)
            .d("application created by  : $salesUserId")
        ApplicationRequest().apply {
            this.customerName = customerFirstName
            this.customerLastName = customerLastName
            this.customerPhoneNumber = customerPhone
            this.panNumber = panNo
            this.floorPreferenceId = floorId
            this.createdBy = salesUserId?.toInt()
        }.also {
            // save locally
            this.applicationRequest = it
        }.also {
            // create application in server
            createApplicationInServer(it)
        }
    }


}