package com.hipla.channel.viewmodel

import com.hipla.channel.common.LogConstant
import com.hipla.channel.entity.*
import com.hipla.channel.entity.api.ifError
import com.hipla.channel.entity.api.ifSuccessful
import com.hipla.channel.entity.response.GenerateOTPResponse
import com.hipla.channel.repo.HiplaRepo
import org.koin.java.KoinJavaComponent
import timber.log.Timber

class ApplicationCustomerInfoViewModel : BaseViewModel() {
    private val hiplaRepo: HiplaRepo by KoinJavaComponent.inject(HiplaRepo::class.java)
    private var generateOTPResponse: GenerateOTPResponse? = null
    private var applicationRequest: ApplicationRequest? = null

    private fun createApplicationInServer() {
        launchIO {
            with(hiplaRepo.createApplication(applicationRequest!!)) {
                ifSuccessful {
                    applicationRequest?.id = it.userReference.id
                    applicationRequest?.ownerId = generateOTPResponse?.recordReference?.id
                    Timber.tag(LogConstant.CUSTOMER_INFO)
                        .d("application ID : :${applicationRequest?.id} created for ${applicationRequest?.customerName}")

                    if ((applicationRequest?.id ?: 0) > 0) {
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

    fun verifyCustomerOTP(otp: String) {
        Timber.tag(LogConstant.CUSTOMER_INFO)
            .d("verify OTP: $otp for userId : ${generateOTPResponse?.recordReference?.id}")
        launchIO {
            appEvent.tryEmit(AppEvent(OTP_VERIFYING))
            if (generateOTPResponse != null) {
                hiplaRepo.verifyOtp(
                    otp = otp,
                    userId = generateOTPResponse!!.recordReference.id.toString(),
                    referenceId = generateOTPResponse!!.referenceId
                ).run {
                    ifSuccessful {
                        if (it.verifyOTPData.referenceId == generateOTPResponse!!.referenceId && it.verifyOTPData.isVerified) {
                            appEvent.tryEmit(AppEvent(OTP_VERIFICATION_SUCCESS))
                            Timber.tag(LogConstant.CUSTOMER_INFO).d("customer OTP verified")
                            createApplicationInServer()
                        } else {
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

    fun createApplicationRequest(
        customerFirstName: String,
        customerLastName: String,
        panNo: String,
        customerPhone: String,
    ): ApplicationRequest {
        applicationRequest = ApplicationRequest().apply {
            this.customerName = "$customerFirstName $customerLastName"
            this.customerPhoneNumber = customerPhone
            this.panNumber = panNo
        }
        return applicationRequest!!
    }

    fun generateCustomerOTP(applicationRequest: ApplicationRequest) {
        launchIO {
            appEvent.tryEmit(AppEvent(OTP_GENERATING))
            applicationRequest.customerPhoneNumber?.let { customerPhoneNo ->
                hiplaRepo.generateOtp("9916555886").run {
                    ifSuccessful {
                        Timber.tag(LogConstant.CUSTOMER_INFO)
                            .d("generate OTP successful for customer name ${applicationRequest.customerName}, referenceId:${it.referenceId}")
                        generateOTPResponse = it
                        appEvent.tryEmit(
                            AppEventWithData<String>(
                                OTP_SHOW_VERIFICATION_DIALOG,
                                it.recordReference.id.toString()
                            )
                        )
                    }
                    ifError {
                        appEvent.tryEmit(AppEvent(OTP_GENERATE_FAILED))
                        Timber.tag(LogConstant.CUSTOMER_INFO)
                            .e("generate OTP failed for customer name ${applicationRequest.customerName}")
                    }
                    appEvent.tryEmit(AppEvent(OTP_GENERATE_COMPLETE))
                }
            } ?: Timber.tag(LogConstant.CUSTOMER_INFO)
                .e("generate OTP failed as customer phone no not found")
        }
    }


}