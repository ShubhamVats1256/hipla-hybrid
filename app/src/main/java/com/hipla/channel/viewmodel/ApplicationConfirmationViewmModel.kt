package com.hipla.channel.viewmodel

import android.os.Bundle
import com.hipla.channel.common.KEY_APP_REQ
import com.hipla.channel.common.LogConstant
import com.hipla.channel.entity.*
import com.hipla.channel.entity.api.ifError
import com.hipla.channel.entity.api.ifSuccessful
import com.hipla.channel.extension.isSuccess
import com.hipla.channel.extension.toApplicationRequest
import com.hipla.channel.repo.HiplaRepo
import org.koin.java.KoinJavaComponent
import timber.log.Timber

class ApplicationConfirmationViewModel : BaseViewModel() {
    private val hiplaRepo: HiplaRepo by KoinJavaComponent.inject(HiplaRepo::class.java)
    private var applicationRequest: ApplicationRequest? = null

    fun extractArguments(arguments: Bundle?): ApplicationRequest? {
        arguments?.getString(KEY_APP_REQ)?.toApplicationRequest()?.let {
            this.applicationRequest = it
            Timber.tag(LogConstant.APP_CONFIRM)
                .d("application request with id ${it.id} for customer : ${it.customerName} received in ")
            return applicationRequest
        }
        return null
    }

    fun updateApplication() {
        launchIO {
            appEvent.tryEmit(AppEvent(APPLICATION_UPDATING))
            if (applicationRequest != null) {
                with(hiplaRepo.updateApplication(applicationRequest!!)) {
                    ifSuccessful {
                        if (it.status.isSuccess()) {
                            reportApplicationUpdateSuccess()
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

    private fun reportApplicationUpdateSuccess() {
        appEvent.tryEmit(
            AppEventWithData(
                id = APPLICATION_UPDATING_SUCCESS,
                extras = applicationRequest!!
            )
        )
    }

    private fun reportApplicationUpdateFailed() {
        appEvent.tryEmit(AppEvent(APPLICATION_UPDATING_FAILED))
    }

}