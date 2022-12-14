package com.hipla.channel.viewmodel

import android.os.Bundle
import com.hipla.channel.common.*
import com.hipla.channel.entity.*
import com.hipla.channel.entity.api.ifError
import com.hipla.channel.entity.api.ifSuccessful
import com.hipla.channel.entity.response.ApplicationUpdateResponse
import com.hipla.channel.entity.response.GenerateOTPResponse
import com.hipla.channel.entity.response.UserDetails
import com.hipla.channel.extension.*
import com.hipla.channel.repo.HiplaRepo
import org.koin.java.KoinJavaComponent
import timber.log.Timber

class ApplicationConfirmationViewModel : BaseViewModel() {
    private val hiplaRepo: HiplaRepo by KoinJavaComponent.inject(HiplaRepo::class.java)
    var applicationRequest: ApplicationRequest? = null
    private var generateOTPResponse: GenerateOTPResponse? = null
    var channelPartnerDetails: UserDetails? = null
    lateinit var flowConfig: FlowConfig
    var unitInfo: UnitInfo? = null

    fun extractArguments(arguments: Bundle?) {
        arguments?.run {
            getString(KEY_PARTNER)?.toUserDetails()?.let {
                this@ApplicationConfirmationViewModel.channelPartnerDetails = it
                Timber.tag(LogConstant.APP_CONFIRM)
                    .d("channel partner name ${channelPartnerDetails?.name} mobile no ${channelPartnerDetails?.phoneNumber}")
            }
            getString(KEY_APP_REQ)?.toApplicationRequest()?.let {
                this@ApplicationConfirmationViewModel.applicationRequest = it
                Timber.tag(LogConstant.APP_CONFIRM)
                    .d("application request with id ${it.id} for customer : ${it.customerName} received in ")
            }
            flowConfig = this.getString(KEY_FLOW_CONFIG)?.toFlowConfig()!!
            unitInfo = this.getString(KEY_UNIT)?.toUnitInfo()
            Timber.tag(LogConstant.APP_CONFIRM).d("Unit selected: $unitInfo")
            Timber.tag(LogConstant.APP_CONFIRM).d("Flow config: $flowConfig")
        }
    }

    private fun updateApplication() {
        launchIO {
            appEvent.tryEmit(AppEvent(APPLICATION_UPDATING))
            if (applicationRequest != null) {
                with(
                    hiplaRepo.updateApplication(
                        applicationRequest!!,
                        pageName = AppConfig.PAGE_CREATE_APPLICATION,
                        appCode = flowConfig.appCode
                    )
                ) {
                    ifSuccessful {
                        if (it.status.isSuccess()) {
                            reportApplicationUpdateSuccess(it)
                        } else {
                            reportApplicationUpdateFailed()
                        }
                        Timber.tag(LogConstant.APP_CONFIRM)
                            .d("application updated successfully for id : ${applicationRequest!!.id}")
                    }
                    ifError {
                        reportApplicationUpdateFailed()
                        Timber.tag(LogConstant.APP_CONFIRM)
                            .e("application update failed for id : ${applicationRequest!!.id}")
                    }
                }
            } else {
                reportApplicationUpdateFailed()
                Timber.tag(LogConstant.APP_CONFIRM)
                    .e("application request argument not found failed for id : ${applicationRequest!!.id}")
            }
        }
    }


    fun verifyCustomerOTP(otp: String) {
        Timber.tag(LogConstant.CUSTOMER_INFO)
            .d("verify OTP: $otp for userId : ${generateOTPResponse?.recordReference?.id}")
        launchIO {
            if (generateOTPResponse != null) {
                appEvent.tryEmit(AppEvent(OTP_VERIFYING))
                hiplaRepo.verifyOtp(
                    otp = otp,
                    userId = generateOTPResponse!!.recordReference.id.toString(),
                    referenceId = generateOTPResponse!!.referenceId,
                    pageName = AppConfig.PAGE_VERIFY_OTP,
                    appCode = flowConfig.appCode
                ).run {
                    ifSuccessful {
                        if (it.verifyOTPData.referenceId == generateOTPResponse!!.referenceId && it.verifyOTPData.isVerified) {
                            appEvent.tryEmit(AppEvent(OTP_VERIFICATION_SUCCESS))
                            Timber.tag(LogConstant.CUSTOMER_INFO).d("customer OTP verified")
                            applicationRequest?.ownerId = generateOTPResponse?.recordReference?.id
                            updateApplication()
                        } else {
                            appEvent.tryEmit(AppEvent(OTP_VERIFICATION_INVALID))
                            Timber.tag(LogConstant.CUSTOMER_INFO).e("customer OTP invalid")
                        }
                    }
                    ifError {
                        Timber.tag(LogConstant.CUSTOMER_INFO).e("customer OTP verification failed")
                        appEvent.tryEmit(AppEvent(OTP_VERIFICATION_FAILED))
                    }
                }
            } else {
                appEvent.tryEmit(AppEvent(OTP_VERIFICATION_FAILED))
                Timber.tag(LogConstant.CUSTOMER_INFO).e("customer OTP server referenceId not found")
            }
            appEvent.tryEmit(AppEvent(OTP_VERIFICATION_COMPLETE))
        }
    }

    fun generateCustomerOTP() {
        launchIO {
            appEvent.tryEmit(AppEvent(OTP_GENERATING))
            applicationRequest?.customerPhoneNumber?.let { customerPhoneNo ->
                hiplaRepo.generateOtp(
                    phoneNo = customerPhoneNo,
                    pageName = AppConfig.PAGE_GENERATE_OTP,
                    appCode = flowConfig.appCode
                ).run {
                    ifSuccessful {
                        Timber.tag(LogConstant.CUSTOMER_INFO)
                            .d("generate OTP successful for customer name ${applicationRequest?.customerName}, referenceId:${it.referenceId}")
                        generateOTPResponse = it
                        appEvent.tryEmit(
                            AppEventWithData(
                                id = OTP_SHOW_VERIFICATION_DIALOG,
                                extras = it.recordReference.id.toString()
                            )
                        )
                    }
                    ifError {
                        appEvent.tryEmit(AppEvent(OTP_GENERATE_FAILED))
                        Timber.tag(LogConstant.CUSTOMER_INFO)
                            .e("generate OTP failed for customer name ${applicationRequest?.customerName}")
                    }
                    appEvent.tryEmit(AppEvent(OTP_GENERATE_COMPLETE))
                }
            } ?: Timber.tag(LogConstant.CUSTOMER_INFO)
                .e("generate OTP failed as customer phone no not found")
        }
    }

    private fun reportApplicationUpdateSuccess(applicationUpdateResponse: ApplicationUpdateResponse) {
        appEvent.tryEmit(
            AppEvent(
                id = APPLICATION_UPDATING_SUCCESS,
                message = applicationUpdateResponse.displayCounter?.toString() ?: ""
            )
        )
    }

    private fun reportApplicationUpdateFailed() {
        appEvent.tryEmit(AppEvent(APPLICATION_UPDATING_FAILED))
    }

}