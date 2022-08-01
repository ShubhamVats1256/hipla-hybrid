package com.hipla.channel.viewmodel

import androidx.lifecycle.MutableLiveData
import com.hipla.channel.common.AppConfig
import com.hipla.channel.common.LogConstant
import com.hipla.channel.entity.SalesUser
import com.hipla.channel.entity.api.ifError
import com.hipla.channel.entity.api.ifSuccessful
import com.hipla.channel.repo.HiplaRepo
import org.koin.java.KoinJavaComponent.inject
import timber.log.Timber
import java.util.concurrent.atomic.AtomicBoolean

class ApplicationFlowViewModel : BaseViewModel() {
    private val hiplaRepo: HiplaRepo by inject(HiplaRepo::class.java)
    private val pageSize: Int = AppConfig.PAGE_DOWNLOAD_SIZE
    private var currentPage: Int = 1
    private var totalPage: Int = 1
    private var isDownloading: AtomicBoolean = AtomicBoolean(false)
    private val salesUserMasterList = mutableListOf<SalesUser>()
    var salesUsersLiveData = MutableLiveData<List<SalesUser>>()
    private var otpReferenceId: String? = null

    fun loadUsers() {
        if (canDownload()) {
            launchIO {
                Timber.tag(LogConstant.FLOW_APP)
                    .d("downloading sales user list for page $currentPage")
                with(hiplaRepo.fetchSalesUserList(currentPage, pageSize)) {
                    ifSuccessful {
                        Timber.tag(LogConstant.FLOW_APP)
                            .d("downloading sales user list successful with size ${it.salesUserList?.size}")
                        totalPage = it.pagination.totalPage
                        currentPage++
                        Timber.tag(LogConstant.FLOW_APP).d("totalPage : ${it.pagination.totalPage}")
                        if (it.salesUserList.isNullOrEmpty().not()) {
                            salesUserMasterList.addAll(it.salesUserList!!)
                            salesUsersLiveData.postValue(salesUserMasterList)
                        }
                        Timber.tag(LogConstant.FLOW_APP)
                            .d("loading sales user list successful ${it.salesUserList?.size}")
                    }
                    ifError {
                        Timber.tag(LogConstant.FLOW_APP).e("downloading sales user list failed")
                    }
                    isDownloading.set(false)
                }
            }
        }
    }

    fun generateOTP(salesUser: SalesUser) {
        launchIO {
            hiplaRepo.generateOtp(salesUser.phoneNumber.toString()).run {
                ifSuccessful {
                    Timber.tag(LogConstant.FLOW_APP)
                        .d("generate OTP successful for ${salesUser.name} : ${salesUser.id}, referenceId:${it.referenceId}")
                    otpReferenceId = it.referenceId
                }
                ifError {
                    Timber.tag(LogConstant.FLOW_APP)
                        .e("generate OTP error for ${salesUser.name} : ${salesUser.id} ")
                }
            }
        }
    }

    fun verifyOtp(salesUser: SalesUser, otp: String) {
        Timber.tag(LogConstant.FLOW_APP).d("verify otp: $otp for userId : ${salesUser.id}")
        launchIO {
            if (otpReferenceId != null) {
                hiplaRepo.verifyOtp(
                    otp = otp,
                    userId = salesUser.id.toString(),
                    referenceId = otpReferenceId!!
                ).run {
                    ifSuccessful {
                        if (it.verifyOTPData.referenceId == otpReferenceId && it.verifyOTPData.isVerified) {
                            Timber.tag(LogConstant.FLOW_APP).d("otp verification successful")
                        } else {
                            Timber.tag(LogConstant.FLOW_APP).d("otp invalid")
                        }
                    }
                    ifError {
                        Timber.tag(LogConstant.FLOW_APP).e("otp verification failed")
                    }
                }
            } else {
                Timber.tag(LogConstant.FLOW_APP).e("otp server referenceId not found")
            }
        }
    }

    private fun canDownload(): Boolean {
        return isDownloading.get() || currentPage <= totalPage
    }

}