package com.hipla.channel.viewmodel

import android.os.Bundle
import com.hipla.channel.common.KEY_APP_REQ
import com.hipla.channel.common.LogConstant
import com.hipla.channel.entity.*
import com.hipla.channel.entity.api.ifError
import com.hipla.channel.entity.api.ifSuccessful
import com.hipla.channel.entity.response.GenerateOTPResponse
import com.hipla.channel.extension.toApplicationRequest
import com.hipla.channel.repo.HiplaRepo
import org.koin.java.KoinJavaComponent
import timber.log.Timber

class ApplicationPaymentInfoViewModel : BaseViewModel() {
    private val hiplaRepo: HiplaRepo by KoinJavaComponent.inject(HiplaRepo::class.java)
    private var generateOTPResponse: GenerateOTPResponse? = null
    private var applicationRequest: ApplicationRequest? = null

    fun extractArguments(arguments: Bundle?) {
        launchIO {
            arguments?.getString(KEY_APP_REQ)?.toApplicationRequest()?.let {
                Timber.tag(LogConstant.PAYMENT_INFO)
                    .d("application request with id ${it.id} for customer : ${it.customerName} received in payment flow")
            }
        }
    }

    fun verifyChannelParnetOTP(otp: String) {
        Timber.tag(LogConstant.PAYMENT_INFO)
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
                            Timber.tag(LogConstant.PAYMENT_INFO).d("customer OTP verified")
                        } else {
                            Timber.tag(LogConstant.PAYMENT_INFO).e("customer OTP invalid")
                        }
                    }
                    ifError {
                        Timber.tag(LogConstant.PAYMENT_INFO).e("customer OTP verification failed")
                        appEvent.tryEmit(AppEvent(OTP_VERIFICATION_FAILED))
                    }
                    appEvent.tryEmit(AppEvent(OTP_VERIFICATION_COMPLETE))
                }
            } else {
                appEvent.tryEmit(AppEvent(OTP_VERIFICATION_FAILED))
                Timber.tag(LogConstant.PAYMENT_INFO).e("customer OTP server referenceId not found")
            }
        }
    }

    fun generateChannelPartnerOTP(channelPartnerMobileNo: String) {
        launchIO {
            appEvent.tryEmit(AppEvent(OTP_GENERATING))
            channelPartnerMobileNo.let { phoneNo ->
                hiplaRepo.generateOtp("9916555886").run {
                    ifSuccessful {
                        Timber.tag(LogConstant.PAYMENT_INFO)
                            .d("generate OTP successful for mobile $phoneNo, referenceId:${it.referenceId}")
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
                        Timber.tag(LogConstant.PAYMENT_INFO)
                            .e("generate OTP failed for mobile $phoneNo")
                    }
                    appEvent.tryEmit(AppEvent(OTP_GENERATE_COMPLETE))
                }
            }
        }
    }


}