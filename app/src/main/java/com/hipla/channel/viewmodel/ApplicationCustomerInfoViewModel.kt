package com.hipla.channel.viewmodel

import com.hipla.channel.common.LogConstant
import com.hipla.channel.entity.ApplicationRequest
import com.hipla.channel.entity.api.ifError
import com.hipla.channel.entity.api.ifSuccessful
import com.hipla.channel.entity.response.GenerateOTPResponse
import com.hipla.channel.repo.HiplaRepo
import org.koin.java.KoinJavaComponent
import timber.log.Timber

class ApplicationCustomerInfoViewModel : BaseViewModel() {
    private val hiplaRepo: HiplaRepo by KoinJavaComponent.inject(HiplaRepo::class.java)
    private var generateOTPResponse : GenerateOTPResponse? = null
    private var applicationRequest : ApplicationRequest? = null

    suspend fun createApplication() {
        launchIO {
           with(hiplaRepo.createApplication(applicationRequest!!)) {
               ifSuccessful {
                   Timber.tag(LogConstant.FLOW_APP).d("application created for ${applicationRequest?.customerName} with ID :${applicationRequest?.id}")
                   applicationRequest?.id = it.id
                   applicationRequest?.ownerId = generateOTPResponse?.userReference?.id
                   if((applicationRequest?.id ?: 0) > 0) {
                       Timber.tag(LogConstant.FLOW_APP).d("customer info collection complete")
                       // launch next flow
                   } else {
                       Timber.tag(LogConstant.FLOW_APP).d("application id is invalid cannot proceed to next flow")
                   }
               }
               ifError {

               }
           }
        }
    }

    fun verifyCustomerOTP(otp: String) {
        Timber.tag(LogConstant.FLOW_APP).d("verify OTP: $otp for userId : ${generateOTPResponse?.userReference?.id}")
        launchIO {
            if (generateOTPResponse != null) {
                hiplaRepo.verifyOtp(
                    otp = otp,
                    userId = generateOTPResponse!!.userReference.id.toString(),
                    referenceId = generateOTPResponse!!.referenceId
                ).run {
                    ifSuccessful {
                        if (it.verifyOTPData.referenceId == generateOTPResponse!!.referenceId && it.verifyOTPData.isVerified) {
                            Timber.tag(LogConstant.FLOW_APP).d("customer OTP verified")
                        } else {
                            Timber.tag(LogConstant.FLOW_APP).e("customer OTP invalid")
                        }
                    }
                    ifError {
                        Timber.tag(LogConstant.FLOW_APP).e("customer OTP verification failed")
                    }
                }
            } else {
                Timber.tag(LogConstant.FLOW_APP).e("customer OTP server referenceId not found")
            }
        }
    }


    fun generateCustomerOTP(applicationRequest: ApplicationRequest) {
        launchIO {
           this.applicationRequest = applicationRequest;
            applicationRequest.customerPhoneNumber?.let {
                hiplaRepo.generateOtp(it).run {
                    ifSuccessful {
                        Timber.tag(LogConstant.FLOW_APP)
                            .d("generate OTP successful for customer name ${applicationRequest.customerName}, referenceId:${it.referenceId}")
                        generateOTPResponse = it
                    }
                    ifError {
                        Timber.tag(LogConstant.FLOW_APP)
                            .e("generate OTP failed for customer name ${applicationRequest.customerName}")
                    }
                }
            } ?: Timber.tag(LogConstant.FLOW_APP).e("generate OTP failed as customer phone no not found")
        }
    }


}