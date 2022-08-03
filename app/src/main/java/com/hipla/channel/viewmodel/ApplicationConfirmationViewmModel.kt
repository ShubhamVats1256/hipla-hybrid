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

class ApplicationConfirmationViewmModel : BaseViewModel() {
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
        }?.also {
            patchApplicationRequestData(it)
        }
    }

    private fun patchApplicationRequestData(applicationRequest: ApplicationRequest) {
        launchIO {
            with(hiplaRepo.updateApplication(applicationRequest)) {
                ifSuccessful {
                    Timber.tag(LogConstant.PAYMENT_INFO)
                        .d("application updated successfully for id : ${applicationRequest.id}")
                }
                ifError {
                    Timber.tag(LogConstant.PAYMENT_INFO)
                        .e("application updated failed for id : ${applicationRequest.id}")
                }
            }
        }
    }

}