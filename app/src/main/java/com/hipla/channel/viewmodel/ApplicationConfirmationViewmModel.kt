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

class ApplicationConfirmationViewModel : BaseViewModel() {
    private val hiplaRepo: HiplaRepo by KoinJavaComponent.inject(HiplaRepo::class.java)
    private var generateOTPResponse: GenerateOTPResponse? = null
    private var applicationRequest: ApplicationRequest? = null

    fun extractArguments(arguments: Bundle?) {
        launchIO {
            arguments?.getString(KEY_APP_REQ)?.toApplicationRequest()?.let {
                this.applicationRequest = it
                appEvent.tryEmit(
                    AppEventWithData(
                        id = APPLICATION_ARGS_EXTRACTED,
                        extras = applicationRequest!!
                    )
                )
                Timber.tag(LogConstant.PAYMENT_INFO)
                    .d("application request with id ${it.id} for customer : ${it.customerName} received in payment flow")
            }
        }
    }

    fun updateApplication() {
        launchIO {
            appEvent.tryEmit(AppEvent(APPLICATION_UPDATING))
            if (applicationRequest != null) {
                with(hiplaRepo.updateApplication(applicationRequest!!)) {
                    ifSuccessful {
                        Timber.tag(LogConstant.PAYMENT_INFO)
                            .d("application updated successfully for id : ${applicationRequest!!.id}")
                        appEvent.tryEmit(AppEvent(APPLICATION_UPDATING_SUCCESS))
                    }
                    ifError {
                        appEvent.tryEmit(AppEvent(APPLICATION_UPDATING_FAILED))
                        Timber.tag(LogConstant.PAYMENT_INFO)
                            .e("application update failed for id : ${applicationRequest!!.id}")
                    }
                }
            } else {
                appEvent.tryEmit(AppEvent(APPLICATION_UPDATING_FAILED))
                Timber.tag(LogConstant.PAYMENT_INFO)
                    .e("application request argument not found failed for id : ${applicationRequest!!.id}")
            }
        }
    }

}