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
                this.applicationRequest = it
                Timber.tag(LogConstant.PAYMENT_INFO)
                    .d("application request with id ${it.id} for customer : ${it.customerName} received in payment flow")
            }
        }
    }

    fun verifyChannelPartnerOTP(otp: String) {
        Timber.tag(LogConstant.PAYMENT_INFO)
            .d("verify OTP: $otp for userId : ${generateOTPResponse?.recordReference?.id}")
        launchIO {
            if (generateOTPResponse != null) {
                appEvent.tryEmit(AppEvent(OTP_VERIFYING))
                hiplaRepo.verifyOtp(
                    otp = otp,
                    userId = generateOTPResponse!!.recordReference.id.toString(),
                    referenceId = generateOTPResponse!!.referenceId
                ).run {
                    ifSuccessful {
                        if (it.verifyOTPData.referenceId == generateOTPResponse!!.referenceId && it.verifyOTPData.isVerified) {
                            Timber.tag(LogConstant.PAYMENT_INFO)
                                .d(" OTP verified, channel partner user ID : ${generateOTPResponse?.recordReference?.id}")
                            applicationRequest?.channelPartnerId =
                                generateOTPResponse?.recordReference?.id.toString()
                            Timber.tag(LogConstant.PAYMENT_INFO)
                                .d("channel partnerId updated to application request")
                            appEvent.tryEmit(AppEvent(OTP_VERIFICATION_SUCCESS))
                            Timber.tag(LogConstant.PAYMENT_INFO).d("customer OTP verified")
                        } else {
                            appEvent.tryEmit(AppEvent(OTP_VERIFICATION_INVALID))
                            Timber.tag(LogConstant.PAYMENT_INFO).e("customer OTP invalid")
                        }
                    }
                    ifError {
                        Timber.tag(LogConstant.PAYMENT_INFO).e("customer OTP verification failed")
                        appEvent.tryEmit(AppEvent(OTP_VERIFICATION_FAILED))
                    }
                }
            } else {
                appEvent.tryEmit(AppEvent(OTP_VERIFICATION_FAILED))
                Timber.tag(LogConstant.PAYMENT_INFO).e("customer OTP server referenceId not found")
            }
            appEvent.tryEmit(AppEvent(OTP_VERIFICATION_COMPLETE))
        }
    }

    fun generateChannelPartnerOTP(channelPartnerMobileNo: String) {
        launchIO {
            channelPartnerMobileNo.let { phoneNo ->
                appEvent.tryEmit(AppEvent(OTP_GENERATING))
                hiplaRepo.generateOtp(phoneNo).run {
                    ifSuccessful {
                        Timber.tag(LogConstant.PAYMENT_INFO)
                            .d("generate OTP successful for mobile $phoneNo, referenceId:${it.referenceId}")
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
                        Timber.tag(LogConstant.PAYMENT_INFO)
                            .e("generate OTP failed for mobile $phoneNo")
                    }
                    appEvent.tryEmit(AppEvent(OTP_GENERATE_COMPLETE))
                }
            }
        }
    }

    fun updateApplicationRequest(
        amountPayable: String,
        paymentType: PaymentType,
        chequeNo: String,
    ): ApplicationRequest? {
        return applicationRequest?.apply {
            this.channelPartnerId = generateOTPResponse?.recordReference?.id.toString()
            this.amountPayable = amountPayable
            this.paymentDetails = chequeNo
            this.paymentType = paymentType
            Timber.tag(LogConstant.PAYMENT_INFO).d("payment details updated to application request")
        }
    }


}