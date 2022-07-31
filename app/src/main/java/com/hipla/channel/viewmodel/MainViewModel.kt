package com.hipla.channel.viewmodel

import com.hipla.channel.repo.HiplaRepo
import org.koin.java.KoinJavaComponent.inject
import timber.log.Timber

class MainViewModel : BaseViewModel() {
    private val hiplaRepo: HiplaRepo by inject(HiplaRepo::class.java)

    fun loadUsers() {
        launchIO {
            val result = hiplaRepo.fetchTest()
            Timber.tag("mainViewModel").d("Response $result")
        }
    }


}