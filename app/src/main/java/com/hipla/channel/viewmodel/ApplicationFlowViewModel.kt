package com.hipla.channel.viewmodel

import androidx.lifecycle.MutableLiveData
import com.hipla.channel.common.AppConfig
import com.hipla.channel.common.LogConstant
import com.hipla.channel.entity.*
import com.hipla.channel.entity.api.ApiError
import com.hipla.channel.entity.api.ifError
import com.hipla.channel.entity.api.ifSuccessful
import com.hipla.channel.entity.response.GenerateOTPResponse
import com.hipla.channel.repo.HiplaRepo
import org.koin.java.KoinJavaComponent.inject
import timber.log.Timber
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

class ApplicationFlowViewModel : BaseViewModel() {
    private val hiplaRepo: HiplaRepo by inject(HiplaRepo::class.java)
    private val pageSize: Int = AppConfig.PAGE_DOWNLOAD_SIZE
    private var currentPageAtomic: AtomicInteger = AtomicInteger(1)
    private var totalPageAtomic: AtomicInteger = AtomicInteger(1)
    private var isDownloading: AtomicBoolean = AtomicBoolean(false)
    private val salesUserMasterList = mutableListOf<SalesUser>()
    var salesUsersLiveData = MutableLiveData<List<SalesUser>>()
    private var generateOTPResponse: GenerateOTPResponse? = null

    fun loadUsers() {
        if (canDownload()) {
            launchIO {
                Timber.tag(LogConstant.FLOW_APP)
                    .d("downloading sales user list for page ${currentPageAtomic.get()}")
                with(hiplaRepo.fetchSalesUserList(currentPageAtomic.get(), pageSize)) {
                    ifSuccessful {
                        Timber.tag(LogConstant.FLOW_APP)
                            .d("downloading sales user list successful with size ${it.salesUserList?.size} for page ${currentPageAtomic.get()}")
                        totalPageAtomic = AtomicInteger(it.pagination.totalPage)
                        currentPageAtomic.getAndIncrement()
                        Timber.tag(LogConstant.FLOW_APP).d("totalPage : ${it.pagination.totalPage}")
                        if (it.salesUserList.isNullOrEmpty().not()) {
                            salesUserMasterList.addAll(it.salesUserList!!)
                            salesUsersLiveData.postValue(salesUserMasterList)
                        }
                        Timber.tag(LogConstant.FLOW_APP)
                            .d("loading sales user list successful ${it.salesUserList?.size}")
                    }
                    ifError {
                        Timber.tag(LogConstant.FLOW_APP).e("sales api error")
                        (it.throwable as? ApiError)?.run {
                            Timber.tag(LogConstant.FLOW_APP).e("error list size ${this.errorList?.size}")
                            if (this.errorList?.isNotEmpty() == true) {
                                appEvent.tryEmit(AppEvent(
                                    id = API_ERROR,
                                    message = this.errorList.first().msg ?: "Connection error"
                                ))
                                Timber.tag(LogConstant.FLOW_APP).e("error")
                            }
                            Timber.tag(LogConstant.FLOW_APP).e("downloading sales user list failed")
                        }
                    }
                    isDownloading.set(false)
                }
            }
        }
    }

    fun generateOTP(salesUser: SalesUser) {
        launchIO {
            appEvent.tryEmit(AppEvent(OTP_GENERATING))
            hiplaRepo.generateOtp(salesUser.phoneNumber.toString()).run {
                ifSuccessful {
                    Timber.tag(LogConstant.FLOW_APP)
                        .d("generate OTP successful for ${salesUser.name} : ${salesUser.id}, referenceId:${it.referenceId}")
                    generateOTPResponse = it
                    appEvent.tryEmit(AppEvent(OTP_GENERATE_SUCCESS))
                    appEvent.tryEmit(
                        AppEventWithData(
                            id = OTP_SHOW_VERIFICATION_DIALOG,
                            extras = it.recordReference.id.toString()
                        )
                    )
                }
                ifError {
                    Timber.tag(LogConstant.FLOW_APP)
                        .e("generate OTP error for ${salesUser.name} : ${salesUser.id} ")
                    appEvent.tryEmit(AppEvent(OTP_GENERATE_FAILED))
                }
                appEvent.tryEmit(AppEvent(OTP_GENERATE_COMPLETE))
            }
        }
    }

    fun verifyOtp(salesUserId: String, otp: String) {
        Timber.tag(LogConstant.FLOW_APP).d("verify otp: $otp for userId : $salesUserId")
        launchIO {
            appEvent.tryEmit(AppEvent(OTP_VERIFYING))
            if (generateOTPResponse != null) {
                hiplaRepo.verifyOtp(
                    otp = otp,
                    userId = salesUserId,
                    referenceId = generateOTPResponse!!.referenceId
                ).run {
                    ifSuccessful {
                        if (it.verifyOTPData.referenceId == generateOTPResponse!!.referenceId && it.verifyOTPData.isVerified) {
                            Timber.tag(LogConstant.FLOW_APP).d("otp verification successful")
                            appEvent.tryEmit(AppEvent(OTP_VERIFICATION_SUCCESS))
                            appEvent.tryEmit(AppEvent(APP_EVENT_START_APPLICATION_FLOW))
                            appEvent.tryEmit(
                                AppEventWithData(
                                    id = APP_EVENT_START_APPLICATION_FLOW,
                                    extras = generateOTPResponse!!.recordReference.id.toString()
                                )
                            )
                        } else {
                            appEvent.tryEmit(AppEvent(OTP_VERIFICATION_INVALID))
                            Timber.tag(LogConstant.FLOW_APP).d("otp invalid")
                        }
                    }
                    ifError {
                        appEvent.tryEmit(AppEvent(OTP_VERIFICATION_FAILED))
                        Timber.tag(LogConstant.FLOW_APP).e("otp verification failed")
                    }
                }
            } else {
                appEvent.tryEmit(AppEvent(OTP_VERIFICATION_FAILED))
                Timber.tag(LogConstant.FLOW_APP).e("otp server referenceId not found")
            }
        }
    }

    private fun canDownload(): Boolean {
        Timber.tag(LogConstant.FLOW_APP)
            .d("current page: ${currentPageAtomic.get()}  is downloading: ${isDownloading.get()}")
        val canDownload =
            isDownloading.get().not() && currentPageAtomic.get() <= totalPageAtomic.get()
        Timber.tag(LogConstant.FLOW_APP).d("can download $canDownload")
        return canDownload
    }

}