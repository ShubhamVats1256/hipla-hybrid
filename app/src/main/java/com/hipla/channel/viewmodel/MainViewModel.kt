package com.hipla.channel.viewmodel

import com.hipla.channel.entity.api.ifError
import com.hipla.channel.entity.api.ifSuccessful
import com.hipla.channel.repo.HiplaRepo
import org.koin.java.KoinJavaComponent.inject
import timber.log.Timber

class MainViewModel : BaseViewModel() {
    private val hiplaRepo: HiplaRepo by inject(HiplaRepo::class.java)

    fun loadUsers() {
        launchIO {
            with(hiplaRepo.fetchSalesUserList()) {
                ifSuccessful {
                    Timber.tag("testfx").d("loading user successful ${it.salesUserList?.size}")
                }
                ifError {
                    Timber.tag("testfx").e("load user error")
                }
            }
        }
    }

}