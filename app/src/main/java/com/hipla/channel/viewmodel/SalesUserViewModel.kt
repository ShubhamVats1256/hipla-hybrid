package com.hipla.channel.viewmodel

import android.os.Bundle
import androidx.lifecycle.MutableLiveData
import com.hipla.channel.common.AppConfig
import com.hipla.channel.common.KEY_FLOW_CONFIG
import com.hipla.channel.common.LogConstant
import com.hipla.channel.entity.*
import com.hipla.channel.entity.api.ApiError
import com.hipla.channel.entity.api.ifError
import com.hipla.channel.entity.api.ifSuccessful
import com.hipla.channel.entity.response.GenerateOTPResponse
import com.hipla.channel.extension.isApplication
import com.hipla.channel.extension.isInventory
import com.hipla.channel.extension.toFlowConfig
import com.hipla.channel.repo.HiplaRepo
import org.koin.java.KoinJavaComponent.inject
import timber.log.Timber
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

class SalesUserViewModel : BaseViewModel() {
    private val hiplaRepo: HiplaRepo by inject(HiplaRepo::class.java)
    private val pageSize: Int = AppConfig.PAGE_DOWNLOAD_SIZE
    private var currentPageAtomic: AtomicInteger = AtomicInteger(1)
    private var totalPageAtomic: AtomicInteger = AtomicInteger(1)
    private var isDownloading: AtomicBoolean = AtomicBoolean(false)
    private val salesUserMasterList = mutableListOf<SalesUser>()
    var salesUsersLiveData = MutableLiveData<List<SalesUser>>()
    private var generateOTPResponse: GenerateOTPResponse? = null
    lateinit var flowConfig: FlowConfig

    fun extractArguments(arguments: Bundle?) {
        arguments?.let {
            flowConfig = it.getString(KEY_FLOW_CONFIG)?.toFlowConfig()!!
            Timber.tag(LogConstant.SALES_LIST).d("flow config: $flowConfig")
        }
    }

    fun getFlowTitle() = flowConfig.title

    fun loadUsers() {
        if (canDownload()) {
            launchIO {
                Timber.tag(LogConstant.SALES_LIST)
                    .d("downloading sales user list for page ${currentPageAtomic.get()}")
                with(
                    hiplaRepo.fetchSalesUserList(
                        currentPageAtomic.get(),
                        pageSize,
                        AppConfig.PAGE_LIST_SALES.lowercase()
                    )
                ) {
                    ifSuccessful {
                        Timber.tag(LogConstant.SALES_LIST)
                            .d("downloading sales user list successful with size ${it.salesUserList?.size} for page ${currentPageAtomic.get()}")
                        totalPageAtomic = AtomicInteger(it.pagination.totalPage)
                        currentPageAtomic.getAndIncrement()
                        Timber.tag(LogConstant.SALES_LIST)
                            .d("totalPage : ${it.pagination.totalPage}")
                        if (it.salesUserList.isNullOrEmpty().not()) {
                            salesUserMasterList.addAll(it.salesUserList!!)
                            salesUsersLiveData.postValue(salesUserMasterList)
                        }
                        Timber.tag(LogConstant.SALES_LIST)
                            .d("loading sales user list successful ${it.salesUserList?.size}")
                    }
                    ifError {
                        Timber.tag(LogConstant.SALES_LIST).e("sales api error")
                        (it.throwable as? ApiError)?.run {
                            Timber.tag(LogConstant.SALES_LIST)
                                .e("error list size ${this.errorList?.size}")
                            if (this.errorList?.isNotEmpty() == true) {
                                appEvent.tryEmit(
                                    AppEvent(
                                        id = API_ERROR,
                                        message = this.errorList.first().msg ?: "Connection error"
                                    )
                                )
                                Timber.tag(LogConstant.SALES_LIST).e("error")
                            }
                            Timber.tag(LogConstant.SALES_LIST)
                                .e("downloading sales user list failed")
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
                    Timber.tag(LogConstant.SALES_LIST)
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
                    Timber.tag(LogConstant.SALES_LIST)
                        .e("generate OTP error for ${salesUser.name} : ${salesUser.id} ")
                    appEvent.tryEmit(AppEvent(OTP_GENERATE_FAILED))
                }
                appEvent.tryEmit(AppEvent(OTP_GENERATE_COMPLETE))
            }
        }
    }

    fun verifyOtp(salesUserId: String, otp: String) {
        Timber.tag(LogConstant.SALES_LIST).d("verify otp: $otp for userId : $salesUserId")
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
                            Timber.tag(LogConstant.SALES_LIST).d("otp verification successful")
                            appEvent.tryEmit(AppEvent(OTP_VERIFICATION_SUCCESS))
                            if (flowConfig.isApplication()) {
                                Timber.tag(LogConstant.SALES_LIST).d("launch application flow")
                                appEvent.tryEmit(
                                    AppEventWithData(
                                        id = APP_EVENT_START_APPLICATION_FLOW,
                                        extras = generateOTPResponse!!.recordReference.id.toString()
                                    )
                                )
                            } else if (flowConfig.isInventory()) {
                                Timber.tag(LogConstant.SALES_LIST).d("launch inventory flow")
                                appEvent.tryEmit(
                                    AppEventWithData(
                                        id = UNIT_LIST_FLOW,
                                        extras = generateOTPResponse!!.recordReference.id.toString()
                                    )
                                )
                            } else {
                                Timber.tag(LogConstant.SALES_LIST).e("unsupported flow")
                            }
                        } else {
                            appEvent.tryEmit(AppEvent(OTP_VERIFICATION_INVALID))
                            Timber.tag(LogConstant.SALES_LIST).d("otp invalid")
                        }
                    }
                    ifError {
                        appEvent.tryEmit(AppEvent(OTP_VERIFICATION_FAILED))
                        Timber.tag(LogConstant.SALES_LIST).e("otp verification failed")
                    }
                }
            } else {
                appEvent.tryEmit(AppEvent(OTP_VERIFICATION_FAILED))
                Timber.tag(LogConstant.SALES_LIST).e("otp server referenceId not found")
            }
        }
    }

    private fun canDownload(): Boolean {
        Timber.tag(LogConstant.SALES_LIST)
            .d("current page: ${currentPageAtomic.get()}  is downloading: ${isDownloading.get()}")
        val canDownload =
            isDownloading.get().not() && currentPageAtomic.get() <= totalPageAtomic.get()
        Timber.tag(LogConstant.SALES_LIST).d("can download $canDownload")
        return canDownload
    }

}