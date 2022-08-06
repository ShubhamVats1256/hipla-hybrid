package com.hipla.channel.viewmodel

import android.graphics.Bitmap
import android.os.Bundle
import com.hipla.channel.common.KEY_APPLICATION_SERVER_INF0
import com.hipla.channel.common.KEY_APP_REQ
import com.hipla.channel.common.LogConstant
import com.hipla.channel.entity.*
import com.hipla.channel.entity.api.ifError
import com.hipla.channel.entity.api.ifSuccessful
import com.hipla.channel.entity.response.ApplicationCreateResponse
import com.hipla.channel.entity.response.GenerateOTPResponse
import com.hipla.channel.entity.response.UserDetails
import com.hipla.channel.extension.toApplicationRequest
import com.hipla.channel.extension.toApplicationServerInfo
import com.hipla.channel.repo.HiplaRepo
import org.koin.java.KoinJavaComponent
import timber.log.Timber
import java.io.OutputStream
import java.net.URL
import java.util.concurrent.atomic.AtomicBoolean
import javax.net.ssl.HttpsURLConnection

class ApplicationPaymentInfoViewModel : BaseViewModel() {
    private val hiplaRepo: HiplaRepo by KoinJavaComponent.inject(HiplaRepo::class.java)
    private var generateOTPResponse: GenerateOTPResponse? = null
    private var applicationRequest: ApplicationRequest? = null
    private var applicationCreateResponse: ApplicationCreateResponse? = null
    private var imageUploadUrl: String? = null
    var channelPartnerDetails: UserDetails? = null
    private var isProofUploadedAtomic = AtomicBoolean(false)
    private var paymentDate : String? = null

    fun extractArguments(arguments: Bundle?) {
        arguments?.getString(KEY_APP_REQ)?.toApplicationRequest()?.let {
            this.applicationRequest = it
            Timber.tag(LogConstant.PAYMENT_INFO)
                .d("application request with id ${it.id} for customer : ${it.customerName} received in payment flow")
        }
        arguments?.getString(KEY_APPLICATION_SERVER_INF0)?.toApplicationServerInfo()?.let {
            this.applicationCreateResponse = it
            imageUploadUrl = it.imageUploadUrl
            Timber.tag(LogConstant.PAYMENT_INFO)
                .d("imageUploadUrl ${it.imageUploadUrl}")
        }
    }

    fun setPaymentDate(paymentDate : String) {
        this.paymentDate = paymentDate
    }

    fun getPaymentDate() =  this.paymentDate

    fun getPaymentProofReadUrl() = applicationCreateResponse?.imageReadUrl

    fun isPaymentProofUploaded() = isProofUploadedAtomic.get()

    fun isChannelPartnerVerified(channelPartnerMobileNo: String?): Boolean {
        Timber.tag(LogConstant.PAYMENT_INFO)
            .d("check if channel partner verified for mobile no : $channelPartnerMobileNo")
        Timber.tag(LogConstant.PAYMENT_INFO)
            .d("channel partner mobile number saved in view model : ${channelPartnerDetails?.phoneNumber}")
        return channelPartnerDetails?.phoneNumber == channelPartnerMobileNo
    }

    fun getAmountPayable()  = applicationRequest?.amountPayable

    fun getPaymentReferenceNo()  = applicationRequest?.paymentDetails

    fun getSelectedPaymentType() : PaymentType = applicationRequest?.paymentType ?: PaymentType.Cheque()

    fun getChannelPartnerPhoneNo()  = channelPartnerDetails?.phoneNumber

    fun verifyChannelPartnerOTP(otp: String, channelPartnerMobileNo: String) {
        val channelPartnerUserId = generateOTPResponse?.recordReference?.id
        Timber.tag(LogConstant.PAYMENT_INFO)
            .d("verify OTP: $otp for userId : $channelPartnerUserId ")
        launchIO {
            if (generateOTPResponse != null) {
                appEvent.tryEmit(AppEvent(OTP_VERIFYING))
                hiplaRepo.verifyOtp(
                    otp = otp,
                    userId = channelPartnerUserId.toString(),
                    referenceId = generateOTPResponse!!.referenceId
                ).run {
                    ifSuccessful { verifyOTPResponse ->
                        if (verifyOTPResponse.verifyOTPData.referenceId == generateOTPResponse!!.referenceId && verifyOTPResponse.verifyOTPData.isVerified) {
                            Timber.tag(LogConstant.PAYMENT_INFO)
                                .d(" OTP verified, channel partner user ID : ${generateOTPResponse?.recordReference?.id}")
                            applicationRequest?.channelPartnerId = channelPartnerUserId.toString()
                            Timber.tag(LogConstant.PAYMENT_INFO)
                                .d("channel partnerId updated to application request")
                            Timber.tag(LogConstant.PAYMENT_INFO).d("channel OTP verified")
                            launchIO {
                                with(hiplaRepo.fetchUserDetails(channelPartnerUserId!!)) {
                                    ifSuccessful {
                                        appEvent.tryEmit(AppEvent(OTP_VERIFICATION_SUCCESS))
                                        appEvent.tryEmit(
                                            AppEventWithData(
                                                FETCH_USER_INFO,
                                                extras = it.userInfo
                                            )
                                        )
                                        channelPartnerDetails = it.userInfo
                                        Timber.tag(LogConstant.PAYMENT_INFO)
                                            .d("channel partner name ${it.userInfo.name}")
                                    }
                                    ifError {
                                        appEvent.tryEmit(AppEvent(OTP_VERIFICATION_SUCCESS))
                                        appEvent.tryEmit(AppEvent(FETCH_USER_ERROR))
                                        Timber.tag(LogConstant.PAYMENT_INFO)
                                            .d("channel partner api error")
                                    }
                                }
                            }
                        } else {
                            appEvent.tryEmit(AppEvent(OTP_VERIFICATION_INVALID))
                            Timber.tag(LogConstant.PAYMENT_INFO).e("channel OTP invalid")
                        }
                    }
                    ifError {
                        Timber.tag(LogConstant.PAYMENT_INFO).e("channel OTP verification failed")
                        appEvent.tryEmit(AppEvent(OTP_VERIFICATION_FAILED))
                    }
                }
            } else {
                appEvent.tryEmit(AppEvent(OTP_VERIFICATION_FAILED))
                Timber.tag(LogConstant.PAYMENT_INFO).e("channel OTP server referenceId not found")
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

    fun uploadImage(bm: Bitmap) {
        launchIO {
            try {
                appEvent.tryEmit(AppEvent(IMAGE_UPLOADING))
                val preSignedUrl = URL(imageUploadUrl)
                val connection = preSignedUrl.openConnection() as HttpsURLConnection
                connection.doOutput = true
                connection.requestMethod = "PUT"
                connection.setRequestProperty(
                    "Content-Type",
                    "image/jpeg"
                )
                val output: OutputStream = connection.outputStream
                bm.compress(Bitmap.CompressFormat.JPEG, 100, output)
                output.flush()
                val responseCode: Int = connection.responseCode
                Timber.tag("testfx").d("response code $responseCode")
                Timber.tag("testfx").d("image uploaded, now verify in backend")
                isProofUploadedAtomic.getAndSet(true)
                appEvent.tryEmit(
                    AppEventWithData(
                        IMAGE_UPLOADED_SUCCESSFULLY,
                        extras = applicationCreateResponse?.imageReadUrl
                    )
                )
            } catch (e: Exception) {
                appEvent.tryEmit(AppEvent(IMAGE_UPLOADED_FAILED))
                e.printStackTrace()
                Timber.tag("testfx").d(e)
            }
        }
    }


}