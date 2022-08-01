package com.hipla.channel.viewmodel

import androidx.lifecycle.MutableLiveData
import com.hipla.channel.common.LogConstant
import com.hipla.channel.entity.SalesUser
import com.hipla.channel.entity.api.ifError
import com.hipla.channel.entity.api.ifSuccessful
import com.hipla.channel.repo.HiplaRepo
import org.koin.java.KoinJavaComponent.inject
import timber.log.Timber
import java.util.concurrent.atomic.AtomicBoolean

class MainViewModel : BaseViewModel() {
    private val hiplaRepo: HiplaRepo by inject(HiplaRepo::class.java)
    private val pageSize: Int = 50
    private var currentPage: Int = 1
    private var totalPage: Int = 0
    private var isDownloading: AtomicBoolean = AtomicBoolean(false)
    private val salesUserMasterList = mutableListOf<SalesUser>()
    var salesUsersLiveData = MutableLiveData<List<SalesUser>>()

    fun loadUsers() {
        if (canDownload()) {
            launchIO {
                with(hiplaRepo.fetchSalesUserList(currentPage, pageSize)) {
                    ifSuccessful {
                        totalPage = it.pagination.totalPage
                        currentPage++
                        if (it.salesUserList.isNullOrEmpty().not()) {
                            salesUserMasterList.addAll(it.salesUserList!!)
                            salesUsersLiveData.postValue(it.salesUserList!!)
                        }
                        Timber.tag(LogConstant.FLOW_APP)
                            .d("loading user successful ${it.salesUserList?.size}")
                    }
                    ifError {
                        Timber.tag(LogConstant.FLOW_APP).e("load user error")
                    }
                    isDownloading.set(false)
                }
            }
        }
    }


    private fun canDownload(): Boolean {
        return isDownloading.get() || currentPage >= totalPage
    }

}